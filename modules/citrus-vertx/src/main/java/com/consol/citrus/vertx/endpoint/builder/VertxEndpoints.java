package com.consol.citrus.vertx.endpoint.builder;

import com.consol.citrus.endpoint.builder.AsyncSyncEndpointBuilder;
import com.consol.citrus.vertx.endpoint.VertxEndpointBuilder;
import com.consol.citrus.vertx.endpoint.VertxSyncEndpointBuilder;

/**
 * @author Christoph Deppisch
 */
public final class VertxEndpoints extends AsyncSyncEndpointBuilder<VertxEndpointBuilder, VertxSyncEndpointBuilder> {
    /**
     * Private constructor setting the sync and async builder implementation.
     */
    private VertxEndpoints() {
        super(new VertxEndpointBuilder(), new VertxSyncEndpointBuilder());
    }

    /**
     * Static entry method for Vertx endpoints.
     * @return
     */
    public static VertxEndpoints vertx() {
        return new VertxEndpoints();
    }
}
