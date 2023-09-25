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

package org.citrusframework.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.message.Message;
import org.citrusframework.ssh.client.SshEndpointConfiguration;
import org.citrusframework.ssh.model.SshRequest;
import org.citrusframework.ssh.model.SshResponse;
import org.citrusframework.util.FileUtils;
import org.apache.sshd.common.util.io.IoUtils;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A command for delegation to a endpoint adapter
 *
 * @author Roland Huss
 * @since 1.3
 */
public class SshCommand implements Command, Runnable {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(SshCommand.class);

    /** Endpoint adapter for creating requests/responses **/
    private final EndpointAdapter endpointAdapter;

    /** Ssh endpoint configuration */
    private final SshEndpointConfiguration endpointConfiguration;

    /** Command to execute **/
    private final String command;

    /** standard input/output/error streams; **/
    private InputStream stdin;
    private OutputStream stdout, stderr;

    /** Callback to be used for signaling the exit status **/
    private ExitCallback exitCallback;

    /** User on which behalf the command is executed **/
    private String user;

    /**
     * Constructor taking a command and the endpoint adapter as arguments
     * @param command command performed
     * @param endpointAdapter endpoint adapter
     * @param endpointConfiguration
     */
    public SshCommand(String command, EndpointAdapter endpointAdapter, SshEndpointConfiguration endpointConfiguration) {
        this.endpointAdapter = endpointAdapter;
        this.command = command;
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public void start(ChannelSession session, Environment env) throws IOException {
        user = env.getEnv().get(Environment.ENV_USER);
        new Thread(this, "CitrusSshCommand: " + command).start();
    }

    @Override
    public void run() {
        try {
            String input = FileUtils.readToString(stdin);
            SshRequest sshRequest = new SshRequest(command, input);

            Message response = endpointAdapter.handleMessage(endpointConfiguration.getMessageConverter().convertInbound(sshRequest, endpointConfiguration, null)
                    .setHeader("user", user));

            SshResponse sshResponse = (SshResponse) endpointConfiguration.getMessageConverter().convertOutbound(response, endpointConfiguration, null);

            copyToStream(sshResponse.getStderr(), stderr);
            copyToStream(sshResponse.getStdout(), stdout);
            exitCallback.onExit(sshResponse.getExit());
        } catch (IOException exp) {
            exitCallback.onExit(1, exp.getMessage());
        } finally {
            IoUtils.closeQuietly(stderr);
            IoUtils.closeQuietly(stdout);
        }
    }

    @Override
    public void destroy(ChannelSession session) {
        logger.warn("Destroy has been called");
    }

    @Override
    public void setInputStream(InputStream in) {
        stdin = in;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        stdout = out;
    }

    @Override
    public void setErrorStream(OutputStream err) {
        stderr = err;
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        exitCallback = callback;
    }

    /**
     * Copy character sequence to outbput stream.
     * @param txt
     * @param stream
     * @throws IOException
     */
    private void copyToStream(String txt, OutputStream stream) throws IOException {
        if (txt != null) {
            stream.write(txt.getBytes());
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
