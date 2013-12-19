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
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageSender;
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
 * @deprecated
 */
public class MessageChannelSender implements MessageSender, BeanFactoryAware, BeanNameAware {

    /** New message channel endpoint */
    private MessageChannelEndpoint messageChannelEndpoint;

    /**
     * Default constructor.
     */
    public MessageChannelSender() {
        this.messageChannelEndpoint = new MessageChannelEndpoint();
    }

    /**
     * Default constructor using message endpoint.
     * @param messageChannelEndpoint
     */
    public MessageChannelSender(MessageChannelEndpoint messageChannelEndpoint) {
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
     * @see com.consol.citrus.message.MessageSender#send(org.springframework.integration.Message)
     * @throws CitrusRuntimeException
     */
    public void send(Message<?> message) {
        messageChannelEndpoint.createProducer().send(message);
    }

    /**
     * Set the message channel.
     * @param channel the channel to set
     */
    public void setChannel(MessageChannel channel) {
        messageChannelEndpoint.setChannel(channel);
    }

    /**
     * Sets the messaging template.
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
     * Set the channel resolver.
     * @param channelResolver the channelResolver to set
     */
    public void setChannelResolver(ChannelResolver channelResolver) {
        messageChannelEndpoint.setChannelResolver(channelResolver);
    }

    /**
     * Sets the destination channel name.
     * @param channelName the channelName to set
     */
    public void setChannelName(String channelName) {
        messageChannelEndpoint.setChannelName(channelName);
    }

    /**
     * Gets the channel.
     * @return the channel
     */
    public MessageChannel getChannel() {
        return messageChannelEndpoint.getChannel();
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
