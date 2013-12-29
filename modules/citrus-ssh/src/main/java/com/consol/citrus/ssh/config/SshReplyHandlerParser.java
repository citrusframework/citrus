package com.consol.citrus.ssh.config;

import com.consol.citrus.ssh.message.SshReplyMessageReceiver;

/**
 * Parser for the reply handler of an SSH request
 * @author Roland Huss
 * @since 1.3
 * @deprecated
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
    protected Class<?> getBeanClass() {
        return SshReplyMessageReceiver.class;
    }
}
