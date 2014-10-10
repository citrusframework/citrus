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

package com.consol.citrus.jms.endpoint;

import com.consol.citrus.endpoint.AbstractEndpointConfiguration;
import com.consol.citrus.jms.message.JmsMessageConverter;
import com.consol.citrus.jms.message.JmsMessageHeaderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.jms.JmsHeaderMapper;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.util.Assert;

import javax.jms.*;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class JmsEndpointConfiguration extends AbstractEndpointConfiguration {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JmsEndpointConfiguration.class);

    /** The connection factory */
    private ConnectionFactory connectionFactory;

    /** The destination object */
    private Destination destination;

    /** The destination name */
    private String destinationName;

    /** The destination resolver */
    private DestinationResolver destinationResolver;

    /** The JMS template */
    private JmsTemplate jmsTemplate;

    /** The header mapper */
    private JmsHeaderMapper headerMapper = new JmsMessageHeaderMapper();

    /** The message converter */
    private JmsMessageConverter messageConverter = new JmsMessageConverter();

    /** Use topics instead of queues */
    private boolean pubSubDomain = false;

    /** Should always use object messages */
    private boolean useObjectMessages = false;

    /**
     * Gets the destination name.
     * @return the destinationName
     */
    public String getDefaultDestinationName() {
        Destination defaultDestination = getJmsTemplate().getDefaultDestination();
        try {
            if (defaultDestination != null) {
                if (defaultDestination instanceof Queue) {
                    return ((Queue)defaultDestination).getQueueName();
                } else if (defaultDestination instanceof Topic) {
                    return ((Topic)defaultDestination).getTopicName();
                } else {
                    return defaultDestination.toString();
                }
            } else {
                return getJmsTemplate().getDefaultDestinationName();
            }
        } catch (JMSException e) {
            log.error("Unable to resolve destination name", e);
            return "";
        }
    }

    /**
     * Creates default JmsTemplate instance from connection factory and destination.
     */
    private void createJmsTemplate() {
        Assert.isTrue(this.connectionFactory != null,
                "Neither 'jmsTemplate' nor 'connectionFactory' is set correctly.");

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
     * the outbound message. Instead the raw message object will be sent over the wire using a JMS object message.
     * @return
     */
    public boolean isUseObjectMessages() {
        return useObjectMessages;
    }

    /**
     *
     * @param useObjectMessages
     */
    public void setUseObjectMessages(boolean useObjectMessages) {
        this.useObjectMessages = useObjectMessages;
    }
}
