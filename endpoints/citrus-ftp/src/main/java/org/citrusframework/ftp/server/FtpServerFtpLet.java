/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.ftp.server;

import java.util.Optional;
import java.util.stream.Stream;

import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.ftp.client.FtpEndpointConfiguration;
import org.citrusframework.ftp.message.FtpMessage;
import org.citrusframework.ftp.model.Command;
import org.citrusframework.ftp.model.CommandResultType;
import org.citrusframework.xml.StringResult;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.ftpserver.ftplet.DefaultFtpReply;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletContext;
import org.apache.ftpserver.ftplet.FtpletResult;
import org.apache.ftpserver.ftplet.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ftp servlet implementation that logs incoming connections and commands forwarding those to
 * endpoint adapter for processing in test case.
 *
 * Test case can manage the Ftp command result by providing a Ftp result message.
 *
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public class FtpServerFtpLet implements Ftplet {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(FtpServerFtpLet.class);

    /** Endpoint configuration */
    private final FtpEndpointConfiguration endpointConfiguration;

    /** Endpoint adapter */
    private final EndpointAdapter endpointAdapter;

    /**
     * Constructor using the server's endpoint adapter implementation.
     * @param endpointConfiguration
     * @param endpointAdapter
     */
    public FtpServerFtpLet(FtpEndpointConfiguration endpointConfiguration, EndpointAdapter endpointAdapter) {
        this.endpointConfiguration = endpointConfiguration;
        this.endpointAdapter = endpointAdapter;
    }

    public FtpMessage handleMessage(FtpMessage request) {
        if (request.getPayload() instanceof Command) {
            StringResult result = new StringResult();
            endpointConfiguration.getMarshaller().marshal(request.getPayload(Command.class), result);
            request.setPayload(result.toString());
        }

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Received request on ftp server: '%s':%n%s",
                    request.getSignal(),
                    request.getPayload(String.class)));
        }

        return Optional.ofNullable(endpointAdapter.handleMessage(request))
                .map(response -> {
                    if (response instanceof FtpMessage) {
                        return (FtpMessage) response;
                    } else {
                        return new FtpMessage(response);
                    }
                })
                .orElseGet(FtpMessage::success);
    }

    @Override
    public void init(FtpletContext ftpletContext) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Total FTP logins: %s", ftpletContext.getFtpStatistics().getTotalLoginNumber()));
        }
    }

    @Override
    public void destroy() {
        logger.info("FTP server shutting down ...");
    }

    @Override
    public FtpletResult beforeCommand(FtpSession session, FtpRequest request) {
        String command = request.getCommand().toUpperCase();

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Received FTP command: '%s'", command));
        }

        if (endpointConfiguration.isAutoLogin() && (command.equals(FTPCmd.USER.getCommand()) || command.equals(FTPCmd.PASS.getCommand()))) {
            return FtpletResult.DEFAULT;
        }

        if (Stream.of(endpointConfiguration.getAutoHandleCommands().split(",")).anyMatch(cmd -> cmd.trim().equals(command))) {
            return FtpletResult.DEFAULT;
        }

        FtpMessage response = handleMessage(FtpMessage.command(FTPCmd.valueOf(command)).arguments(request.getArgument()));
        if (response.hasReplyCode()) {
            writeFtpReply(session, response);
            return FtpletResult.SKIP;
        }

        return FtpletResult.DEFAULT;
    }

    @Override
    public FtpletResult afterCommand(FtpSession session, FtpRequest request, FtpReply reply) {
        return FtpletResult.DEFAULT;
    }

    @Override
    public FtpletResult onConnect(FtpSession session) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Received new FTP connection: '%s'", session.getSessionId()));
        }

        if (!endpointConfiguration.isAutoConnect()) {
            FtpMessage response = handleMessage(FtpMessage.connect(session.getSessionId().toString()));
            if (response.hasReplyCode()) {
                writeFtpReply(session, response);
                return FtpletResult.SKIP;
            }
        }

        return FtpletResult.DEFAULT;
    }

    @Override
    public FtpletResult onDisconnect(FtpSession session) {
        if (!endpointConfiguration.isAutoConnect()) {
            FtpMessage response = handleMessage(FtpMessage.command(FTPCmd.QUIT)
                                                        .arguments(Optional.ofNullable(session.getUser()).map(User::getName).orElse("unknown") + ":" +
                                                                   Optional.ofNullable(session.getUser()).map(User::getPassword).orElse("n/a")));
            if (response.hasReplyCode()) {
                writeFtpReply(session, response);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Closing FTP connection: '%s'", session.getSessionId()));
        }

        return FtpletResult.DISCONNECT;
    }

    /**
     * Construct ftp reply from response message and write reply to given session.
     * @param session
     * @param response
     */
    private void writeFtpReply(FtpSession session, FtpMessage response) {
        try {
            CommandResultType commandResult = response.getPayload(CommandResultType.class);
            FtpReply reply = new DefaultFtpReply(Integer.valueOf(commandResult.getReplyCode()), commandResult.getReplyString());

            session.write(reply);
        } catch (FtpException e) {
            throw new CitrusRuntimeException("Failed to write ftp reply", e);
        }
    }
}
