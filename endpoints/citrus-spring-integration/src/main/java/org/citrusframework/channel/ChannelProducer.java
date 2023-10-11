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

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Producer;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.support.channel.BeanFactoryChannelResolver;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class ChannelProducer implements Producer {
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(ChannelProducer.class);

    /** The producer name */
    private final String name;

    /** Endpoint configuration*/
    private ChannelEndpointConfiguration endpointConfiguration;

    /**
     * Default constructor using endpoint configuration.
     * @param name
     * @param endpointConfiguration
     */
    public ChannelProducer(String name, ChannelEndpointConfiguration endpointConfiguration) {
        this.name = name;
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public void send(Message message, TestContext context) {
        String destinationChannelName = getDestinationChannelName();

        if (logger.isDebugEnabled()) {
            logger.debug("Sending message to channel: '" + destinationChannelName + "'");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Message to send is:" + System.getProperty("line.separator") + message.toString());
        }

        try {
            endpointConfiguration.getMessagingTemplate().send(getDestinationChannel(context),
                    endpointConfiguration.getMessageConverter().convertOutbound(message, endpointConfiguration, context));
        } catch (MessageDeliveryException e) {
            throw new CitrusRuntimeException("Failed to send message to channel: '" + destinationChannelName + "'", e);
        }

        logger.info("Message was sent to channel: '" + destinationChannelName + "'");
    }

    /**
     * Get the destination channel depending on settings in this message sender.
     * Either a direct channel object is set or a channel name which will be resolved
     * to a channel.
     *
     * @return the destination channel object.
     * @param context
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
            if (endpointConfiguration.getChannel() instanceof AbstractMessageChannel) {
                return ((AbstractMessageChannel) endpointConfiguration.getChannel()).getBeanName();
            }

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
     * @param context the test context
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

    @Override
    public String getName() {
        return name;
    }
}
