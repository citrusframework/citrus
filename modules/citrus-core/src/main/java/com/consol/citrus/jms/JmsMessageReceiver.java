/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
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

public class JmsMessageReceiver extends AbstractJmsAdapter implements MessageReceiver {
    
    private long receiveTimeout = 5000L;
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(JmsMessageReceiver.class);
    
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
    
    public Message<?> receive() {
        return receive(receiveTimeout);
    }

    public Message<?> receiveSelected(String selector) {
        return receiveSelected(selector, receiveTimeout);
    }

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
     * @param receiveTimeout the receiveTimeout to set
     */
    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }
}
