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

import java.util.HashMap;
import java.util.Map;

import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;

import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.message.ReplyMessageCorrelator;

/**
 * Synchronous message channel receiver. Receives a message on a {@link MessageChannel} destination and
 * saves the reply channel. A {@link ReplyMessageChannelSender} may ask for the reply channel in order to
 * provide synchronous reply.
 * 
 * @author Christoph Deppisch
 */
public class SyncMessageChannelReceiver extends MessageChannelReceiver implements ReplyMessageChannelHolder {
    /** Reply channel store */
    private Map<String, MessageChannel> replyChannels = new HashMap<String, MessageChannel>();
    
    /** Reply message correlator */
    private ReplyMessageCorrelator correlator = null;
    
    /**
     * @see MessageReceiver#receive(long)
     */
    @Override
    public Message<?> receive(long timeout) {
        Message<?> receivedMessage = super.receive(timeout);
        
        saveReplyMessageChannel(receivedMessage);
        
        return receivedMessage;
    }

    /**
     * @see MessageReceiver#receiveSelected(String, long)
     */
    @Override
    public Message<?> receiveSelected(String selector, long timeout) {
        Message<?> receivedMessage = super.receiveSelected(selector, timeout);
        
        saveReplyMessageChannel(receivedMessage);
        
        return receivedMessage;
    }
    
    /**
     * Store reply message channel.
     * @param receivedMessage
     */
    private void saveReplyMessageChannel(Message<?> receivedMessage) {
        if(correlator != null) {
            replyChannels.put(correlator.getCorrelationKey(receivedMessage), (MessageChannel)receivedMessage.getHeaders().getReplyChannel());
        } else {
            replyChannels.put("", (MessageChannel)receivedMessage.getHeaders().getReplyChannel());
        }
    }

    /**
     * Get the reply message channel with given corelation key.
     */
    public MessageChannel getReplyMessageChannel(String correlationKey) {
        return replyChannels.remove(correlationKey);
    }

    /**
     * Get the reply message channel.
     */
    public MessageChannel getReplyMessageChannel() {
        return replyChannels.remove("");
    }

    /**
     * Set the reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        this.correlator = correlator;
    }
}
