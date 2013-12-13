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
import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.message.AbstractMessageReceiver;
import com.consol.citrus.message.MessageReceiver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.integration.Message;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.core.PollableChannel;
import org.springframework.integration.support.channel.ChannelResolver;

/**
 * Receive messages from Spring message channel instance.
 * @author Christoph Christoph
 * @deprecated
 */
public class MessageChannelReceiver extends AbstractMessageReceiver implements BeanFactoryAware {

    /** New message channel endpoint */
    private MessageChannelEndpoint messageChannelEndpoint;

    /**
     * Default constructor.
     */
    public MessageChannelReceiver() {
        this.messageChannelEndpoint = new MessageChannelEndpoint();
    }

    /**
     * Default constructor using message endpoint.
     * @param messageChannelEndpoint
     */
    public MessageChannelReceiver(MessageChannelEndpoint messageChannelEndpoint) {
        this.messageChannelEndpoint = messageChannelEndpoint;
    }

    /**
     * Gets the message endpoint.
     * @return
     */
    public MessageChannelEndpoint getMessageChannelEndpoint() {
        return messageChannelEndpoint;
    }

    /**
     * Sets the message endpoint.
     * @param messageChannelEndpoint
     */
    public void setMessageChannelEndpoint(MessageChannelEndpoint messageChannelEndpoint) {
        this.messageChannelEndpoint = messageChannelEndpoint;
    }

    /**
     * @see MessageReceiver#receive(long)
     * @throws ActionTimeoutException
     */
    @Override
    public Message<?> receive(long timeout) {
        return messageChannelEndpoint.receive(timeout);
    }

    /**
     * @see MessageReceiver#receiveSelected(String, long)
     */
    @Override
    public Message<?> receiveSelected(String selector, long timeout) {
        return messageChannelEndpoint.receive(selector, timeout);
    }

    /**
     * Set the target channel to receive message from.
     * @param channel the channel to set
     */
    public void setChannel(PollableChannel channel) {
        messageChannelEndpoint.setChannel(channel);
    }

    /**
     * Set the messaging template.
     * @param messagingTemplate the messagingTemplate to set
     */
    public void setMessagingTemplate(MessagingTemplate messagingTemplate) {
        messageChannelEndpoint.setMessagingTemplate(messagingTemplate);
    }
    
    /**
     * Sets the bean factory for channel resolver.
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        messageChannelEndpoint.setBeanFactory(beanFactory);
    }
    
    /**
     * Sets the destination channel name.
     * @param channelName the channelName to set
     */
    public void setChannelName(String channelName) {
        messageChannelEndpoint.setChannelName(channelName);
    }
    
    /**
     * Set the channel resolver.
     * @param channelResolver the channelResolver to set
     */
    public void setChannelResolver(ChannelResolver channelResolver) {
        messageChannelEndpoint.setChannelResolver(channelResolver);
    }

    /**
     * Gets the channel.
     * @return the channel
     */
    public PollableChannel getChannel() {
        return (PollableChannel) messageChannelEndpoint.getChannel();
    }

    /**
     * Gets the channelName.
     * @return the channelName
     */
    public String getChannelName() {
        return messageChannelEndpoint.getChannelName();
    }

    /**
     * Gets the messagingTemplate.
     * @return the messagingTemplate
     */
    public MessagingTemplate getMessagingTemplate() {
        return messageChannelEndpoint.getMessagingTemplate();
    }

    /**
     * Gets the channelResolver.
     * @return the channelResolver
     */
    public ChannelResolver getChannelResolver() {
        return messageChannelEndpoint.getChannelResolver();
    }

    /**
     * Setter for receive timeout.
     * @param receiveTimeout the receiveTimeout to set
     */
    public void setReceiveTimeout(long receiveTimeout) {
        super.setReceiveTimeout(receiveTimeout);
        messageChannelEndpoint.setTimeout(receiveTimeout);
    }

    /**
     * Gets the receiveTimeout.
     * @return the receiveTimeout
     */
    public long getReceiveTimeout() {
        return messageChannelEndpoint.getTimeout();
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
        super.setActor(actor);
        messageChannelEndpoint.setActor(actor);
    }

    @Override
    public void setBeanName(String name) {
        super.setBeanName(name);
        messageChannelEndpoint.setBeanName(name);
    }

    @Override
    public String getName() {
        return messageChannelEndpoint.getName();
    }

}
