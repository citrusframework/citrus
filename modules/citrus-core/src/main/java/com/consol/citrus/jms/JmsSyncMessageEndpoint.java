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

import com.consol.citrus.endpoint.ReplyEndpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.CitrusMessageHeaders;
import com.consol.citrus.message.ReplyMessageCorrelator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.integration.Message;
import org.springframework.integration.jms.JmsHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.jms.connection.ConnectionFactoryUtils;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Synchronous Jms message endpoint. When sending messages endpoint sets replyTo message header and waits for synchronous response.
 * When receiving messages endpoint reads replyTo header from incoming request and sends synchronous response back.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class JmsSyncMessageEndpoint extends JmsMessageEndpoint implements ReplyEndpoint, DisposableBean {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JmsSyncMessageEndpoint.class);

    /** Map of reply destinations */
    private Map<String, Destination> replyDestinations = new HashMap<String, Destination>();

    /** Store of reply messages */
    private Map<String, Message<?>> replyMessages = new HashMap<String, Message<?>>();

    /** Reply message correlator */
    private ReplyMessageCorrelator correlator = null;

    /** JMS connection */
    private Connection connection = null;

    /** JMS session */
    private Session session = null;

    /** Reply destination */
    private Destination replyDestination;

    /** Reply destination name */
    private String replyDestinationName;

    /** Polling interval when waiting for synchronous reply message to arrive */
    private long pollingInterval = 500;

    /** Retry logger */
    private static final Logger RETRY_LOG = LoggerFactory.getLogger("com.consol.citrus.MessageRetryLogger");

    @Override
    public Message<?> receive(String selector, long timeout) {
        Message<?> receivedMessage = super.receive(selector, timeout);
        saveReplyDestination(receivedMessage);

        return receivedMessage;
    }

    @Override
    public void send(Message<?> message) {
        Assert.notNull(message, "Message is empty - unable to send empty message");

        String defaultDestinationName = getDefaultDestinationName();

        log.info("Sending JMS message to destination: '" + defaultDestinationName + "'");

        onOutboundMessage(message);

        MessageProducer messageProducer = null;
        MessageConsumer messageConsumer = null;
        Destination replyToDestination = null;

        try {
            createConnection();
            createSession(connection);

            JmsMessageConverter jmsMessageConverter = new JmsMessageConverter(getMessageConverter(), getHeaderMapper());
            javax.jms.Message jmsRequest = jmsMessageConverter.toMessage(message, session);

            messageProducer = session.createProducer(getDefaultDestination(session));

            replyToDestination = getReplyDestination(session, message);
            if (replyToDestination instanceof TemporaryQueue || replyToDestination instanceof TemporaryTopic) {
                messageConsumer = session.createConsumer(replyToDestination);
            }

            jmsRequest.setJMSReplyTo(replyToDestination);

            messageProducer.send(jmsRequest);

            if (messageConsumer == null) {
                messageConsumer = createMessageConsumer(replyToDestination, jmsRequest.getJMSMessageID());
            }

            log.info("Message was successfully sent to destination: '{}'", defaultDestinationName);
            log.info("Waiting for reply message on destination: '{}'", replyToDestination);

            javax.jms.Message jmsReplyMessage = (getTimeout() >= 0) ? messageConsumer.receive(getTimeout()) : messageConsumer.receive();
            Message<?> responseMessage = (Message<?>)jmsMessageConverter.fromMessage(jmsReplyMessage);

            log.info("Received reply message on destination: '{}'", replyToDestination);

            onInboundMessage(responseMessage);

            saveReplyMessage(message, responseMessage);
        } catch (JMSException e) {
            throw new CitrusRuntimeException(e);
        } finally {
            JmsUtils.closeMessageProducer(messageProducer);
            JmsUtils.closeMessageConsumer(messageConsumer);
            deleteTemporaryDestination(replyToDestination);
        }
    }

    @Override
    public Message<?> receiveReplyMessage(String correlationKey, long timeout) {
        long timeLeft = timeout;
        Message<?> message = findReplyMessage(correlationKey);

        while (message == null && timeLeft > 0) {
            timeLeft -= pollingInterval;

            if (RETRY_LOG.isDebugEnabled()) {
                RETRY_LOG.debug("Reply message did not arrive yet - retrying in " + (timeLeft > 0 ? pollingInterval : pollingInterval + timeLeft) + "ms");
            }

            try {
                Thread.sleep(timeLeft > 0 ? pollingInterval : pollingInterval + timeLeft);
            } catch (InterruptedException e) {
                RETRY_LOG.warn("Thread interrupted while waiting for retry", e);
            }

            message = findReplyMessage(correlationKey);
        }

        return message;
    }

    @Override
    public void sendReplyMessage(Message<?> message) {
        Assert.notNull(message, "Message is empty - unable to send empty message");

        Destination replyDestination;
        Message<?> replyMessage;

        if (correlator != null) {
            Assert.notNull(message.getHeaders().get(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR), "Can not correlate reply destination - " +
                    "you need to set " + CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR + " in message header");

            String correlationKey = correlator.getCorrelationKey(message.getHeaders().get(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR).toString());
            replyDestination = replyDestinations.remove(correlationKey);
            Assert.notNull(replyDestination, "Unable to locate JMS reply destination with correlation key: '" + correlationKey + "'");

            //remove citrus specific header from message
            replyMessage = MessageBuilder.fromMessage(message).removeHeader(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR).build();
        } else {
            replyMessage = message;
            replyDestination = replyDestinations.remove("");
            Assert.notNull(replyDestination, "Unable to locate JMS reply destination");
        }

        log.info("Sending JMS message to destination: '" + getDestinationName(replyDestination) + "'");

        getJmsTemplate().convertAndSend(replyDestination, replyMessage);

        onOutboundMessage(replyMessage);

        log.info("Message was successfully sent to destination: '" + getDestinationName(replyDestination) + "'");
    }

    /**
     * Store the reply destination either straight forward or with a given
     * message correlation key.
     *
     * @param receivedMessage
     */
    public void saveReplyDestination(Message<?> receivedMessage) {
        if (correlator != null) {
            replyDestinations.put(correlator.getCorrelationKey(receivedMessage), (Destination)receivedMessage.getHeaders().get(JmsHeaders.REPLY_TO));
        } else {
            replyDestinations.put("", (Destination)receivedMessage.getHeaders().get(JmsHeaders.REPLY_TO));
        }
    }

    /**
     * Saves reply message with correlation key to local store for later processing.
     * @param correlationKey
     * @param replyMessage the reply message.
     */
    public void saveReplyMessage(String correlationKey, Message<?> replyMessage) {
        replyMessages.put(correlationKey, replyMessage);
    }

    /**
     * Saves reply message to local store for later processing. Constructs correlation key from initial request.
     * @param requestMessage
     * @param replyMessage
     */
    public void saveReplyMessage(Message<?> requestMessage, Message<?> replyMessage) {
        if (correlator != null) {
            saveReplyMessage(correlator.getCorrelationKey(requestMessage), replyMessage);
        } else {
            saveReplyMessage("", replyMessage);
        }
    }

    /**
     * Create new JMS connection.
     * @return connection
     * @throws JMSException
     */
    protected void createConnection() throws JMSException {
        if (connection == null) {
            if (!isPubSubDomain() && getConnectionFactory() instanceof QueueConnectionFactory) {
                connection = ((QueueConnectionFactory) getConnectionFactory()).createQueueConnection();
            } else if (isPubSubDomain() && getConnectionFactory() instanceof TopicConnectionFactory) {
                connection = ((TopicConnectionFactory) getConnectionFactory()).createTopicConnection();
                connection.setClientID(getName());
            } else {
                log.warn("Not able to create a connection with connection factory '" + getConnectionFactory() + "'" +
                        " when using setting 'publish-subscribe-domain' (=" + isPubSubDomain() + ")");

                connection = getConnectionFactory().createConnection();
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
            if (!isPubSubDomain() && connection instanceof QueueConnection) {
                session = ((QueueConnection) connection).createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            } else if (isPubSubDomain() && getConnectionFactory() instanceof TopicConnectionFactory) {
                session = ((TopicConnection) connection).createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            } else {
                log.warn("Not able to create a session with connection factory '" + getConnectionFactory() + "'" +
                        " when using setting 'publish-subscribe-domain' (=" + isPubSubDomain() + ")");

                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
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

        if (replyToDestination instanceof Queue) {
            messageConsumer = session.createConsumer(replyToDestination,
                    "JMSCorrelationID = '" + messageId.replaceAll("'", "''") + "'");
        } else {
            messageConsumer = session.createDurableSubscriber((Topic)replyToDestination, getName(),
                    "JMSCorrelationID = '" + messageId.replaceAll("'", "''") + "'", false);
        }

        return messageConsumer;
    }

    /**
     * Delete temporary destinations.
     * @param destination
     */
    private void deleteTemporaryDestination(Destination destination) {
        log.debug("Delete temporary destination: '{}'", destination);

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

        if (isPubSubDomain() && session instanceof TopicSession){
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
        if (getDestination() != null) {
            return getDestination();
        }

        return resolveDestinationName(getDestinationName(), session);
    }

    /**
     * Resolves the destination name from Jms session.
     * @param name
     * @param session
     * @return
     */
    private Destination resolveDestinationName(String name, Session session) throws JMSException {
        return new DynamicDestinationResolver().resolveDestinationName(session, name, isPubSubDomain());
    }

    /**
     * Get the destination name (either a queue name or a topic name).
     * @return the destinationName
     */
    private String getDestinationName(Destination destination) {
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
                return null;
            }
        } catch (JMSException e) {
            log.error("Error while getting destination name", e);
            return "";
        }
    }

    /**
     * Destroy method closing JMS session and connection
     */
    public void destroy() throws Exception {
        JmsUtils.closeSession(session);

        if (connection != null) {
            ConnectionFactoryUtils.releaseConnection(connection, getConnectionFactory(), true);
        }
    }

    /**
     * Tries to find reply message for correlation key from local store.
     * @param correlationKey
     * @return
     */
    public Message<?> findReplyMessage(String correlationKey) {
        return replyMessages.remove(correlationKey);
    }

    /**
     * Finds reply destination by correlation key in destination store.
     * @param correlationKey
     * @return
     */
    public Destination findReplyDestination(String correlationKey) {
        return replyDestinations.remove(correlationKey);
    }

    /**
     * Finds reply destination by default correlation key in destination store.
     * @return
     */
    public Destination findReplyDestination() {
        return replyDestinations.remove("");
    }

    /**
     * Set the reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        this.correlator = correlator;
    }

    /**
     * Gets the correlator.
     * @return the correlator
     */
    public ReplyMessageCorrelator getCorrelator() {
        return correlator;
    }

    /**
     * Gets the replyDestination.
     * @return the replyDestination
     */
    public Destination getReplyDestination() {
        return replyDestination;
    }

    /**
     * Set the reply destination.
     * @param replyDestination the replyDestination to set
     */
    public void setReplyDestination(Destination replyDestination) {
        this.replyDestination = replyDestination;
    }

    /**
     * Gets the replyDestinationName.
     * @return the replyDestinationName
     */
    public String getReplyDestinationName() {
        return replyDestinationName;
    }

    /**
     * Set the reply destination name.
     * @param replyDestinationName the replyDestinationName to set
     */
    public void setReplyDestinationName(String replyDestinationName) {
        this.replyDestinationName = replyDestinationName;
    }

    /**
     * Gets the pollingInterval.
     * @return the pollingInterval the pollingInterval to get.
     */
    public long getPollingInterval() {
        return pollingInterval;
    }

    /**
     * Sets the pollingInterval.
     * @param pollingInterval the pollingInterval to set
     */
    public void setPollingInterval(long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }
}
