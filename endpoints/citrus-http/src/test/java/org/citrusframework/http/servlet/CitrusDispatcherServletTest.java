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

package org.citrusframework.http.servlet;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.endpoint.adapter.EmptyResponseEndpointAdapter;
import org.citrusframework.endpoint.adapter.TimeoutProducingEndpointAdapter;
import org.citrusframework.http.controller.HttpMessageController;
import org.citrusframework.http.interceptor.DelegatingHandlerInterceptor;
import org.citrusframework.http.interceptor.LoggingHandlerInterceptor;
import org.citrusframework.http.message.HttpMessageConverter;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class CitrusDispatcherServletTest extends AbstractTestNGUnitTest {

    private HttpServer httpServer = Mockito.mock(HttpServer.class);
    private CitrusDispatcherServlet servlet;

    @Autowired
    private HttpMessageController httpMessageController;

    @Autowired
    private DelegatingHandlerInterceptor handlerInterceptor;

    @BeforeClass
    public void setUp() {
        servlet = new CitrusDispatcherServlet(httpServer);
    }

    @Test
    public void testNoBeansInContext() throws Exception {
        reset(httpServer);
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
        when(httpServer.isHandleAttributeHeaders()).thenReturn(false);
        when(httpServer.isHandleCookies()).thenReturn(false);
        when(httpServer.getMessageConverter()).thenReturn(new HttpMessageConverter());

        servlet.initStrategies(applicationContext);

        Assert.assertEquals(handlerInterceptor.getInterceptors().size(), 2L);
        Assert.assertEquals(handlerInterceptor.getInterceptors().get(0).getClass(), LoggingHandlerInterceptor.class);
        Assert.assertEquals(handlerInterceptor.getInterceptors().get(1), interceptors.get(0));
        Assert.assertNotNull(httpMessageController.getEndpointConfiguration().getMessageConverter());
        Assert.assertFalse(httpMessageController.getEndpointConfiguration().isHandleAttributeHeaders());
        Assert.assertFalse(httpMessageController.getEndpointConfiguration().isHandleCookies());

        Assert.assertEquals(httpMessageController.getEndpointAdapter().getClass(), EmptyResponseEndpointAdapter.class);
    }

    @Test
    public void testConfigureMessageController() throws Exception {
        reset(httpServer);

        when(httpServer.getInterceptors()).thenReturn(null);
        when(httpServer.getEndpointAdapter()).thenReturn(new TimeoutProducingEndpointAdapter());
        when(httpServer.isHandleAttributeHeaders()).thenReturn(true);
        when(httpServer.isHandleCookies()).thenReturn(true);
        when(httpServer.getMessageConverter()).thenReturn(new HttpMessageConverter());

        servlet.initStrategies(applicationContext);

        Assert.assertEquals(handlerInterceptor.getInterceptors().size(), 1L);
        Assert.assertEquals(handlerInterceptor.getInterceptors().get(0).getClass(), LoggingHandlerInterceptor.class);
        Assert.assertEquals(httpMessageController.getEndpointAdapter().getClass(), TimeoutProducingEndpointAdapter.class);
        Assert.assertNotNull(httpMessageController.getEndpointConfiguration().getMessageConverter());
        Assert.assertTrue(httpMessageController.getEndpointConfiguration().isHandleAttributeHeaders());
        Assert.assertTrue(httpMessageController.getEndpointConfiguration().isHandleCookies());
    }
}
