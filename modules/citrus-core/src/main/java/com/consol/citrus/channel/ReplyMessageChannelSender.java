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

package com.consol.citrus.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.channel.MessageChannelTemplate;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.util.Assert;

import com.consol.citrus.message.*;

/**
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 */
public class ReplyMessageChannelSender implements MessageSender {
    
    private ReplyMessageChannelHolder replyMessageChannelHolder;

    private MessageChannelTemplate messageChannelTemplate = new MessageChannelTemplate();

    private ReplyMessageCorrelator correlator = null;
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(ReplyMessageChannelSender.class);
    
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
        
        messageChannelTemplate.send(message, replyChannel);
    }
    
    /**
     * @param replyMessageChannelHolder the replyMessageChannelHolder to set
     */
    public void setReplyMessageChannelHolder(ReplyMessageChannelHolder replyMessageChannelHolder) {
        this.replyMessageChannelHolder = replyMessageChannelHolder;
    }

    /**
     * @return the replyMessageChannelHolder
     */
    public ReplyMessageChannelHolder getReplyMessageChannelHolder() {
        return replyMessageChannelHolder;
    }

    /**
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        this.correlator = correlator;
    }
}
