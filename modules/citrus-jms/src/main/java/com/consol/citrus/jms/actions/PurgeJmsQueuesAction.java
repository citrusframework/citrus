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

package com.consol.citrus.jms.actions;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

import javax.jms.*;
import java.util.ArrayList;
import java.util.List;

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
    private List<String> queueNames = new ArrayList<>();

    /** List of queues to be purged */
    private List<Queue> queues = new ArrayList<>();
    
    /** ConnectionFactory */
    private ConnectionFactory connectionFactory;

    /** Time to wait until timeout in ms */
    private long receiveTimeout = 100;
    
    /** Wait some time between message consumption in ms */
    private long sleepTime = 350;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(PurgeJmsQueuesAction.class);

    /**
     * Default constructor.
     */
    public PurgeJmsQueuesAction() {
        setName("purge-queue");
    }

    @SuppressWarnings("PMD.CloseResource") //suppress since session/connection closed via JmsUtils
    @Override
    public void doExecute(TestContext context) {
        log.debug("Purging JMS queues...");
        
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
                purgeQueue(queueName, session);
            }

        } catch (JMSException e) {
            log.error("Error while establishing jms connection", e);
            throw new CitrusRuntimeException(e);
        } finally {
            JmsUtils.closeSession(session);
            JmsUtils.closeConnection(connection, true);
        }

        log.info("Purged JMS queues");
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
        if (log.isDebugEnabled()) {
            log.debug("Try to purge destination " + destinationName);
        }

        int messagesPurged = 0;
        MessageConsumer messageConsumer = session.createConsumer(destination);
        try {
            javax.jms.Message message;
            do {
                message = (receiveTimeout >= 0) ? messageConsumer.receive(receiveTimeout) : messageConsumer.receive();
    
                if (message != null) {
                    log.debug("Removed message from destination " + destinationName);
                    messagesPurged++;

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        log.warn("Interrupted during wait", e);
                    }
                }
            } while (message != null);

            if (log.isDebugEnabled()) {
                log.debug("Purged " + messagesPurged + " messages from destination");
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
     * List of queue names to purge. 
     * @param queueNames the queueNames to set
     */
    public void setQueueNames(List<String> queueNames) {
        this.queueNames = queueNames;
    }

    /**
     * Connection factory.
     * @param connectionFactory the connectionFactory to set
     */
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * @return the queueNames
     */
    public List<String> getQueueNames() {
        return queueNames;
    }

    /**
     * List of queues.
     * @param queues The queues which are to be purged.
     */
    public void setQueues(List<Queue> queues) {
		this.queues = queues;
	}

    /**
     * Receive timeout for reading message from a destination.
     * @param receiveTimeout the receiveTimeout to set
     */
    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
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
     * Sets the sleepTime.
     * @param sleepTime the sleepTime to set
     */
    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    /**
     * Gets the sleepTime.
     * @return the sleepTime the sleepTime to get.
     */
    public long getSleepTime() {
        return sleepTime;
    }

}
