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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.eclipse.jetty.webapp.WebAppContext;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

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

    @BeforeClass
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        servlet = new CitrusWebSocketDispatcherServlet(httpServer);
    }

    @Test
    public void testNoBeansInContext() throws Exception {
        reset(httpServer);

        when(httpServer.getWebSockets()).thenReturn(new ArrayList<WebSocketEndpoint>());

        GenericApplicationContext applicationContext = new GenericApplicationContext();
        applicationContext.refresh();

        servlet.initStrategies(applicationContext);

    }

    @Test
    public void testConfigureHandlerInterceptor() throws Exception {
        List<Object> interceptors = new ArrayList<>();
        interceptors.add(new LoggingHandlerInterceptor());

        reset(httpServer);

        when(httpServer.getInterceptors()).thenReturn(interceptors);
        when(httpServer.getEndpointAdapter()).thenReturn(null);
        when(httpServer.getMessageConverter()).thenReturn(new HttpMessageConverter());
        when(httpServer.getWebSockets()).thenReturn(new ArrayList<>());

        servlet.initStrategies(applicationContext);

        Assert.assertEquals(handlerInterceptor.getInterceptors().size(), 1L);
        Assert.assertEquals(handlerInterceptor.getInterceptors().get(0), interceptors.get(0));
        Assert.assertNotNull(httpMessageController.getEndpointConfiguration().getMessageConverter());

        Assert.assertEquals(httpMessageController.getEndpointAdapter().getClass(), EmptyResponseEndpointAdapter.class);

    }

    @Test
    public void testConfigureMessageController() throws Exception {
        reset(httpServer);

        when(httpServer.getInterceptors()).thenReturn(null);
        when(httpServer.getEndpointAdapter()).thenReturn(new TimeoutProducingEndpointAdapter());
        when(httpServer.getMessageConverter()).thenReturn(new HttpMessageConverter());
        when(httpServer.getWebSockets()).thenReturn(new ArrayList<WebSocketEndpoint>());

        servlet.initStrategies(applicationContext);

        Assert.assertEquals(handlerInterceptor.getInterceptors().size(), 0L);
        Assert.assertEquals(httpMessageController.getEndpointAdapter().getClass(), TimeoutProducingEndpointAdapter.class);
        Assert.assertNotNull(httpMessageController.getEndpointConfiguration().getMessageConverter());


    }

    @Test
    public void testConfigureWebSockerHandler() throws Exception {
        WebSocketEndpoint wsEndpoint = Mockito.mock(WebSocketEndpoint.class);
        WebSocketEndpointConfiguration wsEndpointConfig = Mockito.mock(WebSocketEndpointConfiguration.class);
        String wsId = "wsId";
        String endpointUri = "someEndpointUri";

        List<WebSocketEndpoint> webSockets = new ArrayList<>();
        webSockets.add(wsEndpoint);

        WebAppContext.Context servletContext = Mockito.mock(WebAppContext.Context.class);
        ContextHandler contextHandler = Mockito.mock(ContextHandler.class);
        DecoratedObjectFactory objectFactory = Mockito.mock(DecoratedObjectFactory.class);

        reset(httpServer, servletContext, contextHandler);

        when(httpServer.getInterceptors()).thenReturn(null);
        when(httpServer.getEndpointAdapter()).thenReturn(null);
        when(httpServer.getMessageConverter()).thenReturn(new HttpMessageConverter());
        when(httpServer.getWebSockets()).thenReturn(webSockets);

        when(wsEndpoint.getEndpointConfiguration()).thenReturn(wsEndpointConfig);
        when(wsEndpoint.getName()).thenReturn(wsId);
        wsEndpoint.setWebSocketHandler(isA(CitrusWebSocketHandler.class));

        when(wsEndpointConfig.getEndpointUri()).thenReturn(endpointUri);

        when(servletContext.getContextHandler()).thenReturn(contextHandler);
        when(servletContext.getAttribute(DecoratedObjectFactory.ATTR)).thenReturn(objectFactory);
        when(contextHandler.getServer()).thenReturn(new Server());

        handshakeHandler.setServletContext(servletContext);

        servlet.initStrategies(applicationContext);

        Map<String, ?> urlMap = urlHandlerMapping.getUrlMap();
        Assert.assertEquals(urlMap.size(), 1);
        Assert.assertTrue(urlMap.containsKey(endpointUri));
        Assert.assertNotNull(urlMap.get(endpointUri));

    }
}
