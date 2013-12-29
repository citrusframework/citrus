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
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.message.AbstractMessageReceiver;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.messaging.Producer;
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
    private ChannelEndpoint channelEndpoint;

    /**
     * Default constructor.
     */
    public MessageChannelReceiver() {
        this(new ChannelEndpoint());
    }

    /**
     * Default constructor using message endpoint.
     * @param channelEndpoint
     */
    public MessageChannelReceiver(ChannelEndpoint channelEndpoint) {
        super(channelEndpoint);
        this.channelEndpoint = channelEndpoint;
    }

    @Override
    public Consumer createConsumer() {
        return channelEndpoint.createConsumer();
    }

    @Override
    public Producer createProducer() {
        return channelEndpoint.createProducer();
    }

    @Override
    public EndpointConfiguration getEndpointConfiguration() {
        return channelEndpoint.getEndpointConfiguration();
    }

    /**
     * Gets the message endpoint.
     * @return
     */
    public ChannelEndpoint getChannelEndpoint() {
        return channelEndpoint;
    }

    /**
     * Sets the message endpoint.
     * @param channelEndpoint
     */
    public void setChannelEndpoint(ChannelEndpoint channelEndpoint) {
        this.channelEndpoint = channelEndpoint;
    }

    /**
     * @see MessageReceiver#receive(long)
     * @throws ActionTimeoutException
     */
    @Override
    public Message<?> receive(long timeout) {
        return channelEndpoint.createConsumer().receive(timeout);
    }

    /**
     * @see MessageReceiver#receiveSelected(String, long)
     */
    @Override
    public Message<?> receiveSelected(String selector, long timeout) {
        return channelEndpoint.createConsumer().receive(selector, timeout);
    }

    /**
     * Set the target channel to receive message from.
     * @param channel the channel to set
     */
    public void setChannel(PollableChannel channel) {
        channelEndpoint.setChannel(channel);
    }

    /**
     * Set the messaging template.
     * @param messagingTemplate the messagingTemplate to set
     */
    public void setMessagingTemplate(MessagingTemplate messagingTemplate) {
        channelEndpoint.setMessagingTemplate(messagingTemplate);
    }
    
    /**
     * Sets the bean factory for channel resolver.
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        channelEndpoint.setBeanFactory(beanFactory);
    }
    
    /**
     * Sets the destination channel name.
     * @param channelName the channelName to set
     */
    public void setChannelName(String channelName) {
        channelEndpoint.setChannelName(channelName);
    }
    
    /**
     * Set the channel resolver.
     * @param channelResolver the channelResolver to set
     */
    public void setChannelResolver(ChannelResolver channelResolver) {
        channelEndpoint.setChannelResolver(channelResolver);
    }

    /**
     * Gets the channel.
     * @return the channel
     */
    public PollableChannel getChannel() {
        return (PollableChannel) channelEndpoint.getChannel();
    }

    /**
     * Gets the channelName.
     * @return the channelName
     */
    public String getChannelName() {
        return channelEndpoint.getChannelName();
    }

    /**
     * Gets the messagingTemplate.
     * @return the messagingTemplate
     */
    public MessagingTemplate getMessagingTemplate() {
        return channelEndpoint.getMessagingTemplate();
    }

    /**
     * Gets the channelResolver.
     * @return the channelResolver
     */
    public ChannelResolver getChannelResolver() {
        return channelEndpoint.getChannelResolver();
    }

    /**
     * Setter for receive timeout.
     * @param receiveTimeout the receiveTimeout to set
     */
    public void setReceiveTimeout(long receiveTimeout) {
        super.setReceiveTimeout(receiveTimeout);
        channelEndpoint.setTimeout(receiveTimeout);
    }

    /**
     * Gets the receiveTimeout.
     * @return the receiveTimeout
     */
    public long getReceiveTimeout() {
        return channelEndpoint.getTimeout();
    }

    /**
     * Gets the actor.
     * @return the actor the actor to get.
     */
    public TestActor getActor() {
        return channelEndpoint.getActor();
    }

    /**
     * Sets the actor.
     * @param actor the actor to set
     */
    public void setActor(TestActor actor) {
        super.setActor(actor);
        channelEndpoint.setActor(actor);
    }

    @Override
    public void setBeanName(String name) {
        super.setBeanName(name);
        channelEndpoint.setBeanName(name);
    }

    @Override
    public String getName() {
        return channelEndpoint.getName();
    }

}
