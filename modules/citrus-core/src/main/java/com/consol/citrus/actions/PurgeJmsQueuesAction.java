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

package com.consol.citrus.actions;

import java.util.Collections;
import java.util.List;

import javax.jms.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.connection.ConnectionFactoryUtils;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Action to purge JMS queue destinations by simply consuming 
 * all available messages. As queue purging is a broker implementation specific feature in
 * many cases this action clears all messages from a destination regardless of
 * JMS broker vendor implementations.
 *
 * Receiver will continue to receive messages until message receive timeout is reached, 
 * so no messages are left.
 *  
 * @author Christoph Deppisch
 * @since 2007
 */
public class PurgeJmsQueuesAction extends AbstractTestAction {

    /** List of queue names to be purged */
    private List<String> queueNames = Collections.emptyList();

    /** List of queues to be purged */
    private List<Queue> queues = Collections.emptyList();
    
    /** ConnectionFactory */
    private ConnectionFactory connectionFactory;

    /** Time to wait until timeout in ms */
    private long receiveTimeout = 100;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(PurgeJmsQueuesAction.class);

    /**
     * @see com.consol.citrus.TestAction#execute(TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        log.info("Purging JMS queues...");

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
            log.error("Error while establishing jms queue connection", e);
            throw new CitrusRuntimeException(e);
        } finally {
            JmsUtils.closeSession(session);

            if(connection != null) {
                ConnectionFactoryUtils.releaseConnection(connection, this.connectionFactory, true);
            }
        }

        log.info("JMS queues purged successfully");
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
            log.debug("Try to purge queue " + destinationName);
        }

        MessageConsumer messageConsumer = null;
        try {
            javax.jms.Message message;
            do {
                messageConsumer = session.createConsumer(destination);

                message = (receiveTimeout >= 0) ? messageConsumer
                        .receive(receiveTimeout) : messageConsumer.receive();
    
                if (message != null && log.isDebugEnabled()) {
                    log.debug("Removed message from queue " + destinationName);
                }
            } while (message != null);
    
            JmsUtils.closeMessageConsumer(messageConsumer);
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
     * @param queueConnectionFactory the queueConnectionFactory to set
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

}
