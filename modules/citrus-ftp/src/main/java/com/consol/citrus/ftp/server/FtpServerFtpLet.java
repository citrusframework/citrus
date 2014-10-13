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

package com.consol.citrus.ftp.server;

import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.ftp.message.FtpMessage;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.ftpserver.ftplet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Ftp servlet implementation that logs incoming connections and commands forwarding those to
 * endpoint adapter for processing in test case.
 *
 * Test case can manage the Ftp command result by providing a Ftp result message.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
public class FtpServerFtpLet implements Ftplet {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(FtpServerFtpLet.class);

    /** Endpoint adapter */
    private final EndpointAdapter endpointAdapter;

    /**
     * Constructor using the server's endpoint adapter implementation.
     * @param endpointAdapter
     */
    public FtpServerFtpLet(EndpointAdapter endpointAdapter) {
        this.endpointAdapter = endpointAdapter;
    }

    @Override
    public void init(FtpletContext ftpletContext) throws FtpException {
        log.info(String.format("Total FTP logins: %s", ftpletContext.getFtpStatistics().getTotalLoginNumber()));
    }

    @Override
    public void destroy() {
        log.info("FTP server shutting down ...");
    }

    @Override
    public FtpletResult beforeCommand(FtpSession session, FtpRequest request) throws FtpException, IOException {
        String command = request.getCommand().toUpperCase();

        log.info(String.format("Received FTP command: '%s'", command));

        endpointAdapter.handleMessage(new FtpMessage(FTPCmd.valueOf(command), request.getArgument()));

        return FtpletResult.DEFAULT;
    }

    @Override
    public FtpletResult afterCommand(FtpSession session, FtpRequest request, FtpReply reply) throws FtpException, IOException {
        return FtpletResult.DEFAULT;
    }

    @Override
    public FtpletResult onConnect(FtpSession session) throws FtpException, IOException {
        log.info(String.format("Received new FTP connection: '%s'", session.getSessionId()));

        return FtpletResult.DEFAULT;
    }

    @Override
    public FtpletResult onDisconnect(FtpSession session) throws FtpException, IOException {
        log.info(String.format("Closing FTP connection: '%s'", session.getSessionId()));

        return FtpletResult.DEFAULT;
    }
}
