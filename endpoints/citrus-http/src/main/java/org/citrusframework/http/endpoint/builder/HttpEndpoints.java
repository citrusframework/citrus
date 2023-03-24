package org.citrusframework.http.endpoint.builder;

import org.citrusframework.endpoint.builder.ClientServerEndpointBuilder;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.server.HttpServerBuilder;

/**
 * @author Christoph Deppisch
 */
public final class HttpEndpoints extends ClientServerEndpointBuilder<HttpClientBuilder, HttpServerBuilder> {
    /**
     * Private constructor setting the client and server builder implementation.
     */
    private HttpEndpoints() {
        super(new HttpClientBuilder(), new HttpServerBuilder());
    }

    /**
     * Static entry method for Http client and server endpoint builder.
     * @return
     */
    public static HttpEndpoints http() {
        return new HttpEndpoints();
    }
}
