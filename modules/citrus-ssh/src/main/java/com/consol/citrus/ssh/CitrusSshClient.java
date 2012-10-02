/*
 * Copyright 2006-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.ssh;


import java.io.*;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.AbstractMessageSender;
import com.consol.citrus.message.ReplyMessageHandler;
import com.jcraft.jsch.*;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

/**
 * A SSH client which sends a request specified in a test-cast as SSH EXEC call to a target host
 * and notifies a {@link ReplyMessageHandler} after the SSH call has been returned.
 *
 * @author roland
 * @since 06.09.12
 */
public class CitrusSshClient extends AbstractMessageSender {

    public static final String CLASSPATH_PREFIX = "classpath:";
    // SSH implementation
    private JSch jsch;

    // Host to connect to. Default: localhost
    private String host = "localhost";

    // SSH Port to connect to. Default: 2222
    private int port = 2222;

    // User for doing the SSH communication
    private String user;

    // Password if no private key authentication is used
    private String password;

    // Path to private key of user
    private String privateKeyPath;

    // Password for private key
    private String privateKeyPassword;

    // Whether strict host checking should be performed
    private boolean strictHostChecking = false;

    // If strict host checking is used, path to the 'known_hosts' file
    private String knownHosts;

    // Timeout how long to wait for answering the request
    private long commandTimeout = 1000 * 60 * 5; // 5 minutes

    // Timeout how long to wait for a connection to connect
    private int connectionTimeout = 1000 * 60 * 1; // 1 minute

    // --------------------------
    // Session for the SSH communication
    private Session session;

    // Message parser
    private XmlMapper xmlMapper;

    public CitrusSshClient() {
        jsch = new JSch();
        xmlMapper = new XmlMapper();
    }


    /**
     * Send a message as SSH request. The message format is created from {@link CitrusSshServer}.
     *
     * @param message the message object to send.
     */
    public void send(Message<?> message) {
        String payload = (String) message.getPayload();
        SshRequest request = (SshRequest) xmlMapper.fromXML(payload);

        if (strictHostChecking) {
            setKnownHosts();
        }

        String rUser = getRemoteUser(message);
        connect(rUser);
        ChannelExec ch = null;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        int rc = 0;
        try {
            ch = openChannelExec(ch);
            ch.setErrStream(errStream);
            ch.setOutputStream(outStream);
            ch.setCommand(request.getCommand());
            doConnect(ch);
            if (request.getStdin() != null) {
                sendStandardInput(ch, request.getStdin());
            }
            waitCommandToFinish(ch);
            rc = ch.getExitStatus();
        } finally {
            if (ch != null && ch.isConnected()) {
                ch.disconnect();
            }
            disconnect();
        }
        SshResponse sshResp = new SshResponse(outStream.toString(),errStream.toString(),rc);
        Message response = MessageBuilder.withPayload(xmlMapper.toXML(sshResp))
                                         .setHeader("user", rUser).build();
        informReplyMessageHandler(response,message);
    }

    private String getRemoteUser(Message<?> message) {
        String rUser = (String) message.getHeaders().get("user");
        if (rUser == null) {
            // Use default uses
            rUser = user;
        }
        if (rUser == null) {
            throw new CitrusRuntimeException("No user given for connecting to SSH server");
        }
        return rUser;
    }

    private void setKnownHosts() {
        if (knownHosts == null) {
            throw new CitrusRuntimeException("Strict host checking is enabled but no knownHosts given");
        }
        try {
            InputStream khIs = getInputStreamFromPath(knownHosts);
            if (khIs == null) {
                throw new CitrusRuntimeException("Cannot find knownHosts at " + knownHosts);
            }
            jsch.setKnownHosts(khIs);
        } catch (JSchException e) {
            throw new CitrusRuntimeException("Cannot add known hosts from " + knownHosts + ": " + e,e);
        } catch (FileNotFoundException e) {
            throw new CitrusRuntimeException("Cannot find known hosts file " + knownHosts + ": " + e,e);
        }
    }

    private InputStream getInputStreamFromPath(String pPath) throws FileNotFoundException {
        if (pPath.startsWith(CLASSPATH_PREFIX)) {
            return getClass().getClassLoader().getResourceAsStream(pPath.substring(CLASSPATH_PREFIX.length()));
        } else {
            return new FileInputStream(pPath);
        }
    }

