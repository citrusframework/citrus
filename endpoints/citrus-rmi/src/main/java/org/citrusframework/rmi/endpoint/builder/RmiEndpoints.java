package org.citrusframework.rmi.endpoint.builder;

import org.citrusframework.endpoint.builder.ClientServerEndpointBuilder;
import org.citrusframework.rmi.client.RmiClientBuilder;
import org.citrusframework.rmi.server.RmiServerBuilder;

/**
 * @author Christoph Deppisch
 */
public final class RmiEndpoints extends ClientServerEndpointBuilder<RmiClientBuilder, RmiServerBuilder> {
    /**
     * Private constructor setting the client and server builder implementation.
     */
    private RmiEndpoints() {
        super(new RmiClientBuilder(), new RmiServerBuilder());
    }

    /**
     * Static entry method for Rmi endpoint builder.
     * @return
     */
    public static RmiEndpoints rmi() {
        return new RmiEndpoints();
    }
}
