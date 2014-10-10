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

import com.consol.citrus.message.*;
import org.springframework.messaging.support.MessageBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class ChannelMessageConverter implements MessageConverter<org.springframework.messaging.Message, ChannelEndpointConfiguration> {

    @Override
    public org.springframework.messaging.Message convertOutbound(Message internalMessage, ChannelEndpointConfiguration endpointConfiguration) {
        if (endpointConfiguration.isUseObjectMessages()) {
            return MessageBuilder.withPayload(internalMessage)
                    .build();

        } else {
            return MessageBuilder.withPayload(internalMessage.getPayload())
                    .copyHeaders(internalMessage.copyHeaders())
                    .build();
        }
    }

    @Override
    public Message convertInbound(org.springframework.messaging.Message externalMessage, ChannelEndpointConfiguration endpointConfiguration) {
        if (externalMessage == null) {
            return null;
        }

        Map<String, Object> messageHeaders = new LinkedHashMap<>();
        messageHeaders.putAll(externalMessage.getHeaders());

        Object payload = externalMessage.getPayload();
        if (payload instanceof Message) {
            Message nestedMessage = (Message) payload;

            for (Map.Entry<String, Object> headerEntry : messageHeaders.entrySet()) {
                if (!headerEntry.getKey().startsWith(MessageHeaders.MESSAGE_PREFIX)) {
                    nestedMessage.setHeader(headerEntry.getKey(), headerEntry.getValue());
                }
            }

            return nestedMessage;
        } else {
            return new DefaultMessage(payload, messageHeaders);
        }
    }

    @Override
    public void convertOutbound(org.springframework.messaging.Message externalMessage, Message internalMessage, ChannelEndpointConfiguration endpointConfiguration) {
    }
}
