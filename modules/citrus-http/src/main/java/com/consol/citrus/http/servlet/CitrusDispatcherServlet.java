/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.http.servlet;

import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.client.HttpEndpointConfiguration;
import com.consol.citrus.http.controller.HttpMessageController;
import com.consol.citrus.http.interceptor.DelegatingHandlerInterceptor;
import com.consol.citrus.http.interceptor.MappedInterceptorAdapter;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.http.socket.handler.WebSocketUrlHandlerMapping;
import com.consol.citrus.http.socket.endpoint.WebSocketEndpoint;
import com.consol.citrus.http.socket.handler.CitrusWebSocketHandler;
import com.consol.citrus.http.socket.interceptor.SessionEnricherHandshakeInterceptor;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.servlet.handler.WebRequestHandlerInterceptorAdapter;
import org.springframework.web.socket.server.HandshakeHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.WebSocketHttpRequestHandler;
import org.springframework.web.util.UrlPathHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Citrus dispatcher servlet extends Spring's message dispatcher servlet and just
 * adds optional configuration settings for default mapping strategies and so on.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class CitrusDispatcherServlet extends DispatcherServlet {

    /** Http server hosting the servlet */
    private HttpServer httpServer;

    /** Default bean names used in default configuration */
    private static final String HANDLER_INTERCEPTOR_BEAN_NAME = "citrusHandlerInterceptor";
    private static final String MESSAGE_CONTROLLER_BEAN_NAME = "citrusHttpMessageController";

    /** Default bean names used in default configuration for supporting WebSocket endpoints */
    private static final String URL_HANDLER_MAPPING_BEAN_NAME = "citrusUrlHandlerMapping";
    private static final String HANDSHAKE_HANDLER_BEAN_NAME = "citrusHandshakeHandler";

    /**
     * Default constructor using http server instance that
     * holds this servlet.
     * @param httpServer
     */
    public CitrusDispatcherServlet(HttpServer httpServer) {
        this.httpServer = httpServer;
    }

    @Override
    protected void initStrategies(ApplicationContext context) {
        super.initStrategies(context);

        configureHandlerInterceptor(context);
        configureMessageController(context);
        configureWebSockerHandler(context);
    }

    private void configureWebSockerHandler(ApplicationContext context) {
        List<WebSocketEndpoint> webSocketEndpoints = httpServer.getWebSockets();
        if(CollectionUtils.isEmpty(webSocketEndpoints)) {
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
                String wsPath = webSocketEndpoint.getEndpointConfiguration().getPath();
                String wsId = webSocketEndpoint.getName();
                CitrusWebSocketHandler handler = new CitrusWebSocketHandler();
                webSocketEndpoint.setWebSocketHandler(handler);
                WebSocketHttpRequestHandler wsRequestHandler = new WebSocketHttpRequestHandler(handler, handshakeHandler);
                SessionEnricherHandshakeInterceptor handshakeInterceptor = new SessionEnricherHandshakeInterceptor(wsId, wsPath);
                wsRequestHandler.getHandshakeInterceptors().add(handshakeInterceptor);
                wsHandlers.put(wsPath, wsRequestHandler);
            }
            urlHandlerMapping.setUrlMap(wsHandlers);
            urlHandlerMapping.postRegisterUrlHandlers(wsHandlers);
        } else {
            throw new CitrusRuntimeException(String.format("Invalid Websocket configuration - missing bean. Expected to find bean with name '%s' in citrus-context", URL_HANDLER_MAPPING_BEAN_NAME));
        }
    }

    /**
     * Post process handler interceptors.
     * @param context
     */
    protected void configureHandlerInterceptor(ApplicationContext context) {
        if (context.containsBean(HANDLER_INTERCEPTOR_BEAN_NAME)) {
            DelegatingHandlerInterceptor handlerInterceptor = context.getBean(HANDLER_INTERCEPTOR_BEAN_NAME, DelegatingHandlerInterceptor.class);
            handlerInterceptor.setInterceptors(adaptInterceptors(httpServer.getInterceptors()));
        }
    }

    /**
     * Post process message controller.
     * @param context
     */
    protected void configureMessageController(ApplicationContext context) {
        if (context.containsBean(MESSAGE_CONTROLLER_BEAN_NAME)) {
            HttpMessageController messageController = context.getBean(MESSAGE_CONTROLLER_BEAN_NAME, HttpMessageController.class);
            EndpointAdapter endpointAdapter = httpServer.getEndpointAdapter();

            HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
            endpointConfiguration.setMessageConverter(httpServer.getMessageConverter());
            endpointConfiguration.setHeaderMapper(DefaultHttpHeaderMapper.inboundMapper());
            messageController.setEndpointConfiguration(endpointConfiguration);

            if (endpointAdapter != null) {
                messageController.setEndpointAdapter(endpointAdapter);
            }
        }
    }

    /**
     * Adapts object list to handler interceptors.
     * @param interceptors
     * @return
     */
    private List<HandlerInterceptor> adaptInterceptors(List<Object> interceptors) {
        List<HandlerInterceptor> handlerInterceptors = new ArrayList<HandlerInterceptor>();

        if (interceptors != null) {
            for (Object interceptor : interceptors) {
                if (interceptor instanceof HandlerInterceptor) {
                    handlerInterceptors.add((HandlerInterceptor) interceptor);
                } else if (interceptor instanceof WebRequestInterceptor) {
                    handlerInterceptors.add(new WebRequestHandlerInterceptorAdapter((WebRequestInterceptor) interceptor));
                } else if (interceptor instanceof MappedInterceptor) {
                    handlerInterceptors.add(new MappedInterceptorAdapter((MappedInterceptor)interceptor,
                            new UrlPathHelper(), new AntPathMatcher()));
                }
            }
        }

        return handlerInterceptors;
    }
}
