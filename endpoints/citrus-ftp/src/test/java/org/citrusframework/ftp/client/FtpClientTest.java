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

package org.citrusframework.ftp.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.ftp.message.FtpMessage;
import org.citrusframework.ftp.model.DeleteCommand;
import org.citrusframework.ftp.model.DeleteCommandResult;
import org.citrusframework.ftp.model.ListCommandResult;
import org.citrusframework.ftp.model.PutCommandResult;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.message.Message;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPCmd;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.apache.commons.net.ftp.FTPReply.CLOSING_DATA_CONNECTION;
import static org.apache.commons.net.ftp.FTPReply.FILE_ACTION_OK;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public class FtpClientTest extends AbstractFtpClientTest {

    private FTPClient apacheFtpClient = Mockito.mock(FTPClient.class);
    private FtpClient ftpClient;

    private final FakeFtpServer fakeFtpServer = new FakeFtpServer();
    private static final String UPLOAD_FILE = "upload_file";
    private static final String DOWNLOAD_FILE = "/download_file";
    private static final String SINGLE_FILE = "/single_file";
    private static final String DELETE_FOLDER = "/delete";
    private static final String EMPTY_FOLDER = "/empty_folder";
    private static final String FOLDER = "/folder";
    private static final String COMPLETELY_DELETE_FOLDER = "/completely_delete";

    private String targetPath;

    @BeforeClass
    public void setUp() throws Exception {
        targetPath = System.getProperty("project.build.directory");
        initMockFtpServer();
        initFtpClient();
    }

    private void initFtpClient() throws IOException {
        FtpEndpointConfiguration endpointConfiguration = new FtpEndpointConfiguration();
        endpointConfiguration.setHost("localhost");
        endpointConfiguration.setPort(2221);
        endpointConfiguration.setUser("ftp_user");
        endpointConfiguration.setPassword("ftp_password");

        ftpClient = new FtpClient(endpointConfiguration);
        ftpClient.initialize();
        ftpClient.connectAndLogin();
    }

    private void initMockFtpServer() throws InterruptedException {
        fakeFtpServer.setServerControlPort(2221);
        fakeFtpServer.addUserAccount(new UserAccount("ftp_user", "ftp_password", "/"));

        FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new FileEntry(DOWNLOAD_FILE));
        // modified time is exact to the second, so we have to wait in between writes.
        Thread.sleep(2000);
        fileSystem.add(new FileEntry(DOWNLOAD_FILE + "_2"));
        fileSystem.add(new FileEntry(SINGLE_FILE));
        fileSystem.add(new DirectoryEntry(COMPLETELY_DELETE_FOLDER + "/first_folder"));
        fileSystem.add(new DirectoryEntry(COMPLETELY_DELETE_FOLDER + "/second_folder"));
        fileSystem.add(new FileEntry(COMPLETELY_DELETE_FOLDER + "/first_folder/file1"));
        fileSystem.add(new FileEntry(COMPLETELY_DELETE_FOLDER + "/first_folder/file2"));
        fileSystem.add(new FileEntry(COMPLETELY_DELETE_FOLDER + "/second_folder/file3"));

        fileSystem.add(new DirectoryEntry(DELETE_FOLDER + "/first_folder"));
        fileSystem.add(new DirectoryEntry(DELETE_FOLDER + "/second_folder"));
        fileSystem.add(new FileEntry(DELETE_FOLDER + "/first_folder/file1"));
        fileSystem.add(new FileEntry(DELETE_FOLDER + "/first_folder/file2"));
        fileSystem.add(new FileEntry(DELETE_FOLDER + "/second_folder/file3"));

        fileSystem.add(new DirectoryEntry(EMPTY_FOLDER));

        fileSystem.add(new DirectoryEntry(FOLDER + "/file1"));
        fileSystem.add(new DirectoryEntry(FOLDER + "/file2"));

        fakeFtpServer.setFileSystem(fileSystem);

        fakeFtpServer.start();
    }

    @AfterClass
    public void tearDown() throws Exception {
        ftpClient.destroy();
        fakeFtpServer.stop();
    }

    @Test
    public void testListFiles() {
        assertTrue(fakeFtpServer.getFileSystem().exists(FOLDER));
        FtpMessage ftpMessage = ftpClient.listFiles(listCommand(FOLDER + "/file*"), context);
        verifyMessage(ftpMessage, ListCommandResult.class, CLOSING_DATA_CONNECTION,
                "Requested file action successful.", Arrays.asList("file1", "file2"));
        assertTrue(fakeFtpServer.getFileSystem().exists(FOLDER + "/file1"));
        assertTrue(fakeFtpServer.getFileSystem().exists(FOLDER + "/file2"));
    }

    @Test
    public void testRetrieveFile() {
        assertTrue(fakeFtpServer.getFileSystem().exists(DOWNLOAD_FILE));
        String localFilePath = Paths.get(targetPath, "download_file").toString();
        ftpClient.retrieveFile(getCommand(DOWNLOAD_FILE, localFilePath), context);
        assertTrue(fakeFtpServer.getFileSystem().exists(DOWNLOAD_FILE));
        assertTrue(new File(localFilePath).exists());
    }

    @Test
    public void testRetrieveFileImplicitFilename() {
        assertTrue(fakeFtpServer.getFileSystem().exists(DOWNLOAD_FILE));
        ftpClient.retrieveFile(getCommand(DOWNLOAD_FILE, targetPath + "/"), context);
        assertTrue(fakeFtpServer.getFileSystem().exists(DOWNLOAD_FILE));
        assertTrue(new File(targetPath + DOWNLOAD_FILE).exists());
    }

    @Test
    public void testStoreFile() throws Exception {
        assertFalse(fakeFtpServer.getFileSystem().exists("/" + UPLOAD_FILE));
        Path uploadFile = Paths.get(targetPath, UPLOAD_FILE);
        Files.write(uploadFile, "Upload content\n".getBytes());
        FtpMessage ftpMessage = ftpClient.storeFile(putCommand(Paths.get(targetPath, UPLOAD_FILE).toString(), "/" + UPLOAD_FILE), context);
        verifyMessage(ftpMessage, PutCommandResult.class, CLOSING_DATA_CONNECTION, "226 Created file /upload_file.");
        assertTrue(fakeFtpServer.getFileSystem().exists("/" + UPLOAD_FILE));
        fakeFtpServer.getFileSystem().delete("/" + UPLOAD_FILE);
    }

    @Test
    public void testStoreFileImplicitFilename() throws Exception {
        assertFalse(fakeFtpServer.getFileSystem().exists("/" + UPLOAD_FILE));
        Path uploadFile = Paths.get(targetPath, UPLOAD_FILE);
        Files.write(uploadFile, "Upload content\n".getBytes());
        FtpMessage ftpMessage = ftpClient.storeFile(putCommand(Paths.get(targetPath, UPLOAD_FILE).toString(), "/"), context);
        verifyMessage(ftpMessage, PutCommandResult.class, CLOSING_DATA_CONNECTION, "226 Created file /upload_file.");
        assertTrue(fakeFtpServer.getFileSystem().exists("/" + UPLOAD_FILE));
        fakeFtpServer.getFileSystem().delete("/" + UPLOAD_FILE);
    }

    @Test
    public void testDeleteCurrentDirectory() {
        assertTrue(fakeFtpServer.getFileSystem().exists(COMPLETELY_DELETE_FOLDER));
        DeleteCommand deleteCommand = deleteCommand(COMPLETELY_DELETE_FOLDER);
        deleteCommand.setIncludeCurrent(true);
        FtpMessage ftpMessage = ftpClient.deleteFile(deleteCommand, context);
        verifyMessage(ftpMessage, DeleteCommandResult.class, FILE_ACTION_OK, "250 \"/completely_delete\" removed.");
        assertFalse(fakeFtpServer.getFileSystem().exists(COMPLETELY_DELETE_FOLDER));
    }

    @Test
    public void testDeleteDirectory() {
        assertTrue(fakeFtpServer.getFileSystem().exists(DELETE_FOLDER));
        FtpMessage ftpMessage = ftpClient.deleteFile(deleteCommand(DELETE_FOLDER), context);
        verifyMessage(ftpMessage, DeleteCommandResult.class, FILE_ACTION_OK, "250 \"/delete/second_folder\" removed.");
        assertTrue(fakeFtpServer.getFileSystem().exists(DELETE_FOLDER));
        assertTrue(fakeFtpServer.getFileSystem().listFiles(DELETE_FOLDER).size() == 0);
    }

    @Test
    public void testDeleteAllFilesInEmptyDirectory() {
        assertTrue(fakeFtpServer.getFileSystem().exists(EMPTY_FOLDER));
        FtpMessage ftpMessage = ftpClient.deleteFile(deleteCommand(EMPTY_FOLDER), context);
        verifyMessage(ftpMessage, DeleteCommandResult.class, FILE_ACTION_OK, "250 No files to delete.");
        assertTrue(fakeFtpServer.getFileSystem().exists(EMPTY_FOLDER));
        assertTrue(fakeFtpServer.getFileSystem().listFiles(EMPTY_FOLDER).size() == 0);
    }

    @Test
    public void testDeleteFile() {
        assertTrue(fakeFtpServer.getFileSystem().exists(SINGLE_FILE));
        FtpMessage ftpMessage = ftpClient.deleteFile(deleteCommand(SINGLE_FILE), context);
        verifyMessage(ftpMessage, DeleteCommandResult.class, FILE_ACTION_OK, "250 \"/single_file\" deleted.");
        assertFalse(fakeFtpServer.getFileSystem().exists(SINGLE_FILE));
    }

    @Test(expectedExceptions = {CitrusRuntimeException.class}, expectedExceptionsMessageRegExp = ".*/path/not/valid.*")
    public void testDeleteInvalidPath() {
        String invalidPath = "/path/not/valid";
        assertFalse(fakeFtpServer.getFileSystem().exists(invalidPath));
        ftpClient.deleteFile(deleteCommand(invalidPath), context);
    }

    @Test
    public void testLoginLogout() throws Exception {
        FtpClient ftpClient = new FtpClient();
        ftpClient.setFtpClient(apacheFtpClient);

        reset(apacheFtpClient);

        when(apacheFtpClient.isConnected())
                .thenReturn(false)
                .thenReturn(true);

        when(apacheFtpClient.getReplyString()).thenReturn("OK");
        when(apacheFtpClient.getReplyCode()).thenReturn(200);
        when(apacheFtpClient.logout()).thenReturn(true);

        ftpClient.initialize();
        ftpClient.connectAndLogin();
        ftpClient.destroy();

        verify(apacheFtpClient).configure(any(FTPClientConfig.class));
        verify(apacheFtpClient).addProtocolCommandListener(any(ProtocolCommandListener.class));
        verify(apacheFtpClient).connect("localhost", 22222);
        verify(apacheFtpClient).disconnect();

    }

    @Test
    public void testCommand() throws Exception {
        FtpClient ftpClient = new FtpClient();
        ftpClient.setFtpClient(apacheFtpClient);

        reset(apacheFtpClient);

        when(apacheFtpClient.isConnected()).thenReturn(false);
        when(apacheFtpClient.getReplyString()).thenReturn("OK");
        when(apacheFtpClient.getReplyCode()).thenReturn(200);

        when(apacheFtpClient.sendCommand(FTPCmd.PWD.getCommand(), null)).thenReturn(200);

        ftpClient.send(FtpMessage.command(FTPCmd.PWD), context);

        Message reply = ftpClient.receive(context);

        Assert.assertTrue(reply instanceof FtpMessage);

        FtpMessage ftpReply = (FtpMessage) reply;

        Assert.assertNull(ftpReply.getSignal());
        Assert.assertNull(ftpReply.getArguments());
        Assert.assertEquals(ftpReply.getReplyCode(), new Integer(200));
        Assert.assertEquals(ftpReply.getReplyString(), "OK");

        verify(apacheFtpClient).connect("localhost", 22222);
    }

    @Test
    public void testCommandWithArguments() throws Exception {
        FtpEndpointConfiguration endpointConfiguration = new FtpEndpointConfiguration();
        FtpClient ftpClient = new FtpClient(endpointConfiguration);
        ftpClient.setFtpClient(apacheFtpClient);

        endpointConfiguration.setUser("admin");
        endpointConfiguration.setPassword("consol");

        reset(apacheFtpClient);

        when(apacheFtpClient.isConnected())
                .thenReturn(false)
                .thenReturn(true);

        when(apacheFtpClient.login("admin", "consol")).thenReturn(true);
        when(apacheFtpClient.getReplyString()).thenReturn("OK");
        when(apacheFtpClient.getReplyCode()).thenReturn(200);
        when(apacheFtpClient.sendCommand(FTPCmd.PWD.getCommand(), null)).thenReturn(200);
        when(apacheFtpClient.sendCommand(FTPCmd.MKD.getCommand(), "testDir")).thenReturn(201);

        ftpClient.send(FtpMessage.command(FTPCmd.PWD), context);

        Message reply = ftpClient.receive(context);

        Assert.assertTrue(reply instanceof FtpMessage);

        FtpMessage ftpReply = (FtpMessage) reply;

        Assert.assertNull(ftpReply.getSignal());
        Assert.assertNull(ftpReply.getArguments());
        Assert.assertEquals(ftpReply.getReplyCode(), new Integer(200));
        Assert.assertEquals(ftpReply.getReplyString(), "OK");

        ftpClient.send(FtpMessage.command(FTPCmd.MKD).arguments("testDir"), context);

        reply = ftpClient.receive(context);

        Assert.assertTrue(reply instanceof FtpMessage);

        ftpReply = (FtpMessage) reply;

        Assert.assertNull(ftpReply.getSignal());
        Assert.assertNull(ftpReply.getArguments());
        Assert.assertEquals(ftpReply.getReplyCode(), new Integer(201));
        Assert.assertEquals(ftpReply.getReplyString(), "OK");

        verify(apacheFtpClient).connect("localhost", 22222);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testCommandWithUserLoginFailed() throws Exception {
        FtpEndpointConfiguration endpointConfiguration = new FtpEndpointConfiguration();
        FtpClient ftpClient = new FtpClient(endpointConfiguration);
        ftpClient.setFtpClient(apacheFtpClient);

        endpointConfiguration.setUser("admin");
        endpointConfiguration.setPassword("consol");

        reset(apacheFtpClient);

        when(apacheFtpClient.isConnected()).thenReturn(false);
        when(apacheFtpClient.getReplyString()).thenReturn("OK");
        when(apacheFtpClient.getReplyCode()).thenReturn(200);

        when(apacheFtpClient.login("admin", "consol")).thenReturn(false);

        ftpClient.send(FtpMessage.command(FTPCmd.PWD), context);

        verify(apacheFtpClient).connect("localhost", 22222);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testCommandNegativeReply() throws Exception {
        FtpEndpointConfiguration endpointConfiguration = new FtpEndpointConfiguration();
        endpointConfiguration.setErrorHandlingStrategy(ErrorHandlingStrategy.THROWS_EXCEPTION);

        FtpClient ftpClient = new FtpClient(endpointConfiguration);
        ftpClient.setFtpClient(apacheFtpClient);

        reset(apacheFtpClient);

        when(apacheFtpClient.isConnected()).thenReturn(false);
        when(apacheFtpClient.getReplyString()).thenReturn("OK");
        when(apacheFtpClient.getReplyCode()).thenReturn(200);

        when(apacheFtpClient.sendCommand(FTPCmd.PWD, null)).thenReturn(500);

        ftpClient.send(FtpMessage.command(FTPCmd.PWD), context);

        verify(apacheFtpClient).connect("localhost", 22222);
    }
}
