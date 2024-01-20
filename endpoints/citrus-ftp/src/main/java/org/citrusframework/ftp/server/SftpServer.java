/*
 * Copyright 2006-2018 the original author or authors.
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

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.net.ftp.FTPCmd;
import org.apache.ftpserver.ftplet.DataType;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.util.buffer.BufferUtils;
import org.apache.sshd.scp.common.ScpTransferEventListener;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.server.FileHandle;
import org.apache.sshd.sftp.server.SftpEventListener;
import org.citrusframework.endpoint.AbstractPollableEndpointConfiguration;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.ftp.client.SftpEndpointConfiguration;
import org.citrusframework.ftp.message.FtpMessage;
import org.citrusframework.ftp.model.Command;
import org.citrusframework.ftp.model.CommandResult;
import org.citrusframework.ssh.server.SshServer;
import org.citrusframework.xml.StringResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public class SftpServer extends SshServer implements ScpTransferEventListener, SftpEventListener {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(SftpServer.class);

    /**  This servers endpoint configuration */
    private final SftpEndpointConfiguration endpointConfiguration;

    /**
     * Default constructor using default endpoint configuration.
     */
    public SftpServer() {
        this(new SftpEndpointConfiguration());
    }

    /**
     * Constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public SftpServer(SftpEndpointConfiguration endpointConfiguration) {
        this.endpointConfiguration = endpointConfiguration;
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

        return Optional.ofNullable(getEndpointAdapter().handleMessage(request))
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
    public void startFileEvent(Session session, FileOperation op, Path file, long length, Set<PosixFilePermission> perms) {
        startFolderEvent(session, op, file, perms);
    }

    @Override
    public void startFolderEvent(Session session, FileOperation op, Path file, Set<PosixFilePermission> perms) {
        if (op.equals(FileOperation.SEND)) {
            FtpMessage response = handleMessage(FtpMessage.get(file.toString()));
            if (response.hasException()) {
                throw new CitrusRuntimeException(response.getPayload(CommandResult.class).getException());
            }
        } else if (op.equals(FileOperation.RECEIVE)) {
            FtpMessage response = handleMessage(FtpMessage.put(file.toString()));
            if (response.hasException()) {
                throw new CitrusRuntimeException(response.getPayload(CommandResult.class).getException());
            }
        }
    }

    @Override
    public void initialized(ServerSession session, int version) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Received new SFTP connection: '%s'", Arrays.toString(session.getSessionId())));
        }

        if (!endpointConfiguration.isAutoConnect()) {
            FtpMessage response = handleMessage(FtpMessage.connect(Arrays.toString(session.getSessionId())));
            if (response.hasException()) {
                throw new CitrusRuntimeException(response.getPayload(CommandResult.class).getException());
            }
        }
    }

    @Override
    public void reading(ServerSession session, String remoteHandle, FileHandle localHandle, long offset, byte[] data, int dataOffset, int dataLen) {
        FtpMessage response = handleMessage(FtpMessage.get(localHandle.getFile().toString(), BufferUtils.toHex(BufferUtils.EMPTY_HEX_SEPARATOR, remoteHandle.getBytes(StandardCharsets.UTF_8)), DataType.ASCII));
        if (response.hasException()) {
            throw new CitrusRuntimeException(response.getPayload(CommandResult.class).getException());
        }
    }

    @Override
    public void writing(ServerSession session, String remoteHandle, FileHandle localHandle, long offset, byte[] data, int dataOffset, int dataLen) {
        FtpMessage response = handleMessage(FtpMessage.put(BufferUtils.toHex(BufferUtils.EMPTY_HEX_SEPARATOR, remoteHandle.getBytes(StandardCharsets.UTF_8)), localHandle.getFile().toString(), DataType.ASCII));
        if (response.hasException()) {
            throw new CitrusRuntimeException(response.getPayload(CommandResult.class).getException());
        }
    }

    @Override
    public void destroying(ServerSession session) {
        if (!endpointConfiguration.isAutoConnect()) {
            FtpMessage response = handleMessage(FtpMessage.command(FTPCmd.QUIT)
                    .arguments(Optional.ofNullable(session.getUsername()).orElse("unknown")));

            if (response.hasException()) {
                throw new CitrusRuntimeException(response.getPayload(CommandResult.class).getException());
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Closing FTP connection: '%s'", session.getSessionId()));
        }
    }

    /**
     * Gets the endpointConfiguration.
     *
     * @return
     */
    @Override
    public AbstractPollableEndpointConfiguration getEndpointConfiguration() {
        return endpointConfiguration;
    }

    @Override
    protected ScpTransferEventListener getScpTransferEventListener() {
        return this;
    }

    @Override
    protected SftpEventListener getSftpEventListener() {
        return this;
    }

    @Override
    public void setPort(int port) {
        super.setPort(port);
        this.endpointConfiguration.setPort(port);
    }

    @Override
    public void setUser(String user) {
        super.setUser(user);
        this.endpointConfiguration.setUser(user);
    }

    @Override
    public void setPassword(String password) {
        super.setPassword(password);
        this.endpointConfiguration.setPassword(password);
    }
}
