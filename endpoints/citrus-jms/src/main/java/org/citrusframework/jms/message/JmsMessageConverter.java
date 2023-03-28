/*
 * Copyright 2006-2011 the original author or authors.
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

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jms.endpoint.JmsEndpointConfiguration;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageConverter;
import jakarta.jms.BytesMessage;
import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.MapMessage;
import jakarta.jms.ObjectMessage;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.springframework.jms.support.JmsUtils;
import org.springframework.messaging.MessageHeaders;

/**
 * Basic message converter for converting Spring Integration message implementations to JMS
 * messages and vice versa. Converter combines message converting logic and header mapping.
 * Usually the message's payload is extracted to the JMS message payload and default JMS headers are mapped.
 *
 * @author Christoph Deppisch
 */
public class JmsMessageConverter implements MessageConverter<jakarta.jms.Message, jakarta.jms.Message, JmsEndpointConfiguration> {

    @Override
    public jakarta.jms.Message convertOutbound(Message message, JmsEndpointConfiguration endpointConfiguration, TestContext context) {
        Connection connection = null;
        Session session = null;

        try {
            connection = endpointConfiguration.getConnectionFactory().createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            return createJmsMessage(message, session, endpointConfiguration, context);
        } catch (JMSException e) {
            throw new CitrusRuntimeException("Failed to create JMS message", e);
        } finally {
            JmsUtils.closeSession(session);
            JmsUtils.closeConnection(connection);
        }
    }

    @Override
    public void convertOutbound(jakarta.jms.Message jmsMessage, Message message, JmsEndpointConfiguration endpointConfiguration, TestContext context) {
        Map<String, Object> headers = message.getHeaders();

        if (headers != null) {
            endpointConfiguration.getHeaderMapper().fromHeaders(new MessageHeaders(headers), jmsMessage);
        }
    }

    @Override
    public Message convertInbound(jakarta.jms.Message jmsMessage, JmsEndpointConfiguration endpointConfiguration, TestContext context) {
        if (jmsMessage == null) {
            return null;
        }

        try {
            MessageHeaders headers = endpointConfiguration.getHeaderMapper().toHeaders(jmsMessage);
            Object payload;

            if (jmsMessage instanceof TextMessage) {
                payload = ((TextMessage) jmsMessage).getText();
            } else if (jmsMessage instanceof BytesMessage) {
                byte[] bytes = new byte[(int) ((BytesMessage) jmsMessage).getBodyLength()];
                ((BytesMessage) jmsMessage).readBytes(bytes);
                payload = bytes;
            } else if (jmsMessage instanceof MapMessage) {
                Map<String, Object> map = new HashMap<String, Object>();
                Enumeration en = ((MapMessage) jmsMessage).getMapNames();
                while (en.hasMoreElements()) {
                    String key = (String) en.nextElement();
                    map.put(key, ((MapMessage) jmsMessage).getObject(key));
                }
                payload = map;
            } else if (jmsMessage instanceof ObjectMessage) {
                payload = ((ObjectMessage) jmsMessage).getObject();
            } else {
                payload = jmsMessage;
            }

            if (payload instanceof Message) {
                Message nestedMessage = (Message) payload;
                for (Map.Entry<String, Object> headerEntry : headers.entrySet()) {
                    if (!headerEntry.getKey().startsWith(org.citrusframework.message.MessageHeaders.MESSAGE_PREFIX)) {
                        nestedMessage.setHeader(headerEntry.getKey(), headerEntry.getValue());
                    }
                }

                return nestedMessage;
            } else {
                JmsMessage message = new JmsMessage(payload);
                for (Map.Entry<String, Object> headerEntry : headers.entrySet()) {
                    if (!headerEntry.getKey().startsWith(org.citrusframework.message.MessageHeaders.MESSAGE_PREFIX)) {
                        message.setHeader(headerEntry.getKey(), headerEntry.getValue());
                    }
                }

                return message;
            }
        } catch (JMSException e) {
            throw new CitrusRuntimeException("Failed to convert jms message", e);
        }
    }

    /**
     * Creates JMS message instance from internal message representation. According to message payload type the JMS session
     * creates related JMS message type such as TextMessage, MapMessage, ObjectMessage or BytesMessage.
     *
     * @param message
     * @param session
     * @param endpointConfiguration
     * @param context
     * @return
     */
    public jakarta.jms.Message createJmsMessage(Message message, Session session, JmsEndpointConfiguration endpointConfiguration, TestContext context) {
        try {
            Object payload = message.getPayload();

            jakarta.jms.Message jmsMessage;
            if (endpointConfiguration.isUseObjectMessages()) {
                jmsMessage = session.createObjectMessage(message);
            } else if (payload instanceof jakarta.jms.Message) {
                jmsMessage = (jakarta.jms.Message) payload;
            } else if (payload instanceof String) {
                jmsMessage = session.createTextMessage((String) payload);
            } else if (payload instanceof byte[]) {
                jmsMessage = session.createBytesMessage();
                ((BytesMessage)jmsMessage).writeBytes((byte[]) payload);
            } else if (payload instanceof Map) {
                jmsMessage = session.createMapMessage();
                Map<?, ?> map = ((Map) payload);
                for (Map.Entry entry : map.entrySet()) {
                    if (!(entry.getKey() instanceof String)) {
                        throw new CitrusRuntimeException("Cannot convert non-String key of type [" + entry.getKey() + "] to JMS MapMessage entry");
                    }
                    ((MapMessage)jmsMessage).setObject((String) entry.getKey(), entry.getValue());
                }
            } else if (payload instanceof Serializable) {
                jmsMessage = session.createObjectMessage((Serializable) payload);
            } else {
                throw new CitrusRuntimeException("Cannot convert object of type [" + payload + "] to JMS message. Supported message " +
                        "payloads are: String, byte array, Map<String,?>, Serializable object.");
            }
            convertOutbound(jmsMessage, message, endpointConfiguration, context);

            return jmsMessage;
        } catch (JMSException e) {
            throw new CitrusRuntimeException("Failed to convert jms message", e);
        }
    }

}
