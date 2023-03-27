package org.citrusframework.citrus.vertx.endpoint.builder;

import org.citrusframework.citrus.endpoint.builder.AsyncSyncEndpointBuilder;
import org.citrusframework.citrus.vertx.endpoint.VertxEndpointBuilder;
import org.citrusframework.citrus.vertx.endpoint.VertxSyncEndpointBuilder;

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
