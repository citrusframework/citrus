package com.consol.citrus.jms.endpoint;

import com.consol.citrus.endpoint.builder.AsyncSyncEndpointBuilder;

/**
 * @author Christoph Deppisch
 */
public final class JmsEndpoints extends AsyncSyncEndpointBuilder<JmsEndpointBuilder, JmsSyncEndpointBuilder> {
    /**
     * Private constructor setting the sync and async builder implementation.
     */
    private JmsEndpoints() {
        super(new JmsEndpointBuilder(), new JmsSyncEndpointBuilder());
    }

    /**
     * Static entry method for Jms endpoint builders.
     * @return
     */
    public static JmsEndpoints jms() {
        return new JmsEndpoints();
    }
}
