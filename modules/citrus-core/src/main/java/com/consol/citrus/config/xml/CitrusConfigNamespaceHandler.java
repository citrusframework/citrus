/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.config.xml;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class CitrusConfigNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("jms-message-sender", new JmsMessageSenderParser());
        registerBeanDefinitionParser("jms-message-receiver", new JmsMessageReceiverParser());
        registerBeanDefinitionParser("jms-sync-message-sender", new JmsSyncMessageSenderParser());
        registerBeanDefinitionParser("jms-sync-message-receiver", new JmsSyncMessageReceiverParser());
        registerBeanDefinitionParser("jms-reply-message-handler", new JmsReplyMessageReceiverParser());
        registerBeanDefinitionParser("jms-reply-message-sender", new JmsReplyMessageSenderParser());
        registerBeanDefinitionParser("message-channel-sender", new MessageChannelSenderParser());
        registerBeanDefinitionParser("message-channel-receiver", new MessageChannelReceiverParser());
    }

}
