package org.citrusframework.vertx.endpoint.builder;

import org.citrusframework.endpoint.builder.AsyncSyncEndpointBuilder;
import org.citrusframework.vertx.endpoint.VertxEndpointBuilder;
import org.citrusframework.vertx.endpoint.VertxSyncEndpointBuilder;

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
