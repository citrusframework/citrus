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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.util.StringUtils;

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
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(SyncMessageChannelReceiver.class);
    
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
     * Store reply message channel.
     * @param receivedMessage
     */
    private void saveReplyMessageChannel(Message<?> receivedMessage) {
        MessageChannel replyChannel;
        
        if(receivedMessage.getHeaders().getReplyChannel() instanceof MessageChannel) {
            replyChannel = (MessageChannel)receivedMessage.getHeaders().getReplyChannel();
        } else if(StringUtils.hasText((String)receivedMessage.getHeaders().getReplyChannel())){
            replyChannel = resolveChannelName(receivedMessage.getHeaders().getReplyChannel().toString());
        } else {
            log.warn("Unable to retrieve reply message channel for message \n" + 
                    receivedMessage + "\n - no reply channel found in message headers!");
            return;
        }
        
        if(correlator != null) {
            replyChannels.put(correlator.getCorrelationKey(receivedMessage), replyChannel);
        } else {
            replyChannels.put("", replyChannel);
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
