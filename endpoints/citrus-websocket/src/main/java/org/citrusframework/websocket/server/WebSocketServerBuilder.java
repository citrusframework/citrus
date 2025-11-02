/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.websocket.server;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.http.server.AbstractHttpServerBuilder;
import org.citrusframework.websocket.endpoint.WebSocketEndpoint;
import org.citrusframework.yaml.SchemaProperty;

/**
 * @since 2.5
 */
public class WebSocketServerBuilder extends AbstractHttpServerBuilder<WebSocketServer, WebSocketServerBuilder> {

    private final List<String> webSockets = new ArrayList<>();

    public WebSocketServerBuilder() {
        this(new WebSocketServer());
    }

    public WebSocketServerBuilder(WebSocketServer server) {
        super(server);
    }

    @Override
    public WebSocketServer build() {
        if (referenceResolver != null) {
            if (!webSockets.isEmpty()) {
                webSockets(webSockets.stream()
                        .map(socket -> referenceResolver.resolve(socket, WebSocketEndpoint.class))
                        .toList());
            }
        }

        return super.build();
    }

    /**
     * Sets the webSockets property.
     */
    public WebSocketServerBuilder webSockets(List<WebSocketEndpoint> webSockets) {
        getEndpoint().setWebSockets(webSockets);
        return this;
    }

    @SchemaProperty(description = "Sets the list of web sockets for this server.")
    public void setWebSockets(List<String> webSockets) {
        this.webSockets.addAll(webSockets);
    }
}
