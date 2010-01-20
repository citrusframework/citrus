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

import com.consol.citrus.message.ReplyMessageCorrelator;

/**
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 */
public class SyncMessageChannelReceiver extends MessageChannelReceiver implements ReplyMessageChannelHolder {

    private Map<String, MessageChannel> replyChannels = new HashMap<String, MessageChannel>();
    
    private ReplyMessageCorrelator correlator = null;
    
    @Override
    public Message<?> receive(long timeout) {
        Message<?> receivedMessage = super.receive(timeout);
        
        saveReplyMessageChannel(receivedMessage);
        
        return receivedMessage;
    }

    @Override
    public Message<?> receiveSelected(String selector, long timeout) {
        Message<?> receivedMessage = super.receiveSelected(selector, timeout);
        
        saveReplyMessageChannel(receivedMessage);
        
        return receivedMessage;
    }
    
    /**
     * @param receivedMessage
     */
    private void saveReplyMessageChannel(Message<?> receivedMessage) {
        if(correlator != null) {
            replyChannels.put(correlator.getCorrelationKey(receivedMessage), (MessageChannel)receivedMessage.getHeaders().getReplyChannel());
        } else {
            replyChannels.put("", (MessageChannel)receivedMessage.getHeaders().getReplyChannel());
        }
    }

    public MessageChannel getReplyMessageChannel(String correlationKey) {
        return replyChannels.remove(correlationKey);
    }

    public MessageChannel getReplyMessageChannel() {
        return replyChannels.remove("");
    }

    /**
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        this.correlator = correlator;
    }
}
