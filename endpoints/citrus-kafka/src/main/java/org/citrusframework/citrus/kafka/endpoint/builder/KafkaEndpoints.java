package org.citrusframework.citrus.kafka.endpoint.builder;

import org.citrusframework.citrus.endpoint.builder.AsyncSyncEndpointBuilder;
import org.citrusframework.citrus.kafka.endpoint.KafkaEndpointBuilder;

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
