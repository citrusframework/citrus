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

package org.citrusframework.websocket.interceptor;

import org.citrusframework.websocket.message.WebSocketMessageHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Enriches the Web Socket session with the Web Socket ID and PATH as defined in the http-server
 * citrus context.
 *
 * @author Martin Maher
 * @since 2.3
 */
public class SessionEnricherHandshakeInterceptor implements HandshakeInterceptor {
    /** Id and path of web socket */
    private String wsId;
    private String wsPath;

    /**
     * Default constructor initializing fields.
     * @param wsId
     * @param wsPath
     */
    public SessionEnricherHandshakeInterceptor(String wsId, String wsPath) {
        this.wsId = wsId;
        this.wsPath = wsPath;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            attributes.put(WebSocketMessageHeaders.WEB_SOCKET_ID, wsId);
            attributes.put(WebSocketMessageHeaders.WEB_SOCKET_PATH, wsPath);
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }
}
