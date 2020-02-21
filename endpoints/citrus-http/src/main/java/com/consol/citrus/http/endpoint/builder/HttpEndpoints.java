package com.consol.citrus.http.endpoint.builder;

import com.consol.citrus.endpoint.builder.ClientServerEndpointBuilder;
import com.consol.citrus.http.client.HttpClientBuilder;
import com.consol.citrus.http.server.HttpServerBuilder;

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
