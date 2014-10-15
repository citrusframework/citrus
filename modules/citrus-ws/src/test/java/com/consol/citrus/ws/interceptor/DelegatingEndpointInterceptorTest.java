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

package com.consol.citrus.ws.interceptor;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.ws.server.WebServiceEndpoint;
import org.easymock.EasyMock;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.SmartEndpointInterceptor;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.server.SoapEndpointInterceptor;
import org.springframework.xml.namespace.QNameUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.xml.namespace.QName;
import java.util.*;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class DelegatingEndpointInterceptorTest {

    private DelegatingEndpointInterceptor delegatingEndpointInterceptor = new DelegatingEndpointInterceptor();
    private WebServiceEndpoint webServiceEndpoint = new WebServiceEndpoint();
    private MessageContext messageContext;

    private SmartEndpointInterceptor smartEndpointInterceptorMock = EasyMock.createMock(SmartEndpointInterceptor.class);
    private EndpointInterceptor endpointInterceptorMock = EasyMock.createMock(EndpointInterceptor.class);
    private SoapEndpointInterceptor soapEndpointInterceptorMock = EasyMock.createMock(SoapEndpointInterceptor.class);

    private SoapHeaderElement soapHeaderElement = EasyMock.createMock(SoapHeaderElement.class);

    private CitrusRuntimeException ex = new CitrusRuntimeException();

    @BeforeClass
    public void setup() {
        SaajSoapMessageFactory messageFactory = new SaajSoapMessageFactory();
        messageFactory.afterPropertiesSet();
        messageContext = new DefaultMessageContext(messageFactory);
    }

    @Test
    public void testShouldIntercept() throws Exception {
        Assert.assertTrue(delegatingEndpointInterceptor.shouldIntercept(messageContext, webServiceEndpoint));
    }

    @Test
    public void testIntercept() throws Exception {
        List<EndpointInterceptor> interceptors = new ArrayList<EndpointInterceptor>();
        interceptors.add(endpointInterceptorMock);
        interceptors.add(smartEndpointInterceptorMock);

        delegatingEndpointInterceptor.setInterceptors(interceptors);

        reset(endpointInterceptorMock, smartEndpointInterceptorMock);

        expect(smartEndpointInterceptorMock.shouldIntercept(messageContext, webServiceEndpoint)).andReturn(true).times(4);
        expect(endpointInterceptorMock.handleRequest(messageContext, webServiceEndpoint)).andReturn(true).once();
        expect(smartEndpointInterceptorMock.handleRequest(messageContext, webServiceEndpoint)).andReturn(true).once();
        expect(endpointInterceptorMock.handleResponse(messageContext, webServiceEndpoint)).andReturn(true).once();
        expect(smartEndpointInterceptorMock.handleResponse(messageContext, webServiceEndpoint)).andReturn(true).once();
        expect(endpointInterceptorMock.handleFault(messageContext, webServiceEndpoint)).andReturn(true).once();
        expect(smartEndpointInterceptorMock.handleFault(messageContext, webServiceEndpoint)).andReturn(true).once();
        endpointInterceptorMock.afterCompletion(messageContext, webServiceEndpoint, ex);
        expectLastCall().once();
        smartEndpointInterceptorMock.afterCompletion(messageContext, webServiceEndpoint, ex);
        expectLastCall().once();

        replay(endpointInterceptorMock, smartEndpointInterceptorMock);

        Assert.assertTrue(delegatingEndpointInterceptor.handleRequest(messageContext, webServiceEndpoint));
        Assert.assertTrue(delegatingEndpointInterceptor.handleResponse(messageContext, webServiceEndpoint));
        Assert.assertTrue(delegatingEndpointInterceptor.handleFault(messageContext, webServiceEndpoint));
        delegatingEndpointInterceptor.afterCompletion(messageContext, webServiceEndpoint, ex);

        verify(endpointInterceptorMock, smartEndpointInterceptorMock);
    }

    @Test
    public void testInterceptSoapMustUnderstand() throws Exception {
        QName soapHeader = QNameUtils.createQName("http://citrusframework.org", "soapMustUnderstand", "citrus");

        List<EndpointInterceptor> interceptors = new ArrayList<EndpointInterceptor>();
        interceptors.add(endpointInterceptorMock);
        interceptors.add(smartEndpointInterceptorMock);
        interceptors.add(soapEndpointInterceptorMock);

        SoapMustUnderstandEndpointInterceptor soapMustUnderstandEndpointInterceptor = new SoapMustUnderstandEndpointInterceptor();
        soapMustUnderstandEndpointInterceptor.setAcceptedHeaders(Collections.<String>singletonList(soapHeader.toString()));
        interceptors.add(soapMustUnderstandEndpointInterceptor);

        delegatingEndpointInterceptor.setInterceptors(interceptors);

        reset(endpointInterceptorMock, smartEndpointInterceptorMock, soapEndpointInterceptorMock, soapHeaderElement);

        expect(smartEndpointInterceptorMock.shouldIntercept(messageContext, webServiceEndpoint)).andReturn(false).times(4);
        expect(endpointInterceptorMock.handleRequest(messageContext, webServiceEndpoint)).andReturn(true).once();
        expect(soapEndpointInterceptorMock.handleRequest(messageContext, webServiceEndpoint)).andReturn(true).once();
        expect(endpointInterceptorMock.handleResponse(messageContext, webServiceEndpoint)).andReturn(true).once();
        expect(soapEndpointInterceptorMock.handleResponse(messageContext, webServiceEndpoint)).andReturn(true).once();
        expect(endpointInterceptorMock.handleFault(messageContext, webServiceEndpoint)).andReturn(true).once();
        expect(soapEndpointInterceptorMock.handleFault(messageContext, webServiceEndpoint)).andReturn(true).once();
        endpointInterceptorMock.afterCompletion(messageContext, webServiceEndpoint, ex);
        expectLastCall().once();
        soapEndpointInterceptorMock.afterCompletion(messageContext, webServiceEndpoint, ex);
        expectLastCall().once();

        expect(soapHeaderElement.getName()).andReturn(soapHeader).times(2);
        expect(soapEndpointInterceptorMock.understands(soapHeaderElement)).andReturn(false).once();

        replay(endpointInterceptorMock, smartEndpointInterceptorMock, soapEndpointInterceptorMock, soapHeaderElement);

        Assert.assertTrue(delegatingEndpointInterceptor.handleRequest(messageContext, webServiceEndpoint));
        Assert.assertTrue(delegatingEndpointInterceptor.handleResponse(messageContext, webServiceEndpoint));
        Assert.assertTrue(delegatingEndpointInterceptor.handleFault(messageContext, webServiceEndpoint));
        delegatingEndpointInterceptor.afterCompletion(messageContext, webServiceEndpoint, ex);
        Assert.assertTrue(delegatingEndpointInterceptor.understands(soapHeaderElement));

        verify(endpointInterceptorMock, smartEndpointInterceptorMock, soapEndpointInterceptorMock, soapHeaderElement);
    }
}
