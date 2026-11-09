/*
 * Copyright the original author or authors.
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.sshd.common.keyprovider.ClassLoadableResourceKeyPairProvider;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.subsystem.SubsystemFactory;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.citrusframework.ftp.message.FtpMessage;
import org.citrusframework.ftp.model.DeleteCommand;
import org.citrusframework.ftp.model.DeleteCommandResult;
import org.citrusframework.ftp.model.GetCommandResult;
import org.citrusframework.ftp.model.ListCommandResult;
import org.citrusframework.ftp.model.PutCommandResult;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.apache.commons.net.ftp.FTPReply.CLOSING_DATA_CONNECTION;
import static org.apache.commons.net.ftp.FTPReply.FILE_ACTION_OK;
import static org.apache.commons.net.ftp.FTPReply.FILE_STATUS_OK;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @since 2.7.5
 */
@DisabledOnOs(value = OS.WINDOWS, disabledReason = "Cannot handle win specific file paths.")
public class SftpClientTest extends AbstractFtpClientTest {

    private static SftpClient sftpClient;
    private static SshServer sshServer;

    private static String targetPath;
    private static String remoteFilePath;
    private static String localFilePath;
    private static String inputFileAsString;

    @BeforeAll
    public static void setUp() throws Exception {

        targetPath = System.getProperty("project.build.directory");
        localFilePath = "classpath:ftp/input/hello.xml";
        remoteFilePath = targetPath + "/hello.xml";
        inputFileAsString = FileUtils.readToString(Resources.fromClasspath("ftp/input/hello.xml"), StandardCharsets.UTF_8);
        sshServer = startSftpMockServer();
        sftpClient = createSftpClient();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        sftpClient.destroy();
        sshServer.close();
    }

    @Test
    public void testListFiles() {
        String remoteFilePath = targetPath + "/file1";
        FtpMessage ftpMessage = sftpClient.storeFile(putCommand(localFilePath, remoteFilePath), context);
        verifyMessage(ftpMessage, PutCommandResult.class, CLOSING_DATA_CONNECTION, "Transfer complete");
        assertTrue(Paths.get(remoteFilePath).toFile().exists());
        remoteFilePath = targetPath + "/file2";
        ftpMessage = sftpClient.storeFile(putCommand(localFilePath, remoteFilePath), context);
        verifyMessage(ftpMessage, PutCommandResult.class, CLOSING_DATA_CONNECTION, "Transfer complete");
        assertTrue(Paths.get(remoteFilePath).toFile().exists());

        ftpMessage = sftpClient.listFiles(listCommand(targetPath + "/file*"), context);
        verifyMessage(ftpMessage, ListCommandResult.class, FILE_STATUS_OK,
                "List files complete", Arrays.asList("file1", "file2"));
        assertTrue(Paths.get(targetPath + "/file1").toFile().exists());
        assertTrue(Paths.get(targetPath + "/file2").toFile().exists());
    }

    @Test
    public void testRetrieveFile() {
        FtpMessage ftpMessage = sftpClient.storeFile(putCommand(localFilePath, remoteFilePath), context);
        verifyMessage(ftpMessage, PutCommandResult.class, CLOSING_DATA_CONNECTION, "Transfer complete");
        assertTrue(Paths.get(remoteFilePath).toFile().exists());

        FtpMessage response = sftpClient.retrieveFile(getCommand(remoteFilePath), context);
        verifyMessage(response, GetCommandResult.class, CLOSING_DATA_CONNECTION, "Transfer complete");
        Assertions.assertEquals(inputFileAsString, response.getPayload(GetCommandResult.class).getFile().getData());
    }

    @Test
    public void testRetrieveFileToLocalPath() throws Exception {
        Path localDownloadFilePath = Paths.get(targetPath, "local_download.xml");

        FtpMessage ftpMessage = sftpClient.storeFile(putCommand(localFilePath, remoteFilePath), context);
        verifyMessage(ftpMessage, PutCommandResult.class, CLOSING_DATA_CONNECTION, "Transfer complete");
        assertTrue(Paths.get(remoteFilePath).toFile().exists());

        ftpMessage = sftpClient.retrieveFile(getCommand(remoteFilePath, localDownloadFilePath.toString()), context);
        verifyMessage(ftpMessage, GetCommandResult.class, CLOSING_DATA_CONNECTION, "Transfer complete");
        Assertions.assertEquals(inputFileAsString,
                new String(Files.readAllBytes(localDownloadFilePath), StandardCharsets.UTF_8));
    }

    @Test
    public void testRetrieveFileToLocalPathWithoutFilename() throws Exception {
        Path localDownloadFilePath = Paths.get(targetPath, "local_download.xml");

        FtpMessage ftpMessage = sftpClient.storeFile(putCommand(localFilePath, targetPath + "/"), context);
        verifyMessage(ftpMessage, PutCommandResult.class, CLOSING_DATA_CONNECTION, "Transfer complete");
        assertTrue(Paths.get(remoteFilePath).toFile().exists());

        ftpMessage = sftpClient.retrieveFile(getCommand(remoteFilePath, localDownloadFilePath.toString()), context);
        verifyMessage(ftpMessage, GetCommandResult.class, CLOSING_DATA_CONNECTION, "Transfer complete");
        Assertions.assertEquals(inputFileAsString,
                new String(Files.readAllBytes(localDownloadFilePath), StandardCharsets.UTF_8));
    }

