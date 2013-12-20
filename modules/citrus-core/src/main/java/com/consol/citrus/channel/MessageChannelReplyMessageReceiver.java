/*
 * Copyright 2006-2013 the original author or authors.
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

import com.consol.citrus.TestActor;
import com.consol.citrus.message.ReplyMessageReceiver;
import org.springframework.integration.Message;

/**
 * @author Christoph Deppisch
 * @since 1.4
 * @deprecated
 */
public class MessageChannelReplyMessageReceiver extends ReplyMessageReceiver {

    /** Synchronous endpoint providing the reply messages */
    private ChannelSyncEndpoint messageChannelEndpoint;

    /**
     * Default constructor.
     */
    public MessageChannelReplyMessageReceiver() {
        this.messageChannelEndpoint  = new ChannelSyncEndpoint();
    }

    /**
     * Default constructor with Jms endpoint.
     * @param messageChannelEndpoint
     */
    protected MessageChannelReplyMessageReceiver(ChannelSyncEndpoint messageChannelEndpoint) {
        this.messageChannelEndpoint = messageChannelEndpoint;
    }

    /**
     * Gets the message channel endpoint.
     * @return
     */
    public ChannelSyncEndpoint getMessageChannelEndpoint() {
        return messageChannelEndpoint;
    }

    /**
     * Sets the message channel endpoint
     * @param messageChannelEndpoint
     */
    public void setMessageChannelEndpoint(ChannelSyncEndpoint messageChannelEndpoint) {
        this.messageChannelEndpoint = messageChannelEndpoint;
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receive()
     */
    public Message<?> receive() {
        return messageChannelEndpoint.createConsumer().receive("", messageChannelEndpoint.getTimeout());
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receive(long)
     */
    public Message<?> receive(long timeout) {
        return messageChannelEndpoint.createConsumer().receive("", timeout);
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receiveSelected(java.lang.String)
     */
    public Message<?> receiveSelected(String selector) {
        return messageChannelEndpoint.createConsumer().receive(selector, messageChannelEndpoint.getTimeout());
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receiveSelected(java.lang.String, long)
     */
    public Message<?> receiveSelected(String selector, long timeout) {
        return messageChannelEndpoint.createConsumer().receive(selector, timeout);
    }

    /**
     * @see com.consol.citrus.message.ReplyMessageHandler#onReplyMessage(org.springframework.integration.Message, java.lang.String)
     */
    public void onReplyMessage(Message<?> replyMessage, String correlationKey) {
        ((ChannelSyncProducer) messageChannelEndpoint.createConsumer()).onReplyMessage(correlationKey, replyMessage);
    }

    /**
     * @see com.consol.citrus.message.ReplyMessageHandler#onReplyMessage(org.springframework.integration.Message)
     */
    public void onReplyMessage(Message<?> replyMessage) {
        ((ChannelSyncProducer) messageChannelEndpoint.createConsumer()).onReplyMessage("", replyMessage);
    }

    /**
     * Gets the actor.
     * @return the actor the actor to get.
     */
    public TestActor getActor() {
        return messageChannelEndpoint.getActor();
    }

    /**
     * Sets the actor.
     * @param actor the actor to set
     */
    public void setActor(TestActor actor) {
        messageChannelEndpoint.setActor(actor);
    }

    /**
     * Gets the pollingInterval.
     * @return the pollingInterval the pollingInterval to get.
     */
    public long getPollingInterval() {
        return messageChannelEndpoint.getPollingInterval();
    }

    /**
     * Sets the pollingInterval.
     * @param pollingInterval the pollingInterval to set
     */
    public void setPollingInterval(long pollingInterval) {
        messageChannelEndpoint.setPollingInterval(pollingInterval);
    }

    @Override
    public void setBeanName(String name) {
        messageChannelEndpoint.setBeanName(name);
    }

    @Override
    public String getName() {
        return messageChannelEndpoint.getName();
    }
}
