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
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.*;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.messaging.Producer;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.integration.Message;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

/**
 * Synchronous message sender implementation for JMS. Sender publishes messages to a JMS destination and
 * sets the reply destination in the request message. Sender consumes the reply destination right away and
 * invokes a reply message handler implementation with this reply message.
 *
 * Class can either define a static reply destination or a temporary reply destination.
 *
 * @author Christoph Deppisch
 * @deprecated
 */
public class JmsSyncMessageSender implements MessageSender, BeanNameAware, DisposableBean {

    /** New synchronous JmsEndpoint implementation */
    private JmsSyncEndpoint jmsEndpoint = new JmsSyncEndpoint();

    /** Reply message handler */
    private ReplyMessageHandler replyMessageHandler;

    /**
     * @see com.consol.citrus.message.MessageSender#send(org.springframework.integration.Message)
     * @throws CitrusRuntimeException
     */
    public void send(Message<?> message) {
        jmsEndpoint.createProducer().send(message);
    }

    @Override
    public Consumer createConsumer() {
        return jmsEndpoint.createConsumer();
    }

    @Override
    public Producer createProducer() {
        return jmsEndpoint.createProducer();
    }

    @Override
    public EndpointConfiguration getEndpointConfiguration() {
        return jmsEndpoint.getEndpointConfiguration();
    }

    /**
     * Destroy method closing JMS session and connection
     */
    public void destroy() throws Exception {
        jmsEndpoint.destroy();
    }

    /**
     * Set the connection factory.
     * @param connectionFactory the connectionFactory to set
     */
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        jmsEndpoint.setConnectionFactory(connectionFactory);
    }

    /**
     * Set the reply message handler.
     * @param replyMessageHandler the replyMessageHandler to set
     */
    public void setReplyMessageHandler(ReplyMessageHandler replyMessageHandler) {
        this.replyMessageHandler = replyMessageHandler;

        if (replyMessageHandler instanceof JmsReplyMessageReceiver) {
            ((JmsReplyMessageReceiver) replyMessageHandler).setEndpoint(jmsEndpoint);
        }
    }

    /**
     * Get the reply message handler.
     * @return the replyMessageHandler
     */
    public ReplyMessageHandler getReplyMessageHandler() {
        return replyMessageHandler;
    }

    /**
     * Set the send destination.
     * @param destination the destination to set
     */
    public void setDestination(Destination destination) {
        jmsEndpoint.setDestination(destination);
    }

    /**
     * Set the send destination name.
     * @param destinationName the destinationName to set
     */
    public void setDestinationName(String destinationName) {
        jmsEndpoint.setDestinationName(destinationName);
    }

    /**
     * Set the reply destination.
     * @param replyDestination the replyDestination to set
     */
    public void setReplyDestination(Destination replyDestination) {
        jmsEndpoint.setReplyDestination(replyDestination);
    }

    /**
     * Set the reply destination name.
     * @param replyDestinationName the replyDestinationName to set
     */
    public void setReplyDestinationName(String replyDestinationName) {
        jmsEndpoint.setReplyDestinationName(replyDestinationName);
    }

    /**
     * Set the reply message timeout.
     * @param replyTimeout the replyTimeout to set
     */
    public void setReplyTimeout(long replyTimeout) {
        jmsEndpoint.setTimeout(replyTimeout);
    }

    /**
     * Set the reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        jmsEndpoint.setCorrelator(correlator);
    }

    /**
     * Set whether to use JMS topics instead of JMS queues.
     * @param pubSubDomain the pubSubDomain to set
     */
    public void setPubSubDomain(boolean pubSubDomain) {
        jmsEndpoint.setPubSubDomain(pubSubDomain);
    }

    /**
     * Is this sender using JMS topics instead of JMS queues.
     * @return the pubSubDomain
     */
    public boolean isPubSubDomain() {
        return jmsEndpoint.isPubSubDomain();
    }

    /**
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name) {
        jmsEndpoint.setBeanName(name);
    }

    /**
     * Gets the connectionFactory.
     * @return the connectionFactory
     */
    public ConnectionFactory getConnectionFactory() {
        return jmsEndpoint.getConnectionFactory();
    }

    /**
     * Gets the destination.
     * @return the destination
     */
    public Destination getDestination() {
        return jmsEndpoint.getDestination();
    }

    /**
     * Gets the destinationName.
     * @return the destinationName
     */
    public String getDestinationName() {
        return jmsEndpoint.getDestinationName();
    }

    /**
     * Gets the replyDestination.
     * @return the replyDestination
     */
    public Destination getReplyDestination() {
        return jmsEndpoint.getReplyDestination();
    }

    /**
     * Gets the replyDestinationName.
     * @return the replyDestinationName
     */
    public String getReplyDestinationName() {
        return jmsEndpoint.getReplyDestinationName();
    }

    /**
     * Gets the replyTimeout.
     * @return the replyTimeout
     */
    public long getReplyTimeout() {
        return jmsEndpoint.getTimeout();
    }

    /**
     * Gets the correlator.
     * @return the correlator
     */
    public ReplyMessageCorrelator getCorrelator() {
        return jmsEndpoint.getCorrelator();
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
    public String getName() {
        return jmsEndpoint.getName();
    }

    @Override
    public void setName(String name) {
        jmsEndpoint.setName(name);
    }
}