    @Test
    public void testDeleteFile() {
        FtpMessage ftpMessage = sftpClient.storeFile(putCommand(localFilePath, remoteFilePath), context);
        verifyMessage(ftpMessage, PutCommandResult.class, CLOSING_DATA_CONNECTION, "Transfer complete");
        assertTrue(Paths.get(remoteFilePath).toFile().exists());
        ftpMessage = sftpClient.deleteFile(deleteCommand(remoteFilePath), context);
        verifyMessage(ftpMessage, DeleteCommandResult.class, FILE_ACTION_OK, "Delete file complete");
        Assertions.assertFalse(Paths.get(remoteFilePath).toFile().exists());
    }

    @Test
    public void testDeleteGlob() {
        String remoteFilePathCopy = remoteFilePath.replace(FileUtils.FILE_EXTENSION_XML, "_copy.xml");
        FtpMessage ftpMessage = sftpClient.storeFile(putCommand(localFilePath, remoteFilePath), context);
        verifyMessage(ftpMessage, PutCommandResult.class, CLOSING_DATA_CONNECTION, "Transfer complete");
        ftpMessage = sftpClient.storeFile(putCommand(localFilePath, remoteFilePathCopy), context);
        verifyMessage(ftpMessage, PutCommandResult.class, CLOSING_DATA_CONNECTION, "Transfer complete");
        assertTrue(Paths.get(remoteFilePath).toFile().exists());
        assertTrue(Paths.get(remoteFilePathCopy).toFile().exists());
        ftpMessage = sftpClient.deleteFile(deleteCommand(targetPath + "/hello*.xml"), context);
        verifyMessage(ftpMessage, DeleteCommandResult.class, FILE_ACTION_OK, "Delete file complete");
        Assertions.assertFalse(Paths.get(remoteFilePath).toFile().exists());
        Assertions.assertFalse(Paths.get(remoteFilePathCopy).toFile().exists());
    }

    @Test
    public void testDeleteDirIncludeCurrent() throws Exception {
        // the following dir structure and let is delete recursively via sftp:
        // tmpDir/
        // └── subDir
        //     └── testfile
        Path tmpDir = Paths.get(targetPath, "tmpDir");
        Path subDir = Files.createDirectories(tmpDir.resolve("subDir"));

        writeToFile("test file\n", subDir.resolve("testfile"));

        assertTrue(Files.exists(tmpDir));
        DeleteCommand deleteCommand = deleteCommand(tmpDir.toAbsolutePath().toString());
        deleteCommand.setIncludeCurrent(true);
        FtpMessage ftpMessage = sftpClient.deleteFile(deleteCommand, context);
        verifyMessage(ftpMessage, DeleteCommandResult.class, FILE_ACTION_OK, "Delete file complete");
        Assertions.assertFalse(Files.exists(tmpDir));
    }

    @Test
    public void testDeleteDir() throws Exception {
        // the following dir structure and let is delete recursively via sftp:
        // tmpDir/
        // └── subDir
        //     └── testfile
        Path tmpDir = Paths.get(targetPath, "tmpDir");
        Path subDir = Files.createDirectories(tmpDir.resolve("subDir"));

        writeToFile("test file\n", subDir.resolve("testfile"));

        assertTrue(Files.exists(tmpDir));
        FtpMessage ftpMessage = sftpClient.deleteFile(deleteCommand(tmpDir.toAbsolutePath().toString()), context);
        verifyMessage(ftpMessage, DeleteCommandResult.class, FILE_ACTION_OK, "Delete file complete");
        assertTrue(tmpDir.toFile().list().length == 0);
        assertTrue(Files.exists(tmpDir));
    }

    @Test
    public void testDeleteNoMatches() {
        // this should not throw an exception, even though no files match
        FtpMessage ftpMessage = sftpClient.deleteFile(deleteCommand(targetPath + "/1234*1234"), context);
        verifyMessage(ftpMessage, DeleteCommandResult.class, FILE_ACTION_OK, "Delete file complete");
    }

    private static SshServer startSftpMockServer() throws IOException {
        // SFTP mock server without authentication
        SshServer sshd = SshServer.setUpDefaultServer();
        sshd.setPort(2223);

        ClassLoadableResourceKeyPairProvider resourceKeyPairProvider = new ClassLoadableResourceKeyPairProvider();
        resourceKeyPairProvider.setResources(Collections.singletonList("org/citrusframework/ssh/citrus.pem"));
        sshd.setKeyPairProvider(resourceKeyPairProvider);

        sshd.setPasswordAuthenticator((username, password, session) -> true);

        List<SubsystemFactory> subsystemFactories = new ArrayList<>();
        SftpSubsystemFactory sftpSubsystemFactory = new SftpSubsystemFactory.Builder().build();

        subsystemFactories.add(sftpSubsystemFactory);
        sshd.setSubsystemFactories(subsystemFactories);

        List<String> availableSignatureFactories = sshd.getSignatureFactoriesNames();
        availableSignatureFactories.add("ssh-dss");
        sshd.setSignatureFactoriesNames(availableSignatureFactories);

        sshd.start();

        return sshd;
    }

    private static SftpClient createSftpClient() {
        SftpEndpointConfiguration endpointConfiguration = new SftpEndpointConfiguration();
        endpointConfiguration.setHost("localhost");
        endpointConfiguration.setPort(2223);
        endpointConfiguration.setUser("remote-username");
        endpointConfiguration.setPassword("remote-password");

        SftpClient sftpClient = new SftpClient(endpointConfiguration);
        sftpClient.initialize();
        sftpClient.connectAndLogin();
        return sftpClient;
    }

    private static void writeToFile(String fileContent, Path dir) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(dir, StandardCharsets.UTF_8)) {
            writer.write(fileContent, 0, fileContent.length());
        }
    }
}
