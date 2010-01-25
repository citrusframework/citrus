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

package com.consol.citrus.adapter.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.channel.MessageChannelTemplate;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageHandler;

/**
 * This message handler will forward incoming requests to a Spring Integration
 * message channel. The handler will listen on a reply channel destination for a
 * proper response to return.
 * 
 * In case no reply message is received a fallback message handler can provide a
 * default response message.
 * 
 * @author Christoph Deppisch
 */
public class MessageChannelConnectingMessageHandler implements MessageHandler {

    /** Forwarding message channel */
    private MessageChannel channel;
    
    /** Forwarding channel name */
    private String channelName;

    /** Time to wait for reply message to arrive */
    private long replyTimeout = 5000L;
    
    /** Spring's messasge channel template */
    private MessageChannelTemplate messageChannelTemplate = new MessageChannelTemplate();
    
    /** Fallback message handler */
    private MessageHandler fallbackMessageHandlerDelegate = null;
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(MessageChannelConnectingMessageHandler.class);

    /**
     * @see com.consol.citrus.message.MessageHandler#handleMessage(org.springframework.integration.core.Message)
     * @throws CitrusRuntimeException
     */
    public Message<?> handleMessage(final Message<?> request) {
        log.info("Forwarding request to: " + getChannelName());

        if(log.isDebugEnabled()) {
            log.debug("Message is: " + request.getPayload());
        }

        Message<?> replyMessage = null;
        
        messageChannelTemplate.setReceiveTimeout(replyTimeout);
        replyMessage = messageChannelTemplate.sendAndReceive(request);
        
        if((replyMessage == null || replyMessage.getPayload() == null)) {
            if(fallbackMessageHandlerDelegate != null) {
                log.info("Did not receive reply message - "
                        + "delegating to fallback message handler for response generation");
                
                replyMessage = fallbackMessageHandlerDelegate.handleMessage(request);
            } else {
                log.info("Did not receive reply message - no response is simulated");
            }
        }
        
        return replyMessage;
    }
    
    /**
     * Get the channel name.
     * @return the channelName
     */
    protected String getChannelName() {
        if(channel != null) {
            return channel.getName();
        } else {
            return channelName;
        }
    }
    
    /**
     * Set the reply timeout.
     * @param replyTimeout the replyTimeout to set
     */
    public void setReplyTimeout(long replyTimeout) {
        this.replyTimeout = replyTimeout;
    }

    /**
     * Set the fallback message handler.
     * @param fallbackMessageHandlerDelegate the fallbackMessageHandlerDelegate to set
     */
    public void setFallbackMessageHandlerDelegate(MessageHandler fallbackMessageHandlerDelegate) {
        this.fallbackMessageHandlerDelegate = fallbackMessageHandlerDelegate;
    }

    /**
     * Set the message channel.
     * @param channel the channel to set
     */
    public void setChannel(MessageChannel channel) {
        this.channel = channel;
    }

    /**
     * Set the message channel name.
     * @param channelName the channelName to set
     */
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    /**
     * Set the message channel template.
     * @param messageChannelTemplate the messageChannelTemplate to set
     */
    public void setMessageChannelTemplate(MessageChannelTemplate messageChannelTemplate) {
        this.messageChannelTemplate = messageChannelTemplate;
    }
}
