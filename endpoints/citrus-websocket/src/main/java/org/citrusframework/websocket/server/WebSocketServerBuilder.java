/*
 * Copyright 2006-2016 the original author or authors.
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

import java.util.List;

import org.citrusframework.http.server.AbstractHttpServerBuilder;
import org.citrusframework.websocket.endpoint.WebSocketEndpoint;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class WebSocketServerBuilder extends AbstractHttpServerBuilder<WebSocketServer, WebSocketServerBuilder> {

    public WebSocketServerBuilder() {
        this(new WebSocketServer());
    }

    public WebSocketServerBuilder(WebSocketServer server) {
        super(server);
    }

    /**
     * Sets the webSockets property.
     * @param webSockets
     * @return
     */
    public WebSocketServerBuilder webSockets(List<WebSocketEndpoint> webSockets) {
        getEndpoint().setWebSockets(webSockets);
        return this;
    }
}
