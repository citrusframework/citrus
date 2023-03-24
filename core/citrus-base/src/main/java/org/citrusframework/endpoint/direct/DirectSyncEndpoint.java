package org.citrusframework.endpoint.direct;

import org.citrusframework.messaging.Producer;
import org.citrusframework.messaging.SelectiveConsumer;

/**
 * @author Christoph Deppisch
 */
public class DirectSyncEndpoint extends DirectEndpoint {

    /** One of producer or consumer for this endpoint */
    private DirectSyncProducer syncProducer;
    private DirectSyncConsumer syncConsumer;

    /**
     * Default constructor initializing endpoint.
     */
    public DirectSyncEndpoint() {
        super(new DirectSyncEndpointConfiguration());
    }

    /**
     * Constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public DirectSyncEndpoint(DirectSyncEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
    }

    @Override
    public DirectSyncEndpointConfiguration getEndpointConfiguration() {
        return (DirectSyncEndpointConfiguration) super.getEndpointConfiguration();
    }

    @Override
    public SelectiveConsumer createConsumer() {
        if (syncProducer != null) {
            return syncProducer;
        }

        if (syncConsumer == null) {
            syncConsumer = new DirectSyncConsumer(getConsumerName(), getEndpointConfiguration());
        }

        return syncConsumer;
    }

    @Override
    public Producer createProducer() {
        if (syncConsumer != null) {
            return syncConsumer;
        }

        if (syncProducer == null) {
            syncProducer = new DirectSyncProducer(getProducerName(), getEndpointConfiguration());
        }

        return syncProducer;
    }
}
