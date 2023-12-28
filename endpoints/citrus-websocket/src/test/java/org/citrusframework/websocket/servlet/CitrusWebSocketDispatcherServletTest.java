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

package org.citrusframework.websocket.servlet;

import jakarta.servlet.ServletContext;
import org.citrusframework.endpoint.adapter.EmptyResponseEndpointAdapter;
import org.citrusframework.endpoint.adapter.TimeoutProducingEndpointAdapter;
import org.citrusframework.http.controller.HttpMessageController;
import org.citrusframework.http.interceptor.DelegatingHandlerInterceptor;
import org.citrusframework.http.interceptor.LoggingHandlerInterceptor;
import org.citrusframework.http.message.HttpMessageConverter;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.websocket.endpoint.WebSocketEndpoint;
import org.citrusframework.websocket.endpoint.WebSocketEndpointConfiguration;
import org.citrusframework.websocket.handler.CitrusWebSocketHandler;
import org.citrusframework.websocket.handler.WebSocketUrlHandlerMapping;
import org.citrusframework.websocket.server.WebSocketServer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.DecoratedObjectFactory;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class CitrusWebSocketDispatcherServletTest extends AbstractTestNGUnitTest {

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

    @BeforeMethod
    public void setUp() {
        openMocks(this);
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

        assertEquals(handlerInterceptor.getInterceptors().size(), 1L);
        assertEquals(handlerInterceptor.getInterceptors().get(0), interceptors.get(0));
        assertNotNull(httpMessageController.getEndpointConfiguration().getMessageConverter());

        assertEquals(httpMessageController.getEndpointAdapter().getClass(), EmptyResponseEndpointAdapter.class);

    }

    @Test
    public void testConfigureMessageController() {
        when(httpServer.getInterceptors()).thenReturn(null);
        when(httpServer.getEndpointAdapter()).thenReturn(new TimeoutProducingEndpointAdapter());
        when(httpServer.getMessageConverter()).thenReturn(new HttpMessageConverter());
        when(httpServer.getWebSockets()).thenReturn(new ArrayList<>());

        servlet.initStrategies(applicationContext);

        assertEquals(handlerInterceptor.getInterceptors().size(), 0L);
        assertEquals(httpMessageController.getEndpointAdapter().getClass(), TimeoutProducingEndpointAdapter.class);
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

        when(servletContext.getAttribute(DecoratedObjectFactory.ATTR)).thenReturn(objectFactory);
        when(contextHandler.getServer()).thenReturn(new Server());

        handshakeHandler.setServletContext(servletContext);

        servlet.initStrategies(applicationContext);

        Map<String, ?> urlMap = urlHandlerMapping.getUrlMap();
        assertEquals(urlMap.size(), 1);
        assertTrue(urlMap.containsKey(endpointUri));
        assertNotNull(urlMap.get(endpointUri));
    }
}
