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

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.*;

/**
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 */
public class SyncMessageChannelSender implements MessageSender {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(SyncMessageChannelSender.class);
    
    private MessageChannel channel;
    
    private MessageChannelTemplate messageChannelTemplate = new MessageChannelTemplate();
    
    private ReplyMessageHandler replyMessageHandler;
    
    private long replyTimeout = 5000L;
    
    private ReplyMessageCorrelator correlator = null;
    
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
     * @param replyMessageHandler the replyMessageHandler to set
     */
    public void setReplyMessageHandler(ReplyMessageHandler replyMessageHandler) {
        this.replyMessageHandler = replyMessageHandler;
    }

    /**
     * @return the replyMessageHandler
     */
    public ReplyMessageHandler getReplyMessageHandler() {
        return replyMessageHandler;
    }

    /**
     * @param replyTimeout the replyTimeout to set
     */
    public void setReplyTimeout(long replyTimeout) {
        this.replyTimeout = replyTimeout;
    }

    /**
     * @return the replyTimeout
     */
    public long getReplyTimeout() {
        return replyTimeout;
    }

    /**
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        this.correlator = correlator;
    }

    /**
     * @return the correlator
     */
    public ReplyMessageCorrelator getCorrelator() {
        return correlator;
    }
}
