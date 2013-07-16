/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.admin.service;

import com.consol.citrus.admin.converter.JmsMessageReceiverConverter;
import com.consol.citrus.admin.converter.MessageChannelReceiverConverter;
import com.consol.citrus.admin.model.MessageReceiverItem;
import com.consol.citrus.model.config.core.JmsMessageReceiver;
import com.consol.citrus.model.config.core.MessageChannelReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 1.3.1
 */
@Component
public class MessageReceiverService {

    @Autowired
    private SpringBeanService springBeanService;

    private JmsMessageReceiverConverter jmsMessageReceiverConverter = new JmsMessageReceiverConverter();
    private MessageChannelReceiverConverter messageChannelReceiverConverter = new MessageChannelReceiverConverter();

    /**
     * Gets the message receiver definition by bean id in application context.
     * @param projectConfigFile
     * @param id
     * @return
     */
    public MessageReceiverItem getMessageReceiver(File projectConfigFile, String id) {
        return jmsMessageReceiverConverter.convert(springBeanService.getBeanDefinition(projectConfigFile, id, JmsMessageReceiver.class));
    }

    /**
     * List all message receiver types in application context.
     * @param projectConfigFile
     * @return
     */
    public List<MessageReceiverItem> listMessageReceiver(File projectConfigFile) {
        List<MessageReceiverItem> messageReceiver = new ArrayList<MessageReceiverItem>();

        List<JmsMessageReceiver> jmsMessageReceiver = springBeanService.getBeanDefinitions(projectConfigFile, JmsMessageReceiver.class);
        for (JmsMessageReceiver receiver : jmsMessageReceiver) {
            messageReceiver.add(jmsMessageReceiverConverter.convert(receiver));
        }

        List<MessageChannelReceiver> channelMessageReceiver = springBeanService.getBeanDefinitions(projectConfigFile, MessageChannelReceiver.class);
        for (MessageChannelReceiver receiver : channelMessageReceiver) {
            messageReceiver.add(messageChannelReceiverConverter.convert(receiver));
        }

        return messageReceiver;
    }

}
