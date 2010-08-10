/*
 * Copyright 2006-2010 the original author or authors.
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

import javax.jms.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.Message;
import org.springframework.integration.jms.HeaderMappingMessageConverter;
import org.springframework.integration.jms.JmsHeaderMapper;
import org.springframework.integration.message.GenericMessage;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;

import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.message.MessageReceiver;

/**
 * {@link MessageReceiver} implementation consumes messages from aJMS destination. Destination
 * is given by injected instance or destination name.
 *  
 * @author Christoph Deppisch
 */
public class JmsMessageReceiver extends AbstractJmsAdapter implements MessageReceiver {
    /** Receive timeout */
    private long receiveTimeout = 5000L;
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(JmsMessageReceiver.class);
    
    /**
     * @see com.consol.citrus.message.MessageReceiver#receive(long)
     * @throws ActionTimeoutException
     */
    public Message<?> receive(long timeout) {
        log.info("Receiving message from: " + getDestinationName());
        
        getJmsTemplate().setReceiveTimeout(timeout);
        Object receivedObject = getJmsTemplate().receiveAndConvert();
        
        if(receivedObject == null) {
            throw new ActionTimeoutException("Action timed out while receiving message on " + getDestinationName());
        }
        
        Message<?> receivedMessage;
        if (receivedObject instanceof Message<?>) {
            receivedMessage = (Message<?>)receivedObject;
        } else {
            receivedMessage = new GenericMessage<Object>(receivedObject);
        }
        
        if(log.isDebugEnabled()) {
            log.debug("Message received:");
            log.debug(receivedMessage.toString());
        }
        
        return receivedMessage;
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receiveSelected(java.lang.String, long)
     * @throws ActionTimeoutException
     */
    public Message<?> receiveSelected(String selector, long timeout) {
        log.info("Receiving message from: " + getDestinationName() + "(" + selector + ")");
        
        getJmsTemplate().setReceiveTimeout(timeout);
        Object receivedObject = getJmsTemplate().receiveSelectedAndConvert(selector);
        
        if(receivedObject == null) {
            throw new ActionTimeoutException("Action timed out while receiving message on " + getDestinationName());
        }
        
        Message<?> receivedMessage;
        if (receivedObject instanceof Message<?>) {
            receivedMessage = (Message<?>)receivedObject;
        } else {
            receivedMessage = new GenericMessage<Object>(receivedObject);
        }
        
        if(log.isDebugEnabled()) {
            log.debug("Message received:");
            log.debug(receivedMessage.toString());
        }
        
        return receivedMessage;
    }

    /**
     * Retrieves the destination name (either a queue name or a topic name).
     * @return the destinationName
     */
    protected String getDestinationName() {
        try {
            if(getJmsTemplate().getDefaultDestination() != null) {
                if(getJmsTemplate().getDefaultDestination() instanceof Queue) {
                    return ((Queue)getJmsTemplate().getDefaultDestination()).getQueueName();
                } else if(getJmsTemplate().getDefaultDestination() instanceof Topic) {
                    return ((Topic)getJmsTemplate().getDefaultDestination()).getTopicName();
                } else {
                    return getJmsTemplate().getDefaultDestination().toString();
                }
            } else {
                return getJmsTemplate().getDefaultDestinationName();
            }
        } catch (JMSException e) {
            log.error("Error while getting destination name", e);
            return "";
        }
    }
    
    /**
     * @see com.consol.citrus.message.MessageReceiver#receive()
     */
    public Message<?> receive() {
        return receive(receiveTimeout);
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receiveSelected(java.lang.String)
     */
    public Message<?> receiveSelected(String selector) {
        return receiveSelected(selector, receiveTimeout);
    }

    /**
     * @see org.springframework.integration.jms.AbstractJmsTemplateBasedAdapter#configureMessageConverter(org.springframework.jms.core.JmsTemplate, org.springframework.integration.jms.JmsHeaderMapper)
     */
    @Override
    protected void configureMessageConverter(JmsTemplate jmsTemplate, JmsHeaderMapper headerMapper) {
        MessageConverter converter = jmsTemplate.getMessageConverter();
        if (converter == null || !(converter instanceof HeaderMappingMessageConverter)) {
            HeaderMappingMessageConverter hmmc = new HeaderMappingMessageConverter(converter, headerMapper);
            hmmc.setExtractIntegrationMessagePayload(true);
            jmsTemplate.setMessageConverter(hmmc);
        }
    }

    /**
     * Sets the receive timeout.
     * @param receiveTimeout the receiveTimeout to set
     */
    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }
}
