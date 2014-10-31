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

package com.consol.citrus.jms.message;

import org.springframework.integration.jms.DefaultJmsHeaderMapper;
import org.springframework.integration.jms.JmsHeaders;
import org.springframework.messaging.MessageHeaders;

import javax.jms.Message;
import java.util.HashMap;
import java.util.Map;

/**
 * Citrus JMS header mapper translates internal message headers to Spring integration message headers and
 * vice versa.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
public class JmsMessageHeaderMapper extends DefaultJmsHeaderMapper {

    @Override
    public void fromHeaders(MessageHeaders headers, Message jmsMessage) {
        Map<String, Object> integrationHeaders = new HashMap<String, Object>();

        if (headers.get(CitrusJmsMessageHeaders.CORRELATION_ID) != null) {
            integrationHeaders.put(JmsHeaders.CORRELATION_ID, headers.get(CitrusJmsMessageHeaders.CORRELATION_ID));
        }

        if (headers.get(CitrusJmsMessageHeaders.MESSAGE_ID) != null) {
            integrationHeaders.put(JmsHeaders.MESSAGE_ID, headers.get(CitrusJmsMessageHeaders.MESSAGE_ID));
        }

        if (headers.get(CitrusJmsMessageHeaders.REPLY_TO) != null) {
            integrationHeaders.put(JmsHeaders.REPLY_TO, headers.get(CitrusJmsMessageHeaders.REPLY_TO));
        }

        if (headers.get(CitrusJmsMessageHeaders.TIMESTAMP) != null) {
            integrationHeaders.put(JmsHeaders.TIMESTAMP, headers.get(CitrusJmsMessageHeaders.TIMESTAMP));
        }

        if (headers.get(CitrusJmsMessageHeaders.TYPE) != null) {
            integrationHeaders.put(JmsHeaders.TYPE, headers.get(CitrusJmsMessageHeaders.TYPE));
        }

        if (headers.get(CitrusJmsMessageHeaders.REDELIVERED) != null) {
            integrationHeaders.put(JmsHeaders.REDELIVERED, headers.get(CitrusJmsMessageHeaders.REDELIVERED));
        }

        for (Map.Entry<String, Object> headerEntry : headers.entrySet()) {
            if (!headerEntry.getKey().startsWith(CitrusJmsMessageHeaders.JMS_PREFIX)
                    && !headerEntry.getKey().equals(com.consol.citrus.message.MessageHeaders.ID)
                    && !headerEntry.getKey().equals(com.consol.citrus.message.MessageHeaders.TIMESTAMP)) {
                integrationHeaders.put(headerEntry.getKey(), headerEntry.getValue());
            }
        }

        super.fromHeaders(new MessageHeaders(integrationHeaders), jmsMessage);
    }

    @Override
    public Map<String, Object> toHeaders(Message jmsMessage) {
        Map<String, Object> internalHeaders = new HashMap<>();
        Map<String, Object> jmsHeaders = super.toHeaders(jmsMessage);

        if (jmsHeaders.get(JmsHeaders.CORRELATION_ID) != null) {
            internalHeaders.put(CitrusJmsMessageHeaders.CORRELATION_ID, jmsHeaders.get(JmsHeaders.CORRELATION_ID));
        }

        if (jmsHeaders.get(JmsHeaders.MESSAGE_ID) != null) {
            internalHeaders.put(CitrusJmsMessageHeaders.MESSAGE_ID, jmsHeaders.get(JmsHeaders.MESSAGE_ID));
        }

        if (jmsHeaders.get(JmsHeaders.REPLY_TO) != null) {
            internalHeaders.put(CitrusJmsMessageHeaders.REPLY_TO, jmsHeaders.get(JmsHeaders.REPLY_TO));
        }

        if (jmsHeaders.get(JmsHeaders.TIMESTAMP) != null) {
            internalHeaders.put(CitrusJmsMessageHeaders.TIMESTAMP, jmsHeaders.get(JmsHeaders.TIMESTAMP));
        }

        if (jmsHeaders.get(JmsHeaders.TYPE) != null) {
            internalHeaders.put(CitrusJmsMessageHeaders.TYPE, jmsHeaders.get(JmsHeaders.TYPE));
        }

        if (jmsHeaders.get(JmsHeaders.REDELIVERED) != null) {
            internalHeaders.put(CitrusJmsMessageHeaders.REDELIVERED, jmsHeaders.get(JmsHeaders.REDELIVERED));
        }

        for (Map.Entry<String, Object> headerEntry : jmsHeaders.entrySet()) {
            if (!headerEntry.getKey().startsWith(JmsHeaders.PREFIX)) {
                internalHeaders.put(headerEntry.getKey(), headerEntry.getValue());
            }
        }

        return internalHeaders;
    }
}
