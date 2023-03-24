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

package org.citrusframework.ws.servlet;

import org.citrusframework.endpoint.adapter.EmptyResponseEndpointAdapter;
import org.citrusframework.endpoint.adapter.TimeoutProducingEndpointAdapter;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.ws.addressing.WsAddressingHeaders;
import org.citrusframework.ws.interceptor.*;
import org.citrusframework.ws.message.converter.SoapMessageConverter;
import org.citrusframework.ws.message.converter.WsAddressingMessageConverter;
import org.citrusframework.ws.server.WebServiceEndpoint;
import org.citrusframework.ws.server.WebServiceServer;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;


/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class CitrusMessageDispatcherServletTest extends AbstractTestNGUnitTest {

    private WebServiceServer webServiceServer = Mockito.mock(WebServiceServer.class);
    private CitrusMessageDispatcherServlet servlet;

    @Autowired
    private WebServiceEndpoint webServiceEndpoint;

    @Autowired
    private DelegatingEndpointInterceptor endpointInterceptor;

    @BeforeClass
    public void setUp() {
        reset(webServiceServer);
        when(webServiceServer.getMessageFactoryName()).thenReturn(MessageDispatcherServlet.DEFAULT_MESSAGE_FACTORY_BEAN_NAME);
        servlet = new CitrusMessageDispatcherServlet(webServiceServer);
    }

    @Test
    public void testNoBeansInContext() throws Exception {
        reset(webServiceServer);
        GenericApplicationContext applicationContext = new GenericApplicationContext();
        applicationContext.refresh();

        servlet.initStrategies(applicationContext);

    }

    @Test
    public void testConfigureHandlerInterceptor() throws Exception {
        List<Object> interceptors = new ArrayList<Object>();
        interceptors.add(new LoggingEndpointInterceptor());
        interceptors.add(new SoapMustUnderstandEndpointInterceptor());

        reset(webServiceServer);

        when(webServiceServer.getInterceptors()).thenReturn(interceptors);
        when(webServiceServer.getEndpointAdapter()).thenReturn(null);
        when(webServiceServer.getMessageConverter()).thenReturn(new SoapMessageConverter());
        when(webServiceServer.isHandleMimeHeaders()).thenReturn(false);
        when(webServiceServer.isHandleAttributeHeaders()).thenReturn(false);
        when(webServiceServer.isKeepSoapEnvelope()).thenReturn(false);
        when(webServiceServer.getSoapHeaderNamespace()).thenReturn(null);
        when(webServiceServer.getSoapHeaderPrefix()).thenReturn("");

        servlet.initStrategies(applicationContext);

        Assert.assertEquals(endpointInterceptor.getInterceptors().size(), 2L);
        Assert.assertEquals(endpointInterceptor.getInterceptors().get(0), interceptors.get(0));
        Assert.assertEquals(endpointInterceptor.getInterceptors().get(1), interceptors.get(1));

        Assert.assertEquals(webServiceEndpoint.getEndpointAdapter().getClass(), EmptyResponseEndpointAdapter.class);
        Assert.assertEquals(webServiceEndpoint.getEndpointConfiguration().getMessageConverter().getClass(), SoapMessageConverter.class);
        Assert.assertFalse(webServiceEndpoint.getEndpointConfiguration().isHandleMimeHeaders());
        Assert.assertFalse(webServiceEndpoint.getEndpointConfiguration().isHandleAttributeHeaders());
        Assert.assertFalse(webServiceEndpoint.getEndpointConfiguration().isKeepSoapEnvelope());
        Assert.assertNull(webServiceEndpoint.getDefaultNamespaceUri());
        Assert.assertEquals(webServiceEndpoint.getDefaultPrefix(), "");

    }

    @Test
    public void testConfigureMessageEndpoint() throws Exception {
        reset(webServiceServer);

        when(webServiceServer.getInterceptors()).thenReturn(null);
        when(webServiceServer.getEndpointAdapter()).thenReturn(new TimeoutProducingEndpointAdapter());
        when(webServiceServer.getMessageConverter()).thenReturn(new WsAddressingMessageConverter(new WsAddressingHeaders()));
        when(webServiceServer.isHandleMimeHeaders()).thenReturn(true);
        when(webServiceServer.isHandleAttributeHeaders()).thenReturn(true);
        when(webServiceServer.isKeepSoapEnvelope()).thenReturn(true);
        when(webServiceServer.getSoapHeaderNamespace()).thenReturn("http://citrusframework.org");
        when(webServiceServer.getSoapHeaderPrefix()).thenReturn("CITRUS");

        servlet.initStrategies(applicationContext);

        Assert.assertEquals(endpointInterceptor.getInterceptors().size(), 0L);
        Assert.assertEquals(webServiceEndpoint.getEndpointAdapter().getClass(), TimeoutProducingEndpointAdapter.class);
        Assert.assertEquals(webServiceEndpoint.getEndpointConfiguration().getMessageConverter().getClass(), WsAddressingMessageConverter.class);
        Assert.assertTrue(webServiceEndpoint.getEndpointConfiguration().isHandleMimeHeaders());
        Assert.assertTrue(webServiceEndpoint.getEndpointConfiguration().isHandleAttributeHeaders());
        Assert.assertTrue(webServiceEndpoint.getEndpointConfiguration().isKeepSoapEnvelope());
        Assert.assertEquals(webServiceEndpoint.getDefaultNamespaceUri(), "http://citrusframework.org");
        Assert.assertEquals(webServiceEndpoint.getDefaultPrefix(), "CITRUS");

    }
}
