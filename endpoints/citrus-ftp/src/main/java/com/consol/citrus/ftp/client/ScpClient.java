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

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.ftp.message.FtpMessage;
import com.consol.citrus.ftp.model.CommandType;
import com.consol.citrus.ftp.model.DeleteCommand;
import com.consol.citrus.ftp.model.GetCommand;
import com.consol.citrus.ftp.model.ListCommand;
import com.consol.citrus.ftp.model.PutCommand;
import com.consol.citrus.util.FileUtils;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier;
import org.apache.sshd.client.keyverifier.RejectAllServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.keyprovider.ClassLoadableResourceKeyPairProvider;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.sshd.scp.client.DefaultScpClientCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public class ScpClient extends SftpClient {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(ScpClient.class);

    private org.apache.sshd.scp.client.ScpClient scpClient;

    /**
     * Default constructor initializing endpoint configuration.
     */
    public ScpClient() {
        this(new ScpEndpointConfiguration());
    }

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    protected ScpClient(ScpEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
    }

    @Override
    public ScpEndpointConfiguration getEndpointConfiguration() {
        return (ScpEndpointConfiguration) super.getEndpointConfiguration();
    }

    @Override
    protected FtpMessage createDir(CommandType ftpCommand) {
        throw new UnsupportedOperationException("SCP client does not support create directory operation - please use sftp client");
    }

    @Override
    protected FtpMessage listFiles(ListCommand list, TestContext context) {
        throw new UnsupportedOperationException("SCP client does not support list files operation - please use sftp client");
    }

    @Override
    protected FtpMessage deleteFile(DeleteCommand delete, TestContext context) {
        throw new UnsupportedOperationException("SCP client does not support delete file operation - please use sftp client");
    }

    @Override
    protected FtpMessage storeFile(PutCommand command, TestContext context) {
        try {
            scpClient.upload(FileUtils.getFileResource(command.getFile().getPath(), context).getFile().getAbsolutePath(), command.getTarget().getPath());
        } catch (IOException e) {
            LOG.error("Failed to store file via SCP", e);
            return FtpMessage.error();

        }
        return FtpMessage.success();
    }

    @Override
    protected FtpMessage retrieveFile(GetCommand command, TestContext context) {
        try {
            Resource target = FileUtils.getFileResource(command.getTarget().getPath(), context);
            if (!Optional.ofNullable(target.getFile().getParentFile()).map(File::mkdirs).orElse(true)) {
                LOG.warn("Failed to create target directories in path: " + target.getFile().getAbsolutePath());
            }

            scpClient.download(command.getFile().getPath(), target.getFile().getAbsolutePath());
        } catch (IOException e) {
            LOG.error("Failed to retrieve file via SCP", e);
            return FtpMessage.error();
        }

        return FtpMessage.success();
    }

    @Override
    protected void connectAndLogin() {
        try {
            SshClient client = SshClient.setUpDefaultClient();
            client.start();

            if (getEndpointConfiguration().isStrictHostChecking()) {
                client.setServerKeyVerifier(new KnownHostsServerKeyVerifier(RejectAllServerKeyVerifier.INSTANCE, FileUtils.getFileResource(getEndpointConfiguration().getKnownHosts()).getFile().toPath()));
            } else {
                client.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE);
            }

            ClientSession session = client.connect(getEndpointConfiguration().getUser(), getEndpointConfiguration().getHost(), getEndpointConfiguration().getPort()).verify(getEndpointConfiguration().getTimeout()).getSession();
            session.addPasswordIdentity(getEndpointConfiguration().getPassword());

            if (getPrivateKeyPath() != null) {
                Resource privateKey = FileUtils.getFileResource(getPrivateKeyPath());

                if (privateKey instanceof ClassPathResource) {
                    new ClassLoadableResourceKeyPairProvider(privateKey.getFile().getPath()).loadKeys(session).forEach(session::addPublicKeyIdentity);
                } else {
                    new FileKeyPairProvider(privateKey.getFile().toPath()).loadKeys(session).forEach(session::addPublicKeyIdentity);
                }
            }

            session.auth().verify(getEndpointConfiguration().getTimeout());

            scpClient = new DefaultScpClientCreator().createScpClient(session);
        } catch (Exception e) {
            throw new CitrusRuntimeException(String.format("Failed to login to SCP server using credentials: %s:%s:%s", getEndpointConfiguration().getUser(), getEndpointConfiguration().getPassword(), getEndpointConfiguration().getPrivateKeyPath()), e);

        }
    }

    @Override
    public void initialize() {
    }

    @Override
    public void destroy() {
    }
}
