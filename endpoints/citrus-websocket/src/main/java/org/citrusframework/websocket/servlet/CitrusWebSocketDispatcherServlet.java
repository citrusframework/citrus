/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.websocket.servlet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.servlet.CitrusDispatcherServlet;
import org.citrusframework.websocket.endpoint.WebSocketEndpoint;
import org.citrusframework.websocket.handler.CitrusWebSocketHandler;
import org.citrusframework.websocket.handler.WebSocketUrlHandlerMapping;
import org.citrusframework.websocket.interceptor.SessionEnricherHandshakeInterceptor;
import org.citrusframework.websocket.server.WebSocketServer;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.server.HandshakeHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.WebSocketHttpRequestHandler;

/**
 * Citrus dispatcher servlet extends Spring's message dispatcher servlet and just
 * adds optional configuration settings for default mapping strategies and so on.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class CitrusWebSocketDispatcherServlet extends CitrusDispatcherServlet {

    /** Http server hosting the servlet */
    private WebSocketServer webSocketServer;

    /** Default bean names used in default configuration for supporting WebSocket endpoints */
    protected static final String URL_HANDLER_MAPPING_BEAN_NAME = "citrusUrlHandlerMapping";
    private static final String HANDSHAKE_HANDLER_BEAN_NAME = "citrusHandshakeHandler";

    /**
     * Default constructor using http server instance that
     * holds this servlet.
     * @param webSocketServer
     */
    public CitrusWebSocketDispatcherServlet(WebSocketServer webSocketServer) {
        super(webSocketServer);
        this.webSocketServer = webSocketServer;
    }

    @Override
    protected void initStrategies(ApplicationContext context) {
        super.initStrategies(context);

        configureWebSocketHandler(context);
    }

    private void configureWebSocketHandler(ApplicationContext context) {
        List<WebSocketEndpoint> webSocketEndpoints = webSocketServer.getWebSockets();
        if (CollectionUtils.isEmpty(webSocketEndpoints)) {
            return;
        }

        if (context.containsBean(URL_HANDLER_MAPPING_BEAN_NAME)) {
            WebSocketUrlHandlerMapping urlHandlerMapping = context.getBean(URL_HANDLER_MAPPING_BEAN_NAME, WebSocketUrlHandlerMapping.class);

            HandshakeHandler handshakeHandler = new DefaultHandshakeHandler();
            if (context.containsBean(HANDSHAKE_HANDLER_BEAN_NAME)) {
                handshakeHandler = context.getBean(HANDSHAKE_HANDLER_BEAN_NAME, HandshakeHandler.class);
            }

            Map<String, Object> wsHandlers = new HashMap<>();
            for (WebSocketEndpoint webSocketEndpoint : webSocketEndpoints) {
                String wsPath = webSocketEndpoint.getEndpointConfiguration().getEndpointUri();

                CitrusWebSocketHandler handler = new CitrusWebSocketHandler();
                webSocketEndpoint.setWebSocketHandler(handler);
                WebSocketHttpRequestHandler wsRequestHandler = new WebSocketHttpRequestHandler(handler, handshakeHandler);
                SessionEnricherHandshakeInterceptor handshakeInterceptor = new SessionEnricherHandshakeInterceptor(webSocketEndpoint.getName(), wsPath);
                wsRequestHandler.getHandshakeInterceptors().add(handshakeInterceptor);
                wsHandlers.put(wsPath, wsRequestHandler);
            }
            urlHandlerMapping.setUrlMap(wsHandlers);
            urlHandlerMapping.postRegisterUrlHandlers(wsHandlers);
        } else {
            throw new CitrusRuntimeException(String.format("Invalid WebSocket configuration - missing bean. Expected to find bean with name '%s' in Spring application context", URL_HANDLER_MAPPING_BEAN_NAME));
        }
    }
}
