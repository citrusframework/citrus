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

package org.citrusframework.channel;

import java.util.Optional;

import org.citrusframework.channel.selector.DispatchingMessageSelector;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.AbstractSelectiveMessageConsumer;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.support.channel.BeanFactoryChannelResolver;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class ChannelConsumer extends AbstractSelectiveMessageConsumer {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(ChannelConsumer.class);

    /** Endpoint configuration */
    private final ChannelEndpointConfiguration endpointConfiguration;

    /**
     * Default constructor using endpoint configuration.
     * @param name
     * @param endpointConfiguration
     */
    public ChannelConsumer(String name, ChannelEndpointConfiguration endpointConfiguration) {
        super(name, endpointConfiguration);
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public Message receive(String selector, TestContext context, long timeout) {
        String destinationChannelName;
        MessageChannel destinationChannel = getDestinationChannel(context);

        if (StringUtils.hasText(selector)) {
            destinationChannelName = getDestinationChannelName() + "(" + selector + ")";
        } else {
            destinationChannelName = getDestinationChannelName();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Receiving message from: " + destinationChannelName);
        }

        Message message;
        if (StringUtils.hasText(selector)) {
            if (!(destinationChannel instanceof MessageSelectingQueueChannel)) {
                throw new CitrusRuntimeException("Message channel type '" + endpointConfiguration.getChannel().getClass() +
                        "' does not support selective receive operations.");
            }

            MessageSelector messageSelector = new DispatchingMessageSelector(selector, endpointConfiguration.getBeanFactory(), context);
            MessageSelectingQueueChannel queueChannel = ((MessageSelectingQueueChannel) destinationChannel);

            if (timeout <= 0) {
                message = endpointConfiguration.getMessageConverter().convertInbound(queueChannel.receive(messageSelector), endpointConfiguration, context);
            } else {
                message = endpointConfiguration.getMessageConverter().convertInbound(queueChannel.receive(messageSelector, timeout), endpointConfiguration, context);
            }
        } else {
            if (!(destinationChannel instanceof PollableChannel)) {
                throw new CitrusRuntimeException("Invalid destination channel type " + destinationChannel.getClass().getName() +
                        " - must be of type PollableChannel");
            }

            endpointConfiguration.getMessagingTemplate().setReceiveTimeout(timeout);
            message = endpointConfiguration.getMessageConverter().convertInbound(
                    endpointConfiguration.getMessagingTemplate().receive((PollableChannel) destinationChannel), endpointConfiguration, context);
        }

        if (message == null) {
            throw new MessageTimeoutException(timeout, destinationChannelName);
        }

        logger.debug("Received message from: " + destinationChannelName);
        return message;
    }

    /**
     * Get the destination channel depending on settings in this message sender.
     * Either a direct channel object is set or a channel name which will be resolved
     * to a channel.
     *
     * @param context the test context
     * @return the destination channel object.
     */
    protected MessageChannel getDestinationChannel(TestContext context) {
        if (endpointConfiguration.getChannel() != null) {
            return endpointConfiguration.getChannel();
        } else if (StringUtils.hasText(endpointConfiguration.getChannelName())) {
            return resolveChannelName(endpointConfiguration.getChannelName(), context);
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
        if (endpointConfiguration.getChannel() != null) {
            return endpointConfiguration.getChannel().toString();
        } else if (StringUtils.hasText(endpointConfiguration.getChannelName())) {
            return endpointConfiguration.getChannelName();
        } else {
            throw new CitrusRuntimeException("Neither channel name nor channel object is set - " +
                    "please specify destination channel");
        }
    }

    /**
     * Resolve the channel by name.
     * @param channelName the name to resolve
     * @param context
     * @return the MessageChannel object
     */
    protected MessageChannel resolveChannelName(String channelName, TestContext context) {
        return Optional.ofNullable(endpointConfiguration.getChannelResolver())
                .map(resolver -> resolver.resolveDestination(channelName))
                .orElseGet(() -> {
                    if (endpointConfiguration.getBeanFactory() != null) {
                        return new BeanFactoryChannelResolver(endpointConfiguration.getBeanFactory()).resolveDestination(channelName);
                    }

                    return context.getReferenceResolver().resolve(channelName, MessageChannel.class);
                });
    }
}
