/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.channel.MessageChannelTemplate;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.*;

/**
 * Synchronous message channel sender. After sending message action will listen for reply message. A
 * {@link ReplyMessageHandler} may ask for this reply message and continue with message validation.
 * 
 * @author Christoph Deppisch
 */
public class SyncMessageChannelSender implements MessageSender {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(SyncMessageChannelSender.class);
    
    /** Message channel */
    private MessageChannel channel;
    
    /** Message channel template */
    private MessageChannelTemplate messageChannelTemplate = new MessageChannelTemplate();
    
    /** Reply message handler */
    private ReplyMessageHandler replyMessageHandler;
    
    /** Time to wait for reply message to arrive */
    private long replyTimeout = 5000L;
    
    /** Reply message correlator */
    private ReplyMessageCorrelator correlator = null;
    
    /**
     * @see MessageSender#send(Message)
     * @throws CitrusRuntimeException
     */
    public void send(Message<?> message) {
        log.info("Sending message to: " + channel.getName());

        if (log.isDebugEnabled()) {
            log.debug("Message to be sent:");
            log.debug(message.toString());
        }

        messageChannelTemplate.setReceiveTimeout(replyTimeout);
        Message<?> replyMessage = messageChannelTemplate.sendAndReceive(message, channel);
        
        if(replyMessage == null) {
            throw new CitrusRuntimeException("Failed to send message to channel" + channel.getName());
        }
        
        if(replyMessageHandler != null) {
            if(correlator != null) {
                replyMessageHandler.onReplyMessage(replyMessage,
                    correlator.getCorrelationKey(message));
            } else {
                replyMessageHandler.onReplyMessage(replyMessage);
            }
        }
    }

    /**
     * Set the reply message handler
     * @param replyMessageHandler the replyMessageHandler to set
     */
    public void setReplyMessageHandler(ReplyMessageHandler replyMessageHandler) {
        this.replyMessageHandler = replyMessageHandler;
    }

    /**
     * Get the reply message handler.
     * @return the replyMessageHandler
     */
    public ReplyMessageHandler getReplyMessageHandler() {
        return replyMessageHandler;
    }

    /**
     * Set the reply timeout.
     * @param replyTimeout the replyTimeout to set
     */
    public void setReplyTimeout(long replyTimeout) {
        this.replyTimeout = replyTimeout;
    }

    /**
     * Get the reply timeout.
     * @return the replyTimeout
     */
    public long getReplyTimeout() {
        return replyTimeout;
    }

    /**
     * Set the reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        this.correlator = correlator;
    }

    /**
     * Get the reply message correlator.
     * @return the correlator
     */
    public ReplyMessageCorrelator getCorrelator() {
        return correlator;
    }
}
