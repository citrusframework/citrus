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

import com.consol.citrus.message.MessageHandler;
import com.consol.citrus.util.FileUtils;
import org.apache.sshd.server.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.FileCopyUtils;

/**
 * A command for delegation to a message handler
 *
 * @author roland
 * @since 05.09.12
 */
public class SshCommand implements Command, Runnable {

    // Logger
    private static Logger log = LoggerFactory.getLogger(SshCommand.class);


    /** Message handler for creating requests/responses **/
    private MessageHandler messageHandler;

    /** Command to execute **/
    private String command;

    /** standard input/output/error streams; **/
    private InputStream stdin;
    private OutputStream stdout, stderr;

    /** Callback to be used for signaling the exit status **/
    private ExitCallback exitCallback;

    /** User on which behalf the command is executed **/
    private String user;

    /**
     * Constructor taking a command and the messagehandler as arguments
     * @param pCommand command performend
     * @param pMessageHandler message handler
     */
    public SshCommand(String pCommand, MessageHandler pMessageHandler) {
        messageHandler = pMessageHandler;
        command = pCommand;
    }

    /**
     * {@inheritDoc}
     */
    public void start(Environment env) throws IOException {
        user = env.getEnv().get(Environment.ENV_USER);
        new Thread(this, "CitrusSshCommand: " + command).start();
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
        try {
            String input = FileUtils.readToString(stdin);
            SshRequest req = new SshRequest(command,input);

            SshResponse resp = sendToMessageHandler(req);

            copyToStream(resp.getStderr(),stderr);
            copyToStream(resp.getStdout(),stdout);
            exitCallback.onExit(resp.getExit());
        } catch (IOException exp) {
            exitCallback.onExit(1,exp.getMessage());
        }
    }

    /**
     * Delegate to message handler implementation.
     * @param pReq
     * @return
     */
    private SshResponse sendToMessageHandler(SshRequest pReq) {
        XmlMapper mapper = new XmlMapper();
        Message<?> response = messageHandler.handleMessage(
                MessageBuilder.withPayload(mapper.toXML(pReq))
                              .setHeader("user", user)
                              .build());
        String msgResp = (String) response.getPayload();
        return (SshResponse) mapper.fromXML(msgResp);
    }


    /** {@inheritDoc} */
    public void destroy() {
        log.warn("Destroy has been called");
    }

    /** {@inheritDoc} */
    public void setInputStream(InputStream in) {
        stdin = in;
    }

    /** {@inheritDoc} */
    public void setOutputStream(OutputStream out) {
        stdout = out;
    }

    /** {@inheritDoc} */
    public void setErrorStream(OutputStream err) {
        stderr = err;
    }

    /** {@inheritDoc} */
    public void setExitCallback(ExitCallback callback) {
        exitCallback = callback;
    }

    // ====================================================================

    /**
     * Copy character sequence to outbput stream.
     * @param txt
     * @param stream
     * @throws IOException
     */
    private void copyToStream(String txt, OutputStream stream) throws IOException {
        if (txt != null) {
            FileCopyUtils.copy(txt.getBytes(), stream);
        }
    }

    /**
     * Gets the command.
     * @return
     */
    public String getCommand() {
        return command;
    }
}
