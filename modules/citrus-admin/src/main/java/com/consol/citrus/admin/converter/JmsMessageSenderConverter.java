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

package com.consol.citrus.admin.converter;

import com.consol.citrus.admin.model.MessageSenderItem;
import com.consol.citrus.model.config.core.JmsMessageSender;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class JmsMessageSenderConverter implements MessageSenderConverter<JmsMessageSender> {

    @Override
    public MessageSenderItem convert(JmsMessageSender jmsMessageSender) {
        MessageSenderItem messageSenderType = new MessageSenderItem();

        messageSenderType.setName(jmsMessageSender.getId());

        if (StringUtils.hasText(jmsMessageSender.getDestinationName())) {
            messageSenderType.setDestination(jmsMessageSender.getDestinationName());
        } else {
            messageSenderType.setDestination("ref:" + jmsMessageSender.getDestination());
        }

        messageSenderType.setType("JMS");

        return messageSenderType;
    }
}
