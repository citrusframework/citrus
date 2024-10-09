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

package org.citrusframework.websocket.endpoint.builder;

import org.citrusframework.endpoint.builder.ClientServerEndpointBuilder;
import org.citrusframework.websocket.client.WebSocketClientBuilder;
import org.citrusframework.websocket.server.WebSocketServerBuilder;

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
