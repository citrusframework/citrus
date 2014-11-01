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

import com.consol.citrus.endpoint.adapter.EmptyResponseEndpointAdapter;
import com.consol.citrus.endpoint.adapter.TimeoutProducingEndpointAdapter;
import com.consol.citrus.http.controller.HttpMessageController;
import com.consol.citrus.http.interceptor.DelegatingHandlerInterceptor;
import com.consol.citrus.http.interceptor.LoggingHandlerInterceptor;
import com.consol.citrus.http.message.HttpMessageConverter;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class CitrusDispatcherServletTest extends AbstractTestNGUnitTest {

    private HttpServer httpServer = EasyMock.createMock(HttpServer.class);
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

        replay(httpServer);

        servlet.initStrategies(applicationContext);

        Assert.assertEquals(handlerInterceptor.getInterceptors().size(), 0L);
        Assert.assertEquals(httpMessageController.getEndpointAdapter().getClass(), TimeoutProducingEndpointAdapter.class);
        Assert.assertNotNull(httpMessageController.getEndpointConfiguration().getMessageConverter());


        verify(httpServer);
    }
}
