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

package com.consol.citrus.websocket.server;

import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.websocket.endpoint.WebSocketEndpoint;
import com.consol.citrus.websocket.servlet.CitrusWebSocketDispatcherServlet;
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
        setContextConfigLocation("classpath:com/consol/citrus/websocket/citrus-servlet-context.xml");
    }

    /**
     * Gets the Citrus dispatcher servlet.
     * @return
     */
    protected DispatcherServlet getDispatherServlet() {
        return new CitrusWebSocketDispatcherServlet(this);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

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
     * @param webSockets
     */
    public void setWebSockets(List<WebSocketEndpoint> webSockets) {
        this.webSockets = webSockets;
    }
}
