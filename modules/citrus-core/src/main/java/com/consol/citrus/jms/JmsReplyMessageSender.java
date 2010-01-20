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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.core.Message;
import org.springframework.integration.jms.HeaderMappingMessageConverter;
import org.springframework.integration.jms.JmsHeaderMapper;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.util.Assert;

import com.consol.citrus.message.*;

/**
 * This JMS message sender is quite similar to Spring's AbstractJmsTemplateBasedAdapter that is 
 * already used in out asynchronous JMS senders and receivers. But AbstractJmsTemplateBasedAdapter is
 * working with static default destinations.
 * 
 * In this class we rather operate with dynamic destinations. Therefore this adapter implementation has 
 * slight differences.
 * 
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 */
public class JmsReplyMessageSender implements MessageSender, InitializingBean {
    private JmsReplyDestinationHolder replyDestinationHolder;

    private ConnectionFactory connectionFactory;

    private JmsTemplate jmsTemplate;

    private JmsHeaderMapper headerMapper;

    private boolean initialized;

    private final Object initializationMonitor = new Object();
    
    private ReplyMessageCorrelator correlator = null;
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(JmsReplyMessageSender.class);

    public JmsReplyMessageSender(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public JmsReplyMessageSender() {
    }

    public void send(Message<?> message) {
        Assert.notNull(message, "Can not send empty message");
        
        Destination replyDestination;
        
        if(correlator != null) {
            Assert.notNull(message.getHeaders().get(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR), "Can not correlate reply destination - " +
            		"you need to set " + CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR + " in message header");
            
            replyDestination = replyDestinationHolder.getReplyDestination(correlator.getCorrelationKey(message.getHeaders().get(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR).toString()));
            
            //remove citrus specific header from message
            message = MessageBuilder.fromMessage(message).removeHeader(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR).build();
        } else {
            replyDestination = replyDestinationHolder.getReplyDestination();
        }
        
        Assert.notNull(replyDestination, "Not able to find temporary reply destination");
        
        log.info("Sending message to: " + getDestinationName(replyDestination));

        if (log.isDebugEnabled()) {
            log.debug("Message to be sent:");
            log.debug(message.toString());
        }
        
        getJmsTemplate().convertAndSend(replyDestination, message);
    }
    
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void setHeaderMapper(JmsHeaderMapper headerMapper) {
        this.headerMapper = headerMapper;
    }
    
    /**
     * @param replyDestinationHolder the replyDestinationHolder to set
     */
    public void setReplyDestinationHolder(
            JmsReplyDestinationHolder replyDestinationHolder) {
        this.replyDestinationHolder = replyDestinationHolder;
    }

    protected JmsTemplate getJmsTemplate() {
        if (this.jmsTemplate == null) {
            this.afterPropertiesSet();
        }
        return this.jmsTemplate;
    }
    
    /**
     * @return the destinationName
     */
    protected String getDestinationName(Destination destination) {
        try {
            if(destination != null) {
                if(destination instanceof Queue) {
                    return ((Queue)destination).getQueueName();
                } else if(destination instanceof Topic) {
                    return ((Topic)destination).getTopicName();
                } else {
                    return destination.toString();
                }
            } else {
                return null;
            }
        } catch (JMSException e) {
            log.error("Error while getting destination name", e);
            return "";
        }
    }

    public void afterPropertiesSet() {
        synchronized (this.initializationMonitor) {
            if (this.initialized) {
                return;
            }
            
            Assert.notNull(replyDestinationHolder, "'replyDestinationHolder' is required");
            
            if (this.jmsTemplate == null) {
                Assert.isTrue(this.connectionFactory != null, "'connectionFactory'is required.");
                this.jmsTemplate = new JmsTemplate();
                jmsTemplate.setConnectionFactory(this.connectionFactory);
            }
            
            this.configureMessageConverter(this.jmsTemplate, this.headerMapper);
            this.initialized = true;
        }
    }
    
    protected void configureMessageConverter(JmsTemplate jmsTemplate, JmsHeaderMapper headerMapper) {
        MessageConverter converter = jmsTemplate.getMessageConverter();
        if (converter == null || !(converter instanceof HeaderMappingMessageConverter)) {
            HeaderMappingMessageConverter hmmc = new HeaderMappingMessageConverter(converter, headerMapper);
            hmmc.setExtractIntegrationMessagePayload(true);
            jmsTemplate.setMessageConverter(hmmc);
        }
    }

    /**
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        this.correlator = correlator;
    }
}
