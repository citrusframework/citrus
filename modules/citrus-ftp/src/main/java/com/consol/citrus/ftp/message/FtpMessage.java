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

package com.consol.citrus.ftp.message;

import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import org.apache.commons.net.ftp.FTPCmd;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class FtpMessage extends DefaultMessage {

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
    public FtpMessage(FTPCmd command, String arguments) {
        super(command.getCommand());

        setHeader(FtpMessageHeaders.FTP_COMMAND, command.getCommand());
        setHeader(FtpMessageHeaders.FTP_ARGS, arguments);
    }

    /**
     * Sets the ftp command.
     * @param command
     * @return
     */
    public FtpMessage command(FTPCmd command) {
        setHeader(FtpMessageHeaders.FTP_COMMAND, command);
        return this;
    }

    /**
     * Sets the reply string.
     * @param replyString
     */
    public FtpMessage replyString(String replyString) {
        setHeader(FtpMessageHeaders.FTP_REPLY_STRING, replyString);
        return this;
    }

    /**
     * Sets the command args.
     * @param arguments
     */
    public FtpMessage arguments(String arguments) {
        setHeader(FtpMessageHeaders.FTP_ARGS, arguments);
        return this;
    }

    /**
     * Sets the reply code.
     * @param replyCode
     */
    public FtpMessage replyCode(Integer replyCode) {
        setHeader(FtpMessageHeaders.FTP_REPLY_CODE, replyCode);
        return this;
    }

    /**
     * Gets the ftp command
     */
    public FTPCmd getCommand() {
        Object command = getHeader(FtpMessageHeaders.FTP_COMMAND);

        if (command != null) {
            return FTPCmd.valueOf(command.toString());
        }

        return null;
    }

    /**
     * Gets the command args.
     */
    public String getArguments() {
        Object args = getHeader(FtpMessageHeaders.FTP_ARGS);

        if (args != null) {
            return args.toString();
        }

        return null;
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
        }

        return null;
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
}
