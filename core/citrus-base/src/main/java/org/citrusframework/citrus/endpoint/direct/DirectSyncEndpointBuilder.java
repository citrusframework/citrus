package org.citrusframework.citrus.endpoint.direct;

import org.citrusframework.citrus.endpoint.AbstractEndpointBuilder;
import org.citrusframework.citrus.message.MessageCorrelator;
import org.citrusframework.citrus.message.MessageQueue;

/**
 * @author Christoph Deppisch
 */
public class DirectSyncEndpointBuilder extends AbstractEndpointBuilder<DirectSyncEndpoint> {

    /** Endpoint target */
    private DirectSyncEndpoint endpoint = new DirectSyncEndpoint();

    @Override
    protected DirectSyncEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the queueName property.
     * @param queueName
     * @return
     */
    public DirectSyncEndpointBuilder queue(String queueName) {
        endpoint.getEndpointConfiguration().setQueueName(queueName);
        return this;
    }

    /**
     * Sets the queue property.
     * @param queue
     * @return
     */
    public DirectSyncEndpointBuilder queue(MessageQueue queue) {
        endpoint.getEndpointConfiguration().setQueue(queue);
        return this;
    }

    /**
     * Sets the polling interval.
     * @param pollingInterval
     * @return
     */
    public DirectSyncEndpointBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    /**
     * Sets the message correlator.
     * @param correlator
     * @return
     */
    public DirectSyncEndpointBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public DirectSyncEndpointBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }
}
