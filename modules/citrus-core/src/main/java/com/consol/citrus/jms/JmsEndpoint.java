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

package com.consol.citrus.jms;

import com.consol.citrus.endpoint.AbstractEndpoint;
import com.consol.citrus.messaging.*;
import org.springframework.integration.jms.JmsHeaderMapper;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.destination.DestinationResolver;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

/**
 * Jms message endpoint capable of sending/receiving messages from Jms message destination. Either uses a Jms connection factory or
 * a Spring Jms template to connect with Jms destinations.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class JmsEndpoint extends AbstractEndpoint {

    /** Either cached producer or consumer */
    private JmsProducer jmsProducer;
    private JmsConsumer jmsConsumer;

    /**
     * Default constructor initializing endpoint configuration.
     */
    protected JmsEndpoint() {
        super(new JmsEndpointConfiguration());
    }

    /**
     * Constructor with endpoint configuration.
     * @param endpointConfiguration
     */
    protected JmsEndpoint(JmsEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
    }

    @Override
    public SelectiveConsumer createConsumer() {
        if (jmsConsumer == null) {
            jmsConsumer = new JmsConsumer(getEndpointConfiguration());
        }

        return jmsConsumer;
    }

    @Override
    public Producer createProducer() {
        if (jmsProducer == null) {
            jmsProducer = new JmsProducer(getEndpointConfiguration());
        }

        return jmsProducer;
    }

    @Override
    public JmsEndpointConfiguration getEndpointConfiguration() {
        return (JmsEndpointConfiguration) super.getEndpointConfiguration();
    }

    /**
     * Does domain use topics instead of queues.
     * @return the pubSubDomain
     */
    public boolean isPubSubDomain() {
        return getEndpointConfiguration().isPubSubDomain();
    }

    /**
     * Sets if domain uses topics instead of queues.
     * @param pubSubDomain the pubSubDomain to set
     */
    public void setPubSubDomain(boolean pubSubDomain) {
        getEndpointConfiguration().setPubSubDomain(pubSubDomain);
    }

    /**
     * Gets the connection factory.
     * @return the connectionFactory
     */
    public ConnectionFactory getConnectionFactory() {
        return getEndpointConfiguration().getConnectionFactory();
    }

    /**
     * Sets the connection factory.
     * @param connectionFactory the connectionFactory to set
     */
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        getEndpointConfiguration().setConnectionFactory(connectionFactory);
    }

    /**
     * Gets the destination.
     * @return the destination
     */
    public Destination getDestination() {
        return getEndpointConfiguration().getDestination();
    }

    /**
     * Sets the destination.
     * @param destination the destination to set
     */
    public void setDestination(Destination destination) {
        getEndpointConfiguration().setDestination(destination);
    }

    /**
     * Gets the destination name.
     * @return the destinationName
     */
    public String getDestinationName() {
        return getEndpointConfiguration().getDestinationName();
    }

    /**
     * Sets the destination name.
     * @param destinationName the destinationName to set
     */
    public void setDestinationName(String destinationName) {
        getEndpointConfiguration().setDestinationName(destinationName);
    }

    /**
     * Sets the destination resolver.
     * @return the destinationResolver
     */
    public DestinationResolver getDestinationResolver() {
        return getEndpointConfiguration().getDestinationResolver();
    }

    /**
     * Gets the destination resolver.
     * @param destinationResolver the destinationResolver to set
     */
    public void setDestinationResolver(DestinationResolver destinationResolver) {
        getEndpointConfiguration().setDestinationResolver(destinationResolver);
    }

    /**
     * Gets the JMS message converter.
     * @return the messageConverter
     */
    public MessageConverter getMessageConverter() {
        return getEndpointConfiguration().getMessageConverter();
    }

    /**
     * Sets the JMS message converter.
     * @param messageConverter the messageConverter to set
     */
    public void setMessageConverter(MessageConverter messageConverter) {
        getEndpointConfiguration().setMessageConverter(messageConverter);
    }

    /**
     * Gets the JMS header mapper.
     * @return the headerMapper
     */
    public JmsHeaderMapper getHeaderMapper() {
        return getEndpointConfiguration().getHeaderMapper();
    }

    /**
     * Sets the JMS header mapper.
     * @param headerMapper the headerMapper to set
     */
    public void setHeaderMapper(JmsHeaderMapper headerMapper) {
        getEndpointConfiguration().setHeaderMapper(headerMapper);
    }

    /**
     * Sets the JMS template.
     * @param jmsTemplate the jmsTemplate to set
     */
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        getEndpointConfiguration().setJmsTemplate(jmsTemplate);
    }

    /**
     * Gets the JMS template.
     * @return the jmsTemplate
     */
    public JmsTemplate getJmsTemplate() {
        return getEndpointConfiguration().getJmsTemplate();
    }
}
