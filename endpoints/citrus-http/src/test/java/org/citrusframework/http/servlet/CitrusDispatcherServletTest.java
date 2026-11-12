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

package org.citrusframework.http.servlet;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.base.endpoint.adapter.EmptyResponseEndpointAdapter;
import org.citrusframework.base.endpoint.adapter.TimeoutProducingEndpointAdapter;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.http.controller.HttpMessageController;
import org.citrusframework.http.interceptor.DelegatingHandlerInterceptor;
import org.citrusframework.http.interceptor.LoggingHandlerInterceptor;
import org.citrusframework.http.message.DelegatingHttpEntityMessageConverter;
import org.citrusframework.http.message.HttpMessageConverter;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @since 1.4
 */
public class CitrusDispatcherServletTest extends AbstractUnitTest {

    private HttpServer httpServer = Mockito.mock(HttpServer.class);
    private CitrusDispatcherServlet servlet;

    @Autowired
    private HttpMessageController httpMessageController;

    @Autowired
    private DelegatingHandlerInterceptor handlerInterceptor;

    @BeforeEach
    public void setUp() {
        servlet = new CitrusDispatcherServlet(httpServer);
    }

    @Test
    public void testNoBeansInContext() {
        reset(httpServer);
        GenericApplicationContext applicationContext = new GenericApplicationContext();
        applicationContext.refresh();

        servlet.initStrategies(applicationContext);

    }

    @Test
    public void testParentContextBeansAreNotConfigured() {
        DelegatingHandlerInterceptor parentHandlerInterceptor = new DelegatingHandlerInterceptor();
        HttpMessageController parentMessageController = new HttpMessageController();
        DelegatingHttpEntityMessageConverter parentMessageConverter = Mockito.mock(DelegatingHttpEntityMessageConverter.class);
        HttpEndpointConfiguration initialEndpointConfiguration = parentMessageController.getEndpointConfiguration();

        // Parent context beans are shared with other servers, so the servlet must leave them untouched.
        GenericApplicationContext parentContext = new GenericApplicationContext();
        parentContext.registerBean("citrusHandlerInterceptor", DelegatingHandlerInterceptor.class, () -> parentHandlerInterceptor);
        parentContext.registerBean("citrusLoggingInterceptor", LoggingHandlerInterceptor.class);
        parentContext.registerBean("citrusHttpMessageController", HttpMessageController.class, () -> parentMessageController);
        parentContext.registerBean("citrusHttpMessageConverter", DelegatingHttpEntityMessageConverter.class, () -> parentMessageConverter);
        parentContext.refresh();

        GenericApplicationContext servletContext = new GenericApplicationContext();
        servletContext.setParent(parentContext);
        servletContext.refresh();

        servlet.initStrategies(servletContext);

        Assertions.assertTrue(parentHandlerInterceptor.getInterceptors().isEmpty());
        Assertions.assertSame(initialEndpointConfiguration, parentMessageController.getEndpointConfiguration());
        Mockito.verifyNoInteractions(parentMessageConverter);
    }

    @Test
    public void testConfigureHandlerInterceptor() {
        List<Object> interceptors = new ArrayList<>();
        interceptors.add(new LoggingHandlerInterceptor());

        reset(httpServer);

        when(httpServer.getInterceptors()).thenReturn(interceptors);
        when(httpServer.getEndpointAdapter()).thenReturn(null);
        when(httpServer.isHandleAttributeHeaders()).thenReturn(false);
        when(httpServer.isHandleCookies()).thenReturn(false);
        when(httpServer.getMessageConverter()).thenReturn(new HttpMessageConverter());

        servlet.initStrategies(applicationContext);

        Assertions.assertEquals(2L, handlerInterceptor.getInterceptors().size());
        Assertions.assertEquals(LoggingHandlerInterceptor.class, handlerInterceptor.getInterceptors().get(0).getClass());
        Assertions.assertEquals(interceptors.get(0), handlerInterceptor.getInterceptors().get(1));
        Assertions.assertNotNull(httpMessageController.getEndpointConfiguration().getMessageConverter());
        Assertions.assertFalse(httpMessageController.getEndpointConfiguration().isHandleAttributeHeaders());
        Assertions.assertFalse(httpMessageController.getEndpointConfiguration().isHandleCookies());

        Assertions.assertEquals(EmptyResponseEndpointAdapter.class, httpMessageController.getEndpointAdapter().getClass());
    }

    @Test
    public void testConfigureMessageController() {
        reset(httpServer);

        when(httpServer.getInterceptors()).thenReturn(null);
        when(httpServer.getEndpointAdapter()).thenReturn(new TimeoutProducingEndpointAdapter());
        when(httpServer.isHandleAttributeHeaders()).thenReturn(true);
        when(httpServer.isHandleCookies()).thenReturn(true);
        when(httpServer.getMessageConverter()).thenReturn(new HttpMessageConverter());

        servlet.initStrategies(applicationContext);

        Assertions.assertEquals(1L, handlerInterceptor.getInterceptors().size());
        Assertions.assertEquals(LoggingHandlerInterceptor.class, handlerInterceptor.getInterceptors().get(0).getClass());
        Assertions.assertEquals(TimeoutProducingEndpointAdapter.class, httpMessageController.getEndpointAdapter().getClass());
        Assertions.assertNotNull(httpMessageController.getEndpointConfiguration().getMessageConverter());
        Assertions.assertTrue(httpMessageController.getEndpointConfiguration().isHandleAttributeHeaders());
        Assertions.assertTrue(httpMessageController.getEndpointConfiguration().isHandleCookies());
    }
}
