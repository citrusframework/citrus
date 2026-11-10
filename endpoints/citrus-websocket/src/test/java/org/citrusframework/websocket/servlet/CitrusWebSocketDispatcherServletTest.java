/*
 * Copyright the original author or authors.
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

import jakarta.servlet.ServletContext;
import org.citrusframework.base.endpoint.adapter.EmptyResponseEndpointAdapter;
import org.citrusframework.base.endpoint.adapter.TimeoutProducingEndpointAdapter;
import org.citrusframework.http.controller.HttpMessageController;
import org.citrusframework.http.interceptor.DelegatingHandlerInterceptor;
import org.citrusframework.http.interceptor.LoggingHandlerInterceptor;
import org.citrusframework.http.message.HttpMessageConverter;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.websocket.endpoint.WebSocketEndpoint;
import org.citrusframework.websocket.endpoint.WebSocketEndpointConfiguration;
import org.citrusframework.websocket.handler.CitrusWebSocketHandler;
import org.citrusframework.websocket.handler.WebSocketUrlHandlerMapping;
import org.citrusframework.websocket.server.WebSocketServer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.DecoratedObjectFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @since 2.3
 */
@ExtendWith(MockitoExtension.class)
public class CitrusWebSocketDispatcherServletTest extends AbstractUnitTest {

    @Mock
    private WebSocketServer httpServer;
    private CitrusWebSocketDispatcherServlet servlet;

    @Autowired
    private HttpMessageController httpMessageController;

    @Autowired
    private DelegatingHandlerInterceptor handlerInterceptor;

    @Autowired
    private WebSocketUrlHandlerMapping urlHandlerMapping;

    @Autowired
    private DefaultHandshakeHandler handshakeHandler;

    @BeforeEach
    @Override
    public void prepareTest() {
        super.prepareTest();
        servlet = new CitrusWebSocketDispatcherServlet(httpServer);
    }

    @Test
    public void testNoBeansInContext() {
        when(httpServer.getWebSockets()).thenReturn(new ArrayList<>());

        GenericApplicationContext applicationContext = new GenericApplicationContext();
        applicationContext.refresh();

        servlet.initStrategies(applicationContext);

    }

    @Test
    public void testConfigureHandlerInterceptor() {
        List<Object> interceptors = new ArrayList<>();
        interceptors.add(new LoggingHandlerInterceptor());

        when(httpServer.getInterceptors()).thenReturn(interceptors);
        when(httpServer.getEndpointAdapter()).thenReturn(null);
        when(httpServer.getMessageConverter()).thenReturn(new HttpMessageConverter());
        when(httpServer.getWebSockets()).thenReturn(new ArrayList<>());

        servlet.initStrategies(applicationContext);

        assertEquals(1L, handlerInterceptor.getInterceptors().size());
        assertEquals(interceptors.get(0), handlerInterceptor.getInterceptors().get(0));
        assertNotNull(httpMessageController.getEndpointConfiguration().getMessageConverter());

        assertEquals(EmptyResponseEndpointAdapter.class, httpMessageController.getEndpointAdapter().getClass());

    }

    @Test
    public void testConfigureMessageController() {
        when(httpServer.getInterceptors()).thenReturn(null);
        when(httpServer.getEndpointAdapter()).thenReturn(new TimeoutProducingEndpointAdapter());
        when(httpServer.getMessageConverter()).thenReturn(new HttpMessageConverter());
        when(httpServer.getWebSockets()).thenReturn(new ArrayList<>());

        servlet.initStrategies(applicationContext);

        assertEquals(0L, handlerInterceptor.getInterceptors().size());
        assertEquals(TimeoutProducingEndpointAdapter.class, httpMessageController.getEndpointAdapter().getClass());
        assertNotNull(httpMessageController.getEndpointConfiguration().getMessageConverter());

    }

    @Test
    public void testConfigureWebSockerHandler() {
        WebSocketEndpoint wsEndpoint = mock(WebSocketEndpoint.class);
        WebSocketEndpointConfiguration wsEndpointConfig = mock(WebSocketEndpointConfiguration.class);
        String wsId = "wsId";
        String endpointUri = "someEndpointUri";

        List<WebSocketEndpoint> webSockets = new ArrayList<>();
        webSockets.add(wsEndpoint);

        ServletContext servletContext = mock(ServletContext.class);
        ContextHandler contextHandler = mock(ContextHandler.class);
        DecoratedObjectFactory objectFactory = mock(DecoratedObjectFactory.class);

        when(httpServer.getInterceptors()).thenReturn(null);
        when(httpServer.getEndpointAdapter()).thenReturn(null);
        when(httpServer.getMessageConverter()).thenReturn(new HttpMessageConverter());
        when(httpServer.getWebSockets()).thenReturn(webSockets);

        when(wsEndpoint.getEndpointConfiguration()).thenReturn(wsEndpointConfig);
        when(wsEndpoint.getName()).thenReturn(wsId);
        wsEndpoint.setWebSocketHandler(isA(CitrusWebSocketHandler.class));

        when(wsEndpointConfig.getEndpointUri()).thenReturn(endpointUri);

        lenient().when(servletContext.getAttribute(DecoratedObjectFactory.ATTR)).thenReturn(objectFactory);
        lenient().when(contextHandler.getServer()).thenReturn(new Server());

        handshakeHandler.setServletContext(servletContext);

        servlet.initStrategies(applicationContext);

        Map<String, ?> urlMap = urlHandlerMapping.getUrlMap();
        assertEquals(1, urlMap.size());
        assertTrue(urlMap.containsKey(endpointUri));
        assertNotNull(urlMap.get(endpointUri));
    }
}
