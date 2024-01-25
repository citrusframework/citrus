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

package org.citrusframework.ftp.message;

import java.util.List;
import java.util.Optional;

import org.apache.commons.net.ftp.FTPCmd;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.ftpserver.ftplet.DataType;
import org.citrusframework.ftp.model.Command;
import org.citrusframework.ftp.model.CommandResult;
import org.citrusframework.ftp.model.CommandResultType;
import org.citrusframework.ftp.model.CommandType;
import org.citrusframework.ftp.model.ConnectCommand;
import org.citrusframework.ftp.model.DeleteCommand;
import org.citrusframework.ftp.model.DeleteCommandResult;
import org.citrusframework.ftp.model.GetCommand;
import org.citrusframework.ftp.model.GetCommandResult;
import org.citrusframework.ftp.model.ListCommand;
import org.citrusframework.ftp.model.ListCommandResult;
import org.citrusframework.ftp.model.PutCommand;
import org.citrusframework.ftp.model.PutCommandResult;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.citrusframework.xml.StringResult;
import org.citrusframework.xml.StringSource;

/**
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public class FtpMessage extends DefaultMessage {

    private static final String OPEN_COMMAND = "OPEN";

    private CommandType command;
    private CommandResultType commandResult;

    private FtpMarshaller marshaller = new FtpMarshaller();

    /**
     * Constructs copy of given message.
     * @param message
     */
    public FtpMessage(Message message) {
        super(message);
    }

    /**
     * Default constructor using command as payload.
     * @param command
     */
    private FtpMessage(CommandType command) {
        super(command);
        this.command = command;
        setCommandHeader(command);
        setHeader(FtpMessageHeaders.FTP_ARGS, command.getArguments());
    }

    /**
     * Default constructor using command result as payload.
     * @param commandResult
     */
    private FtpMessage(CommandResultType commandResult) {
        super(commandResult);
        this.commandResult = commandResult;
    }

    /**
     * Sets the ftp command.
     * @param command
     * @return
     */
    public static FtpMessage command(FTPCmd command) {
        Command cmd = new Command();
        cmd.setSignal(command.getCommand());
        return new FtpMessage(cmd);
    }

    /**
     * Creates new connect command message.
     * @param sessionId
     * @return
     */
    public static FtpMessage connect(String sessionId) {
        ConnectCommand cmd = new ConnectCommand();
        cmd.setSignal("OPEN");
        cmd.setSessionId(sessionId);
        return new FtpMessage(cmd);
    }

    /**
     * Creates new put command message.
     * @param localPath
     * @return
     */
    public static FtpMessage put(String localPath) {
        return put(localPath, DataType.ASCII);
    }

    /**
     * Creates new put command message.
     * @param localPath
     * @param type
     * @return
     */
    public static FtpMessage put(String localPath, DataType type) {
        return put(localPath, FileUtils.getFileName(FileUtils.getFileResource(localPath).getLocation()), type);
    }

    /**
     * Creates new put command message.
     * @param localPath
     * @param remotePath
     * @param type
     * @return
     */
    public static FtpMessage put(String localPath, String remotePath, DataType type) {
        PutCommand cmd = new PutCommand();
        cmd.setSignal(FTPCmd.STOR.getCommand());

        PutCommand.File file = new PutCommand.File();
        file.setPath(localPath);
        file.setType(type.name());
        cmd.setFile(file);

        PutCommand.Target target = new PutCommand.Target();
        target.setPath(remotePath);
        cmd.setTarget(target);
        return new FtpMessage(cmd);
    }

    /**
     * Creates new get command message.
     * @param remotePath
     * @return
     */
    public static FtpMessage get(String remotePath) {
        return get(remotePath, DataType.ASCII);
    }

    /**
     * Creates new get command message.
     * @param remotePath
     * @param type
     * @return
     */
    public static FtpMessage get(String remotePath, DataType type) {
        return get(remotePath, FileUtils.getFileName(FileUtils.getFileResource(remotePath).getLocation()), type);
    }

    /**
     * Creates new get command message.
     * @param remotePath
     * @param localPath
     * @param type
     * @return
     */
    public static FtpMessage get(String remotePath, String localPath, DataType type) {
        GetCommand cmd = new GetCommand();
        cmd.setSignal(FTPCmd.RETR.getCommand());

        GetCommand.File file = new GetCommand.File();
        file.setPath(remotePath);
        file.setType(type.name());
        cmd.setFile(file);

        GetCommand.Target target = new GetCommand.Target();
        target.setPath(localPath);
        cmd.setTarget(target);
        return new FtpMessage(cmd);
    }

    /**
     * Creates new delete command message.
     * @param remotePath
     * @return
     */
    public static FtpMessage delete(String remotePath) {
        DeleteCommand cmd = new DeleteCommand();
        cmd.setSignal(FTPCmd.DELE.getCommand());

        DeleteCommand.Target target = new DeleteCommand.Target();
        target.setPath(remotePath);
        cmd.setTarget(target);
        return new FtpMessage(cmd);
    }

    /**
     * Creates new delete command message.
     * @param remotePath
     * @return
     */
    public static FtpMessage list(String remotePath) {
        ListCommand cmd = new ListCommand();
        cmd.setSignal(FTPCmd.LIST.getCommand());

        ListCommand.Target target = new ListCommand.Target();
        target.setPath(remotePath);
        cmd.setTarget(target);
        return new FtpMessage(cmd);
    }

    public static FtpMessage success() {
        CommandResult commandResult = new CommandResult();
        commandResult.setSuccess(true);
        return result(commandResult);
    }

    public static FtpMessage success(int replyCode) {
        return success(replyCode, "@ignore@");
    }

    public static FtpMessage success(int replyCode, String replyString) {
        return result(replyCode, replyString, true);
    }

    public static FtpMessage error() {
        CommandResult commandResult = new CommandResult();
        commandResult.setSuccess(false);
        return result(commandResult);
    }

    public static FtpMessage error(int replyCode) {
        return success(replyCode, "@ignore@");
    }

    public static FtpMessage error(int replyCode, String replyString) {
        return result(replyCode, replyString, false);
    }

    public static FtpMessage result(int replyCode, String replyString, boolean success) {
        CommandResult commandResult = new CommandResult();
        commandResult.setReplyCode(String.valueOf(replyCode));
        commandResult.setReplyString(replyString);
        commandResult.setSuccess(success);
        return result(commandResult);
    }

    public static FtpMessage deleteResult(int replyCode, String replyString, boolean success) {
        DeleteCommandResult result = new DeleteCommandResult();
        result.setReplyCode(String.valueOf(replyCode));
        result.setReplyString(replyString);
        result.setSuccess(success);
        return result(result);
    }

    public static FtpMessage putResult(int replyCode, String replyString, boolean success) {
        PutCommandResult result = new PutCommandResult();
        result.setReplyCode(String.valueOf(replyCode));
        result.setReplyString(replyString);
        result.setSuccess(success);
        return result(result);
    }

    public static FtpMessage result(CommandResultType commandResult) {
        FtpMessage ftpMessage = new FtpMessage(commandResult);
        ftpMessage.setHeader(FtpMessageHeaders.FTP_REPLY_CODE, commandResult.getReplyCode());
        ftpMessage.setHeader(FtpMessageHeaders.FTP_REPLY_STRING, commandResult.getReplyString());
        return ftpMessage;
    }

    public static FtpMessage result(int replyCode, String replyString, List<String> fileNames) {
        ListCommandResult listCommandResult = new ListCommandResult();
        listCommandResult.setReplyCode(String.valueOf(replyCode));
        listCommandResult.setReplyString(replyString);
        listCommandResult.setSuccess(true);
        ListCommandResult.Files files = new ListCommandResult.Files();

        for (String fileName : fileNames) {
            ListCommandResult.Files.File file = new ListCommandResult.Files.File();
            file.setPath(fileName);
            files.getFiles().add(file);
        }

        listCommandResult.setFiles(files);

        return result(listCommandResult);
    }

    public static FtpMessage result(int replyCode, String replyString, String path, String content) {
        GetCommandResult getCommandResult = new GetCommandResult();
        getCommandResult.setReplyCode(String.valueOf(replyCode));
        getCommandResult.setReplyString(replyString);
        getCommandResult.setSuccess(true);

        GetCommandResult.File file = new GetCommandResult.File();
        file.setPath(path);
        file.setData(content);

        getCommandResult.setFile(file);

        return result(getCommandResult);
    }

    /**
     * Sets the command args.
     * @param arguments
     */
    public FtpMessage arguments(String arguments) {
        if (command != null) {
            command.setArguments(arguments);
        }

        setHeader(FtpMessageHeaders.FTP_ARGS, arguments);
        return this;
    }

    /**
     * Gets the ftp command signal.
     */
    public String getSignal() {
        return Optional.ofNullable(getHeader(FtpMessageHeaders.FTP_COMMAND)).map(Object::toString).orElse(null);
    }

    /**
     * Gets the command args.
     */
    public String getArguments() {
        return Optional.ofNullable(getHeader(FtpMessageHeaders.FTP_ARGS)).map(Object::toString).orElse(null);
    }

    /**
     * Gets the reply code.
     */
    public Integer getReplyCode() {
        Object replyCode = getHeader(FtpMessageHeaders.FTP_REPLY_CODE);

        if (replyCode != null) {
            if (replyCode instanceof Integer) {
                return (Integer) replyCode;
            } else {
                return Integer.valueOf(replyCode.toString());
            }
        } else if (commandResult != null) {
            return Optional.ofNullable(commandResult.getReplyCode()).map(Integer::valueOf).orElse(FTPReply.COMMAND_OK);
        }

        return null;
    }

    /**
     * Check if reply code is set on this message.
     * @return
     */
    public boolean hasReplyCode() {
        return getHeader(FtpMessageHeaders.FTP_REPLY_CODE) != null ||
                Optional.ofNullable(commandResult)
                        .map(result -> StringUtils.hasText(result.getReplyCode()))
                        .orElse(false);
    }

    /**
     * Check if reply code is set on this message.
     * @return
     */
    public boolean hasException() {
        return Optional.ofNullable(commandResult)
                        .map(result -> StringUtils.hasText(result.getException()))
                        .orElse(false);
    }

    /**
     * Gets the reply string.
     */
    public String getReplyString() {
        Object replyString = getHeader(FtpMessageHeaders.FTP_REPLY_STRING);

        if (replyString != null) {
            return replyString.toString();
        }

        return null;
    }

    @Override
    public <T> T getPayload(Class<T> type) {
        return getPayload(type, marshaller);
    }

    public <T> T getPayload(Class<T> type, FtpMarshaller marshaller) {
        if (CommandType.class.isAssignableFrom(type)) {
            return (T) getCommand(marshaller);
        } else if (CommandResultType.class.isAssignableFrom(type)) {
            return (T) getCommandResult(marshaller);
        } else if (String.class.equals(type)) {
            return (T) getPayload(marshaller);
        } else {
            return super.getPayload(type);
        }
    }

    @Override
    public Object getPayload() {
        return getPayload(marshaller);
    }

    public Object getPayload(FtpMarshaller marshaller) {
        StringResult payloadResult = new StringResult();
        if (command != null) {
            marshaller.marshal(command, payloadResult);
            return payloadResult.toString();
        } else if (commandResult != null) {
            marshaller.marshal(commandResult, payloadResult);
            return payloadResult.toString();
        }

        return super.getPayload();
    }

    @Override
    public DefaultMessage setPayload(Object payload) {
        if (payload instanceof CommandType commandType) {
            this.command = commandType;
        } else if (payload instanceof CommandResultType commandResultType) {
            this.commandResult = commandResultType;
        }
        return super.setPayload(payload);
    }

    /**
     * Gets the command result if any or tries to unmarshal String payload representation to an command result model.
     * @return
     */
    private <T extends CommandResultType> T getCommandResult(FtpMarshaller marshaller) {
        if (commandResult == null) {
            Object payload = getPayload(marshaller);

            if (payload instanceof CommandResultType commandResultType) {
                this.commandResult = commandResultType;
            } else if (payload instanceof String payloadString) {
                this.commandResult = (T) marshaller.unmarshal(new StringSource(payloadString));
            }
        }

        return (T) commandResult;
    }

    /**
     * Gets the command if any or tries to unmarshal String payload representation to an command model.
     * @return
     */
    private <T extends CommandType> T getCommand(FtpMarshaller marshaller) {
        if (command == null) {
            Object payload = getPayload(marshaller);

            if (payload instanceof CommandType commandType) {
                this.command = commandType;
            } else if (payload instanceof String payloadString) {
                this.command = (T) marshaller.unmarshal(new StringSource(payloadString));
            }
        }

        return (T) command;
    }

    /**
     * Gets command header as ftp signal from command object.
     * @param command
     */
    private void setCommandHeader(CommandType command) {
        String header;
        if (command instanceof ConnectCommand) {
            header = FtpMessage.OPEN_COMMAND;
        } else if (command instanceof GetCommand) {
            header = FTPCmd.RETR.getCommand();
        } else if (command instanceof PutCommand) {
            header = FTPCmd.STOR.getCommand();
        } else if (command instanceof ListCommand) {
            header = FTPCmd.LIST.getCommand();
        } else if (command instanceof DeleteCommand) {
            header = FTPCmd.DELE.getCommand();
        } else {
            header = command.getSignal();
        }

        setHeader(FtpMessageHeaders.FTP_COMMAND, header);
    }

    public FtpMessage marshaller(FtpMarshaller marshaller) {
        this.marshaller = marshaller;
        return this;
    }

}
