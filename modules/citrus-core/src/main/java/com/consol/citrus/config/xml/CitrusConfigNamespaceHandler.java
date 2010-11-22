/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.config.xml;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Namespace handler for components in Citrus configuration.
 * 
 * @author Christoph Deppisch
 */
public class CitrusConfigNamespaceHandler extends NamespaceHandlerSupport {

    /**
     * @see org.springframework.beans.factory.xml.NamespaceHandler#init()
     */
    public void init() {
        registerBeanDefinitionParser("jms-message-sender", new JmsMessageSenderParser());
        registerBeanDefinitionParser("jms-message-receiver", new JmsMessageReceiverParser());
        registerBeanDefinitionParser("jms-sync-message-sender", new JmsSyncMessageSenderParser());
        registerBeanDefinitionParser("jms-sync-message-receiver", new JmsSyncMessageReceiverParser());
        registerBeanDefinitionParser("jms-reply-message-handler", new JmsReplyMessageReceiverParser());
        registerBeanDefinitionParser("jms-reply-message-sender", new JmsReplyMessageSenderParser());
        registerBeanDefinitionParser("message-channel-sender", new MessageChannelSenderParser());
        registerBeanDefinitionParser("message-channel-receiver", new MessageChannelReceiverParser());
        registerBeanDefinitionParser("sync-message-channel-sender", new SyncMessageChannelSenderParser());
        registerBeanDefinitionParser("sync-message-channel-receiver", new SyncMessageChannelReceiverParser());
        registerBeanDefinitionParser("message-channel-reply-handler", new ReplyMessageChannelReceiverParser());
        registerBeanDefinitionParser("message-channel-reply-sender", new ReplyMessageChannelSenderParser());
    }

}
