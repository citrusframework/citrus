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

import com.consol.citrus.ftp.message.FtpMessage;
import com.consol.citrus.ftp.model.*;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.util.FileUtils;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.UserAuth;
import org.apache.sshd.server.auth.UserAuthNoneFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public class SftpClientTest extends AbstractTestNGUnitTest {

    private SftpClient sftpClient;
    private SshServer sshServer;

    private String targetPath;
    private String remoteFilePath;
    private String localFilePath;
    private String inputFileAsString;

    @BeforeClass
    public void setUp() throws Exception {
        targetPath = System.getProperty("project.build.directory");
        localFilePath = "classpath:ftp/input/hello.xml";
        remoteFilePath = targetPath + "/hello.xml";
        inputFileAsString = FileUtils.readToString(new ClassPathResource("ftp/input/hello.xml"), StandardCharsets.UTF_8);
        sshServer = startSftpMockServer();
        sftpClient = createSftpClient();
    }

    @AfterClass
    public void tearDown() throws Exception {
        sftpClient.destroy();
        sshServer.close();
    }

    @Test
    public void testRetrieveFile() {
        sftpClient.storeFile(putCommand(localFilePath, remoteFilePath), context);
        Assert.assertTrue(Paths.get(remoteFilePath).toFile().exists());

        FtpMessage response = sftpClient.retrieveFile(getCommand(remoteFilePath), context);
        Assert.assertEquals(response.getPayload(GetCommandResult.class).getFile().getData(), inputFileAsString);
    }

    @Test
    public void testRetrieveFileToLocalPath() throws Exception {
        Path localDownloadFilePath = Paths.get(targetPath, "local_download.xml");

        sftpClient.storeFile(putCommand(localFilePath, remoteFilePath), context);
        Assert.assertTrue(Paths.get(remoteFilePath).toFile().exists());

        sftpClient.retrieveFile(getCommand(remoteFilePath, localDownloadFilePath.toString()), context);
        Assert.assertEquals(inputFileAsString,
                new String(Files.readAllBytes(localDownloadFilePath), "UTF-8"));
    }

    @Test
    public void testRetrieveFileToLocalPathWithoutFilename() throws Exception {
        Path localDownloadFilePath = Paths.get(targetPath, "local_download.xml");

        sftpClient.storeFile(putCommand(localFilePath, targetPath + "/"), context);
        Assert.assertTrue(Paths.get(remoteFilePath).toFile().exists());

        sftpClient.retrieveFile(getCommand(remoteFilePath, localDownloadFilePath.toString()), context);
        Assert.assertEquals(inputFileAsString,
                new String(Files.readAllBytes(localDownloadFilePath), "UTF-8"));
    }

    @Test
    public void testDeleteFile() {
        sftpClient.storeFile(putCommand(localFilePath, remoteFilePath), context);
        Assert.assertTrue(Paths.get(remoteFilePath).toFile().exists());
        sftpClient.deleteFile(deleteCommand(remoteFilePath), context);
        Assert.assertFalse(Paths.get(remoteFilePath).toFile().exists());
    }

    @Test
    public void testDeleteGlob() {
        String remoteFilePathCopy = remoteFilePath.replace(".xml", "_copy.xml");
        sftpClient.storeFile(putCommand(localFilePath, remoteFilePath), context);
        sftpClient.storeFile(putCommand(localFilePath, remoteFilePathCopy), context);
        Assert.assertTrue(Paths.get(remoteFilePath).toFile().exists());
        Assert.assertTrue(Paths.get(remoteFilePathCopy).toFile().exists());
        sftpClient.deleteFile(deleteCommand(targetPath + "/hello*.xml"), context);
        Assert.assertFalse(Paths.get(remoteFilePath).toFile().exists());
        Assert.assertFalse(Paths.get(remoteFilePathCopy).toFile().exists());
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

        Assert.assertTrue(Files.exists(tmpDir));
        DeleteCommand deleteCommand = deleteCommand(tmpDir.toAbsolutePath().toString());
        deleteCommand.setIncludeCurrent(true);
        sftpClient.deleteFile(deleteCommand, context);
        Assert.assertFalse(Files.exists(tmpDir));
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

        Assert.assertTrue(Files.exists(tmpDir));
        sftpClient.deleteFile(deleteCommand(tmpDir.toAbsolutePath().toString()), context);
        Assert.assertTrue(tmpDir.toFile().list().length == 0);
        Assert.assertTrue(Files.exists(tmpDir));
    }

    @Test
    public void testDeleteNoMatches() {
        // this should not throw an exception, even though no files match
        sftpClient.deleteFile(deleteCommand(targetPath + "/1234*1234"), context);
    }

    private SshServer startSftpMockServer() throws IOException {
        // SFTP mock server without authentication
        SshServer sshd = SshServer.setUpDefaultServer();
        sshd.setPort(2222);

        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(
                Paths.get(targetPath, "sshd_hostkey.ser")));

        List<NamedFactory<UserAuth>> userAuthFactories = new ArrayList<>();
        userAuthFactories.add(UserAuthNoneFactory.INSTANCE);
        sshd.setUserAuthFactories(userAuthFactories);

        List<NamedFactory<Command>> namedFactoryList = new ArrayList<>();
        namedFactoryList.add(new SftpSubsystemFactory());
        sshd.setSubsystemFactories(namedFactoryList);

        sshd.start();

        return sshd;
    }

    private SftpClient createSftpClient() {
        SftpEndpointConfiguration endpointConfiguration = new SftpEndpointConfiguration();
        endpointConfiguration.setHost("localhost");
        endpointConfiguration.setPort(2222);
        endpointConfiguration.setUser("remote-username");
        endpointConfiguration.setPassword("remote-password");

        SftpClient sftpClient = new SftpClient(endpointConfiguration);
        sftpClient.afterPropertiesSet();
        sftpClient.connectAndLogin();
        return sftpClient;
    }

    private void writeToFile(String fileContent, Path dir) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(dir, Charset.forName("UTF-8"))) {
            writer.write(fileContent, 0, fileContent.length());
        }
    }

    private GetCommand getCommand(String remoteFilePath) {
        return getCommand(remoteFilePath, remoteFilePath);
    }

    private GetCommand getCommand(String remoteFilePath, String localFilePath) {
        GetCommand command = new GetCommand();
        GetCommand.File file = new GetCommand.File();
        file.setPath(remoteFilePath);
        file.setType("ASCII");
        command.setFile(file);
        GetCommand.Target target = new GetCommand.Target();
        target.setPath(localFilePath);
        command.setTarget(target);

        return command;
    }

    private PutCommand putCommand(String localFilePath, String remoteFilePath) {
        PutCommand command = new PutCommand();
        PutCommand.File file = new PutCommand.File();
        file.setPath(localFilePath);
        file.setType("ASCII");
        command.setFile(file);
        PutCommand.Target target = new PutCommand.Target();
        target.setPath(remoteFilePath);
        command.setTarget(target);

        return command;
    }

    private DeleteCommand deleteCommand(String targetPath) {
        DeleteCommand command = new DeleteCommand();
        DeleteCommand.Target target = new DeleteCommand.Target();
        target.setPath(targetPath);
        command.setTarget(target);

        command.setRecursive(true);

        return command;
    }
}