    private String getPrivateKeyPath() throws IOException {
        if (!StringUtils.hasText(privateKeyPath)) {
            return null;
        } else if (privateKeyPath.startsWith(CLASSPATH_PREFIX)) {
            File priv = File.createTempFile("citrus-ssh-test","priv");
            InputStream is = getClass().getClassLoader().getResourceAsStream(privateKeyPath.substring(CLASSPATH_PREFIX.length()));
            if (is == null) {
                throw new CitrusRuntimeException("No private key found at " + privateKeyPath);
            }
            FileCopyUtils.copy(is, new FileOutputStream(priv));
            return priv.getAbsolutePath();
        } else {
            return privateKeyPath;
        }
    }

    // ===============================================================================================

    private void connect(String rUser) {
        if (session == null || !session.isConnected()) {
            try {
                if (StringUtils.hasText(privateKeyPath)) {
                    jsch.addIdentity(getPrivateKeyPath(),privateKeyPassword);
                }
            } catch (JSchException e) {
                throw new CitrusRuntimeException("Cannot add private key " + privateKeyPath + ": " + e,e);
            } catch (IOException e) {
                throw new CitrusRuntimeException("Cannot open private key file " + privateKeyPath + ": " + e,e);
            }
            try {
                session = jsch.getSession(rUser,host,port);
                if (StringUtils.hasText(password)) {
                    session.setUserInfo(new UserInfoWithPlainPassword(password));
                }
                session.setConfig("StrictHostKeyChecking",strictHostChecking ? "yes" : "no");
                session.connect();
            } catch (JSchException e) {
                throw new CitrusRuntimeException("Cannot connect via SSH: " + e,e);
            }
        }
    }

    private void disconnect() {
        if (session.isConnected()) {
            session.disconnect();
        }
    }

    private ChannelExec openChannelExec(ChannelExec pCh) throws CitrusRuntimeException {
        try {
            pCh = (ChannelExec) session.openChannel("exec");
        } catch (JSchException e) {
            throw new CitrusRuntimeException("Cannot open EXEC SSH channel: " + e,e);
        }
        return pCh;
    }

    private void waitCommandToFinish(ChannelExec pCh) {
        final long until = System.currentTimeMillis() + commandTimeout;

        try {
            while (!pCh.isClosed() && System.currentTimeMillis() < until) {
                Thread.sleep(250);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted", e);
        }

        if (!pCh.isClosed()) {
            throw new CitrusRuntimeException("Timeout: Channel not finished within " + commandTimeout + " ms");
        }
    }

    private void sendStandardInput(ChannelExec pCh, String pInput) {
        OutputStream os = null;
        try {
            os = pCh.getOutputStream();
            os.write(pInput.getBytes());
        } catch (IOException e) {
            throw new CitrusRuntimeException("Cannot write to standard input of SSH channel: " + e,e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    // best try
                }
            }
        }
    }

    private void doConnect(ChannelExec pCh) {
        try {
            if (connectionTimeout != 0) {
                pCh.connect(connectionTimeout);
            } else {
                pCh.connect();
            }
        } catch (JSchException e) {
            throw new CitrusRuntimeException("Cannot connect EXEC SSH channel: " + e,e);
        }
    }

    public void setHost(String pHost) {
        host = pHost;
    }

    public void setPort(int pPort) {
        port = pPort;
    }

    public void setUser(String pUser) {
        user = pUser;
    }

    public void setPassword(String pPassword) {
        password = pPassword;
    }

    public void setPrivateKeyPath(String pPrivateKeyPath) {
        privateKeyPath = pPrivateKeyPath;
    }

    public void setPrivateKeyPassword(String pPrivateKeyPassword) {
        privateKeyPassword = pPrivateKeyPassword;
    }

    public void setStrictHostChecking(boolean pStrictHostChecking) {
        strictHostChecking = pStrictHostChecking;
    }

    public void setKnownHosts(String pKnownHosts) {
        knownHosts = pKnownHosts;
    }

    public void setCommandTimeout(long pCommandTimeout) {
        commandTimeout = pCommandTimeout;
    }

    public void setConnectionTimeout(int pConnectionTimeout) {
        connectionTimeout = pConnectionTimeout;
    }


    // =========================================================

    // UserInfo which simply returns a plain password
    private static class UserInfoWithPlainPassword implements UserInfo {
        private String password;

        public UserInfoWithPlainPassword(String pPassword) {
            password = pPassword;
        }

        public String getPassphrase() {
            return null;
        }

        public String getPassword() {
            return password;
        }

        public boolean promptPassword(String message) {
            return false;
        }

        public boolean promptPassphrase(String message) {
            return false;
        }

        public boolean promptYesNo(String message) {
            return false;
        }

        public void showMessage(String message) {
        }
    }
}
