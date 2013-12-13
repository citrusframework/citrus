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

import com.consol.citrus.channel.selector.DispatchingMessageSelector;
import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.integration.*;
import org.springframework.integration.core.*;
import org.springframework.integration.support.channel.BeanFactoryChannelResolver;
import org.springframework.integration.support.channel.ChannelResolver;
import org.springframework.util.StringUtils;

/**
 * Basic message endpoint sends and receives message from Spring message channel. When receiving messages channel must
 * implement {@link org.springframework.integration.core.PollableChannel} interface. When using message selector channel
 * must be of type {@link com.consol.citrus.channel.MessageSelectingQueueChannel}.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MessageChannelEndpoint extends AbstractMessageChannelEndpoint implements BeanFactoryAware {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(MessageChannelEndpoint.class);

    /** Destination channel */
    private MessageChannel channel;

    /** Destination channel name */
    private String channelName;

    /** Message channel template */
    private MessagingTemplate messagingTemplate = new MessagingTemplate();

    /** The parent bean factory used for channel name resolving */
    private BeanFactory beanFactory;

    /** Channel resolver instance */
    private ChannelResolver channelResolver;

    @Override
    public void send(Message<?> message) {
        String destinationChannelName = getDestinationChannelName();

        log.info("Sending message to channel: '" + destinationChannelName + "'");

        if (log.isDebugEnabled()) {
            log.debug("Message to send is:" + System.getProperty("line.separator") + message.toString());
        }

        try {
            messagingTemplate.send(getDestinationChannel(), message);
        } catch (MessageDeliveryException e) {
            throw new CitrusRuntimeException("Failed to send message to channel: '" + destinationChannelName + "'", e);
        }

        log.info("Message was successfully sent to channel: '" + destinationChannelName + "'");
    }

    @Override
    public Message<?> receive(String selector, long timeout) {
        String destinationChannelName;
        MessageChannel destinationChannel = getDestinationChannel();

        if (StringUtils.hasText(selector)) {
            destinationChannelName = getDestinationChannelName() + "(" + selector + ")";
        } else {
            destinationChannelName = getDestinationChannelName();
        }

        log.info("Receiving message from: " + destinationChannelName);

        Message<?> message;
        if (StringUtils.hasText(selector)) {
            if (!(destinationChannel instanceof MessageSelectingQueueChannel)) {
                throw new CitrusRuntimeException("Message channel type '" + channel.getClass() +
                        "' does not support selective receive operations.");
            }

            MessageSelector messageSelector = new DispatchingMessageSelector(selector, beanFactory);
            MessageSelectingQueueChannel queueChannel = ((MessageSelectingQueueChannel) destinationChannel);

            if (timeout <= 0) {
                message = queueChannel.receive(messageSelector);
            } else {
                message = queueChannel.receive(messageSelector, timeout);
            }
        } else {
            if (!(destinationChannel instanceof PollableChannel)) {
                throw new CitrusRuntimeException("Invalid destination channel type " + destinationChannel.getClass().getName() +
                        " - must be of type PollableChannel");
            }

            messagingTemplate.setReceiveTimeout(timeout);
            message = messagingTemplate.receive((PollableChannel) destinationChannel);
        }

        if (message == null) {
            throw new ActionTimeoutException("Action timeout while receiving message from channel '"
                    + destinationChannelName + "'");
        }

        return message;
    }

    /**
     * Get the destination channel depending on settings in this message sender.
     * Either a direct channel object is set or a channel name which will be resolved
     * to a channel.
     *
     * @return the destination channel object.
     */
    protected MessageChannel getDestinationChannel() {
        if (channel != null) {
            return channel;
        } else if (StringUtils.hasText(channelName)) {
            return resolveChannelName(channelName);
        } else {
            throw new CitrusRuntimeException("Neither channel name nor channel object is set - " +
                    "please specify destination channel");
        }
    }

    /**
     * Gets the channel name depending on what is set in this message sender.
     * Either channel name is set directly or channel object is consulted for channel name.
     *
     * @return the channel name.
     */
    protected String getDestinationChannelName() {
        if (channel != null) {
            return channel.toString();
        } else if (StringUtils.hasText(channelName)) {
            return channelName;
        } else {
            throw new CitrusRuntimeException("Neither channel name nor channel object is set - " +
                    "please specify destination channel");
        }
    }

    /**
     * Resolve the channel by name.
     * @param channelName the name to resolve
     * @return the MessageChannel object
     */
    protected MessageChannel resolveChannelName(String channelName) {
        if (channelResolver == null) {
            channelResolver = new BeanFactoryChannelResolver(beanFactory);
        }

        return channelResolver.resolveChannelName(channelName);
    }

    /**
     * Set the message channel.
     * @param channel the channel to set
     */
    public void setChannel(MessageChannel channel) {
        this.channel = channel;
    }

    /**
     * Sets the messaging template.
     * @param messagingTemplate the messagingTemplate to set
     */
    public void setMessagingTemplate(MessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Sets the bean factory for channel resolver.
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    /**
     * Set the channel resolver.
     * @param channelResolver the channelResolver to set
     */
    public void setChannelResolver(ChannelResolver channelResolver) {
        this.channelResolver = channelResolver;
    }

    /**
     * Sets the destination channel name.
     * @param channelName the channelName to set
     */
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    /**
     * Gets the channel.
     * @return the channel
     */
    public MessageChannel getChannel() {
        return channel;
    }

    /**
     * Gets the channelName.
     * @return the channelName
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * Gets the messagingTemplate.
     * @return the messagingTemplate
     */
    public MessagingTemplate getMessagingTemplate() {
        return messagingTemplate;
    }

    /**
     * Gets the channelResolver.
     * @return the channelResolver
     */
    public ChannelResolver getChannelResolver() {
        return channelResolver;
    }
}
