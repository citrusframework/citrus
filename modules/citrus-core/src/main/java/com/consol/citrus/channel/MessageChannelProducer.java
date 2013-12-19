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

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.messaging.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.*;
import org.springframework.integration.support.channel.BeanFactoryChannelResolver;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MessageChannelProducer implements Producer {
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(MessageChannelProducer.class);

    /** Endpoint configuration*/
    private MessageChannelEndpointConfiguration endpointConfiguration;

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public MessageChannelProducer(MessageChannelEndpointConfiguration endpointConfiguration) {
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public void send(Message<?> message) {
        String destinationChannelName = getDestinationChannelName();

        log.info("Sending message to channel: '" + destinationChannelName + "'");

        if (log.isDebugEnabled()) {
            log.debug("Message to send is:" + System.getProperty("line.separator") + message.toString());
        }

        try {
            endpointConfiguration.getMessagingTemplate().send(getDestinationChannel(), message);
        } catch (MessageDeliveryException e) {
            throw new CitrusRuntimeException("Failed to send message to channel: '" + destinationChannelName + "'", e);
        }

        log.info("Message was successfully sent to channel: '" + destinationChannelName + "'");
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

        return endpointConfiguration.getChannelResolver().resolveChannelName(channelName);
    }
}
