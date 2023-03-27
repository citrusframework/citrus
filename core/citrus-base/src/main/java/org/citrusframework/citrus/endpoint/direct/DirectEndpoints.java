package org.citrusframework.citrus.endpoint.direct;

import org.citrusframework.citrus.endpoint.builder.AsyncSyncEndpointBuilder;

/**
 * @author Christoph Deppisch
 */
public class DirectEndpoints extends AsyncSyncEndpointBuilder<DirectEndpointBuilder, DirectSyncEndpointBuilder> {

    /**
     * Private constructor setting the sync and async builder implementation.
     */
    private DirectEndpoints() {
        super(new DirectEndpointBuilder(), new DirectSyncEndpointBuilder());
    }

    /**
     * Static entry method for in memory endpoint builders.
     * @return
     */
    public static DirectEndpoints direct() {
        return new DirectEndpoints();
    }
}
