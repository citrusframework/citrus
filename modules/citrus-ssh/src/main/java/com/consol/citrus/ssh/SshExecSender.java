package com.consol.citrus.ssh;


import java.io.*;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.AbstractMessageSender;
import com.consol.citrus.message.ReplyMessageHandler;
import com.jcraft.jsch.*;
import com.thoughtworks.xstream.XStream;
import groovy.util.ResourceException;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.FileCopyUtils;

/**
 * A SSH client which sends a request specified in a test-cast as SSH EXEC call to a target host
 * and notifies a {@link ReplyMessageHandler} after the SSH call has been returned.
 *
 * @author roland
 * @since 06.09.12
 */
public class SshExecSender extends AbstractMessageSender {

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
    private long scriptTimeout = 1000 * 60 * 5; // 5 minutes

    // --------------------------
    // Session for the SSH communication
    private Session session;

    // Message parser
    private XStream xstream;

    public SshExecSender() throws IOException {
        jsch = new JSch();
        xstream = new XStream();
        xstream.alias("ssh-request",SshRequest.class);
        xstream.alias("ssh-response",SshResponse.class);

    }


    public void send(Message<?> message) {
        String payload = (String) message.getPayload();
        String rUser = (String) message.getHeaders().get("user");
        SshRequest request = (SshRequest) xstream.fromXML(payload);
        if (rUser == null) {
            rUser = user;
        }
        if (rUser == null) {
            throw new CitrusRuntimeException("No user given for connecting to SSH server");
        }

        if (strictHostChecking) {
            if (knownHosts == null) {
                throw new CitrusRuntimeException("Strict host checking is enabled but no knownHosts given");
            }
            jsch.setKnownHosts(getInputStreamFromPath(knownHosts));
        }

        connect();
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
        } catch (ResourceException e) {
            throw new CitrusRuntimeException("Cannot execute " + request.getCommand() + ": " + e,e);
        } finally {
            if (ch != null && ch.isConnected()) {
                ch.disconnect();
            }
            disconnect();
        }
        SshResponse sshResp = new SshResponse(outStream.toString(),errStream.toString(),rc);
        Message response = MessageBuilder.withPayload(xstream.toXML(sshResp)).build();
        informReplyMessageHandler(response,message);
    }

    private String getInputStreamFromPath(String pPath) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private String getPrivateKeyPath() {
        File priv = File.createTempFile("citrus-ssh-test","priv");
        FileCopyUtils.copy(getClass().getResourceAsStream("test_user.priv"), new FileOutputStream(priv));
        privateKeyPath = priv.getAbsolutePath();
        return privateKeyPath;
    }

    // ===============================================================================================

    private void connect() {
        if (session == null || !session.isConnected()) {
            try {
                jsch.addIdentity(getPrivateKeyPath(),privateKeyPassword);
            } catch (JSchException e) {
                throw new CitrusRuntimeException("Cannot open private key file name " + privateKeyPath + ": " + e,e);
            }
            if (user == null) {
                throw new CitrusRuntimeException("No user given for remote connection");
            }
            try {
                session = jsch.getSession(user,host,port);
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

    private ChannelExec openChannelExec(ChannelExec pCh) throws ResourceException {
        try {
            pCh = (ChannelExec) session.openChannel("exec");
        } catch (JSchException e) {
            throw new ResourceException("Cannot open EXEC SSH channel: " + e,e);
        }
        return pCh;
    }

    private void waitCommandToFinish(ChannelExec pCh) throws ResourceException {
        final long until = System.currentTimeMillis() + scriptTimeout;

        try {
            while (!pCh.isClosed() && System.currentTimeMillis() < until) {
                Thread.sleep(250);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted", e);
        }

        if (!pCh.isClosed()) {
            throw new CitrusRuntimeException("Timeout: Channel not finished within " + scriptTimeout + " ms");
        }
    }

    private void sendStandardInput(ChannelExec pCh, String pInput) throws ResourceException {
        OutputStream os = null;
        try {
            os = pCh.getOutputStream();
            os.write(pInput.getBytes());
        } catch (IOException e) {
            throw new ResourceException("Cannot write to standard input of SSH channel: " + e,e);
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

    private void doConnect(ChannelExec pCh) throws ResourceException {
        try {
            pCh.connect();
        } catch (JSchException e) {
            throw new ResourceException("Cannot connect EXEC SSH channel: " + e,e);
        }
    }
}
