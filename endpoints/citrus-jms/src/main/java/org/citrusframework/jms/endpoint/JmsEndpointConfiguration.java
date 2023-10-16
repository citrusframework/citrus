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

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Queue;
import jakarta.jms.Topic;

import org.citrusframework.endpoint.AbstractPollableEndpointConfiguration;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jms.endpoint.resolver.DynamicDestinationNameResolver;
import org.citrusframework.jms.message.JmsMessageConverter;
import org.citrusframework.jms.message.JmsMessageHeaderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.JmsHeaderMapper;
import org.springframework.jms.support.destination.DestinationResolver;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class JmsEndpointConfiguration extends AbstractPollableEndpointConfiguration {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(JmsEndpointConfiguration.class);

    /** The connection factory */
    private ConnectionFactory connectionFactory;

    /** The destination object */
    private Destination destination;

    /** The destination name */
    private String destinationName;

    /** The destination resolver */
    private DestinationResolver destinationResolver;

    /** Resolves dynamic destination names */
    private EndpointUriResolver destinationNameResolver = new DynamicDestinationNameResolver();

    /** The JMS template */
    private JmsTemplate jmsTemplate;

    /** The header mapper */
    private JmsHeaderMapper headerMapper = new JmsMessageHeaderMapper();

    /** The message converter */
    private JmsMessageConverter messageConverter = new JmsMessageConverter();

    /** Use topics instead of queues */
    private boolean pubSubDomain = false;

    /** Start topic subscription immediately at startup and cache all incoming message events in local channel */
    private boolean autoStart = false;

    /** Durable subscriber settings */
    private boolean durableSubscription = false;
    private String durableSubscriberName;

    /** Should always use object messages */
    private boolean useObjectMessages = false;

    /** Enable/disable filtering of Citrus internal headers */
    private boolean filterInternalHeaders = true;

    /**
     * Get the destination name (either a queue name or a topic name).
     * @param destination
     * @return the destinationName
     */
    public String getDestinationName(Destination destination) {
        try {
            if (destination instanceof Queue) {
                return ((Queue) destination).getQueueName();
            } else if (destination instanceof Topic) {
                return ((Topic) destination).getTopicName();
            } else {
                return destination.toString();
            }
        } catch (JMSException e) {
            logger.error("Unable to resolve destination name", e);
            return "";
        }
    }

    /**
     * Creates default JmsTemplate instance from connection factory and destination.
     */
    private void createJmsTemplate() {
        if (this.connectionFactory == null) {
            throw new CitrusRuntimeException("Neither 'jmsTemplate' nor 'connectionFactory' is set correctly.");
        }

        jmsTemplate = new JmsTemplate();

        jmsTemplate.setConnectionFactory(this.connectionFactory);

        if (this.destination != null) {
            jmsTemplate.setDefaultDestination(this.destination);
        } else if (this.destinationName != null) {
            jmsTemplate.setDefaultDestinationName(this.destinationName);
        }

        if (this.destinationResolver != null) {
            jmsTemplate.setDestinationResolver(this.destinationResolver);
        }

        jmsTemplate.setPubSubDomain(pubSubDomain);
    }

    /**
     * Does domain use topics instead of queues.
     * @return the pubSubDomain
     */
    public boolean isPubSubDomain() {
        return pubSubDomain;
    }

    /**
     * Sets if domain uses topics instead of queues.
     * @param pubSubDomain the pubSubDomain to set
     */
    public void setPubSubDomain(boolean pubSubDomain) {
        this.pubSubDomain = pubSubDomain;
    }

    /**
     * Gets the connection factory.
     * @return the connectionFactory
     */
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * Sets the connection factory.
     * @param connectionFactory the connectionFactory to set
     */
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Gets the destination.
     * @return the destination
     */
    public Destination getDestination() {
        return destination;
    }

    /**
     * Sets the destination.
     * @param destination the destination to set
     */
    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    /**
     * Gets the destination name.
     * @return the destinationName
     */
    public String getDestinationName() {
        return destinationName;
    }

    /**
     * Sets the destination name.
     * @param destinationName the destinationName to set
     */
    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    /**
     * Sets the destination resolver.
     * @return the destinationResolver
     */
    public DestinationResolver getDestinationResolver() {
        return destinationResolver;
    }

    /**
     * Gets the destination resolver.
     * @param destinationResolver the destinationResolver to set
     */
    public void setDestinationResolver(DestinationResolver destinationResolver) {
        this.destinationResolver = destinationResolver;
    }

    /**
     * Sets the JMS template.
     * @param jmsTemplate the jmsTemplate to set
     */
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    /**
     * Gets the JMS template.
     * @return the jmsTemplate
     */
    public JmsTemplate getJmsTemplate() {
        if (jmsTemplate == null) {
            createJmsTemplate();
        }

        return jmsTemplate;
    }

    /**
     * Gets the message converter.
     * @return
     */
    public JmsMessageConverter getMessageConverter() {
        return messageConverter;
    }

    /**
     * Sets the message converter.
     * @param messageConverter
     */
    public void setMessageConverter(JmsMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Gets the JMS header mapper.
     * @return the headerMapper
     */
    public JmsHeaderMapper getHeaderMapper() {
        return headerMapper;
    }

    /**
     * Sets the JMS header mapper.
     * @param headerMapper the headerMapper to set
     */
    public void setHeaderMapper(JmsHeaderMapper headerMapper) {
        this.headerMapper = headerMapper;
    }

    /**
     * Determines weather to convert outbound messages or not. If conversion is disabled endpoint will not convert
     * the outbound message. Instead, the raw message object will be sent over the wire using a JMS object message.
     * @return
     */
    public boolean isUseObjectMessages() {
        return useObjectMessages;
    }

    /**
     * Setting to control object message mode.
     * @param useObjectMessages
     */
    public void setUseObjectMessages(boolean useObjectMessages) {
        this.useObjectMessages = useObjectMessages;
    }

    /**
     * Determines if internal message headers should be filtered when creating the JMS message.
     * @return
     */
    public boolean isFilterInternalHeaders() {
        return filterInternalHeaders;
    }

    /**
     * Setting to control filtering of internal message headers.
     * @param filterInternalHeaders
     */
    public void setFilterInternalHeaders(boolean filterInternalHeaders) {
        this.filterInternalHeaders = filterInternalHeaders;

        if (headerMapper instanceof JmsMessageHeaderMapper) {
            ((JmsMessageHeaderMapper) headerMapper).setFilterInternalHeaders(filterInternalHeaders);
        }
    }

    /**
     * Gets the destinationNameResolver.
     *
     * @return
     */
    public EndpointUriResolver getDestinationNameResolver() {
        return destinationNameResolver;
    }

    /**
     * Sets the destinationNameResolver.
     *
     * @param destinationNameResolver
     */
    public void setDestinationNameResolver(EndpointUriResolver destinationNameResolver) {
        this.destinationNameResolver = destinationNameResolver;
    }

    /**
     * Gets the autoStart.
     *
     * @return
     */
    public boolean isAutoStart() {
        return autoStart;
    }

    /**
     * Sets the autoStart.
     *
     * @param autoStart
     */
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    /**
     * Gets the durableSubscription.
     *
     * @return
     */
    public boolean isDurableSubscription() {
        return durableSubscription;
    }

    /**
     * Sets the durableSubscription.
     *
     * @param durableSubscription
     */
    public void setDurableSubscription(boolean durableSubscription) {
        this.durableSubscription = durableSubscription;
    }

    /**
     * Gets the durableSubscriberName.
     *
     * @return
     */
    public String getDurableSubscriberName() {
        return durableSubscriberName;
    }

    /**
     * Sets the durableSubscriberName.
     *
     * @param durableSubscriberName
     */
    public void setDurableSubscriberName(String durableSubscriberName) {
        this.durableSubscriberName = durableSubscriberName;
    }
}
