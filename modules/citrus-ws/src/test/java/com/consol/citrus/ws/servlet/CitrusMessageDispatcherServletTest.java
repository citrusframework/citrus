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

package com.consol.citrus.ws.servlet;

import com.consol.citrus.endpoint.adapter.EmptyResponseEndpointAdapter;
import com.consol.citrus.endpoint.adapter.TimeoutProducingEndpointAdapter;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.ws.addressing.WsAddressingHeaders;
import com.consol.citrus.ws.interceptor.*;
import com.consol.citrus.ws.message.converter.SoapMessageConverter;
import com.consol.citrus.ws.message.converter.WsAddressingMessageConverter;
import com.consol.citrus.ws.server.WebServiceEndpoint;
import com.consol.citrus.ws.server.WebServiceServer;
import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class CitrusMessageDispatcherServletTest extends AbstractTestNGUnitTest {

    private WebServiceServer webServiceServer = EasyMock.createMock(WebServiceServer.class);
    private CitrusMessageDispatcherServlet servlet;

    @Autowired
    private WebServiceEndpoint webServiceEndpoint;

    @Autowired
    private DelegatingEndpointInterceptor endpointInterceptor;

    @BeforeClass
    public void setUp() {
        reset(webServiceServer);
        expect(webServiceServer.getMessageFactoryName()).andReturn(MessageDispatcherServlet.DEFAULT_MESSAGE_FACTORY_BEAN_NAME).once();
        replay(webServiceServer);

        servlet = new CitrusMessageDispatcherServlet(webServiceServer);
    }

    @Test
    public void testNoBeansInContext() throws Exception {
        reset(webServiceServer);
        replay(webServiceServer);

        GenericApplicationContext applicationContext = new GenericApplicationContext();
        applicationContext.refresh();

        servlet.initStrategies(applicationContext);

        verify(webServiceServer);
    }

    @Test
    public void testConfigureHandlerInterceptor() throws Exception {
        List<Object> interceptors = new ArrayList<Object>();
        interceptors.add(new LoggingEndpointInterceptor());
        interceptors.add(new SoapMustUnderstandEndpointInterceptor());

        reset(webServiceServer);

        expect(webServiceServer.getInterceptors()).andReturn(interceptors).once();
        expect(webServiceServer.getEndpointAdapter()).andReturn(null).once();
        expect(webServiceServer.getMessageConverter()).andReturn(new SoapMessageConverter()).once();
        expect(webServiceServer.isHandleMimeHeaders()).andReturn(false).once();
        expect(webServiceServer.isKeepSoapEnvelope()).andReturn(false).once();
        expect(webServiceServer.getSoapHeaderNamespace()).andReturn(null).once();
        expect(webServiceServer.getSoapHeaderPrefix()).andReturn("").once();

        replay(webServiceServer);

        servlet.initStrategies(applicationContext);

        Assert.assertEquals(endpointInterceptor.getInterceptors().size(), 2L);
        Assert.assertEquals(endpointInterceptor.getInterceptors().get(0), interceptors.get(0));
        Assert.assertEquals(endpointInterceptor.getInterceptors().get(1), interceptors.get(1));

        Assert.assertEquals(webServiceEndpoint.getEndpointAdapter().getClass(), EmptyResponseEndpointAdapter.class);
        Assert.assertEquals(webServiceEndpoint.getEndpointConfiguration().getMessageConverter().getClass(), SoapMessageConverter.class);
        Assert.assertFalse(webServiceEndpoint.getEndpointConfiguration().isHandleMimeHeaders());
        Assert.assertFalse(webServiceEndpoint.getEndpointConfiguration().isKeepSoapEnvelope());
        Assert.assertNull(webServiceEndpoint.getDefaultNamespaceUri());
        Assert.assertEquals(webServiceEndpoint.getDefaultPrefix(), "");

        verify(webServiceServer);
    }

    @Test
    public void testConfigureMessageEndpoint() throws Exception {
        reset(webServiceServer);

        expect(webServiceServer.getInterceptors()).andReturn(null).once();
        expect(webServiceServer.getEndpointAdapter()).andReturn(new TimeoutProducingEndpointAdapter()).once();
        expect(webServiceServer.getMessageConverter()).andReturn(new WsAddressingMessageConverter(new WsAddressingHeaders())).once();
        expect(webServiceServer.isHandleMimeHeaders()).andReturn(true).once();
        expect(webServiceServer.isKeepSoapEnvelope()).andReturn(true).once();
        expect(webServiceServer.getSoapHeaderNamespace()).andReturn("http://citrusframework.org").times(2);
        expect(webServiceServer.getSoapHeaderPrefix()).andReturn("CITRUS").times(2);

        replay(webServiceServer);

        servlet.initStrategies(applicationContext);

        Assert.assertEquals(endpointInterceptor.getInterceptors().size(), 0L);
        Assert.assertEquals(webServiceEndpoint.getEndpointAdapter().getClass(), TimeoutProducingEndpointAdapter.class);
        Assert.assertEquals(webServiceEndpoint.getEndpointConfiguration().getMessageConverter().getClass(), WsAddressingMessageConverter.class);
        Assert.assertTrue(webServiceEndpoint.getEndpointConfiguration().isHandleMimeHeaders());
        Assert.assertTrue(webServiceEndpoint.getEndpointConfiguration().isKeepSoapEnvelope());
        Assert.assertEquals(webServiceEndpoint.getDefaultNamespaceUri(), "http://citrusframework.org");
        Assert.assertEquals(webServiceEndpoint.getDefaultPrefix(), "CITRUS");

        verify(webServiceServer);
    }
}
