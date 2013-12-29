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

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.ReplyMessageCorrelator;
import com.consol.citrus.message.ReplyMessageHandler;
import org.springframework.integration.Message;

/**
 * Synchronous message channel sender. After sending message action will listen for reply message. A
 * {@link ReplyMessageHandler} may ask for this reply message and continue with message validation.
 * 
 * @author Christoph Deppisch
 * @deprecated
 */
public class SyncMessageChannelSender extends MessageChannelSender {

    /** Reply message handler */
    private ReplyMessageHandler replyMessageHandler;

    /**
     * Default constructor initializing endpoint.
     */
    public SyncMessageChannelSender() {
        super(new ChannelSyncEndpoint());
    }

    @Override
    public ChannelSyncEndpoint getChannelEndpoint() {
        return (ChannelSyncEndpoint) super.getChannelEndpoint();
    }

    /**
     * @see com.consol.citrus.message.MessageSender#send(org.springframework.integration.Message)
     * @throws CitrusRuntimeException
     */
    public void send(Message<?> message) {
        getChannelEndpoint().createProducer().send(message);
    }
    
    /**
     * Set the reply message handler
     * @param replyMessageHandler the replyMessageHandler to set
     */
    public void setReplyMessageHandler(ReplyMessageHandler replyMessageHandler) {
        this.replyMessageHandler = replyMessageHandler;

        if (replyMessageHandler instanceof MessageChannelReplyMessageReceiver) {
            ((MessageChannelReplyMessageReceiver) replyMessageHandler).setEndpoint(getChannelEndpoint());
        }
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
        getChannelEndpoint().getEndpointConfiguration().setTimeout(replyTimeout);
    }

    /**
     * Get the reply timeout.
     * @return the replyTimeout
     */
    public long getReplyTimeout() {
        return getChannelEndpoint().getEndpointConfiguration().getTimeout();
    }

    /**
     * Set the reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        getChannelEndpoint().getEndpointConfiguration().setCorrelator(correlator);
    }

    /**
     * Get the reply message correlator.
     * @return the correlator
     */
    public ReplyMessageCorrelator getCorrelator() {
        return getChannelEndpoint().getEndpointConfiguration().getCorrelator();
    }
    
}
