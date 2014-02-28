package com.consol.citrus.ssh.config;

import com.consol.citrus.config.xml.ReplyMessageReceiverParser;
import com.consol.citrus.ssh.message.SshReplyMessageReceiver;

/**
 * Parser for the reply handler of an SSH request
 * @author Roland Huss
 * @since 1.3
 * @deprecated
 */
public class SshReplyHandlerParser extends ReplyMessageReceiverParser<SshReplyMessageReceiver> {

    public SshReplyHandlerParser() {
        super(SshReplyMessageReceiver.class);
    }
}
