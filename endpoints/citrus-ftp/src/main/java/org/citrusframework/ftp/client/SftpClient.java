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

package org.citrusframework.ftp.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.ftpserver.ftplet.DataType;
import org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.ftp.message.FtpMessage;
import org.citrusframework.ftp.model.CommandType;
import org.citrusframework.ftp.model.DeleteCommand;
import org.citrusframework.ftp.model.GetCommand;
import org.citrusframework.ftp.model.ListCommand;
import org.citrusframework.ftp.model.PutCommand;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public class SftpClient extends FtpClient {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(SftpClient.class);

    /** Session for the SSH communication */
    private Session session;

    /** Apache ftp client */
    private JSch ssh;

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
    protected FtpMessage executeCommand(CommandType ftpCommand, TestContext context) {
        if (ftpCommand.getSignal().equals(FTPCmd.MKD.getCommand())) {
            return createDir(ftpCommand);
        } else if (ftpCommand.getSignal().equals(FTPCmd.LIST.getCommand())) {
            return listFiles(FtpMessage.list(ftpCommand.getArguments()).getPayload(ListCommand.class), context);
        } else if (ftpCommand.getSignal().equals(FTPCmd.DELE.getCommand())) {
            return deleteFile(FtpMessage.delete(ftpCommand.getArguments()).getPayload(DeleteCommand.class), context);
        } else if (ftpCommand.getSignal().equals(FTPCmd.STOR.getCommand())) {
            return storeFile(FtpMessage.put(ftpCommand.getArguments()).getPayload(PutCommand.class), context);
        } else if (ftpCommand.getSignal().equals(FTPCmd.RETR.getCommand())) {
            return retrieveFile(FtpMessage.get(ftpCommand.getArguments()).getPayload(GetCommand.class), context);
        } else {
            throw new CitrusRuntimeException(String.format("Unsupported ftp command '%s'", ftpCommand.getSignal()));
        }
    }

    /**
     * Execute mkDir command and create new directory.
     * @param ftpCommand
     * @return
     */
    protected FtpMessage createDir(CommandType ftpCommand) {
        try {
            sftp.mkdir(ftpCommand.getArguments());
            return FtpMessage.result(FTPReply.PATHNAME_CREATED, "Pathname created", true);
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

            return FtpMessage.result(FTPReply.FILE_STATUS_OK, "List files complete", fileNames);
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

        return FtpMessage.deleteResult(FTPReply.FILE_ACTION_OK, "Delete file complete", true);
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
    protected FtpMessage storeFile(PutCommand command, TestContext context) {
        try {
            String localFilePath = context.replaceDynamicContentInString(command.getFile().getPath());
            String remoteFilePath = addFileNameToTargetPath(localFilePath, context.replaceDynamicContentInString(command.getTarget().getPath()));

            String dataType = context.replaceDynamicContentInString(Optional.ofNullable(command.getFile().getType()).orElseGet(() -> DataType.BINARY.name()));
            try (InputStream localFileInputStream = getLocalFileInputStream(command.getFile().getPath(), dataType, context)) {
                sftp.put(localFileInputStream, remoteFilePath);
            }
        } catch (IOException | SftpException e) {
            throw new CitrusRuntimeException("Failed to put file to FTP server", e);
        }

        return FtpMessage.putResult(FTPReply.CLOSING_DATA_CONNECTION, "Transfer complete", true);
    }

    @Override
    protected FtpMessage retrieveFile(GetCommand command, TestContext context) {
        try {
            String remoteFilePath = context.replaceDynamicContentInString(command.getFile().getPath());
            String localFilePath = addFileNameToTargetPath(remoteFilePath, context.replaceDynamicContentInString(command.getTarget().getPath()));

            try (InputStream inputStream = sftp.get(remoteFilePath)) {
                byte[] bytes = FileUtils.copyToByteArray(inputStream);

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
                    fileContent = Base64.encodeBase64String(FileUtils.copyToByteArray(FileUtils.getFileResource(localFilePath)));
                } else {
                    fileContent = FileUtils.readToString(FileUtils.getFileResource(localFilePath));
                }

                return FtpMessage.result(FTPReply.CLOSING_DATA_CONNECTION, "Transfer complete", localFilePath, fileContent);
            } else {
                return FtpMessage.result(FTPReply.CLOSING_DATA_CONNECTION, "Transfer complete", localFilePath, null);
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to get file from FTP server", e);
        }
    }

    @Override
    protected void connectAndLogin() {
        if (getEndpointConfiguration().isStrictHostChecking()) {
            setKnownHosts();
        }

        if (session == null || !session.isConnected()) {
            try {
                if (StringUtils.hasText(getEndpointConfiguration().getPrivateKeyPath())) {
                    ssh.addIdentity(getPrivateKeyPath(), getEndpointConfiguration().getPrivateKeyPassword());
                }
            } catch (JSchException e) {
                throw new CitrusRuntimeException("Cannot add private key " + getEndpointConfiguration().getPrivateKeyPath() + ": " + e,e);
            } catch (IOException e) {
                throw new CitrusRuntimeException("Cannot open private key file " + getEndpointConfiguration().getPrivateKeyPath() + ": " + e,e);
            }

            try {
                session = ssh.getSession(getEndpointConfiguration().getUser(), getEndpointConfiguration().getHost(), getEndpointConfiguration().getPort());

                if (StringUtils.hasText(getEndpointConfiguration().getPassword())) {
                    session.setUserInfo(new UserInfoWithPlainPassword(getEndpointConfiguration().getPassword()));
                    session.setPassword(getEndpointConfiguration().getPassword());
                }

                session.setConfig(KnownHostsServerKeyVerifier.STRICT_CHECKING_OPTION, getEndpointConfiguration().isStrictHostChecking() ? "yes" : "no");
                session.setConfig("PreferredAuthentications", getEndpointConfiguration().getPreferredAuthentications());

                getEndpointConfiguration().getSessionConfigs().entrySet()
                        .stream()
                        .peek(entry -> logger.info(String.format("Setting session configuration: %s='%s'", entry.getKey(), entry.getValue())))
                        .forEach(entry -> session.setConfig(entry.getKey(), entry.getValue()));

                session.connect((int) getEndpointConfiguration().getTimeout());

                Channel channel = session.openChannel("sftp");
                channel.connect((int) getEndpointConfiguration().getTimeout());
                sftp = (ChannelSftp) channel;

                logger.info("Opened secure connection to FTP server");
            } catch (JSchException e) {
                throw new CitrusRuntimeException(String.format("Failed to login to FTP server using credentials: %s:%s", getEndpointConfiguration().getUser(), getEndpointConfiguration().getPassword()), e);
            }
        }
    }

    private void setKnownHosts() {
        if (getEndpointConfiguration().getKnownHosts() == null) {
            throw new CitrusRuntimeException("Strict host checking is enabled but no knownHosts given");
        }

        try {
            ssh.setKnownHosts(FileUtils.getFileResource(getEndpointConfiguration().getKnownHosts()).getInputStream());
        } catch (JSchException e) {
            throw new CitrusRuntimeException("Cannot add known hosts from " + getEndpointConfiguration().getKnownHosts() + ": " + e,e);
        }
    }

    protected String getPrivateKeyPath() throws IOException {
        if (!StringUtils.hasText(getEndpointConfiguration().getPrivateKeyPath())) {
            return null;
        } else if (getEndpointConfiguration().getPrivateKeyPath().startsWith(Resources.CLASSPATH_RESOURCE_PREFIX)) {
            File priv = File.createTempFile("citrus-sftp","priv");
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

    @Override
    public void initialize() {
        if (ssh == null) {
            ssh = new JSch();
        }
    }

    @Override
    public void destroy() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            logger.info("Closed connection to FTP server");
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
