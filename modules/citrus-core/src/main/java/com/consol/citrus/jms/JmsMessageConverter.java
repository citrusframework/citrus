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

import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.jms.JmsHeaderMapper;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.util.Assert;

/**
 * Basic message converter for converting Spring Integration message implementations to JMS
 * messages and vice versa. Converter combines message converting logic and header mapping. 
 * Usually the message's payload is extracted to the JMS message payload and default JMS headers are mapped.
 * 
 * @author Christoph Deppisch
 */
public class JmsMessageConverter implements MessageConverter {

    /** The message converter delegate */
    private MessageConverter messageConverter;

    /** The header mapper */
    private JmsHeaderMapper headerMapper;
    
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(JmsMessageConverter.class);
    
    /**
     * Default constructor using fields.
     * @param messageConverter
     * @param headerMapper
     */
    public JmsMessageConverter(MessageConverter messageConverter, JmsHeaderMapper headerMapper) {
        Assert.notNull(messageConverter, "Missing required message converter");
        Assert.notNull(headerMapper, "Missing required header mapper");
        
        this.messageConverter = messageConverter;
        this.headerMapper = headerMapper;
    }

    /**
     * Convert Spring integration message to JMS message.
     */
    public javax.jms.Message toMessage(Object object, Session session) 
        throws JMSException, MessageConversionException {
        
        MessageHeaders headers = null;
        javax.jms.Message jmsMessage = null;
        
        Object payload;
        if (object instanceof Message) {
            headers = ((Message<?>) object).getHeaders();
            payload = ((Message<?>) object).getPayload();
        } else {
            payload = object;
        }
        
        jmsMessage = messageConverter.toMessage(payload, session);
        if (headers != null) {
            headerMapper.fromHeaders(headers, jmsMessage);
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Just converted [" + payload + "] to JMS Message [" + jmsMessage + "]");
        }
        
        return jmsMessage;
    }

    /**
     * Convert JMS message to Spring integration message.
     */
    public Object fromMessage(javax.jms.Message jmsMessage) 
        throws JMSException, MessageConversionException {
        
        MessageBuilder<?> builder = null;
        Object conversionResult = messageConverter.fromMessage(jmsMessage);
        
        if (conversionResult == null) {
            return null;
        }
        
        builder = MessageBuilder.withPayload(conversionResult);
        
        Map<String, ?> headers = headerMapper.toHeaders(jmsMessage);
        Message<?> message = builder.copyHeaders(headers).build();
        
        if (log.isDebugEnabled()) {
            log.debug("Just converted JMS Message [" + jmsMessage + "] to integration message [" + message + "]");
        }
        
        return message;
    }
    
}
