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

package com.consol.citrus.websocket.server;

import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.http.message.HttpMessageConverter;
import com.consol.citrus.http.server.HttpServerBuilder;
import com.consol.citrus.websocket.endpoint.WebSocketEndpoint;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class WebSocketServerBuilder extends HttpServerBuilder {

    /** Endpoint target */
    private WebSocketServer endpoint = new WebSocketServer();

    @Override
    protected WebSocketServer getEndpoint() {
        return endpoint;
    }

    @Override
    public WebSocketServer build() {
        return (WebSocketServer) super.build();
    }

    /**
     * Sets the webSockets property.
     * @param webSockets
     * @return
     */
    public WebSocketServerBuilder webSockets(List<WebSocketEndpoint> webSockets) {
        endpoint.setWebSockets(webSockets);
        return this;
    }

    @Override
    public WebSocketServerBuilder port(int port) {
        endpoint.setPort(port);
        return this;
    }

    @Override
    public WebSocketServerBuilder autoStart(boolean autoStart) {
        endpoint.setAutoStart(autoStart);
        return this;
    }

    @Override
    public WebSocketServerBuilder contextConfigLocation(String configLocation) {
        endpoint.setContextConfigLocation(configLocation);
        return this;
    }

    @Override
    public WebSocketServerBuilder resourceBase(String resourceBase) {
        endpoint.setResourceBase(resourceBase);
        return this;
    }

    @Override
    public WebSocketServerBuilder rootParentContext(boolean rootParentContext) {
        endpoint.setUseRootContextAsParent(rootParentContext);
        return this;
    }

    @Override
    public WebSocketServerBuilder connectors(List<Connector> connectors) {
        endpoint.setConnectors(connectors.toArray(new Connector[connectors.size()]));
        return this;
    }

    @Override
    public WebSocketServerBuilder connector(Connector connector) {
        endpoint.setConnector(connector);
        return this;
    }

    @Override
    public WebSocketServerBuilder servletName(String servletName) {
        endpoint.setServletName(servletName);
        return this;
    }

    @Override
    public WebSocketServerBuilder servletMappingPath(String servletMappingPath) {
        endpoint.setServletMappingPath(servletMappingPath);
        return this;
    }

    @Override
    public WebSocketServerBuilder contextPath(String contextPath) {
        endpoint.setContextPath(contextPath);
        return this;
    }

    @Override
    public WebSocketServerBuilder servletHandler(ServletHandler servletHandler) {
        endpoint.setServletHandler(servletHandler);
        return this;
    }

    @Override
    public WebSocketServerBuilder securityHandler(SecurityHandler securityHandler) {
        endpoint.setSecurityHandler(securityHandler);
        return this;
    }

    @Override
    public WebSocketServerBuilder messageConverter(HttpMessageConverter messageConverter) {
        endpoint.setMessageConverter(messageConverter);
        return this;
    }

    @Override
    public WebSocketServerBuilder timeout(long timeout) {
        endpoint.setDefaultTimeout(timeout);
        return this;
    }

    @Override
    public WebSocketServerBuilder endpointAdapter(EndpointAdapter endpointAdapter) {
        endpoint.setEndpointAdapter(endpointAdapter);
        return this;
    }

    /**
     * Sets the debug logging enabled flag.
     * @param enabled
     * @return
     */
    public WebSocketServerBuilder debugLogging(boolean enabled) {
        endpoint.setDebugLogging(enabled);
        return this;
    }

    @Override
    public WebSocketServerBuilder interceptors(List<HandlerInterceptor> interceptors) {
        endpoint.setInterceptors((List) interceptors);
        return this;
    }
}
