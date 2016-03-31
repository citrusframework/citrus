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

package com.consol.citrus.websocket.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.config.annotation.AbstractAnnotationConfigParser;
import com.consol.citrus.context.ReferenceResolver;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.http.message.HttpMessageConverter;
import com.consol.citrus.websocket.endpoint.WebSocketEndpoint;
import com.consol.citrus.websocket.message.WebSocketMessageConverter;
import com.consol.citrus.websocket.server.*;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class WebSocketServerConfigParser extends AbstractAnnotationConfigParser<WebSocketServerConfig, WebSocketServer> {

    /**
     * Constructor matching super.
     * @param referenceResolver
     */
    public WebSocketServerConfigParser(ReferenceResolver referenceResolver) {
        super(referenceResolver);
    }

    @Override
    public WebSocketServer parse(WebSocketServerConfig annotation) {
        WebSocketServerBuilder builder = new WebSocketServerBuilder();

        List<WebSocketEndpoint> webSockets = new ArrayList<>();
        WebSocketConfig[] webSocketConfigs = annotation.webSockets();
        for (WebSocketConfig webSocketConfig : webSocketConfigs) {
            WebSocketServerEndpointConfiguration webSocketConfiguration = new WebSocketServerEndpointConfiguration();
            webSocketConfiguration.setEndpointUri(webSocketConfig.path());

            if (StringUtils.hasText(webSocketConfig.messageConverter())) {
                webSocketConfiguration.setMessageConverter(getReferenceResolver().resolve(webSocketConfig.messageConverter(), WebSocketMessageConverter.class));
            }

            webSocketConfiguration.setTimeout(webSocketConfig.timeout());

            WebSocketEndpoint webSocket = new WebSocketEndpoint(webSocketConfiguration);
            webSocket.setName(webSocketConfig.id());
            webSockets.add(webSocket);
        }

        builder.webSockets(webSockets);

        builder.autoStart(annotation.autoStart());
        builder.timeout(annotation.timeout());

        if (StringUtils.hasText(annotation.endpointAdapter())) {
            builder.endpointAdapter(getReferenceResolver().resolve(annotation.endpointAdapter(), EndpointAdapter.class));
        }

        builder.interceptors(getReferenceResolver().resolve(annotation.interceptors(), HandlerInterceptor.class));

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(getReferenceResolver().resolve(annotation.actor(), TestActor.class));
        }

        builder.port(annotation.port());

        if (StringUtils.hasText(annotation.contextConfigLocation())) {
            builder.contextConfigLocation(annotation.contextConfigLocation());
        }

        if (StringUtils.hasText(annotation.resourceBase())) {
            builder.resourceBase(annotation.resourceBase());
        }

        builder.rootParentContext(annotation.rootParentContext());

        builder.connectors(getReferenceResolver().resolve(annotation.connectors(), Connector.class));

        if (StringUtils.hasText(annotation.connector())) {
            builder.connector(getReferenceResolver().resolve(annotation.connector(), Connector.class));
        }

        if (StringUtils.hasText(annotation.servletName())) {
            builder.servletName(annotation.servletName());
        }

        if (StringUtils.hasText(annotation.servletMappingPath())) {
            builder.servletMappingPath(annotation.servletMappingPath());
        }

        if (StringUtils.hasText(annotation.contextPath())) {
            builder.contextPath(annotation.contextPath());
        }

        if (StringUtils.hasText(annotation.servletHandler())) {
            builder.servletHandler(getReferenceResolver().resolve(annotation.servletHandler(), ServletHandler.class));
        }

        if (StringUtils.hasText(annotation.securityHandler())) {
            builder.securityHandler(getReferenceResolver().resolve(annotation.securityHandler(), SecurityHandler.class));
        }

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(getReferenceResolver().resolve(annotation.messageConverter(), HttpMessageConverter.class));
        }

        return builder.build();
    }
}
