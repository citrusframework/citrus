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

package org.citrusframework.websocket.config.annotation;

import org.citrusframework.TestActor;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.http.message.HttpMessageConverter;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.websocket.endpoint.WebSocketEndpoint;
import org.citrusframework.websocket.message.WebSocketMessageConverter;
import org.citrusframework.websocket.server.WebSocketServer;
import org.citrusframework.websocket.server.WebSocketServerBuilder;
import org.citrusframework.websocket.server.WebSocketServerEndpointConfiguration;
import org.eclipse.jetty.ee10.servlet.ServletHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.ArrayList;
import java.util.List;

import static org.citrusframework.util.StringUtils.hasText;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class WebSocketServerConfigParser implements AnnotationConfigParser<WebSocketServerConfig, WebSocketServer> {

    @Override
    public WebSocketServer parse(WebSocketServerConfig annotation, ReferenceResolver referenceResolver) {
        WebSocketServerBuilder builder = new WebSocketServerBuilder();

        List<WebSocketEndpoint> webSockets = new ArrayList<>();
        WebSocketConfig[] webSocketConfigs = annotation.webSockets();
        for (WebSocketConfig webSocketConfig : webSocketConfigs) {
            WebSocketServerEndpointConfiguration webSocketConfiguration = new WebSocketServerEndpointConfiguration();
            webSocketConfiguration.setEndpointUri(webSocketConfig.path());

            if (hasText(webSocketConfig.messageConverter())) {
                webSocketConfiguration.setMessageConverter(referenceResolver.resolve(webSocketConfig.messageConverter(), WebSocketMessageConverter.class));
            }

            webSocketConfiguration.setTimeout(webSocketConfig.timeout());

            WebSocketEndpoint webSocket = new WebSocketEndpoint(webSocketConfiguration);
            webSocket.setName(webSocketConfig.id());
            webSockets.add(webSocket);
        }

        builder.webSockets(webSockets);

        builder.autoStart(annotation.autoStart());
        builder.timeout(annotation.timeout());

        builder.debugLogging(annotation.debugLogging());

        if (hasText(annotation.endpointAdapter())) {
            builder.endpointAdapter(referenceResolver.resolve(annotation.endpointAdapter(), EndpointAdapter.class));
        }

        builder.interceptors(referenceResolver.resolve(annotation.interceptors(), HandlerInterceptor.class));

        if (hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        builder.port(annotation.port());

        if (hasText(annotation.contextConfigLocation())) {
            builder.contextConfigLocation(annotation.contextConfigLocation());
        }

        if (hasText(annotation.resourceBase())) {
            builder.resourceBase(annotation.resourceBase());
        }

        builder.rootParentContext(annotation.rootParentContext());

        builder.connectors(referenceResolver.resolve(annotation.connectors(), Connector.class));

        if (hasText(annotation.connector())) {
            builder.connector(referenceResolver.resolve(annotation.connector(), Connector.class));
        }

        if (hasText(annotation.servletName())) {
            builder.servletName(annotation.servletName());
        }

        if (hasText(annotation.servletMappingPath())) {
            builder.servletMappingPath(annotation.servletMappingPath());
        }

        if (hasText(annotation.contextPath())) {
            builder.contextPath(annotation.contextPath());
        }

        if (hasText(annotation.servletHandler())) {
            builder.servletHandler(referenceResolver.resolve(annotation.servletHandler(), ServletHandler.class));
        }

        if (hasText(annotation.securityHandler())) {
            builder.securityHandler(referenceResolver.resolve(annotation.securityHandler(), SecurityHandler.class));
        }

        if (hasText(annotation.messageConverter())) {
            builder.messageConverter(referenceResolver.resolve(annotation.messageConverter(), HttpMessageConverter.class));
        }

        builder.initialize();

        return builder.build();
    }
}
