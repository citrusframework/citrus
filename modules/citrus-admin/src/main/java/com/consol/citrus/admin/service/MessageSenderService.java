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

import com.consol.citrus.admin.converter.JmsMessageSenderConverter;
import com.consol.citrus.admin.model.MessageSenderType;
import com.consol.citrus.model.config.core.JmsMessageSender;
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

    private JmsMessageSenderConverter jmsMessageSenderConverter = new JmsMessageSenderConverter();

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
        List<JmsMessageSender> jsmMessageSender = springBeanService.getBeanDefinitions(projectConfigFile, JmsMessageSender.class);

        for (JmsMessageSender sender : jsmMessageSender) {
            messageSender.add(jmsMessageSenderConverter.convert(sender));
        }

        return messageSender;
    }
}
