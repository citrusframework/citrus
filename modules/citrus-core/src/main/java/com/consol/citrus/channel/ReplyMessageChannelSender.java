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
import org.springframework.integration.channel.MessageChannelTemplate;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;
import org.springframework.integration.message.MessageBuilder;
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
    private MessageChannelTemplate messageChannelTemplate = new MessageChannelTemplate();

    /** Reply message correlator */
    private ReplyMessageCorrelator correlator = null;
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(ReplyMessageChannelSender.class);
    
    /**
     * @see MessageSender#send(Message)
     */
    public void send(Message<?> message) {
        Assert.notNull(message, "Can not send empty message");
        
        MessageChannel replyChannel;
        
        if(correlator != null) {
            Assert.notNull(message.getHeaders().get(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR), "Can not correlate reply destination - " +
                    "you need to set " + CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR + " in message header");
            
            replyChannel = replyMessageChannelHolder.getReplyMessageChannel(correlator.getCorrelationKey(message.getHeaders().get(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR).toString()));
            
            //remove citrus specific header from message
            message = MessageBuilder.fromMessage(message).removeHeader(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR).build();
        } else {
            replyChannel = replyMessageChannelHolder.getReplyMessageChannel();
        }
        
        Assert.notNull(replyChannel, "Not able to find temporary reply channel");
        
        log.info("Sending message to: " + replyChannel.getName());

        if (log.isDebugEnabled()) {
            log.debug("Message to be sent:");
            log.debug(message.toString());
        }
        
        if(!messageChannelTemplate.send(message, replyChannel)) {
            throw new CitrusRuntimeException("Failed to send message to channel '" + replyChannel.getName() + "'");
        }
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
     * Set the message channel template.
     * @param messageChannelTemplate the messageChannelTemplate to set
     */
    public void setMessageChannelTemplate(
            MessageChannelTemplate messageChannelTemplate) {
        this.messageChannelTemplate = messageChannelTemplate;
    }
}
