package com.consol.citrus.ssh.config;

import com.consol.citrus.message.ReplyMessageHandler;
import com.consol.citrus.ssh.SshReplyHandler;

/**
 * Parser for the reply handler of an SSH request
 * @author roland
 * @since 11.09.12
 */
public class SshReplyHandlerParser extends AbstractSshParser {
    @Override
    protected String[] getAttributePropertyMapping() {
        return new String[0];
    }

    @Override
    protected String[] getAttributePropertyReferenceMapping() {
        return new String[] {
                "actor","actor"
        };
    }

    @Override
    protected Class getBeanClass() {
        return ReplyMessageHandler.class;
    }
}
