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

package com.consol.citrus.websocket.client;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.websocket.endpoint.AbstractWebSocketEndpointConfiguration;
import com.consol.citrus.websocket.handler.CitrusWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

/**
 * Web socket endpoint configuration for client side web socket communication.
 * @author Martin Maher
 * @since 2.3
 */
public class WebSocketClientEndpointConfiguration extends AbstractWebSocketEndpointConfiguration {
    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketClientEndpointConfiguration.class);

    /** Web socket handler */
    private CitrusWebSocketHandler handler;

    /** Web socket client implementation */
    private WebSocketClient client = new StandardWebSocketClient();

    @Override
    public CitrusWebSocketHandler getHandler() {
        if (handler == null) {
            handler = getWebSocketClientHandler(getEndpointUri());
        }
        return handler;
    }

    @Override
    public void setHandler(CitrusWebSocketHandler handler) {
        throw new UnsupportedOperationException("Not allowed to set web socket handler directly!");
    }

    /**
     * Creates new client web socket handler by opening a new socket connection to server.
     * @param url
     * @return
     */
    private CitrusWebSocketHandler getWebSocketClientHandler(String url) {
        CitrusWebSocketHandler handler = new CitrusWebSocketHandler();
        ListenableFuture<WebSocketSession> future = client.doHandshake(handler, url);
        try {
            future.get();
        } catch (Exception e) {
            String errMsg = String.format("Failed to connect to Web Socket server - '%s'", url);
            LOG.error(errMsg);
            throw new CitrusRuntimeException(errMsg);
        }
        return handler;
    }

    /**
     * Sets the web socket client.
     * @param client
     */
    public void setClient(WebSocketClient client) {
        this.client = client;
    }
}
