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

package com.consol.citrus.jms;

import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.jms.DefaultJmsHeaderMapper;
import org.springframework.integration.jms.JmsHeaderMapper;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.jms.support.converter.*;

import javax.jms.JMSException;
import javax.jms.Session;
import java.util.Map;

/**
 * Basic message converter for converting Spring Integration message implementations to JMS
 * messages and vice versa. Converter combines message converting logic and header mapping. 
 * Usually the message's payload is extracted to the JMS message payload and default JMS headers are mapped.
 * 
 * @author Christoph Deppisch
 */
public class JmsMessageConverter implements MessageConverter {

    /** The message converter delegate */
    private MessageConverter jmsMessageConverter = new SimpleMessageConverter();

    /** The header mapper */
    private JmsHeaderMapper headerMapper = new DefaultJmsHeaderMapper();
    
    /**
     * Convert Spring integration message to JMS message.
     */
    public javax.jms.Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
        javax.jms.Message jmsMessage;

        MessageHeaders headers = null;
        Object payload;
        if (object instanceof Message) {
            headers = ((Message) object).getHeaders();
            payload = ((Message) object).getPayload();
        } else {
            payload = object;
        }
        
        jmsMessage = jmsMessageConverter.toMessage(payload, session);
        if (headers != null) {
            headerMapper.fromHeaders(headers, jmsMessage);
        }

        return jmsMessage;
    }

    /**
     * Convert JMS message to Spring integration message.
     */
    public Object fromMessage(javax.jms.Message jmsMessage) throws JMSException, MessageConversionException {
        if (jmsMessage == null) {
            return null;
        }

        Map<String, ?> headers = headerMapper.toHeaders(jmsMessage);
        return MessageBuilder.withPayload(jmsMessageConverter.fromMessage(jmsMessage))
                                        .copyHeaders(headers)
                                        .build();
    }

    /**
     * Gets the JMS message converter.
     * @return the jmsMessageConverter
     */
    public MessageConverter getJmsMessageConverter() {
        return jmsMessageConverter;
    }

    /**
     * Sets the JMS message converter.
     * @param jmsMessageConverter the jmsMessageConverter to set
     */
    public void setJmsMessageConverter(MessageConverter jmsMessageConverter) {
        this.jmsMessageConverter = jmsMessageConverter;
    }

    /**
     * Gets the JMS header mapper.
     * @return the headerMapper
     */
    public JmsHeaderMapper getHeaderMapper() {
        return headerMapper;
    }

    /**
     * Sets the JMS header mapper.
     * @param headerMapper the headerMapper to set
     */
    public void setHeaderMapper(JmsHeaderMapper headerMapper) {
        this.headerMapper = headerMapper;
    }
    
}
