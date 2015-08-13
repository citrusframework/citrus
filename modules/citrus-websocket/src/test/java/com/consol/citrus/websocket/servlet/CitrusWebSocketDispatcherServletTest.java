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

package com.consol.citrus.websocket.servlet;

import com.consol.citrus.endpoint.adapter.EmptyResponseEndpointAdapter;
import com.consol.citrus.endpoint.adapter.TimeoutProducingEndpointAdapter;
import com.consol.citrus.http.controller.HttpMessageController;
import com.consol.citrus.http.interceptor.DelegatingHandlerInterceptor;
import com.consol.citrus.http.interceptor.LoggingHandlerInterceptor;
import com.consol.citrus.http.message.HttpMessageConverter;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.websocket.endpoint.WebSocketEndpoint;
import com.consol.citrus.websocket.endpoint.WebSocketEndpointConfiguration;
import com.consol.citrus.websocket.handler.CitrusWebSocketHandler;
import com.consol.citrus.websocket.handler.WebSocketUrlHandlerMapping;
import com.consol.citrus.websocket.server.WebSocketServer;
import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class CitrusWebSocketDispatcherServletTest extends AbstractTestNGUnitTest {

    private WebSocketServer httpServer = EasyMock.createMock(WebSocketServer.class);
    private CitrusWebSocketDispatcherServlet servlet;

    @Autowired
    private HttpMessageController httpMessageController;

    @Autowired
    private DelegatingHandlerInterceptor handlerInterceptor;

    @Autowired
    private WebSocketUrlHandlerMapping urlHandlerMapping;

    @BeforeClass
    public void setUp() {
        servlet = new CitrusWebSocketDispatcherServlet(httpServer);
    }

    @Test
    public void testNoBeansInContext() throws Exception {
        reset(httpServer);

        expect(httpServer.getWebSockets()).andReturn(new ArrayList<WebSocketEndpoint>()).once();

        replay(httpServer);

        GenericApplicationContext applicationContext = new GenericApplicationContext();
        applicationContext.refresh();

        servlet.initStrategies(applicationContext);

        verify(httpServer);
    }

    @Test
    public void testConfigureHandlerInterceptor() throws Exception {
        List<Object> interceptors = new ArrayList<Object>();
        interceptors.add(new LoggingHandlerInterceptor());

        reset(httpServer);

        expect(httpServer.getInterceptors()).andReturn(interceptors).once();
        expect(httpServer.getEndpointAdapter()).andReturn(null).once();
        expect(httpServer.getMessageConverter()).andReturn(new HttpMessageConverter()).once();
        expect(httpServer.getWebSockets()).andReturn(new ArrayList<WebSocketEndpoint>()).once();

        replay(httpServer);

        servlet.initStrategies(applicationContext);

        Assert.assertEquals(handlerInterceptor.getInterceptors().size(), 1L);
        Assert.assertEquals(handlerInterceptor.getInterceptors().get(0), interceptors.get(0));
        Assert.assertNotNull(httpMessageController.getEndpointConfiguration().getMessageConverter());

        Assert.assertEquals(httpMessageController.getEndpointAdapter().getClass(), EmptyResponseEndpointAdapter.class);

        verify(httpServer);
    }

    @Test
    public void testConfigureMessageController() throws Exception {
        reset(httpServer);

        expect(httpServer.getInterceptors()).andReturn(null).once();
        expect(httpServer.getEndpointAdapter()).andReturn(new TimeoutProducingEndpointAdapter()).once();
        expect(httpServer.getMessageConverter()).andReturn(new HttpMessageConverter()).once();
        expect(httpServer.getWebSockets()).andReturn(new ArrayList<WebSocketEndpoint>()).once();

        replay(httpServer);

        servlet.initStrategies(applicationContext);

        Assert.assertEquals(handlerInterceptor.getInterceptors().size(), 0L);
        Assert.assertEquals(httpMessageController.getEndpointAdapter().getClass(), TimeoutProducingEndpointAdapter.class);
        Assert.assertNotNull(httpMessageController.getEndpointConfiguration().getMessageConverter());


        verify(httpServer);
    }

    @Test
    public void testConfigureWebSockerHandler() throws Exception {
        WebSocketEndpoint wsEndpoint = EasyMock.createMock(WebSocketEndpoint.class);
        WebSocketEndpointConfiguration wsEndpointConfig = EasyMock.createMock(WebSocketEndpointConfiguration.class);
        String wsId = "wsId";
        String endpointUri = "someEndpointUri";

        List<WebSocketEndpoint> webSockets = new ArrayList<>();
        webSockets.add(wsEndpoint);

        reset(httpServer);

        expect(httpServer.getInterceptors()).andReturn(null).once();
        expect(httpServer.getEndpointAdapter()).andReturn(null).once();
        expect(httpServer.getMessageConverter()).andReturn(new HttpMessageConverter()).once();
        expect(httpServer.getWebSockets()).andReturn(webSockets).once();

        expect(wsEndpoint.getEndpointConfiguration()).andReturn(wsEndpointConfig).once();
        expect(wsEndpoint.getName()).andReturn(wsId).once();
        wsEndpoint.setWebSocketHandler(isA(CitrusWebSocketHandler.class));

        expect(wsEndpointConfig.getEndpointUri()).andReturn(endpointUri).once();

        replay(httpServer);
        replay(wsEndpoint);
        replay(wsEndpointConfig);

        servlet.initStrategies(applicationContext);

        Map<String, ?> urlMap = urlHandlerMapping.getUrlMap();
        Assert.assertEquals(urlMap.size(), 1);
        Assert.assertTrue(urlMap.containsKey(endpointUri));
        Assert.assertNotNull(urlMap.get(endpointUri));

        verify(httpServer);
        verify(wsEndpoint);
        verify(wsEndpointConfig);
    }
}
