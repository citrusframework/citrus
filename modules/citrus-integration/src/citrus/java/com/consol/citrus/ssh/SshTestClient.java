package com.consol.citrus.ssh;

import java.io.*;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.jcraft.jsch.*;
import groovy.util.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

/**
 * Test SSH Client
 *
 * @author roland
 * @since 05.09.12
 */
public class SshTestClient {

    Logger log = LoggerFactory.getLogger(SshTestClient.class);

    // SSH implementation
    private JSch jsch;

    // Session for the SSH communication
    private Session session;
    private String privateKeyFile;
    private String user;
    private int port;
    private static final long SCRIPT_TIMEOUT = 1000 * 60 * 5; // 5 minutes

    /**
     * Default constructor.
     * @throws IOException
     */
    public SshTestClient() throws IOException {
        jsch = new JSch();
        port = 9072;
        user = "roland";
        File priv = File.createTempFile("citrus-ssh-test","priv");
        FileCopyUtils.copy(getClass().getResourceAsStream("test_user.priv"),new FileOutputStream(priv));
        privateKeyFile = priv.getAbsolutePath();
    }

    /**
     * Connects to ssh server.
     */
    public void connect() {
        if (session == null || !session.isConnected()) {
            try {
                jsch.addIdentity(privateKeyFile);
            } catch (JSchException e) {
                throw new CitrusRuntimeException("Cannot open private key file name " + privateKeyFile + ": " + e,e);
            }
            if (user == null) {
                throw new CitrusRuntimeException("No user given for remote connection");
            }
            try {
                session = jsch.getSession(user,"localhost",port);
                session.setConfig("StrictHostKeyChecking","no");
                session.connect();
            } catch (JSchException e) {
                throw new CitrusRuntimeException("Cannot connect via SSH: " + e,e);
            }
        }
    }

    /**
     * Executes ssh command.
     * @param pCommand
     * @param pInput
     * @return
     */
    public String execute(String pCommand, String pInput) {
        connect();
        ChannelExec ch = null;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        int rc = 0;
        try {
            ch = openChannelExec(ch);
            ch.setErrStream(errStream);
            ch.setOutputStream(outStream);
            ch.setCommand(pCommand);
            doConnect(ch);
            if (pInput != null) {
                sendStandardInput(ch, pInput);
            }
            waitCommandToFinish(ch);
            rc = ch.getExitStatus();
        } catch (ResourceException e) {
            throw new CitrusRuntimeException("Cannot execute " + pCommand + ": " + e,e);
        } finally {
            if (ch != null && ch.isConnected()) {
                ch.disconnect();
            }
            disconnect();
        }
        if (rc != 0) {
            throw new CitrusRuntimeException(errStream.toString());
        }
        log.info("Stdout: " + outStream);
        log.info("Exit code:" + rc);
        return outStream.toString();
    }

    /**
     * Disconnect from ssh server.
     */
    public void disconnect() {
        if (session.isConnected()) {
            session.disconnect();
        }
    }

    /**
     * Open ssh channel.
     * @param pCh
     * @return
     * @throws ResourceException
     */
    private ChannelExec openChannelExec(ChannelExec pCh) throws ResourceException {
        try {
            pCh = (ChannelExec) session.openChannel("exec");
        } catch (JSchException e) {
            throw new ResourceException("Cannot open EXEC SSH channel: " + e,e);
        }
        return pCh;
    }

    /**
     * Finish command and wait for it.
     * @param pCh
     * @throws ResourceException
     */
    private void waitCommandToFinish(ChannelExec pCh) throws ResourceException {
        final long until = System.currentTimeMillis() + SCRIPT_TIMEOUT;

        try {
            while (!pCh.isClosed() && System.currentTimeMillis() < until) {
                Thread.sleep(250);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted", e);
        }

        if (!pCh.isClosed()) {
            throw new CitrusRuntimeException("Timeout: Channel not finished within " + SCRIPT_TIMEOUT + " ms");
        }
    }

    /**
     * Log to standard input.
     * @param pCh
     * @param pInput
     * @throws ResourceException
     */
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

    /**
     * Connect with channel.
     * @param pCh
     * @throws ResourceException
     */
    private void doConnect(ChannelExec pCh) throws ResourceException {
        try {
            pCh.connect();
        } catch (JSchException e) {
            throw new ResourceException("Cannot connect EXEC SSH channel: " + e,e);
        }
    }
}
