/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.http.socket.endpoint;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.socket.handler.CitrusWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

/**
 * @author Martin Maher
 * @since 2.3
 */
public class WebSocketClientEndpointConfiguration extends WebSocketEndpointConfiguration {
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketClientEndpointConfiguration.class);

    private CitrusWebSocketHandler handler;

    public CitrusWebSocketHandler getHandler() {
        if (handler == null) {
            handler = getWebSocketClientHandler(getEndpointUri());
        }
        return handler;
    }

    @Override
    public void setHandler(CitrusWebSocketHandler handler) {
        throw new CitrusRuntimeException("Not supported");
    }

    private CitrusWebSocketHandler getWebSocketClientHandler(String url) {
        WebSocketClient client = new StandardWebSocketClient();
        CitrusWebSocketHandler handler = new CitrusWebSocketHandler();
        ListenableFuture<WebSocketSession> future = client.doHandshake(handler, url);
        try {
            future.get();
        } catch (Exception e) {
            String errMsg = String.format("Error connecting to Web Socket server - '%s", url);
            LOG.error(errMsg);
            throw new CitrusRuntimeException(errMsg);
        }
        return handler;
    }
}
