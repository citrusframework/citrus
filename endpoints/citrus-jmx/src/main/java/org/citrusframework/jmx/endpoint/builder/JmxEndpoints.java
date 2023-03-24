package org.citrusframework.jmx.endpoint.builder;

import org.citrusframework.endpoint.builder.ClientServerEndpointBuilder;
import org.citrusframework.jmx.client.JmxClientBuilder;
import org.citrusframework.jmx.server.JmxServerBuilder;

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
