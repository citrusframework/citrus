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
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.messaging.Producer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.channel.ChannelResolver;

/**
 * Publish message to a {@link MessageChannel}.
 * 
 * @author Christoph Christoph
 * @deprecated since Citrus 1.4, in favor of {@link com.consol.citrus.channel.ChannelEndpoint}
 */
@Deprecated
public class MessageChannelSender implements MessageSender, BeanFactoryAware, BeanNameAware {

    /** New message channel endpoint */
    private ChannelEndpoint channelEndpoint;

    /**
     * Default constructor.
     */
    public MessageChannelSender() {
        this.channelEndpoint = new ChannelEndpoint();
    }

    /**
     * Default constructor using message endpoint.
     * @param channelEndpoint
     */
    public MessageChannelSender(ChannelEndpoint channelEndpoint) {
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
    public ChannelEndpointConfiguration getEndpointConfiguration() {
        return channelEndpoint.getEndpointConfiguration();
    }

    /**
     * Gets the message endpoint.
     * @return
     */
    public ChannelEndpoint getChannelEndpoint() {
        return channelEndpoint;
    }

    @Override
    public void send(Message<?> message) {
        channelEndpoint.createProducer().send(message);
    }

    /**
     * Set the message channel.
     * @param channel the channel to set
     */
    public void setChannel(MessageChannel channel) {
        getEndpointConfiguration().setChannel(channel);
    }

    /**
     * Sets the messaging template.
     * @param messagingTemplate the messagingTemplate to set
     */
    public void setMessagingTemplate(MessagingTemplate messagingTemplate) {
        getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
    }

    /**
     * Sets the bean factory for channel resolver.
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        channelEndpoint.setBeanFactory(beanFactory);
    }

    /**
     * Set the channel resolver.
     * @param channelResolver the channelResolver to set
     */
    public void setChannelResolver(ChannelResolver channelResolver) {
        getEndpointConfiguration().setChannelResolver(channelResolver);
    }

    /**
     * Sets the destination channel name.
     * @param channelName the channelName to set
     */
    public void setChannelName(String channelName) {
        getEndpointConfiguration().setChannelName(channelName);
    }

    /**
     * Gets the channel.
     * @return the channel
     */
    public MessageChannel getChannel() {
        return getEndpointConfiguration().getChannel();
    }

    /**
     * Gets the channelName.
     * @return the channelName
     */
    public String getChannelName() {
        return getEndpointConfiguration().getChannelName();
    }

    /**
     * Gets the messagingTemplate.
     * @return the messagingTemplate
     */
    public MessagingTemplate getMessagingTemplate() {
        return getEndpointConfiguration().getMessagingTemplate();
    }

    /**
     * Gets the channelResolver.
     * @return the channelResolver
     */
    public ChannelResolver getChannelResolver() {
        return getEndpointConfiguration().getChannelResolver();
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
        channelEndpoint.setActor(actor);
    }

    @Override
    public void setBeanName(String name) {
        channelEndpoint.setBeanName(name);
    }

    @Override
    public String getName() {
        return channelEndpoint.getName();
    }

    @Override
    public void setName(String name) {
        channelEndpoint.setName(name);
    }
}
