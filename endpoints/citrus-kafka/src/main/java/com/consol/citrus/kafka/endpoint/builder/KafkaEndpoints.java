package com.consol.citrus.kafka.endpoint.builder;

import com.consol.citrus.endpoint.builder.AsyncSyncEndpointBuilder;
import com.consol.citrus.kafka.endpoint.KafkaEndpointBuilder;

/**
 * @author Christoph Deppisch
 */
public final class KafkaEndpoints extends AsyncSyncEndpointBuilder<KafkaEndpointBuilder, KafkaEndpointBuilder> {
    /**
     * Private constructor setting the sync and async builder implementation.
     */
    private KafkaEndpoints() {
        super(new KafkaEndpointBuilder(), new KafkaEndpointBuilder());
    }

    /**
     * Static entry method for kafka endpoints.
     * @return
     */
    public static KafkaEndpoints kafka() {
        return new KafkaEndpoints();
    }
}
