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

import javax.jms.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.jms.DefaultJmsHeaderMapper;
import org.springframework.integration.jms.JmsHeaderMapper;
import org.springframework.jms.connection.ConnectionFactoryUtils;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.*;
import com.consol.citrus.report.MessageTracingTestListener;

/**
 * Synchronous message sender implementation for JMS. Sender publishes messages to a JMS destination and
 * sets the reply destination in the request message. Sender consumes the reply destination right away and
 * invokes a reply message handler implementation with this reply message.
 *
 * Class can either define a static reply destination or a temporary reply destination.
 *
 * @author Christoph Deppisch
 */
public class JmsSyncMessageSender implements MessageSender, BeanNameAware, DisposableBean {
    /** JMS connection factory */
    private ConnectionFactory connectionFactory;

    /** JMS connection */
    private Connection connection = null;

    /** JMS session */
    private Session session = null;

    /** Destination instance */
    private Destination destination;

    /** Destination name */
    private String destinationName;

    /** Reply destination */
    private Destination replyDestination;

    /** Reply destination name */
    private String replyDestinationName;

    /** Reply message handler */
    private ReplyMessageHandler replyMessageHandler;

    /** The message converter */
    private MessageConverter messageConverter = new SimpleMessageConverter();

    /** The header mapper */
    private JmsHeaderMapper headerMapper = new DefaultJmsHeaderMapper();

    /** Time to synchronously wait for reply */
    private long replyTimeout = 5000L;

    /** Reply message corelator */
    private ReplyMessageCorrelator correlator = null;

    /** Use JMS topics instead of queues */
    private boolean pubSubDomain = false;

    /** Message sender name */
    private String name;
    
    @Autowired(required=false)
    private MessageTracingTestListener messageTracingTestListener;

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(JmsSyncMessageSender.class);

    /**
     * @see com.consol.citrus.message.MessageSender#send(org.springframework.integration.Message)
     * @throws CitrusRuntimeException
     */
    public void send(Message<?> message) {
        Assert.notNull(message, "Message is empty - unable to send empty message");

        String defaultDestinationName = getDefaultDestinationName();

        log.info("Sending JMS message to destination: '" + defaultDestinationName + "'");

        if (log.isDebugEnabled()) {
            log.debug("Message to send is:\n" + message.toString());
        }
        
        if (messageTracingTestListener != null) {
            messageTracingTestListener.traceMessage("Send synchronous JMS message:\n" + message.toString());
        }

        MessageProducer messageProducer = null;
        MessageConsumer messageConsumer = null;
        Destination replyToDestination = null;

        try {
            createConnection();
            createSession(connection);

            JmsMessageConverter jmsMessageConverter = new JmsMessageConverter(messageConverter, headerMapper);
            javax.jms.Message jmsRequest = jmsMessageConverter.toMessage(message, session);

            messageProducer = session.createProducer(getDefaultDestination(session));

            replyToDestination = getReplyDestination(session, message);
            jmsRequest.setJMSReplyTo(replyToDestination);

            messageConsumer = createMessageConsumer(replyToDestination, jmsRequest.getJMSMessageID());

            messageProducer.send(jmsRequest);

            log.info("Message was successfully sent to destination: '{}'", defaultDestinationName);
            log.info("Waiting for reply message on destination: '{}'", replyToDestination);

            javax.jms.Message jmsReplyMessage = (replyTimeout >= 0) ? messageConsumer.receive(replyTimeout) : messageConsumer.receive();

            log.info("Received reply message from destination: '{}'", replyToDestination);

            informReplyMessageHandler((Message<?>)jmsMessageConverter.fromMessage(jmsReplyMessage), message);
        } catch (JMSException e) {
            throw new CitrusRuntimeException(e);
        } finally {
            JmsUtils.closeMessageProducer(messageProducer);
            JmsUtils.closeMessageConsumer(messageConsumer);
            deleteTemporaryDestination(replyToDestination);
        }
    }

