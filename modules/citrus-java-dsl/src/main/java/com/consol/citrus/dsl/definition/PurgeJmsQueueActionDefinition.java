/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.dsl.definition;

import com.consol.citrus.jms.actions.PurgeJmsQueuesAction;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import java.util.Arrays;
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
 * @author Max Argyo, Giulia DelBravo
 * @since 1.3
 * @deprecated since 2.3 in favor of using {@link com.consol.citrus.dsl.builder.PurgeJmsQueuesBuilder}
 */
public class PurgeJmsQueueActionDefinition extends AbstractActionDefinition<PurgeJmsQueuesAction> {

	/**
	 * Constructor using action field.
	 * @param action
	 */
	public PurgeJmsQueueActionDefinition(PurgeJmsQueuesAction action) {
	    super(action);
    }

	/**
	 * Default constructor.
	 */
	public PurgeJmsQueueActionDefinition() {
		super(new PurgeJmsQueuesAction());
	}

	/**
     * Sets the Connection factory.
     * @param connectionFactory the queueConnectionFactory to set
     */
	public PurgeJmsQueueActionDefinition connectionFactory(ConnectionFactory connectionFactory) {
		action.setConnectionFactory(connectionFactory);
		return this;
	}

	/**
     * List of queues to purge in this action.
     * @param queues The queues which are to be purged.
     */
	public PurgeJmsQueueActionDefinition queues(List<Queue> queues) {
		action.getQueues().addAll(queues);
		return this;
	}

	/**
	 * List of queues to purge in this action.
	 * @param queues
	 * @return
	 */
	public PurgeJmsQueueActionDefinition queues(Queue... queues) {
		return queues(Arrays.asList(queues));
	}

	/**
     * Adds a new queue to the list of queues to purge in this action.
     * @param queue
     * @return
     */
    public PurgeJmsQueueActionDefinition queue(Queue queue) {
        action.getQueues().add(queue);
        return this;
    }

	/**
     * List of queue names to purge in this action.
     * @param names the queueNames to set
     */
	public PurgeJmsQueueActionDefinition queueNames(List<String> names) {
		action.getQueueNames().addAll(names);
		return this;
	}

	/**
	 * List of queue names to purge in this action.
	 * @param names
	 * @return
	 */
	public PurgeJmsQueueActionDefinition queueNames(String... names) {
		return queueNames(Arrays.asList(names));
	}

	/**
     * Adds a queue name to the list of queues to purge in this action.
     * @param name
     * @return
     */
    public PurgeJmsQueueActionDefinition queue(String name) {
        action.getQueueNames().add(name);
        return this;
    }

	/**
     * Receive timeout for reading message from a destination.
     * @param receiveTimeout the receiveTimeout to set
     */
	public PurgeJmsQueueActionDefinition timeout(long receiveTimeout) {
		action.setReceiveTimeout(receiveTimeout);
		return this;
	}

	/**
     * Sets the sleepTime.
     * @param millis the sleepTime to set
     */
	public PurgeJmsQueueActionDefinition sleep(long millis) {
		action.setSleepTime(millis);
		return this;
	}

	/**
	 * Checks if connection factory is set properly.
	 * @return
	 */
	public boolean hasConnectionFactory() {
		return action.getConnectionFactory() != null;
	}
}
