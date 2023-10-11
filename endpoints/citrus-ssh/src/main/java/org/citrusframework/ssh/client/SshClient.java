/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.ssh.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.message.Message;
import org.citrusframework.message.correlation.CorrelationManager;
import org.citrusframework.message.correlation.PollingCorrelationManager;
import org.citrusframework.messaging.Producer;
import org.citrusframework.messaging.ReplyConsumer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.citrusframework.spi.Resources;
import org.citrusframework.ssh.model.SshRequest;
import org.citrusframework.ssh.model.SshResponse;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;

/**
 * Ssh client connects to ssh server and sends commands to that server.
 *
 * @author Roland Huss, Christoph Deppisch
 * @since 1.4
 */
public class SshClient extends AbstractEndpoint implements Producer, ReplyConsumer {

    /** Store of reply messages */
    private CorrelationManager<Message> correlationManager;

    // Session for the SSH communication
    private Session session;

    // SSH implementation
    private JSch jsch = new JSch();

    /**
     * Default constructor initializing endpoint configuration.
     */
    public SshClient() {
        this(new SshEndpointConfiguration());
    }

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    protected SshClient(SshEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);

        this.correlationManager = new PollingCorrelationManager<>(endpointConfiguration, "Reply message did not arrive yet");
    }

    @Override
    public SshEndpointConfiguration getEndpointConfiguration() {
        return (SshEndpointConfiguration) super.getEndpointConfiguration();
    }

    /**
     * Send a message as SSH request. The message format is created from
     * {@link org.citrusframework.ssh.server.SshServer}.
     *
     * @param message the message object to send.
     * @param context
     */
    public void send(Message message, TestContext context) {
        String correlationKeyName = getEndpointConfiguration().getCorrelator().getCorrelationKeyName(getName());
        String correlationKey = getEndpointConfiguration().getCorrelator().getCorrelationKey(message);
        correlationManager.saveCorrelationKey(correlationKeyName, correlationKey, context);

        SshRequest request = (SshRequest) getEndpointConfiguration().getMessageConverter().convertOutbound(message, getEndpointConfiguration(), context);

        if (getEndpointConfiguration().isStrictHostChecking()) {
            setKnownHosts();
        }

        String rUser = getRemoteUser(message);
        connect(rUser);
        ChannelExec channelExec = null;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        int rc;
        try {
            channelExec = openChannelExec();
            channelExec.setErrStream(errStream);
            channelExec.setOutputStream(outStream);
            channelExec.setCommand(request.getCommand());
            doConnect(channelExec);
            if (request.getStdin() != null) {
                sendStandardInput(channelExec, request.getStdin());
            }
            waitCommandToFinish(channelExec);
            rc = channelExec.getExitStatus();
        } finally {
            if (channelExec != null && channelExec.isConnected()) {
                channelExec.disconnect();
            }
            disconnect();
        }
        SshResponse sshResp = new SshResponse(outStream.toString(),errStream.toString(),rc);
        Message response = getEndpointConfiguration().getMessageConverter().convertInbound(sshResp, getEndpointConfiguration(), context)
                .setHeader("user", rUser);

        correlationManager.store(correlationKey, response);
    }

    @Override
    public Message receive(TestContext context) {
        return receive(correlationManager.getCorrelationKey(
                getEndpointConfiguration().getCorrelator().getCorrelationKeyName(getName()), context), context);
    }

    @Override
    public Message receive(String selector, TestContext context) {
        return receive(selector, context, getEndpointConfiguration().getTimeout());
    }

    @Override
    public Message receive(TestContext context, long timeout) {
        return receive(correlationManager.getCorrelationKey(
                getEndpointConfiguration().getCorrelator().getCorrelationKeyName(getName()), context), context, timeout);
    }

    @Override
    public Message receive(String selector, TestContext context, long timeout) {
        Message message = correlationManager.find(selector, timeout);

        if (message == null) {
            throw new MessageTimeoutException(timeout, "SSH server out stream");
        }

        return message;
    }

    @Override
    public Producer createProducer() {
        return this;
    }

    @Override
    public SelectiveConsumer createConsumer() {
        return this;
    }

    private void connect(String rUser) {
        if (session == null || !session.isConnected()) {
            try {
                if (StringUtils.hasText(getEndpointConfiguration().getPrivateKeyPath())) {
                    jsch.addIdentity(getPrivateKeyPath(), getEndpointConfiguration().getPrivateKeyPassword());
                }
            } catch (JSchException e) {
                throw new CitrusRuntimeException("Cannot add private key " + getEndpointConfiguration().getPrivateKeyPath() + ": " + e,e);
            } catch (IOException e) {
                throw new CitrusRuntimeException("Cannot open private key file " + getEndpointConfiguration().getPrivateKeyPath() + ": " + e,e);
            }

            try {
                session = jsch.getSession(rUser, getEndpointConfiguration().getHost(), getEndpointConfiguration().getPort());
                if (StringUtils.hasText(getEndpointConfiguration().getPassword())) {
                    session.setUserInfo(new UserInfoWithPlainPassword(getEndpointConfiguration().getPassword()));
                    session.setPassword(getEndpointConfiguration().getPassword());
                }
                session.setConfig(KnownHostsServerKeyVerifier.STRICT_CHECKING_OPTION, getEndpointConfiguration().isStrictHostChecking() ? "yes" : "no");
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

    private ChannelExec openChannelExec() throws CitrusRuntimeException {
        ChannelExec channelExec;
        try {
            channelExec = (ChannelExec) session.openChannel("exec");
        } catch (JSchException e) {
            throw new CitrusRuntimeException("Cannot open EXEC SSH channel: " + e,e);
        }
        return channelExec;
    }

    private void waitCommandToFinish(ChannelExec pCh) {
        final long until = System.currentTimeMillis() + getEndpointConfiguration().getCommandTimeout();

        try {
            while (!pCh.isClosed() && System.currentTimeMillis() < until) {
                Thread.sleep(250);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted", e);
        }

        if (!pCh.isClosed()) {
            throw new CitrusRuntimeException("Timeout: Channel not finished within " + getEndpointConfiguration().getCommandTimeout() + " ms");
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
            if (getEndpointConfiguration().getConnectionTimeout() != 0) {
                pCh.connect(getEndpointConfiguration().getConnectionTimeout());
            } else {
                pCh.connect();
            }
        } catch (JSchException e) {
            throw new CitrusRuntimeException("Cannot connect EXEC SSH channel: " + e,e);
        }
    }

    private String getRemoteUser(Message message) {
        String rUser = (String) message.getHeader("user");
        if (rUser == null) {
            // Use default uses
            rUser = getEndpointConfiguration().getUser();
        }
        if (rUser == null) {
            throw new CitrusRuntimeException("No user given for connecting to SSH server");
        }
        return rUser;
    }

    private void setKnownHosts() {
        if (getEndpointConfiguration().getKnownHosts() == null) {
            throw new CitrusRuntimeException("Strict host checking is enabled but no knownHosts given");
        }

        try {
            InputStream khIs = FileUtils.getFileResource(getEndpointConfiguration().getKnownHosts()).getInputStream();
            if (khIs == null) {
                throw new CitrusRuntimeException("Cannot find knownHosts at " + getEndpointConfiguration().getKnownHosts());
            }
            jsch.setKnownHosts(khIs);
        } catch (JSchException e) {
            throw new CitrusRuntimeException("Cannot add known hosts from " + getEndpointConfiguration().getKnownHosts() + ": " + e,e);
        }
    }

    private String getPrivateKeyPath() throws IOException {
        if (!StringUtils.hasText(getEndpointConfiguration().getPrivateKeyPath())) {
            return null;
        } else if (getEndpointConfiguration().getPrivateKeyPath().startsWith(Resources.CLASSPATH_RESOURCE_PREFIX)) {
            File priv = File.createTempFile("citrus-ssh","priv");
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(getEndpointConfiguration().getPrivateKeyPath().substring(Resources.CLASSPATH_RESOURCE_PREFIX.length()));
                    FileOutputStream fos = new FileOutputStream(priv)) {
                if (is == null) {
                    throw new CitrusRuntimeException("No private key found at " + getEndpointConfiguration().getPrivateKeyPath());
                }
                fos.write(is.readAllBytes());
                fos.flush();
            }
            return priv.getAbsolutePath();
        } else {
            return getEndpointConfiguration().getPrivateKeyPath();
        }
    }

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

    /**
     * Gets the JSch implementation.
     * @return
     */
    public JSch getJsch() {
        return jsch;
    }

    /**
     * Sets the JSch implementation.
     * @param jsch
     */
    public void setJsch(JSch jsch) {
        this.jsch = jsch;
    }

    /**
     * Sets the correlation manager.
     * @param correlationManager
     */
    public void setCorrelationManager(CorrelationManager<Message> correlationManager) {
        this.correlationManager = correlationManager;
    }

}
