package org.citrusframework.citrus.websocket.endpoint.builder;

import org.citrusframework.citrus.endpoint.builder.ClientServerEndpointBuilder;
import org.citrusframework.citrus.websocket.client.WebSocketClientBuilder;
import org.citrusframework.citrus.websocket.server.WebSocketServerBuilder;

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
