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

import com.consol.citrus.TestActor;
import com.consol.citrus.message.MessageSender;
import com.consol.citrus.message.ReplyMessageCorrelator;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.integration.Message;
import org.springframework.integration.core.MessagingTemplate;

/**
 * Send reply messages to channel destinations.
 * 
 * @author Christoph Deppisch
 * @deprecated
 */
public class ReplyMessageChannelSender implements MessageSender, BeanNameAware {
    
    /** Holding dynamic reply channel  */
    private ReplyMessageChannelHolder replyMessageChannelHolder;

    /** New message channel endpoint */
    private ChannelSyncEndpoint messageChannelEndpoint;

    /**
     * Default constructor.
     */
    public ReplyMessageChannelSender() {
        this.messageChannelEndpoint = new ChannelSyncEndpoint();
    }

    /**
     * Default constructor using message endpoint.
     * @param messageChannelEndpoint
     */
    public ReplyMessageChannelSender(ChannelSyncEndpoint messageChannelEndpoint) {
        this.messageChannelEndpoint = messageChannelEndpoint;
    }

    /**
     * Gets the message endpoint.
     * @return
     */
    public ChannelSyncEndpoint getMessageChannelEndpoint() {
        return messageChannelEndpoint;
    }

    /**
     * Sets the message endpoint.
     * @param messageChannelEndpoint
     */
    public void setMessageChannelEndpoint(ChannelSyncEndpoint messageChannelEndpoint) {
        this.messageChannelEndpoint = messageChannelEndpoint;
    }

    /**
     * @see com.consol.citrus.message.MessageSender#send(org.springframework.integration.Message)
     */
    public void send(Message<?> message) {
        ((ChannelSyncConsumer)messageChannelEndpoint.createConsumer()).send(message);
    }
    
    /**
     * Set the reply message holder.
     * @param replyMessageChannelHolder the replyMessageChannelHolder to set
     */
    public void setReplyMessageChannelHolder(ReplyMessageChannelHolder replyMessageChannelHolder) {
        this.replyMessageChannelHolder = replyMessageChannelHolder;

        if (replyMessageChannelHolder instanceof SyncMessageChannelReceiver) {
            setMessageChannelEndpoint(((SyncMessageChannelReceiver) replyMessageChannelHolder).getMessageChannelEndpoint());
        }
    }

    /**
     * Get the reply message holder.
     * @return the replyMessageChannelHolder
     */
    public ReplyMessageChannelHolder getReplyMessageChannelHolder() {
        return replyMessageChannelHolder;
    }

    /**
     * Set the message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        messageChannelEndpoint.setCorrelator(correlator);
    }

    /**
     * Set the messaging template.
     * @param messagingTemplate the messagingTemplate to set
     */
    public void setMessagingTemplate(MessagingTemplate messagingTemplate) {
        messageChannelEndpoint.setMessagingTemplate(messagingTemplate);
    }

    /**
     * Gets the messagingTemplate.
     * @return the messagingTemplate
     */
    public MessagingTemplate getMessagingTemplate() {
        return messageChannelEndpoint.getMessagingTemplate();
    }

    /**
     * Gets the correlator.
     * @return the correlator
     */
    public ReplyMessageCorrelator getCorrelator() {
        return messageChannelEndpoint.getCorrelator();
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

    @Override
    public void setBeanName(String name) {
        messageChannelEndpoint.setBeanName(name);
    }

    @Override
    public String getName() {
        return messageChannelEndpoint.getName();
    }
}
