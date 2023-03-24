package org.citrusframework.endpoint.direct;

import org.citrusframework.endpoint.AbstractEndpointConfiguration;
import org.citrusframework.message.MessageQueue;

/**
 * @author Christoph Deppisch
 */
public class DirectEndpointConfiguration extends AbstractEndpointConfiguration {

    /** Destination queue */
    private MessageQueue queue;

    /** Destination queue name */
    private String queueName;

    /**
     * Set the message queue.
     * @param queue the queue to set
     */
    public void setQueue(MessageQueue queue) {
        this.queue = queue;
    }

    /**
     * Sets the destination queue name.
     * @param queueName the queueName to set
     */
    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    /**
     * Gets the queue.
     * @return the queue
     */
    public MessageQueue getQueue() {
        return queue;
    }

    /**
     * Gets the queueName.
     * @return the queueName
     */
    public String getQueueName() {
        return queueName;
    }
}
