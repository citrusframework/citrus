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
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.messaging.AbstractSelectiveMessageConsumer;
import com.consol.citrus.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.integration.core.MessageSelector;
import org.springframework.messaging.PollableChannel;
import org.springframework.integration.support.channel.BeanFactoryChannelResolver;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class ChannelConsumer extends AbstractSelectiveMessageConsumer {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(ChannelConsumer.class);

    /** Endpoint configuration */
    private ChannelEndpointConfiguration endpointConfiguration;

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
        MessageChannel destinationChannel = getDestinationChannel();

        if (StringUtils.hasText(selector)) {
            destinationChannelName = getDestinationChannelName() + "(" + selector + ")";
        } else {
            destinationChannelName = getDestinationChannelName();
        }

        log.info("Receiving message from: " + destinationChannelName);

        Message message;
        if (StringUtils.hasText(selector)) {
            if (!(destinationChannel instanceof MessageSelectingQueueChannel)) {
                throw new CitrusRuntimeException("Message channel type '" + endpointConfiguration.getChannel().getClass() +
                        "' does not support selective receive operations.");
            }

            MessageSelector messageSelector = new DispatchingMessageSelector(selector, endpointConfiguration.getBeanFactory());
            MessageSelectingQueueChannel queueChannel = ((MessageSelectingQueueChannel) destinationChannel);

            if (timeout <= 0) {
                message = endpointConfiguration.getMessageConverter().convertInbound(queueChannel.receive(messageSelector), endpointConfiguration);
            } else {
                message = endpointConfiguration.getMessageConverter().convertInbound(queueChannel.receive(messageSelector, timeout), endpointConfiguration);
            }
        } else {
            if (!(destinationChannel instanceof PollableChannel)) {
                throw new CitrusRuntimeException("Invalid destination channel type " + destinationChannel.getClass().getName() +
                        " - must be of type PollableChannel");
            }

            endpointConfiguration.getMessagingTemplate().setReceiveTimeout(timeout);
            message = endpointConfiguration.getMessageConverter().convertInbound(
                    endpointConfiguration.getMessagingTemplate().receive((PollableChannel) destinationChannel), endpointConfiguration);
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
        if (endpointConfiguration.getChannel() != null) {
            return endpointConfiguration.getChannel();
        } else if (StringUtils.hasText(endpointConfiguration.getChannelName())) {
            return resolveChannelName(endpointConfiguration.getChannelName());
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
     * @return the MessageChannel object
     */
    protected MessageChannel resolveChannelName(String channelName) {
        if (endpointConfiguration.getChannelResolver() == null) {
            endpointConfiguration.setChannelResolver(new BeanFactoryChannelResolver(endpointConfiguration.getBeanFactory()));
        }

        return endpointConfiguration.getChannelResolver().resolveDestination(channelName);
    }
}
