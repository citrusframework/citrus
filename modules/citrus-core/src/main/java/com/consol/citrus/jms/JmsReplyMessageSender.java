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

import com.consol.citrus.report.MessageListeners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.Assert;

import com.consol.citrus.message.*;

/**
 * This JMS message sender is quite similar to Spring's AbstractJmsTemplateBasedAdapter that is 
 * already used in asynchronous JMS senders and receivers. But AbstractJmsTemplateBasedAdapter is
 * working with static default destinations.
 * 
 * In this class we rather operate with dynamic destinations. Therefore this adapter implementation has 
 * slight differences.
 * 
 * @author Christoph Deppisch
 */
public class JmsReplyMessageSender extends AbstractJmsAdapter implements MessageSender {
    /** Reply destination holder */
    private JmsReplyDestinationHolder replyDestinationHolder;

    /** Reply message correlator */
    private ReplyMessageCorrelator correlator = null;
    
    @Autowired(required = false)
    private MessageListeners messageListener;
    
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(JmsReplyMessageSender.class);

    /**
     * @see com.consol.citrus.message.MessageSender#send(org.springframework.integration.Message)
     */
    public void send(Message<?> message) {
        Assert.notNull(message, "Message is empty - unable to send empty message");
        
        Destination replyDestination;
        Message<?> replyMessage;
        
        if (correlator != null) {
            Assert.notNull(message.getHeaders().get(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR), "Can not correlate reply destination - " +
            		"you need to set " + CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR + " in message header");
            
            String correlationKey = correlator.getCorrelationKey(message.getHeaders().get(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR).toString());
            replyDestination = replyDestinationHolder.getReplyDestination(correlationKey);
            Assert.notNull(replyDestination, "Unable to locate JMS reply destination with correlation key: '" + correlationKey + "'");
            
            //remove citrus specific header from message
            replyMessage = MessageBuilder.fromMessage(message).removeHeader(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR).build();
        } else {
            replyMessage = message;
            replyDestination = replyDestinationHolder.getReplyDestination();
            Assert.notNull(replyDestination, "Unable to locate JMS reply destination");
        }
        
        log.info("Sending JMS message to destination: '" + getDestinationName(replyDestination) + "'");

        getJmsTemplate().convertAndSend(replyDestination, replyMessage);
        
        if (messageListener != null) {
            messageListener.onOutboundMessage(replyMessage.toString());
        } else {
            log.info("Sent message is:" + System.getProperty("line.separator") + replyMessage.toString());
        }
        
        log.info("Message was successfully sent to destination: '" + getDestinationName(replyDestination) + "'");
    }
    
    /**
     * Set the reply destination.
     * @param replyDestinationHolder the replyDestinationHolder to set
     */
    public void setReplyDestinationHolder(
            JmsReplyDestinationHolder replyDestinationHolder) {
        this.replyDestinationHolder = replyDestinationHolder;
    }

    /**
     * Get the destination name (either a queue name or a topic name).
     * @return the destinationName
     */
    protected String getDestinationName(Destination destination) {
        try {
            if (destination != null) {
                if (destination instanceof Queue) {
                    return ((Queue)destination).getQueueName();
                } else if (destination instanceof Topic) {
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

    /**
     * In addition to usual initializing steps check that replySestinationHolder is set correctly.
     */
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        
        Assert.notNull(replyDestinationHolder, "Missing required property 'replyDestinationHolder'");
    }
    
    /**
     * Set the message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        this.correlator = correlator;
    }

    /**
     * Gets the replyDestinationHolder.
     * @return the replyDestinationHolder
     */
    public JmsReplyDestinationHolder getReplyDestinationHolder() {
        return replyDestinationHolder;
    }

    /**
     * Gets the correlator.
     * @return the correlator
     */
    public ReplyMessageCorrelator getCorrelator() {
        return correlator;
    }
}
