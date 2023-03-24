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

package org.citrusframework.channel;

import java.util.LinkedHashMap;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageConverter;
import org.citrusframework.message.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class ChannelMessageConverter implements MessageConverter<org.springframework.messaging.Message<?>, org.springframework.messaging.Message<?>, ChannelEndpointConfiguration> {

    @Override
    public org.springframework.messaging.Message<?> convertOutbound(Message internalMessage, ChannelEndpointConfiguration endpointConfiguration, TestContext context) {
        if (endpointConfiguration.isUseObjectMessages()) {
            return MessageBuilder.withPayload(internalMessage)
                    .build();

        }

        Map<String, Object> headers = new LinkedHashMap<>();
        for (Map.Entry<String, Object> headerEntry: internalMessage.getHeaders().entrySet()) {
            if (endpointConfiguration.isFilterInternalHeaders()) {
                if (!headerEntry.getKey().startsWith(MessageHeaders.PREFIX)) {
                    headers.put(headerEntry.getKey(), headerEntry.getValue());
                }
            } else {
                if (!headerEntry.getKey().equals(org.citrusframework.message.MessageHeaders.ID)
                        && !headerEntry.getKey().equals(org.citrusframework.message.MessageHeaders.TIMESTAMP)) {
                    headers.put(headerEntry.getKey(), headerEntry.getValue());
                }
            }
        }

        return MessageBuilder.withPayload(internalMessage.getPayload())
                .copyHeaders(headers)
                .build();
    }

    @Override
    public Message convertInbound(org.springframework.messaging.Message<?> externalMessage, ChannelEndpointConfiguration endpointConfiguration, TestContext context) {
        if (externalMessage == null) {
            return null;
        }

        Map<String, Object> messageHeaders = new LinkedHashMap<>(externalMessage.getHeaders());

        Object payload = externalMessage.getPayload();
        if (payload instanceof Message) {
            Message nestedMessage = (Message) payload;

            for (Map.Entry<String, Object> headerEntry : messageHeaders.entrySet()) {
                if (endpointConfiguration.isFilterInternalHeaders()) {
                    if (!headerEntry.getKey().startsWith(MessageHeaders.PREFIX)) {
                        nestedMessage.setHeader(headerEntry.getKey(), headerEntry.getValue());
                    }
                } else {
                    if (!headerEntry.getKey().equals(org.citrusframework.message.MessageHeaders.ID)
                            && !headerEntry.getKey().equals(MessageHeaders.TIMESTAMP)) {
                        nestedMessage.setHeader(headerEntry.getKey(), headerEntry.getValue());
                    }
                }
            }

            return nestedMessage;
        } else {
            return new DefaultMessage(payload, messageHeaders);
        }
    }

    @Override
    public void convertOutbound(org.springframework.messaging.Message<?> externalMessage, Message internalMessage, ChannelEndpointConfiguration endpointConfiguration, TestContext context) {
    }
}
