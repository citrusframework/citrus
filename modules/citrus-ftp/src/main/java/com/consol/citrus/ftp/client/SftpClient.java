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

package com.consol.citrus.ftp.client;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.ftp.message.FtpMessage;
import com.consol.citrus.ftp.model.*;
import com.consol.citrus.util.FileUtils;
import com.jcraft.jsch.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.ftpserver.ftplet.DataType;
import org.apache.ftpserver.ftplet.FtpReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public class SftpClient extends FtpClient {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(SftpClient.class);

    /** Apache ftp client */
    private JSch ssh;
    private Session session;
    private ChannelSftp sftp;

    /**
     * Default constructor initializing endpoint configuration.
     */
    public SftpClient() {
        this(new SftpEndpointConfiguration());
    }

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    protected SftpClient(SftpEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
    }

    @Override
    public SftpEndpointConfiguration getEndpointConfiguration() {
        return (SftpEndpointConfiguration) super.getEndpointConfiguration();
    }

    @Override
    protected FtpMessage executeCommand(CommandType ftpCommand) {
        try {
            if (ftpCommand.getSignal().equals(FTPCmd.MKD.getCommand())) {
                sftp.mkdir(ftpCommand.getArguments());
                return FtpMessage.result(FtpReply.REPLY_257_PATHNAME_CREATED, "Pathname created", true);
            } else {
                throw new CitrusRuntimeException(String.format("Unsupported ftp command '%s'", ftpCommand.getSignal()));
            }
        } catch (SftpException e) {
            throw new CitrusRuntimeException("Failed to execute ftp command", e);
        }
    }

    @Override
    protected FtpMessage listFiles(ListCommand list, TestContext context) {
        String remoteFilePath = Optional.ofNullable(list.getTarget())
                                        .map(ListCommand.Target::getPath)
                                        .map(context::replaceDynamicContentInString)
                                        .orElse("");

        try {
            List<String> fileNames = new ArrayList<>();
            Vector<ChannelSftp.LsEntry> entries = sftp.ls(remoteFilePath);
            for (ChannelSftp.LsEntry entry : entries) {
                fileNames.add(entry.getFilename());
            }

            return FtpMessage.result(FtpReply.REPLY_150_FILE_STATUS_OKAY, "List files complete", fileNames);
        } catch (SftpException e) {
            throw new CitrusRuntimeException(String.format("Failed to list files in path '%s'", remoteFilePath), e);
        }
    }

    @Override
    protected FtpMessage deleteFile(DeleteCommand delete, TestContext context) {
        String remoteFilePath = context.replaceDynamicContentInString(delete.getTarget().getPath());

        try {
            if (!StringUtils.hasText(remoteFilePath)) {
                return null;
            }

            if (isDirectory(remoteFilePath)) {
                sftp.cd(remoteFilePath);

                if (delete.isRecursive()) {
                    Vector<ChannelSftp.LsEntry> entries = sftp.ls(".");
                    List<String> excludedDirs = Arrays.asList(".", "..");

                    for (ChannelSftp.LsEntry entry : entries) {
                        if (!excludedDirs.contains(entry.getFilename())) {
                            DeleteCommand recursiveDelete = new DeleteCommand();
                            DeleteCommand.Target target = new DeleteCommand.Target();
                            target.setPath(remoteFilePath + "/" + entry.getFilename());
                            recursiveDelete.setTarget(target);
                            recursiveDelete.setIncludeCurrent(true);
                            deleteFile(recursiveDelete, context);
                        }
                    }
                }

                if (delete.isIncludeCurrent()) {
                    // we cannot delete the current working directory, so go to root directory and delete from there
                    sftp.cd("..");
                    sftp.rmdir(remoteFilePath);
                }
            } else {
                sftp.rm(remoteFilePath);
            }
        } catch (SftpException e) {
            throw new CitrusRuntimeException("Failed to delete file from FTP server", e);
        }

        return FtpMessage.result(FtpReply.REPLY_150_FILE_STATUS_OKAY, "Delete file complete", true);
    }

    @Override
    protected boolean isDirectory(String remoteFilePath) {
        try {
            return !remoteFilePath.contains("*") && sftp.stat(remoteFilePath).isDir();
        } catch (SftpException e) {
            throw new CitrusRuntimeException("Failed to check file state", e);
        }
    }

    @Override
    protected FtpMessage storeFile(PutCommand put, TestContext context) {
        try {
            String localFilePath = context.replaceDynamicContentInString(put.getFile().getPath());
            String remoteFilePath = addFileNameToTargetPath(localFilePath, context.replaceDynamicContentInString(put.getTarget().getPath()));

            try (InputStream localFileInputStream = FileUtils.getFileResource(localFilePath).getInputStream()) {
                sftp.put(localFileInputStream, remoteFilePath);
            }
        } catch (IOException | SftpException e) {
            throw new CitrusRuntimeException("Failed to put file to FTP server", e);
        }

        return FtpMessage.result(FtpReply.REPLY_226_CLOSING_DATA_CONNECTION, "Transfer complete", true);
    }

    @Override
    protected FtpMessage retrieveFile(GetCommand command, TestContext context) {
        try {
            String remoteFilePath = context.replaceDynamicContentInString(command.getFile().getPath());
            String localFilePath = addFileNameToTargetPath(remoteFilePath, context.replaceDynamicContentInString(command.getTarget().getPath()));

            try (InputStream inputStream = sftp.get(remoteFilePath)) {
                byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);

                // create intermediate directories if necessary
                Path localFilePathObj = Paths.get(localFilePath);
                Files.createDirectories(localFilePathObj.getParent());
                Files.write(localFilePathObj, bytes);
            } catch (SftpException e) {
                throw new CitrusRuntimeException(String.format("Failed to get file from FTP server. Remote path: %s. Local file path: %s. Error: %s",
                        remoteFilePath, localFilePath, e.getMessage()));
            }

            if (getEndpointConfiguration().isAutoReadFiles()) {
                String fileContent;
                if (command.getFile().getType().equals(DataType.BINARY.name())) {
                    fileContent = Base64.encodeBase64String(FileCopyUtils.copyToByteArray(FileUtils.getFileResource(localFilePath).getInputStream()));
                } else {
                    fileContent = FileUtils.readToString(FileUtils.getFileResource(localFilePath));
                }

                return FtpMessage.result(FtpReply.REPLY_226_CLOSING_DATA_CONNECTION, "Transfer complete", localFilePath, fileContent);
            } else {
                return FtpMessage.result(FtpReply.REPLY_226_CLOSING_DATA_CONNECTION, "Transfer complete", localFilePath, null);
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to get file from FTP server", e);
        }
    }

    @Override
    protected void connectAndLogin() {
        if (session == null || !session.isConnected()) {
            try {
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                session = ssh.getSession(getEndpointConfiguration().getUser(), getEndpointConfiguration().getHost(), getEndpointConfiguration().getPort());
                session.setConfig(config);
                session.setPassword(getEndpointConfiguration().getPassword());
                session.connect();
                Channel channel = session.openChannel("sftp");
                channel.connect();
                sftp = (ChannelSftp) channel;

                log.info("Opened secure connection to FTP server");
            } catch (JSchException e) {
                throw new CitrusRuntimeException(String.format("Failed to login to FTP server using credentials: %s:%s", getEndpointConfiguration().getUser(), getEndpointConfiguration().getPassword()), e);
            }
        }
    }

    @Override
    public void afterPropertiesSet() {
        if (ssh == null) {
            ssh = new JSch();
        }
    }

    @Override
    public void destroy() throws Exception {
        if (session != null && session.isConnected()) {
            session.disconnect();
            log.info("Closed connection to FTP server");
        }

        sftp.disconnect();
    }

    /**
     * Gets the ssh.
     *
     * @return
     */
    public JSch getSsh() {
        return ssh;
    }

    /**
     * Sets the ssh.
     *
     * @param ssh
     */
    public void setSsh(JSch ssh) {
        this.ssh = ssh;
    }
}
