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
import org.springframework.integration.channel.PollableChannel;
import org.springframework.integration.core.Message;

import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.message.AbstractMessageReceiver;

/**
 * @author Christoph Christoph Deppisch Consol* Software GmbH
 */
public class MessageChannelReceiver extends AbstractMessageReceiver {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(MessageChannelReceiver.class);
    
    private PollableChannel channel;
    
    private MessageChannelTemplate messageChannelTemplate = new MessageChannelTemplate();
    
    @Override
    public Message<?> receive(long timeout) {
        log.info("Receiving message from: " + channel.getName());
        
        messageChannelTemplate.setReceiveTimeout(timeout);
        Message<?> received = messageChannelTemplate.receive(channel);
        
        if(received == null) {
            throw new ActionTimeoutException("Action timeout while receiving message from channel '"
                    + channel.getName() + "'");
        }
        
        return received;
    }

    @Override
    public Message<?> receiveSelected(String selector, long timeout) {
        throw new UnsupportedOperationException("MessageChannelTemplate " +
        		"does not support selected receiving.");
    }

    /**
     * @param channel the channel to set
     */
    public void setChannel(PollableChannel channel) {
        this.channel = channel;
    }

    /**
     * @param messageChannelTemplate the messageChannelTemplate to set
     */
    public void setMessageChannelTemplate(
            MessageChannelTemplate messageChannelTemplate) {
        this.messageChannelTemplate = messageChannelTemplate;
    }
}
