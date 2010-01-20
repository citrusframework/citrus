/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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
 * Action to purge JMS queue destinations by simply consuming all available messages.
 *
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 * @since 24.01.2007
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

    /** Purges a queue identified by its name. */
    private void purgeQueue(String queueName, Session session) throws JMSException {
        purgeDestination(getDestination(session, queueName), session, queueName);
    }

    /** Purges a queue identified by its instance. */
    private void purgeQueue(Queue queue, Session session) throws JMSException {
        purgeDestination(queue, session, queue.getQueueName());
    }


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
    
    
    private Destination getDestination(Session session, String queueName) throws JMSException {
    	return new DynamicDestinationResolver().resolveDestinationName(session, queueName, false);
	}

	/**
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
     * 
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
     * @param queueNames the queueNames to set
     */
    public void setQueueNames(List<String> queueNames) {
        this.queueNames = queueNames;
    }

    /**
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
     * @param queues The queues which are to be purged.
     */
    public void setQueues(List<Queue> queues) {
		this.queues = queues;
	}

    /**
     * @param receiveTimeout the receiveTimeout to set
     */
    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

}
