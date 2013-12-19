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

import com.consol.citrus.endpoint.AbstractMessageEndpoint;
import com.consol.citrus.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.channel.ChannelResolver;

/**
 * Basic message endpoint sends and receives message from Spring message channel. When receiving messages channel must
 * implement {@link org.springframework.integration.core.PollableChannel} interface. When using message selector channel
 * must be of type {@link com.consol.citrus.channel.MessageSelectingQueueChannel}.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MessageChannelEndpoint extends AbstractMessageEndpoint implements BeanFactoryAware {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(MessageChannelEndpoint.class);

    private MessageChannelConsumer messageChannelConsumer;
    private MessageChannelProducer messageChannelProducer;

    /**
     * Default constructor initializing endpoint configuration.
     */
    protected MessageChannelEndpoint() {
        super(new MessageChannelEndpointConfiguration());
    }

    /**
     * Constructor with endpoint configuration.
     * @param endpointConfiguration
     */
    protected MessageChannelEndpoint(MessageChannelEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
    }

    @Override
    public SelectiveConsumer createConsumer() {
        if (messageChannelConsumer == null) {
            messageChannelConsumer = new MessageChannelConsumer(getEndpointConfiguration());
        }

        return messageChannelConsumer;
    }

    @Override
    public Producer createProducer() {
        if (messageChannelProducer == null) {
            messageChannelProducer = new MessageChannelProducer(getEndpointConfiguration());
        }

        return messageChannelProducer;
    }

    @Override
    public MessageChannelEndpointConfiguration getEndpointConfiguration() {
        return (MessageChannelEndpointConfiguration) super.getEndpointConfiguration();
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
        getEndpointConfiguration().setBeanFactory(beanFactory);
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
}
