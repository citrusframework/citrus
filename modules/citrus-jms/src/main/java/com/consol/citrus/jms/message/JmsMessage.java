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

import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;

import javax.jms.Destination;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class JmsMessage extends DefaultMessage {

    /**
     * Constructs copy of given message.
     * @param message
     */
    public JmsMessage(Message message) {
        super(message);
    }

    /**
     * Default constructor using message payload.
     * @param payload
     */
    public JmsMessage(Object payload) {
        super(payload);
    }

    /**
     * Default constructor using message payload and headers.
     * @param payload
     * @param headers
     */
    public JmsMessage(Object payload, Map<String, Object> headers) {
        super(payload, headers);

        if (!headers.containsKey(CitrusJmsMessageHeaders.MESSAGE_ID)) {
            setJmsMessageId(getId());
        }

        if (!headers.containsKey(CitrusJmsMessageHeaders.TIMESTAMP)) {
            setJmsTimestamp(getTimestamp());
        }
    }

    /**
     * Sets the JMS message id header.
     * @param messageId
     */
    public JmsMessage setJmsMessageId(String messageId) {
        setHeader(CitrusJmsMessageHeaders.MESSAGE_ID, messageId);
        return this;
    }

    /**
     * Gets the JMS messageId header.
     * @return
     */
    public String getJmsMessageId() {
        Object messageId = getHeader(CitrusJmsMessageHeaders.MESSAGE_ID);

        if (messageId != null) {
            return messageId.toString();
        }

        return null;
    }

    /**
     * Sets the JMS timestamp header.
     * @param timestamp
     */
    public JmsMessage setJmsTimestamp(Long timestamp) {
        setHeader(CitrusJmsMessageHeaders.TIMESTAMP, timestamp);
        return this;
    }

    /**
     * Gets the JMS timestamp header.
     * @return
     */
    public Long getJmsTimestamp() {
        Object timestamp = getHeader(CitrusJmsMessageHeaders.TIMESTAMP);

        if (timestamp != null) {
            return Long.valueOf(timestamp.toString());
        }

        return null;
    }

    /**
     * Sets the JMS correlation id header.
     * @param correlationId
     */
    public JmsMessage setCorrelationId(String correlationId) {
        setHeader(CitrusJmsMessageHeaders.CORRELATION_ID, correlationId);
        return this;
    }

    /**
     * Gets the JMS correlationId header.
     * @return
     */
    public String getCorrelationId() {
        Object correlationId = getHeader(CitrusJmsMessageHeaders.CORRELATION_ID);

        if (correlationId != null) {
            return correlationId.toString();
        }

        return null;
    }

    /**
     * Sets the JMS reply to header.
     * @param replyTo
     */
    public JmsMessage setReplyTo(Destination replyTo) {
        setHeader(CitrusJmsMessageHeaders.REPLY_TO, replyTo);
        return this;
    }

    /**
     * Gets the JMS reply to header.
     * @return
     */
    public Destination getReplyTo() {
        Object replyTo = getHeader(CitrusJmsMessageHeaders.REPLY_TO);

        if (replyTo != null) {
            return (Destination) replyTo;
        }

        return null;
    }

    /**
     * Sets the JMS redelivered header.
     * @param redelivered
     */
    public JmsMessage setRedelivered(String redelivered) {
        setHeader(CitrusJmsMessageHeaders.REDELIVERED, redelivered);
        return this;
    }

    /**
     * Gets the JMS redelivered header.
     * @return
     */
    public String getRedelivered() {
        Object redelivered = getHeader(CitrusJmsMessageHeaders.REDELIVERED);

        if (redelivered != null) {
            return redelivered.toString();
        }

        return null;
    }

    /**
     * Sets the JMS type header.
     * @param type
     */
    public JmsMessage setType(String type) {
        setHeader(CitrusJmsMessageHeaders.TYPE, type);
        return this;
    }

    /**
     * Gets the JMS type header.
     * @return
     */
    public String getType() {
        Object type = getHeader(CitrusJmsMessageHeaders.TYPE);

        if (type != null) {
            return type.toString();
        }

        return null;
    }
}
