/*
 * Copyright 2006-2024 the original author or authors.
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

import org.citrusframework.http.server.HttpServer;
import org.citrusframework.websocket.endpoint.WebSocketEndpoint;
import org.citrusframework.websocket.servlet.CitrusWebSocketDispatcherServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class WebSocketServer extends HttpServer {

    /**
     * Captures all WebSocket endpoints
     */
    private List<WebSocketEndpoint> webSockets = new ArrayList<>();

    /**
     * Default constructor overwrites context config location.
     */
    public WebSocketServer() {
        setContextConfigLocation("classpath:org/citrusframework/websocket/citrus-servlet-context.xml");
    }

    @Override
    protected DispatcherServlet getDispatcherServlet() {
        return new CitrusWebSocketDispatcherServlet(this);
    }

    @Override
    protected void configure(ServletContextHandler contextHandler) {
        contextHandler.addServletContainerInitializer(new JettyWebSocketServletContainerInitializer());
    }

    @Override
    public void initialize() {
        super.initialize();

        for (WebSocketEndpoint webSocket : webSockets) {
            webSocket.setActor(getActor());
        }
    }

    /**
     * Gets the WebSocket endpoints (id, uri)
     */
    public List<WebSocketEndpoint> getWebSockets() {
        return webSockets;
    }

    /**
     * Sets the WebSocket endpoints (id, uri)
     *
     * @param webSockets
     */
    public void setWebSockets(List<WebSocketEndpoint> webSockets) {
        this.webSockets = webSockets;
    }
}
