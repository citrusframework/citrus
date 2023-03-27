package org.citrusframework.citrus.jmx.endpoint.builder;

import org.citrusframework.citrus.endpoint.builder.ClientServerEndpointBuilder;
import org.citrusframework.citrus.jmx.client.JmxClientBuilder;
import org.citrusframework.citrus.jmx.server.JmxServerBuilder;

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
