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
import org.springframework.integration.channel.PollableChannel;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;

import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.message.AbstractMessageReceiver;
import com.consol.citrus.message.MessageReceiver;

/**
 * Receive messages from {@link MessageChannel} instance.
 * @author Christoph Christoph
 */
public class MessageChannelReceiver extends AbstractMessageReceiver {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(MessageChannelReceiver.class);
    
    /** Pollable channel */
    private PollableChannel channel;
    
    /** Message channel template */
    private MessageChannelTemplate messageChannelTemplate = new MessageChannelTemplate();
    
    /**
     * @see MessageReceiver#receive(long)
     * @throws ActionTimeoutException
     */
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

    /**
     * @see MessageReceiver#receiveSelected(String, long)
     */
    @Override
    public Message<?> receiveSelected(String selector, long timeout) {
        throw new UnsupportedOperationException("MessageChannelTemplate " +
        		"does not support selected receiving.");
    }

    /**
     * Set the target channel to receive message from.
     * @param channel the channel to set
     */
    public void setChannel(PollableChannel channel) {
        this.channel = channel;
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
