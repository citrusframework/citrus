/*
 * Copyright 2006-2010 the original author or authors.
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

import com.consol.citrus.TestActor;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.integration.jms.JmsHeaderMapper;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.destination.DestinationResolver;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

/**
 * Basic adapter class for JMS communication. The adapter uses Spring's {@link JmsTemplate}.
 * 
 * @author Christoph Deppisch
 * @deprecated
 */
public abstract class AbstractJmsAdapter implements BeanNameAware {

    /** New JmsEndpoint implementation */
    private JmsMessageEndpoint jmsEndpoint;

    /**
     * Default constructor.
     */
    public AbstractJmsAdapter() {
        this.jmsEndpoint  = new JmsMessageEndpoint();
    }

    /**
     * Default constructor with Jms endpoint.
     * @param jmsEndpoint
     */
    protected AbstractJmsAdapter(JmsMessageEndpoint jmsEndpoint) {
        this.jmsEndpoint = jmsEndpoint;
    }

    /**
     * Gets the Jms endpoint.
     * @return
     */
    public JmsMessageEndpoint getJmsEndpoint() {
        return jmsEndpoint;
    }

    /**
     * Sets the Jms endpoint
     * @param jmsEndpoint
     */
    public void setJmsEndpoint(JmsMessageEndpoint jmsEndpoint) {
        this.jmsEndpoint = jmsEndpoint;
    }

    /**
     * Does domain use topics instead of queues.
     * @return the pubSubDomain
     */
    public boolean isPubSubDomain() {
        return jmsEndpoint.isPubSubDomain();
    }

    /**
     * Sets if domain uses topics instead of queues.
     * @param pubSubDomain the pubSubDomain to set
     */
    public void setPubSubDomain(boolean pubSubDomain) {
        jmsEndpoint.setPubSubDomain(pubSubDomain);
    }

    /**
     * Gets the connection factory.
     * @return the connectionFactory
     */
    public ConnectionFactory getConnectionFactory() {
        return jmsEndpoint.getConnectionFactory();
    }

    /**
     * Sets the connection factory.
     * @param connectionFactory the connectionFactory to set
     */
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        jmsEndpoint.setConnectionFactory(connectionFactory);
    }

    /**
     * Gets the destination.
     * @return the destination
     */
    public Destination getDestination() {
        return jmsEndpoint.getDestination();
    }

    /**
     * Sets the destination.
     * @param destination the destination to set
     */
    public void setDestination(Destination destination) {
        jmsEndpoint.setDestination(destination);
    }
    
    /**
     * Gets the destination name.
     * @return the destinationName
     */
    public String getDefaultDestinationName() {
        return jmsEndpoint.getEndpointConfiguration().getDefaultDestinationName();
    }
    
    /**
     * Gets the destination name.
     * @return the destinationName
     */
    public String getDestinationName() {
        return jmsEndpoint.getDestinationName();
    }

    /**
     * Sets the destination name.
     * @param destinationName the destinationName to set
     */
    public void setDestinationName(String destinationName) {
        jmsEndpoint.setDestinationName(destinationName);
    }

    /**
     * Sets the destination resolver.
     * @return the destinationResolver
     */
    public DestinationResolver getDestinationResolver() {
        return jmsEndpoint.getDestinationResolver();
    }

    /**
     * Gets the destination resolver.
     * @param destinationResolver the destinationResolver to set
     */
    public void setDestinationResolver(DestinationResolver destinationResolver) {
        jmsEndpoint.setDestinationResolver(destinationResolver);
    }

    /**
     * Gets the JMS message converter.
     * @return the messageConverter
     */
    public MessageConverter getMessageConverter() {
        return jmsEndpoint.getMessageConverter();
    }

    /**
     * Sets the JMS message converter.
     * @param messageConverter the messageConverter to set
     */
    public void setMessageConverter(MessageConverter messageConverter) {
        jmsEndpoint.setMessageConverter(messageConverter);
    }

    /**
     * Gets the JMS header mapper.
     * @return the headerMapper
     */
    public JmsHeaderMapper getHeaderMapper() {
        return jmsEndpoint.getHeaderMapper();
    }

    /**
     * Sets the JMS header mapper.
     * @param headerMapper the headerMapper to set
     */
    public void setHeaderMapper(JmsHeaderMapper headerMapper) {
        jmsEndpoint.setHeaderMapper(headerMapper);
    }

    /**
     * Sets the JMS template.
     * @param jmsTemplate the jmsTemplate to set
     */
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        jmsEndpoint.setJmsTemplate(jmsTemplate);
    }

    /**
     * Gets the JMS template.
     * @return the jmsTemplate
     */
    public JmsTemplate getJmsTemplate() {
        return jmsEndpoint.getJmsTemplate();
    }

    /**
     * Gets the actor.
     * @return the actor the actor to get.
     */
    public TestActor getActor() {
        return jmsEndpoint.getActor();
    }

    /**
     * Sets the actor.
     * @param actor the actor to set
     */
    public void setActor(TestActor actor) {
        jmsEndpoint.setActor(actor);
    }

    @Override
    public void setBeanName(String name) {
        jmsEndpoint.setBeanName(name);
    }

    /**
     * Gets the adapter's name - usually the Spring bean name.
     * @return
     */
    public String getName() {
        return jmsEndpoint.getName();
    }

}
