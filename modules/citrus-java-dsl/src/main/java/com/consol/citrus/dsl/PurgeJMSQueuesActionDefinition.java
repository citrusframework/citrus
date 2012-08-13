package com.consol.citrus.dsl;

import java.util.Arrays;
import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;

import com.consol.citrus.actions.PurgeJmsQueuesAction;

/**
 * Action to purge JMS queue destinations by simply consuming 
 * all available messages. As queue purging is a broker implementation specific feature in
 * many cases this action clears all messages from a destination regardless of
 * JMS broker vendor implementations.
 *
 * Receiver will continue to receive messages until message receive timeout is reached, 
 * so no messages are left.
 */
public class PurgeJMSQueuesActionDefinition extends AbstractActionDefinition<PurgeJmsQueuesAction> {

	public PurgeJMSQueuesActionDefinition(PurgeJmsQueuesAction action) {
	    super(action);
    }

	/**
     * Sets the Connection factory.
     * @param queueConnectionFactory the queueConnectionFactory to set
     */
	public PurgeJMSQueuesActionDefinition connectionFactory(ConnectionFactory connectionFactory) {
		action.setConnectionFactory(connectionFactory);
		return this;
	}
	
	/**
     * List of queues.
     * @param queues The queues which are to be purged.
     */
	public PurgeJMSQueuesActionDefinition queues(List<Queue> queues) {
		action.setQueues(queues);
		return this;
	}
	
	public PurgeJMSQueuesActionDefinition queues(Queue... queues) {
		return queues(Arrays.asList(queues));
	}
	
	/**
     * List of queue names to purge. 
     * @param queueNames the queueNames to set
     */
	public PurgeJMSQueuesActionDefinition queueNames(List<String> names) {
		action.setQueueNames(names);
		return this;
	}
	
	public PurgeJMSQueuesActionDefinition queueNames(String... names) {
		return queueNames(Arrays.asList(names));
	}
	
	/**
     * Receive timeout for reading message from a destination.
     * @param receiveTimeout the receiveTimeout to set
     */
	public PurgeJMSQueuesActionDefinition receiveTimeout(long receiveTimeout) {
		action.setReceiveTimeout(receiveTimeout);
		return this;
	}
	
	/**
     * Sets the sleepTime.
     * @param sleepTime the sleepTime to set
     */
	public PurgeJMSQueuesActionDefinition sleep(long millis) {
		action.setSleepTime(millis);
		return this;
	}
}
