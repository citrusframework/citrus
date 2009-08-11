package com.consol.citrus.actions;

import java.security.AccessControlException;
import java.util.Iterator;
import java.util.List;

import javax.jms.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Bean to explicitly purge jms queues.
 *
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 * @since 24.01.2007
 */
public class PurgeJmsQueuesBean extends AbstractTestAction {

    /** List of queues to be purged */
    private List queueNames;

    /** QueueConnectionFactory */
    private QueueConnectionFactory connectionFactory;

    /** Time to wait until timeout in ms */
    private long receiveTimeout = 10;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(PurgeJmsQueuesBean.class);

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.TestAction#execute(TestContext)
     */
    @Override
    public void execute(TestContext context) throws CitrusRuntimeException {
        log.info("Purging JMS queues...");

        QueueConnection qcon = null;
        QueueSession qsession = null;
        QueueReceiver qreceiver = null;

        try {
            qcon = connectionFactory.createQueueConnection();
            qsession = qcon.createQueueSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);

            for (Iterator iter = queueNames.iterator(); iter.hasNext();) {
                String queueName = (String) iter.next();

                if (log.isDebugEnabled()) {
                    log.debug("Try to purge queue " + queueName);
                }

                Queue queue = qsession.createQueue(queueName);
                qreceiver = qsession.createReceiver(queue);

                qcon.start();
                while (qreceiver.receive(receiveTimeout) != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Removed message from queue " + queueName);
                    }
                }

                qreceiver.close();
            }
        } catch (JMSException e) {
            log.error("Error while establishing jms queue connection", e);
            throw new CitrusRuntimeException(e);
        } finally {
            if (qreceiver != null) {
                try {
                    qreceiver.close();
                } catch (JMSException e) {
                    log.error("Error while closing the jms queue receiver", e);
                }
            }

            if (qsession != null) {
                try {
                    qsession.close();
                } catch (JMSException e) {
                    log.error("Error while closing the jms queue session", e);
                }
            }

            if (qcon != null) {
                try {
                    qcon.close();
                } catch (JMSException e) {
                    log.error("Error while closing the jms queue connection", e);
                } catch (AccessControlException e) {
                    log.debug("Error while closing the jms queue connection", e);
                }
            }
        }

        log.info("JMS queues purged successfully");
    }

    /**
     * @param queueNames the queueNames to set
     */
    public void setQueueNames(List queueNames) {
        this.queueNames = queueNames;
    }

    /**
     * @param queueConnectionFactory the queueConnectionFactory to set
     */
    public void setQueueConnectionFactory(
            QueueConnectionFactory queueConnectionFactory) {
        this.connectionFactory = queueConnectionFactory;
    }

    /**
     * @return the queueNames
     */
    public List getQueueNames() {
        return queueNames;
    }
}