    /**
     * Informs reply message handler for further processing
     * of reply message.
     * @param responseMessage the reply message.
     * @param requestMessage the initial request message.
     */
    protected void informReplyMessageHandler(Message<?> responseMessage, Message<?> requestMessage) {
        if (messageTracingTestListener != null) {
            messageTracingTestListener.traceMessage("Received synchronous JMS reply message:\n" + responseMessage.toString());
        }
        
        if (replyMessageHandler != null) {
            log.info("Informing reply message handler for further processing");

            if (correlator != null) {
                replyMessageHandler.onReplyMessage(responseMessage, correlator.getCorrelationKey(requestMessage));
            } else {
                replyMessageHandler.onReplyMessage(responseMessage);
            }
        }
    }

    /**
     * Creates a message consumer on temporary/durable queue or topic. Durable queue/topic destinations
     * require a message selector to be set.
     *
     * @param replyToDestination the reply destination.
     * @param messageId the messageId used for optional message selector.
     * @return
     * @throws JMSException
     */
    private MessageConsumer createMessageConsumer(Destination replyToDestination, String messageId) throws JMSException {
        MessageConsumer messageConsumer;

        if (replyToDestination instanceof TemporaryQueue || replyToDestination instanceof TemporaryTopic) {
            messageConsumer = session.createConsumer(replyToDestination);
        } else if (replyToDestination instanceof Queue) {
            messageConsumer = session.createConsumer(replyToDestination,
                    "JMSCorrelationID = '" + messageId.replaceAll("'", "''") + "'");
        } else {
            messageConsumer = session.createDurableSubscriber((Topic)replyToDestination, name,
                    "JMSCorrelationID = '" + messageId.replaceAll("'", "''") + "'", false);
        }

        return messageConsumer;
    }

    /**
     * Delete temporary destinations.
     * @param destination
     */
    private void deleteTemporaryDestination(Destination destination) {
        try {
            if (destination instanceof TemporaryQueue) {
                ((TemporaryQueue) destination).delete();
            } else if (destination instanceof TemporaryTopic) {
                ((TemporaryTopic) destination).delete();
            }
        } catch (JMSException e) {
            log.error("Error while deleting temporary destination '" + destination + "'", e);
        }
    }

    /**
     * Retrieve the reply destination either by injected instance, destination name or
     * by creating a new temporary destination.
     *
     * @param session current JMS session
     * @param message holding possible reply destination in header.
     * @return the reply destination.
     * @throws JMSException
     */
    private Destination getReplyDestination(Session session, Message<?> message) throws JMSException {
        if (message.getHeaders().getReplyChannel() != null) {
            if (message.getHeaders().getReplyChannel() instanceof Destination) {
                return (Destination)message.getHeaders().getReplyChannel();
            } else {
                return resolveDestinationName(message.getHeaders().getReplyChannel().toString(), session);
            }
        } else if (replyDestination != null) {
            return replyDestination;
        } else if (StringUtils.hasText(replyDestinationName)) {
            return resolveDestinationName(this.replyDestinationName, session);
        }

        if (pubSubDomain && session instanceof TopicSession){
            return session.createTemporaryTopic();
        } else {
            return session.createTemporaryQueue();
        }
    }

    /**
     * Get send destination either from injected destination instance or by resolving
     * a destination name.
     *
     * @param session current JMS session
     * @return the destination.
     * @throws JMSException
     */
    private Destination getDefaultDestination(Session session) throws JMSException {
        if (destination != null) {
            return destination;
        }

        return resolveDestinationName(destinationName, session);
    }

    /**
     *
     * @param destinationName
     * @return
     */
    private Destination resolveDestinationName(String name, Session session) throws JMSException {
        return new DynamicDestinationResolver().resolveDestinationName(session, name, pubSubDomain);
    }

    /**
     * Get the destination name (either queue name or topic name).
     * @return the destinationName
     */
    protected String getDefaultDestinationName() {
        try {
            if (destination != null) {
                if (destination instanceof Queue) {
                    return ((Queue)destination).getQueueName();
                } else if (destination instanceof Topic) {
                    return ((Topic)destination).getTopicName();
                } else {
                    return destination.toString();
                }
            } else {
                return destinationName;
            }
        } catch (JMSException e) {
            log.error("Error while getting destination name", e);
            return "";
        }
    }

