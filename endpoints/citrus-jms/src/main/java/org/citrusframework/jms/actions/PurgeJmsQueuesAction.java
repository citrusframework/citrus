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

package org.citrusframework.jms.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Queue;
import jakarta.jms.QueueConnection;
import jakarta.jms.QueueConnectionFactory;
import jakarta.jms.Session;
import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

/**
 * Action to purge JMS queue destinations by simply consuming
 * all available messages. As queue purging is a broker implementation specific feature in
 * many cases this action clears all messages from a destination regardless of
 * JMS broker vendor implementations.
 *
 * Consumer will continue to receive messages until message receive timeout is reached,
 * so no messages are left.
 *
 * @author Christoph Deppisch
 * @since 2007
 */
public class PurgeJmsQueuesAction extends AbstractTestAction {

    /** List of queue names to be purged */
    private final List<String> queueNames;

    /** List of queues to be purged */
    private final List<Queue> queues;

    /** ConnectionFactory */
    private final ConnectionFactory connectionFactory;

    /** Time to wait until timeout in ms */
    private final long receiveTimeout;

    /** Wait some time between message consumption in ms */
    private final long sleepTime;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(PurgeJmsQueuesAction.class);

    /**
     * Default constructor.
     */
    public PurgeJmsQueuesAction(Builder builder) {
        super("purge-queue", builder);

        this.queueNames = builder.queueNames;
        this.queues = builder.queues;
        this.connectionFactory = builder.connectionFactory;
        this.receiveTimeout = builder.receiveTimeout;
        this.sleepTime = builder.sleepTime;
    }

    @SuppressWarnings("PMD.CloseResource") //suppress since session/connection closed via JmsUtils
    @Override
    public void doExecute(TestContext context) {
        logger.debug("Purging JMS queues...");

        Connection connection = null;
        Session session = null;

        try {
        	connection = createConnection();
            session = createSession(connection);
            connection.start();

            for (Queue queue : queues) {
                purgeQueue(queue, session);
            }
            for (String queueName : queueNames) {
                purgeQueue(context.replaceDynamicContentInString(queueName), session);
            }

        } catch (JMSException e) {
            logger.error("Error while establishing jms connection", e);
            throw new CitrusRuntimeException(e);
        } finally {
            JmsUtils.closeSession(session);
            JmsUtils.closeConnection(connection, true);
        }

        logger.info("Purged JMS queues");
    }

    /**
     * Purges a queue destination identified by its name.
     * @param queueName
     * @param session
     * @throws JMSException
     */
    private void purgeQueue(String queueName, Session session) throws JMSException {
        purgeDestination(getDestination(session, queueName), session, queueName);
    }

    /**
     * Purges a queue destination.
     * @param queue
     * @param session
     * @throws JMSException
     */
    private void purgeQueue(Queue queue, Session session) throws JMSException {
        purgeDestination(queue, session, queue.getQueueName());
    }

