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

package org.citrusframework.jms.endpoint;

import jakarta.jms.Destination;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.AbstractSelectiveMessageConsumer;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class JmsConsumer extends AbstractSelectiveMessageConsumer {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(JmsConsumer.class);

    /** Endpoint configuration */
    protected final JmsEndpointConfiguration endpointConfiguration;

    /**
     * Default constructor using endpoint.
     * @param name
     * @param endpointConfiguration
     */
    public JmsConsumer(String name, JmsEndpointConfiguration endpointConfiguration) {
        super(name, endpointConfiguration);
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public Message receive(String selector, TestContext context, long timeout) {
        endpointConfiguration.getJmsTemplate().setReceiveTimeout(timeout);
        jakarta.jms.Message receivedJmsMessage;

        if (endpointConfiguration.getDestination() != null) {
            receivedJmsMessage = receive(endpointConfiguration.getDestination(), selector);
        } else if (StringUtils.hasText(endpointConfiguration.getDestinationName())) {
            receivedJmsMessage = receive(context.replaceDynamicContentInString(endpointConfiguration.getDestinationName()), selector);
        } else if (endpointConfiguration.getJmsTemplate().getDefaultDestination() != null) {
            receivedJmsMessage = receive(endpointConfiguration.getJmsTemplate().getDefaultDestination(), selector);
        } else if (StringUtils.hasText(endpointConfiguration.getJmsTemplate().getDefaultDestinationName())) {
            receivedJmsMessage = receive(context.replaceDynamicContentInString(endpointConfiguration.getJmsTemplate().getDefaultDestinationName()), selector);
        } else {
            throw new CitrusRuntimeException("Unable to receive message - JMS destination not set");
        }

        Message receivedMessage = endpointConfiguration.getMessageConverter().convertInbound(receivedJmsMessage, endpointConfiguration, context);
        context.onInboundMessage(receivedMessage);

        return receivedMessage;
    }

    /**
     * Receive message from destination name.
     * @param destinationName
     * @param selector
     * @return
     */
    private jakarta.jms.Message receive(String destinationName, String selector) {
        jakarta.jms.Message receivedJmsMessage;

        if (logger.isDebugEnabled()) {
            logger.debug("Receiving JMS message on destination: '" + getDestinationNameWithSelector(destinationName, selector) + "'");
        }

        if (StringUtils.hasText(selector)) {
            receivedJmsMessage = endpointConfiguration.getJmsTemplate().receiveSelected(destinationName, selector);
        } else {
            receivedJmsMessage = endpointConfiguration.getJmsTemplate().receive(destinationName);
        }

        if (receivedJmsMessage == null) {
            throw new MessageTimeoutException(endpointConfiguration.getTimeout(), getDestinationNameWithSelector(destinationName, selector));
        }

        logger.info("Received JMS message on destination: '" + getDestinationNameWithSelector(destinationName, selector) + "'");

        return receivedJmsMessage;
    }

    /**
     * Receive message from destination.
     * @param destination
     * @param selector
     * @return
     */
    private jakarta.jms.Message receive(Destination destination, String selector) {
        jakarta.jms.Message receivedJmsMessage;

        if (logger.isDebugEnabled()) {
            logger.debug("Receiving JMS message on destination: '" + getDestinationNameWithSelector(endpointConfiguration.getDestinationName(destination), selector) + "'");
        }

        if (StringUtils.hasText(selector)) {
            receivedJmsMessage = endpointConfiguration.getJmsTemplate().receiveSelected(destination, selector);
        } else {
            receivedJmsMessage = endpointConfiguration.getJmsTemplate().receive(destination);
        }

        if (receivedJmsMessage == null) {
            throw new MessageTimeoutException(endpointConfiguration.getTimeout(), getDestinationNameWithSelector(endpointConfiguration.getDestinationName(destination), selector));
        }

        logger.info("Received JMS message on destination: '" + getDestinationNameWithSelector(endpointConfiguration.getDestinationName(destination), selector) + "'");

        return receivedJmsMessage;
    }

    /**
     * Helper method to construct proper representation of destination name and selector if given.
     * @param destinationName
     * @param selector
     * @return
     */
    private String getDestinationNameWithSelector(String destinationName, String selector) {
        return destinationName + (StringUtils.hasText(selector) ? "(" + selector + ")" : "");
    }

}