    /**
     * Create new JMS connection.
     * @return connection
     * @throws JMSException
     */
    protected void createConnection() throws JMSException {
        if (connection == null) {
            if (!pubSubDomain && connectionFactory instanceof QueueConnectionFactory) {
                connection = ((QueueConnectionFactory) connectionFactory).createQueueConnection();
            } else if (pubSubDomain && connectionFactory instanceof TopicConnectionFactory) {
                connection = ((TopicConnectionFactory) connectionFactory).createTopicConnection();
                connection.setClientID(name);
            } else {
                log.warn("Not able to create a connection with connection factory '" + connectionFactory + "'" +
                        " when using setting 'publish-subscribe-domain' (=" + pubSubDomain + ")");

                connection = connectionFactory.createConnection();
            }

            connection.start();
        }
    }

    /**
     * Create new JMS session.
     * @param connection to use for session creation.
     * @return session.
     * @throws JMSException
     */
    protected void createSession(Connection connection) throws JMSException {
        if (session == null) {
            if (!pubSubDomain && connection instanceof QueueConnection) {
                session = ((QueueConnection) connection).createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            } else if (pubSubDomain && connectionFactory instanceof TopicConnectionFactory) {
                session = ((TopicConnection) connection).createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            } else {
                log.warn("Not able to create a session with connection factory '" + connectionFactory + "'" +
                        " when using setting 'publish-subscribe-domain' (=" + pubSubDomain + ")");

                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            }
        }
    }

    /**
     * Destroy method closing JMS session and connection
     */
    public void destroy() throws Exception {
        JmsUtils.closeSession(session);

        if (connection != null) {
            ConnectionFactoryUtils.releaseConnection(connection, this.connectionFactory, true);
        }
    }

    /**
     * Set the connection factory.
     * @param connectionFactory the connectionFactory to set
     */
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Set the reply message handler.
     * @param replyMessageHandler the replyMessageHandler to set
     */
    public void setReplyMessageHandler(ReplyMessageHandler replyMessageHandler) {
        this.replyMessageHandler = replyMessageHandler;
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
        this.destination = destination;
    }

    /**
     * Set the send destination name.
     * @param destinationName the destinationName to set
     */
    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    /**
     * Set the reply destination.
     * @param replyDestination the replyDestination to set
     */
    public void setReplyDestination(Destination replyDestination) {
        this.replyDestination = replyDestination;
    }

    /**
     * Set the reply destination name.
     * @param replyDestinationName the replyDestinationName to set
     */
    public void setReplyDestinationName(String replyDestinationName) {
        this.replyDestinationName = replyDestinationName;
    }

    /**
     * Set the reply message timeout.
     * @param replyTimeout the replyTimeout to set
     */
    public void setReplyTimeout(long replyTimeout) {
        this.replyTimeout = replyTimeout;
    }

    /**
     * Set the reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        this.correlator = correlator;
    }

    /**
     * Set whether to use JMS topics instead of JMS queues.
     * @param pubSubDomain the pubSubDomain to set
     */
    public void setPubSubDomain(boolean pubSubDomain) {
        this.pubSubDomain = pubSubDomain;
    }

    /**
     * Is this sender using JMS topics instead of JMS queues.
     * @return the pubSubDomain
     */
    public boolean isPubSubDomain() {
        return pubSubDomain;
    }

    /**
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name) {
        this.name = name;
    }

    /**
     * Gets the connectionFactory.
     * @return the connectionFactory
     */
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * Gets the destination.
     * @return the destination
     */
    public Destination getDestination() {
        return destination;
    }

    /**
     * Gets the destinationName.
     * @return the destinationName
     */
    public String getDestinationName() {
        return destinationName;
    }

    /**
     * Gets the replyDestination.
     * @return the replyDestination
     */
    public Destination getReplyDestination() {
        return replyDestination;
    }

    /**
     * Gets the replyDestinationName.
     * @return the replyDestinationName
     */
    public String getReplyDestinationName() {
        return replyDestinationName;
    }

    /**
     * Gets the replyTimeout.
     * @return the replyTimeout
     */
    public long getReplyTimeout() {
        return replyTimeout;
    }

    /**
     * Gets the correlator.
     * @return the correlator
     */
    public ReplyMessageCorrelator getCorrelator() {
        return correlator;
    }
}
