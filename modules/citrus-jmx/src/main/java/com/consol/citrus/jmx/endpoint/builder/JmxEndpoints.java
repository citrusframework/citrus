package com.consol.citrus.jmx.endpoint.builder;

import com.consol.citrus.endpoint.builder.ClientServerEndpointBuilder;
import com.consol.citrus.jmx.client.JmxClientBuilder;
import com.consol.citrus.jmx.server.JmxServerBuilder;

/**
 * @author Christoph Deppisch
 */
public final class JmxEndpoints extends ClientServerEndpointBuilder<JmxClientBuilder, JmxServerBuilder> {

    /**
     * Private constructor setting the client and server builder implementation.
     */
    private JmxEndpoints() {
        super(new JmxClientBuilder(), new JmxServerBuilder());
    }

    /**
     * Static entry method for Jmx endpoints.
     * @return
     */
    public static JmxEndpoints jmx() {
        return new JmxEndpoints();
    }
}
