package com.consol.citrus.endpoint.direct;

import com.consol.citrus.endpoint.AbstractEndpoint;
import com.consol.citrus.messaging.Producer;
import com.consol.citrus.messaging.SelectiveConsumer;

/**
 * Direct message endpoint implementation sends and receives message from in memory message queue.
 *
 * @author Christoph Deppisch
 * @since 3.0
 */
public class DirectEndpoint extends AbstractEndpoint {

    /** Cached producer or consumer */
    private DirectConsumer channelConsumer;
    private DirectProducer channelProducer;

    /**
     * Default constructor initializing endpoint configuration.
     */
    public DirectEndpoint() {
        super(new DirectEndpointConfiguration());
    }

    /**
     * Constructor with endpoint configuration.
     * @param endpointConfiguration
     */
    public DirectEndpoint(DirectEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
    }

    @Override
    public SelectiveConsumer createConsumer() {
        if (channelConsumer == null) {
            channelConsumer = new DirectConsumer(getConsumerName(), getEndpointConfiguration());
        }

        return channelConsumer;
    }

    @Override
    public Producer createProducer() {
        if (channelProducer == null) {
            channelProducer = new DirectProducer(getProducerName(), getEndpointConfiguration());
        }

        return channelProducer;
    }

    @Override
    public DirectEndpointConfiguration getEndpointConfiguration() {
        return (DirectEndpointConfiguration) super.getEndpointConfiguration();
    }
}
