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
import com.consol.citrus.message.MessageSender;

/**
 * Publish message to a {@link MessageChannel}.
 * 
 * @author Christoph Christoph
 */
public class MessageChannelSender implements MessageSender {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(MessageChannelSender.class);
    
    /** Destination channel */
    private MessageChannel channel;
    
    /** Message channel template */
    private MessageChannelTemplate messageChannelTemplate = new MessageChannelTemplate();
    
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
        
        if(!messageChannelTemplate.send(message, channel)) {
            throw new CitrusRuntimeException("Failed to send message to channel" + channel.getName());
        }
    }

    /**
     * Set the message channel.
     * @param channel the channel to set
     */
    public void setChannel(MessageChannel channel) {
        this.channel = channel;
    }

    /**
     * Sets the message channel template.
     * @param messageChannelTemplate the messageChannelTemplate to set
     */
    public void setMessageChannelTemplate(
            MessageChannelTemplate messageChannelTemplate) {
        this.messageChannelTemplate = messageChannelTemplate;
    }
}
