/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.channel;

import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.MessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class ChannelMessageConverter implements MessageConverter<Message, ChannelEndpointConfiguration> {

    @Override
    public Message convertOutbound(com.consol.citrus.message.Message internalMessage, ChannelEndpointConfiguration endpointConfiguration) {
        return MessageBuilder.withPayload(internalMessage.getPayload())
                    .copyHeaders(internalMessage.getHeaders())
                    .build();
    }

    @Override
    public com.consol.citrus.message.Message convertInbound(Message externalMessage, ChannelEndpointConfiguration endpointConfiguration) {
        if (externalMessage == null) {
            return null;
        }

        Map<String, Object> messageHeaders = new LinkedHashMap<>();
        messageHeaders.putAll(externalMessage.getHeaders());
        return new DefaultMessage(externalMessage.getPayload(), messageHeaders);
    }

    @Override
    public void convertOutbound(Message externalMessage, com.consol.citrus.message.Message internalMessage, ChannelEndpointConfiguration endpointConfiguration) {
    }
}
