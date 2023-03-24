package org.citrusframework.ftp.client;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.ftp.message.FtpMessage;
import org.citrusframework.ftp.model.*;
import org.citrusframework.testng.AbstractTestNGUnitTest;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Georgi Todorov
 * @since 2.7.5
 */
public abstract class AbstractFtpClientTest extends AbstractTestNGUnitTest {

    protected GetCommand getCommand(String remoteFilePath) {
        return getCommand(remoteFilePath, remoteFilePath);
    }

    protected ListCommand listCommand(String remoteFilePath) {
        ListCommand command = new ListCommand();
        ListCommand.Target target = new ListCommand.Target();
        target.setPath(remoteFilePath);
        command.setTarget(target);

        return command;
    }

    protected GetCommand getCommand(String remoteFilePath, String localFilePath) {
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

    protected PutCommand putCommand(String localFilePath, String remoteFilePath) {
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

    protected DeleteCommand deleteCommand(String targetPath) {
        DeleteCommand command = new DeleteCommand();
        DeleteCommand.Target target = new DeleteCommand.Target();
        target.setPath(targetPath);
        command.setTarget(target);

        command.setRecursive(true);

        return command;
    }

    protected void verifyMessage(FtpMessage message, Class expectedCommandResultType, Integer expectedReplyCode, String expectedReplyMessage) {
        assertEquals(message.getReplyCode(), expectedReplyCode);
        String actualReplyMessage = message.getReplyString();
        assertTrue(actualReplyMessage.contains(expectedReplyMessage),
                String.format("Expected reply message '%s' is not part of the actual reply message '%s'!",
                        expectedReplyMessage, actualReplyMessage));
        Object payload = message.getPayload(expectedCommandResultType);
        assertNotNull(payload);
        assertTrue(expectedCommandResultType.isAssignableFrom(payload.getClass()),
                String.format("The expected command result type '%s' is not assignable from the actual one '%s'!",
                        expectedCommandResultType, payload.getClass()));
    }

    protected void verifyMessage(FtpMessage message, Class expectedCommandResultType, Integer expectedReplyCode, String expectedReplyMessage, List<String> fileNames) {
        verifyMessage(message, expectedCommandResultType, expectedReplyCode, expectedReplyMessage);
        ListCommandResult listCommandResult = message.getPayload(ListCommandResult.class);
        List<ListCommandResult.Files.File> files = listCommandResult.getFiles().getFiles();
        fileNames.stream()
                .forEach(fileName -> verifyFile(files, fileName));
        assertEquals(files.size(), fileNames.size());
    }

    private void verifyFile(List<ListCommandResult.Files.File> files, String fileName) {
        files.stream()
                .filter(f -> fileName.equals(f.getPath()))
                .findAny()
                .orElseThrow(() ->
                        new CitrusRuntimeException(String.format("File '%s' could not be found in the specified list of files.", fileName))
                );
    }

}
