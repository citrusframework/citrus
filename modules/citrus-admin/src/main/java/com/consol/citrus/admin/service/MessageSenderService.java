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

import com.consol.citrus.admin.converter.HttpMessageSenderConverter;
import com.consol.citrus.admin.converter.JmsMessageSenderConverter;
import com.consol.citrus.admin.converter.MessageChannelSenderConverter;
import com.consol.citrus.admin.converter.WsMessageSenderConverter;
import com.consol.citrus.admin.model.MessageSenderType;
import com.consol.citrus.model.config.core.JmsMessageSender;
import com.consol.citrus.model.config.core.MessageChannelSender;
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
public class MessageSenderService {

    @Autowired
    private SpringBeanService springBeanService;

    /** Object converters */
    private JmsMessageSenderConverter jmsMessageSenderConverter = new JmsMessageSenderConverter();
    private MessageChannelSenderConverter messageChannelSenderConverter = new MessageChannelSenderConverter();
    private HttpMessageSenderConverter httpMessageSenderConverter = new HttpMessageSenderConverter();
    private WsMessageSenderConverter wsMessageSenderConverter = new WsMessageSenderConverter();

    /**
     * Gets the message sender definition by bean id in application context.
     * @param projectConfigFile
     * @param id
     * @return
     */
    public MessageSenderType getMessageSender(File projectConfigFile, String id) {
        return jmsMessageSenderConverter.convert(springBeanService.getBeanDefinition(projectConfigFile, id, JmsMessageSender.class));
    }

    /**
     * List all message sender types in application context.
     * @param projectConfigFile
     * @return
     */
    public List<MessageSenderType> listMessageSender(File projectConfigFile) {
        List<MessageSenderType> messageSender = new ArrayList<MessageSenderType>();

        List<JmsMessageSender> jmsMessageSender = springBeanService.getBeanDefinitions(projectConfigFile, JmsMessageSender.class);
        for (JmsMessageSender sender : jmsMessageSender) {
            messageSender.add(jmsMessageSenderConverter.convert(sender));
        }

        List<MessageChannelSender> channelMessageSender = springBeanService.getBeanDefinitions(projectConfigFile, MessageChannelSender.class);
        for (MessageChannelSender sender : channelMessageSender) {
            messageSender.add(messageChannelSenderConverter.convert(sender));
        }

        List<com.consol.citrus.model.config.http.MessageSender> httpMessageSender = springBeanService.getBeanDefinitions(projectConfigFile, com.consol.citrus.model.config.http.MessageSender.class);
        for (com.consol.citrus.model.config.http.MessageSender sender : httpMessageSender) {
            messageSender.add(httpMessageSenderConverter.convert(sender));
        }

        List<com.consol.citrus.model.config.ws.MessageSender> wsMessageSender = springBeanService.getBeanDefinitions(projectConfigFile, com.consol.citrus.model.config.ws.MessageSender.class);
        for (com.consol.citrus.model.config.ws.MessageSender sender : wsMessageSender) {
            messageSender.add(wsMessageSenderConverter.convert(sender));
        }

        return messageSender;
    }
}
