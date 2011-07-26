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

package com.consol.citrus.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.*;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.Assert;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.*;

/**
 * Send reply messages to channel destinations.
 * 
 * @author Christoph Deppisch
 */
public class ReplyMessageChannelSender implements MessageSender {
    
    /** Holding dynamic reply channel  */
    private ReplyMessageChannelHolder replyMessageChannelHolder;

    /** Message channel template */
    private MessagingTemplate messagingTemplate = new MessagingTemplate();

    /** Reply message correlator */
    private ReplyMessageCorrelator correlator = null;
    
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(ReplyMessageChannelSender.class);
    
    /**
     * @see com.consol.citrus.message.MessageSender#send(org.springframework.integration.Message)
     */
    public void send(Message<?> message) {
        Assert.notNull(message, "Can not send empty message");
        
        MessageChannel replyChannel;
        Message<?> replyMessage;
        
        if (correlator != null) {
            Assert.notNull(message.getHeaders().get(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR), "Can not correlate reply destination - " +
                    "you need to set " + CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR + " in message header");
            
            String correlationKey = correlator.getCorrelationKey(message.getHeaders().get(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR).toString());
            replyChannel = replyMessageChannelHolder.getReplyMessageChannel(correlationKey);
            Assert.notNull(replyChannel, "Unable to locate reply channel with correlation key: " + correlationKey);
            
            //remove citrus specific header from message
            replyMessage = MessageBuilder.fromMessage(message).removeHeader(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR).build();
        } else {
            replyMessage = message;
            replyChannel = replyMessageChannelHolder.getReplyMessageChannel();
            Assert.notNull(replyChannel, "Unable to locate reply channel");
        }
        
        log.info("Sending message to reply channel: '" + replyChannel + "'");

        if (log.isDebugEnabled()) {
            log.debug("Message to send is:\n" + replyMessage.toString());
        }
        
        try {
            messagingTemplate.send(replyChannel, replyMessage);
        } catch (MessageDeliveryException e) {
            throw new CitrusRuntimeException("Failed to send message to channel: '" + replyChannel + "'", e);
        }
        
        log.info("Message was successfully sent to reply channel: '" + replyChannel + "'");
    }
    
    /**
     * Set the reply message holder.
     * @param replyMessageChannelHolder the replyMessageChannelHolder to set
     */
    public void setReplyMessageChannelHolder(ReplyMessageChannelHolder replyMessageChannelHolder) {
        this.replyMessageChannelHolder = replyMessageChannelHolder;
    }

    /**
     * Get the reply message holder.
     * @return the replyMessageChannelHolder
     */
    public ReplyMessageChannelHolder getReplyMessageChannelHolder() {
        return replyMessageChannelHolder;
    }

    /**
     * Set the message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        this.correlator = correlator;
    }

    /**
     * Set the messaging template.
     * @param messagingTemplate the messagingTemplate to set
     */
    public void setMessagingTemplate(MessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Gets the messagingTemplate.
     * @return the messagingTemplate
     */
    public MessagingTemplate getMessagingTemplate() {
        return messagingTemplate;
    }

    /**
     * Gets the correlator.
     * @return the correlator
     */
    public ReplyMessageCorrelator getCorrelator() {
        return correlator;
    }
}
