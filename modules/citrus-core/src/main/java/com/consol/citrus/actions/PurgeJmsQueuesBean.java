package com.consol.citrus.actions;

import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.connection.ConnectionFactoryUtils;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Bean to purge JMS queue destinations by simply consuming all available messages.
 *
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 * @since 24.01.2007
 */
public class PurgeJmsQueuesBean extends AbstractTestAction {

    /** List of queues to be purged */
    private List<String> queueNames;

    @Autowired
    /** ConnectionFactory */
    private ConnectionFactory connectionFactory;

    /** Time to wait until timeout in ms */
    private long receiveTimeout = 100;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(PurgeJmsQueuesBean.class);

    /**
     * @see com.consol.citrus.TestAction#execute(TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        log.info("Purging JMS queues...");

        Connection connection = null;
        Session session = null;
        MessageConsumer messageConsumer = null;
        
        try {
        	connection = createConnection();
            session = createSession(connection);
            connection.start();
            
            for (String queueName : queueNames) {
                if (log.isDebugEnabled()) {
                    log.debug("Try to purge queue " + queueName);
                }

                messageConsumer = session.createConsumer(getDestination(session, queueName));
                
                javax.jms.Message message;
                do {
                	message = (receiveTimeout >= 0) ? messageConsumer.receive(receiveTimeout) : messageConsumer.receive();
                	
                    if (message != null && log.isDebugEnabled()) {
                        log.debug("Removed message from queue " + queueName);
                    }
                } while (message != null);

                JmsUtils.closeMessageConsumer(messageConsumer);
            }
        } catch (JMSException e) {
            log.error("Error while establishing jms queue connection", e);
            throw new CitrusRuntimeException(e);
        } finally {
        	JmsUtils.closeMessageConsumer(messageConsumer);
            JmsUtils.closeSession(session);

            if(connection != null) {
                ConnectionFactoryUtils.releaseConnection(connection, this.connectionFactory, true);
            }
        }

        log.info("JMS queues purged successfully");
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
}
