package org.citrusframework.endpoint.direct;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.message.MessageQueue;

/**
 * @author Christoph Deppisch
 */
public class DirectEndpointBuilder extends AbstractEndpointBuilder<DirectEndpoint> {

    /** Endpoint target */
    private DirectEndpoint endpoint = new DirectEndpoint();

    @Override
    protected DirectEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the queueName property.
     * @param queueName
     * @return
     */
    public DirectEndpointBuilder queue(String queueName) {
        endpoint.getEndpointConfiguration().setQueueName(queueName);
        return this;
    }

    /**
     * Sets the queue property.
     * @param queue
     * @return
     */
    public DirectEndpointBuilder queue(MessageQueue queue) {
        endpoint.getEndpointConfiguration().setQueue(queue);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public DirectEndpointBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }
}
