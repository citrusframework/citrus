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
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Producer;
import org.citrusframework.util.ObjectHelper;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class JmsProducer implements Producer {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(JmsProducer.class);

    /** The producer name. */
    private final String name;

    /** Endpoint configuration */
    private final JmsEndpointConfiguration endpointConfiguration;

    /**
     * Default constructor using endpoint configuration.
     * @param name
     * @param endpointConfiguration
     */
    public JmsProducer(String name, JmsEndpointConfiguration endpointConfiguration) {
        this.name = name;
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public void send(final Message message, final TestContext context) {
        ObjectHelper.assertNotNull(message, "Message is empty - unable to send empty message");

        if (endpointConfiguration.getDestination() != null) {
            send(message, endpointConfiguration.getDestination(), context);
        } else if (StringUtils.hasText(endpointConfiguration.getDestinationName())) {
            if (endpointConfiguration.getDestinationNameResolver() != null) {
                send(message, context.replaceDynamicContentInString(endpointConfiguration.getDestinationNameResolver().resolveEndpointUri(message, endpointConfiguration.getDestinationName())), context);
            } else {
                send(message, context.replaceDynamicContentInString(endpointConfiguration.getDestinationName()), context);
            }
        } else if (endpointConfiguration.getJmsTemplate().getDefaultDestination() != null) {
            send(message, endpointConfiguration.getJmsTemplate().getDefaultDestination(), context);
        } else if (StringUtils.hasText(endpointConfiguration.getJmsTemplate().getDefaultDestinationName())) {
            send(message, context.replaceDynamicContentInString(endpointConfiguration.getJmsTemplate().getDefaultDestinationName()), context);
        } else {
            throw new CitrusRuntimeException("Unable to send message - JMS destination not set");
        }

        context.onOutboundMessage(message);
    }

    /**
     * Send message using destination name.
     * @param message
     * @param destinationName
     * @param context
     */
    private void send(Message message, String destinationName, TestContext context) {
        if (logger.isDebugEnabled()) {
            logger.debug("Sending JMS message to destination: '" + destinationName + "'");
        }

        endpointConfiguration.getJmsTemplate().send(destinationName, session -> {
            jakarta.jms.Message jmsMessage = endpointConfiguration.getMessageConverter().createJmsMessage(message, session, endpointConfiguration, context);
            endpointConfiguration.getMessageConverter().convertOutbound(jmsMessage, message, endpointConfiguration, context);
            return jmsMessage;
        });

        logger.info("Message was sent to JMS destination: '" + destinationName + "'");
    }

    /**
     * Send message using destination.
     * @param message
     * @param destination
     * @param context
     */
    private void send(Message message, Destination destination, TestContext context) {
        if (logger.isDebugEnabled()) {
            logger.debug("Sending JMS message to destination: '" + endpointConfiguration.getDestinationName(destination) + "'");
        }

        endpointConfiguration.getJmsTemplate().send(destination, session -> {
            jakarta.jms.Message jmsMessage = endpointConfiguration.getMessageConverter().createJmsMessage(message, session, endpointConfiguration, context);
            endpointConfiguration.getMessageConverter().convertOutbound(jmsMessage, message, endpointConfiguration, context);
            return jmsMessage;
        });

        logger.info("Message was sent to JMS destination: '" + endpointConfiguration.getDestinationName(destination) + "'");
    }

    @Override
    public String getName() {
        return name;
    }

}
