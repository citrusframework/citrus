package com.consol.citrus.websocket.endpoint.builder;

import com.consol.citrus.endpoint.builder.ClientServerEndpointBuilder;
import com.consol.citrus.websocket.client.WebSocketClientBuilder;
import com.consol.citrus.websocket.server.WebSocketServerBuilder;

/**
 * @author Christoph Deppisch
 */
public final class WebSocketEndpoints extends ClientServerEndpointBuilder<WebSocketClientBuilder, WebSocketServerBuilder> {
    /**
     * Private constructor setting the client and server builder implementation.
     */
    private WebSocketEndpoints() {
        super(new WebSocketClientBuilder(), new WebSocketServerBuilder());
    }

    /**
     * Static entry method for websocket endpoints.
     * @return
     */
    public static WebSocketEndpoints websocket() {
        return new WebSocketEndpoints();
    }
}
