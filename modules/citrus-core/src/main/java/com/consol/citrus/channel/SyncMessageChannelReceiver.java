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

import com.consol.citrus.message.ReplyMessageCorrelator;
import com.consol.citrus.messaging.ReplyProducer;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;

/**
 * Synchronous message channel receiver. Receives a message on a {@link MessageChannel} destination and
 * saves the reply channel. A {@link ReplyMessageChannelSender} may ask for the reply channel in order to
 * provide synchronous reply.
 * 
 * @author Christoph Deppisch
 * @deprecated
 */
public class SyncMessageChannelReceiver extends MessageChannelReceiver implements ReplyMessageChannelHolder {

    public SyncMessageChannelReceiver() {
        super(new MessageChannelSyncEndpoint());
    }

    @Override
    public MessageChannelSyncEndpoint getMessageChannelEndpoint() {
        return (MessageChannelSyncEndpoint) super.getMessageChannelEndpoint();
    }

    @Override
    public Message<?> receive(long timeout) {
        return getMessageChannelEndpoint().createConsumer().receive(timeout);
    }

    @Override
    public Message<?> receiveSelected(String selector, long timeout) {
        return getMessageChannelEndpoint().createConsumer().receive(selector, timeout);
    }
    
    /**
     * Get the reply message channel with given corelation key.
     */
    public MessageChannel getReplyMessageChannel(String correlationKey) {
        return ((MessageChannelSyncConsumer) getMessageChannelEndpoint().createConsumer()).findReplyChannel(correlationKey);
    }

    /**
     * Get the reply message channel.
     */
    public MessageChannel getReplyMessageChannel() {
        return ((MessageChannelSyncConsumer) getMessageChannelEndpoint().createConsumer()).findReplyChannel("");
    }

    /**
     * Set the reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        getMessageChannelEndpoint().setCorrelator(correlator);
    }

    /**
     * Gets the correlator.
     * @return the correlator
     */
    public ReplyMessageCorrelator getCorrelator() {
        return getMessageChannelEndpoint().getCorrelator();
    }
    
}
