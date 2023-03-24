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

package org.citrusframework.jms.message;

import java.util.HashMap;
import java.util.Map;

import jakarta.jms.Message;
import org.springframework.jms.support.JmsHeaders;
import org.springframework.jms.support.SimpleJmsHeaderMapper;
import org.springframework.messaging.MessageHeaders;

/**
 * Citrus JMS header mapper translates internal message headers to Spring integration message headers and
 * vice versa.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
public class JmsMessageHeaderMapper extends SimpleJmsHeaderMapper {

    private boolean filterInternalHeaders = true;

    @Override
    public void fromHeaders(MessageHeaders headers, Message jmsMessage) {
        Map<String, Object> integrationHeaders = new HashMap<>();

        if (headers.get(JmsMessageHeaders.CORRELATION_ID) != null) {
            integrationHeaders.put(JmsHeaders.CORRELATION_ID, headers.get(JmsMessageHeaders.CORRELATION_ID));
        }

        if (headers.get(JmsMessageHeaders.MESSAGE_ID) != null) {
            integrationHeaders.put(JmsHeaders.MESSAGE_ID, headers.get(JmsMessageHeaders.MESSAGE_ID));
        }

        if (headers.get(JmsMessageHeaders.REPLY_TO) != null) {
            integrationHeaders.put(JmsHeaders.REPLY_TO, headers.get(JmsMessageHeaders.REPLY_TO));
        }

        if (headers.get(JmsMessageHeaders.TIMESTAMP) != null) {
            integrationHeaders.put(JmsHeaders.TIMESTAMP, headers.get(JmsMessageHeaders.TIMESTAMP));
        }

        if (headers.get(JmsMessageHeaders.TYPE) != null) {
            integrationHeaders.put(JmsHeaders.TYPE, headers.get(JmsMessageHeaders.TYPE));
        }

        if (headers.get(JmsMessageHeaders.REDELIVERED) != null) {
            integrationHeaders.put(JmsHeaders.REDELIVERED, headers.get(JmsMessageHeaders.REDELIVERED));
        }

        if (headers.get(JmsMessageHeaders.PRIORITY) != null) {
            integrationHeaders.put(JmsHeaders.PRIORITY, headers.get(JmsMessageHeaders.PRIORITY));
        }

        if (headers.get(JmsMessageHeaders.DESTINATION) != null) {
            integrationHeaders.put(JmsHeaders.DESTINATION, headers.get(JmsMessageHeaders.DESTINATION));
        }

        if (headers.get(JmsMessageHeaders.DELIVERY_MODE) != null) {
            integrationHeaders.put(JmsHeaders.DELIVERY_MODE, headers.get(JmsMessageHeaders.DELIVERY_MODE));
        }

        if (headers.get(JmsMessageHeaders.EXPIRATION) != null) {
            integrationHeaders.put(JmsHeaders.EXPIRATION, headers.get(JmsMessageHeaders.EXPIRATION));
        }

        for (Map.Entry<String, Object> headerEntry : headers.entrySet()) {
            if (filterInternalHeaders) {
                if (!headerEntry.getKey().startsWith(org.citrusframework.message.MessageHeaders.PREFIX)) {
                    integrationHeaders.put(headerEntry.getKey(), headerEntry.getValue());
                }
            } else {
                if (!headerEntry.getKey().equals(org.citrusframework.message.MessageHeaders.ID)
                        && !headerEntry.getKey().equals(org.citrusframework.message.MessageHeaders.TIMESTAMP)) {
                    integrationHeaders.put(headerEntry.getKey(), headerEntry.getValue());
                }
            }
        }

        super.fromHeaders(new MessageHeaders(integrationHeaders), jmsMessage);
    }

    @Override
    public MessageHeaders toHeaders(Message jmsMessage) {
        Map<String, Object> internalHeaders = new HashMap<>();
        Map<String, Object> jmsHeaders = super.toHeaders(jmsMessage);

        if (jmsHeaders.get(JmsHeaders.CORRELATION_ID) != null) {
            internalHeaders.put(JmsMessageHeaders.CORRELATION_ID, jmsHeaders.get(JmsHeaders.CORRELATION_ID));
        }

        if (jmsHeaders.get(JmsHeaders.MESSAGE_ID) != null) {
            internalHeaders.put(JmsMessageHeaders.MESSAGE_ID, jmsHeaders.get(JmsHeaders.MESSAGE_ID));
        }

        if (jmsHeaders.get(JmsHeaders.REPLY_TO) != null) {
            internalHeaders.put(JmsMessageHeaders.REPLY_TO, jmsHeaders.get(JmsHeaders.REPLY_TO));
        }

        if (jmsHeaders.get(JmsHeaders.TIMESTAMP) != null) {
            internalHeaders.put(JmsMessageHeaders.TIMESTAMP, jmsHeaders.get(JmsHeaders.TIMESTAMP));
        }

        if (jmsHeaders.get(JmsHeaders.TYPE) != null) {
            internalHeaders.put(JmsMessageHeaders.TYPE, jmsHeaders.get(JmsHeaders.TYPE));
        }

        if (jmsHeaders.get(JmsHeaders.REDELIVERED) != null) {
            internalHeaders.put(JmsMessageHeaders.REDELIVERED, jmsHeaders.get(JmsHeaders.REDELIVERED));
        }

        if (jmsHeaders.get(JmsHeaders.PRIORITY) != null) {
            internalHeaders.put(JmsMessageHeaders.PRIORITY, jmsHeaders.get(JmsHeaders.PRIORITY));
        }

        if (jmsHeaders.get(JmsHeaders.DESTINATION) != null) {
            internalHeaders.put(JmsMessageHeaders.DESTINATION, jmsHeaders.get(JmsHeaders.DESTINATION));
        }

        if (jmsHeaders.get(JmsHeaders.DELIVERY_MODE) != null) {
            internalHeaders.put(JmsMessageHeaders.DELIVERY_MODE, jmsHeaders.get(JmsHeaders.DELIVERY_MODE));
        }

        if (jmsHeaders.get(JmsHeaders.EXPIRATION) != null) {
            internalHeaders.put(JmsMessageHeaders.EXPIRATION, jmsHeaders.get(JmsHeaders.EXPIRATION));
        }

        for (Map.Entry<String, Object> headerEntry : jmsHeaders.entrySet()) {
            if (!headerEntry.getKey().startsWith(JmsHeaders.PREFIX)) {
                internalHeaders.put(headerEntry.getKey(), headerEntry.getValue());
            }
        }

        return new MessageHeaders(internalHeaders);
    }

    public void setFilterInternalHeaders(boolean filterInternalHeaders) {
        this.filterInternalHeaders = filterInternalHeaders;
    }
}
