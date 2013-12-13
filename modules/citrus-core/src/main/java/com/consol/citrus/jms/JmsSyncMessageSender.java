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
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.*;
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
    private JmsSyncMessageEndpoint jmsMessageEndpoint = new JmsSyncMessageEndpoint();

    /** Reply message handler */
    private ReplyMessageHandler replyMessageHandler;

    /**
     * @see com.consol.citrus.message.MessageSender#send(org.springframework.integration.Message)
     * @throws CitrusRuntimeException
     */
    public void send(Message<?> message) {
        jmsMessageEndpoint.send(message);
    }

    /**
     * Destroy method closing JMS session and connection
     */
    public void destroy() throws Exception {
        jmsMessageEndpoint.destroy();
    }

    /**
     * Set the connection factory.
     * @param connectionFactory the connectionFactory to set
     */
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        jmsMessageEndpoint.setConnectionFactory(connectionFactory);
    }

    /**
     * Set the reply message handler.
     * @param replyMessageHandler the replyMessageHandler to set
     */
    public void setReplyMessageHandler(ReplyMessageHandler replyMessageHandler) {
        this.replyMessageHandler = replyMessageHandler;

        if (replyMessageHandler instanceof JmsReplyMessageReceiver) {
            ((JmsReplyMessageReceiver) replyMessageHandler).setJmsEndpoint(jmsMessageEndpoint);
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
        jmsMessageEndpoint.setDestination(destination);
    }

    /**
     * Set the send destination name.
     * @param destinationName the destinationName to set
     */
    public void setDestinationName(String destinationName) {
        jmsMessageEndpoint.setDestinationName(destinationName);
    }

    /**
     * Set the reply destination.
     * @param replyDestination the replyDestination to set
     */
    public void setReplyDestination(Destination replyDestination) {
        jmsMessageEndpoint.setReplyDestination(replyDestination);
    }

    /**
     * Set the reply destination name.
     * @param replyDestinationName the replyDestinationName to set
     */
    public void setReplyDestinationName(String replyDestinationName) {
        jmsMessageEndpoint.setReplyDestinationName(replyDestinationName);
    }

    /**
     * Set the reply message timeout.
     * @param replyTimeout the replyTimeout to set
     */
    public void setReplyTimeout(long replyTimeout) {
        jmsMessageEndpoint.setTimeout(replyTimeout);
    }

    /**
     * Set the reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        jmsMessageEndpoint.setCorrelator(correlator);
    }

    /**
     * Set whether to use JMS topics instead of JMS queues.
     * @param pubSubDomain the pubSubDomain to set
     */
    public void setPubSubDomain(boolean pubSubDomain) {
        jmsMessageEndpoint.setPubSubDomain(pubSubDomain);
    }

    /**
     * Is this sender using JMS topics instead of JMS queues.
     * @return the pubSubDomain
     */
    public boolean isPubSubDomain() {
        return jmsMessageEndpoint.isPubSubDomain();
    }

    /**
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name) {
        jmsMessageEndpoint.setBeanName(name);
    }

    /**
     * Gets the connectionFactory.
     * @return the connectionFactory
     */
    public ConnectionFactory getConnectionFactory() {
        return jmsMessageEndpoint.getConnectionFactory();
    }

    /**
     * Gets the destination.
     * @return the destination
     */
    public Destination getDestination() {
        return jmsMessageEndpoint.getDestination();
    }

    /**
     * Gets the destinationName.
     * @return the destinationName
     */
    public String getDestinationName() {
        return jmsMessageEndpoint.getDestinationName();
    }

    /**
     * Gets the replyDestination.
     * @return the replyDestination
     */
    public Destination getReplyDestination() {
        return jmsMessageEndpoint.getReplyDestination();
    }

    /**
     * Gets the replyDestinationName.
     * @return the replyDestinationName
     */
    public String getReplyDestinationName() {
        return jmsMessageEndpoint.getReplyDestinationName();
    }

    /**
     * Gets the replyTimeout.
     * @return the replyTimeout
     */
    public long getReplyTimeout() {
        return jmsMessageEndpoint.getTimeout();
    }

    /**
     * Gets the correlator.
     * @return the correlator
     */
    public ReplyMessageCorrelator getCorrelator() {
        return jmsMessageEndpoint.getCorrelator();
    }

    /**
     * Gets the actor.
     * @return the actor the actor to get.
     */
    public TestActor getActor() {
        return jmsMessageEndpoint.getActor();
    }

    /**
     * Sets the actor.
     * @param actor the actor to set
     */
    public void setActor(TestActor actor) {
        jmsMessageEndpoint.setActor(actor);
    }

    @Override
    public String getName() {
        return jmsMessageEndpoint.getName();
    }
}
