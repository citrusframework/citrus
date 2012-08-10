package com.consol.citrus.dsl;

import java.util.Arrays;
import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;

import com.consol.citrus.actions.PurgeJmsQueuesAction;

public class PurgeJMSQueuesActionDefinition extends AbstractActionDefinition<PurgeJmsQueuesAction> {

	public PurgeJMSQueuesActionDefinition(PurgeJmsQueuesAction action) {
	    super(action);
    }

	public PurgeJMSQueuesActionDefinition connectionFactory(ConnectionFactory connectionFactory) {
		action.setConnectionFactory(connectionFactory);
		return this;
	}
	
	public PurgeJMSQueuesActionDefinition queues(List<Queue> queues) {
		action.setQueues(queues);
		return this;
	}
	
	public PurgeJMSQueuesActionDefinition queues(Queue... queues) {
		return queues(Arrays.asList(queues));
	}
	
	public PurgeJMSQueuesActionDefinition queueNames(List<String> names) {
		action.setQueueNames(names);
		return this;
	}
	
	public PurgeJMSQueuesActionDefinition queueNames(String... names) {
		return queueNames(Arrays.asList(names));
	}
	
	public PurgeJMSQueuesActionDefinition receiveTimeout(long receiveTimeout) {
		action.setReceiveTimeout(receiveTimeout);
		return this;
	}
	
	public PurgeJMSQueuesActionDefinition sleep(long millis) {
		action.setSleepTime(millis);
		return this;
	}
}
