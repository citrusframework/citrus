package com.consol.citrus.config.xml;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class CitrusConfigNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("jms-message-sender", new JmsMessageSenderParser());
        registerBeanDefinitionParser("jms-message-receiver", new JmsMessageReceiverParser());
        registerBeanDefinitionParser("jms-sync-message-sender", new JmsSyncMessageSenderParser());
        registerBeanDefinitionParser("jms-sync-message-receiver", new JmsSyncMessageReceiverParser());
        registerBeanDefinitionParser("jms-reply-message-handler", new ReplyMessageReceiverParser());
        registerBeanDefinitionParser("jms-reply-message-sender", new JmsReplyMessageSenderParser());
    }

}
