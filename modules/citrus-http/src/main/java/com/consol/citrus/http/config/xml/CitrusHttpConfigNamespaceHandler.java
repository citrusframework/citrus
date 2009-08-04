package com.consol.citrus.http.config.xml;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class CitrusHttpConfigNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("server", new HttpServerParser());
        registerBeanDefinitionParser("message-sender", new HttpMessageSenderParser());
        registerBeanDefinitionParser("reply-message-handler", new HttpReplyMessageReceiverParser());
    }

}