    /**
     * Purge destination by receiving all available messages.
     * @param destination
     * @param session
     * @param destinationName
     * @throws JMSException
     */
    private void purgeDestination(Destination destination, Session session, String destinationName) throws JMSException {
        if (logger.isDebugEnabled()) {
            logger.debug("Try to purge destination " + destinationName);
        }

        int messagesPurged = 0;
        MessageConsumer messageConsumer = session.createConsumer(destination);
        try {
            jakarta.jms.Message message;
            do {
                message = (receiveTimeout >= 0) ? messageConsumer.receive(receiveTimeout) : messageConsumer.receive();

                if (message != null) {
                    logger.debug("Removed message from destination " + destinationName);
                    messagesPurged++;

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        logger.warn("Interrupted during wait", e);
                    }
                }
            } while (message != null);

            if (logger.isDebugEnabled()) {
                logger.debug("Purged " + messagesPurged + " messages from destination");
            }
        } finally {
            JmsUtils.closeMessageConsumer(messageConsumer);
        }
    }

    /**
     * Resolves destination by given name.
     * @param session
     * @param queueName
     * @return
     * @throws JMSException
     */
    private Destination getDestination(Session session, String queueName) throws JMSException {
    	return new DynamicDestinationResolver().resolveDestinationName(session, queueName, false);
	}

	/**
	 * Create queue connection.
     * @return
     * @throws JMSException
     */
    protected Connection createConnection() throws JMSException {
        if (connectionFactory instanceof QueueConnectionFactory) {
            return ((QueueConnectionFactory) connectionFactory).createQueueConnection();
        }
        return connectionFactory.createConnection();
    }

    /**
     * Create queue session.
     * @param connection
     * @return
     * @throws JMSException
     */
    protected Session createSession(Connection connection) throws JMSException {
        if (connection instanceof QueueConnection) {
            return ((QueueConnection) connection).createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        }
        return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    /**
     * @return the queueNames
     */
    public List<String> getQueueNames() {
        return queueNames;
    }

    /**
     * Gets the queues.
     * @return the queues
     */
    public List<Queue> getQueues() {
        return queues;
    }

    /**
     * Gets the connectionFactory.
     * @return the connectionFactory
     */
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * Gets the receiveTimeout.
     * @return the receiveTimeout
     */
    public long getReceiveTimeout() {
        return receiveTimeout;
    }

    /**
     * Gets the sleepTime.
     * @return the sleepTime the sleepTime to get.
     */
    public long getSleepTime() {
        return sleepTime;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractTestActionBuilder<PurgeJmsQueuesAction, Builder> implements ReferenceResolverAware {

        private final List<String> queueNames = new ArrayList<>();
        private final List<Queue> queues = new ArrayList<>();
        private ConnectionFactory connectionFactory;
        private long receiveTimeout = 100;
        private long sleepTime = 350;

        private ReferenceResolver referenceResolver;

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder purgeQueues() {
            return new Builder();
        }

        /**
         * Sets the Connection factory.
         * @param connectionFactory the queueConnectionFactory to set
         */
        public Builder connectionFactory(ConnectionFactory connectionFactory) {
            this.connectionFactory = connectionFactory;
            return this;
        }

        /**
         * List of queues to purge in this action.
         * @param queues The queues which are to be purged.
         */
        public Builder queues(List<Queue> queues) {
            this.queues.addAll(queues);
            return this;
        }

        /**
         * List of queues to purge in this action.
         * @param queues
         * @return
         */
        public Builder queues(Queue... queues) {
            return queues(Arrays.asList(queues));
        }

        /**
         * Adds a new queue to the list of queues to purge in this action.
         * @param queue
         * @return
         */
        public Builder queue(Queue queue) {
            this.queues.add(queue);
            return this;
        }

        /**
         * List of queue names to purge in this action.
         * @param names the queueNames to set
         */
        public Builder queueNames(List<String> names) {
            this.queueNames.addAll(names);
            return this;
        }

        /**
         * List of queue names to purge in this action.
         * @param names
         * @return
         */
        public Builder queueNames(String... names) {
            return queueNames(Arrays.asList(names));
        }

        /**
         * Adds a queue name to the list of queues to purge in this action.
         * @param name
         * @return
         */
        public Builder queue(String name) {
            this.queueNames.add(name);
            return this;
        }

        /**
         * Receive timeout for reading message from a destination.
         * @param receiveTimeout the receiveTimeout to set
         */
        public Builder timeout(long receiveTimeout) {
            this.receiveTimeout = receiveTimeout;
            return this;
        }

        /**
         * Sets the sleepTime.
         * @param millis the sleepTime to set
         */
        public Builder sleep(long millis) {
            this.sleepTime = millis;
            return this;
        }

        /**
         * Sets the bean reference resolver for using endpoint names.
         * @param referenceResolver
         */
        public Builder withReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
            return this;
        }

        @Override
        public PurgeJmsQueuesAction build() {
            if (connectionFactory == null &&
                    referenceResolver != null
                    && referenceResolver.isResolvable("connectionFactory")) {
                connectionFactory(referenceResolver.resolve("connectionFactory", ConnectionFactory.class));
            }

            return new PurgeJmsQueuesAction(this);
        }

        @Override
        public void setReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
        }
    }

}
