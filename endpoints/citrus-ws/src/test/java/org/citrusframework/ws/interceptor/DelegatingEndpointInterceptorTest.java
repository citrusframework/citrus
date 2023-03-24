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

package org.citrusframework.ws.interceptor;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.ws.server.WebServiceEndpoint;
import org.mockito.Mockito;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.SmartEndpointInterceptor;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.server.SoapEndpointInterceptor;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.xml.namespace.QName;
import java.util.*;

import static org.mockito.Mockito.*;


/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class DelegatingEndpointInterceptorTest {

    private DelegatingEndpointInterceptor delegatingEndpointInterceptor = new DelegatingEndpointInterceptor();
    private WebServiceEndpoint webServiceEndpoint = new WebServiceEndpoint();
    private MessageContext messageContext;

    private SmartEndpointInterceptor smartEndpointInterceptorMock = Mockito.mock(SmartEndpointInterceptor.class);
    private EndpointInterceptor endpointInterceptorMock = Mockito.mock(EndpointInterceptor.class);
    private SoapEndpointInterceptor soapEndpointInterceptorMock = Mockito.mock(SoapEndpointInterceptor.class);

    private SoapHeaderElement soapHeaderElement = Mockito.mock(SoapHeaderElement.class);

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

        when(smartEndpointInterceptorMock.shouldIntercept(messageContext, webServiceEndpoint)).thenReturn(true);
        when(endpointInterceptorMock.handleRequest(messageContext, webServiceEndpoint)).thenReturn(true);
        when(smartEndpointInterceptorMock.handleRequest(messageContext, webServiceEndpoint)).thenReturn(true);
        when(endpointInterceptorMock.handleResponse(messageContext, webServiceEndpoint)).thenReturn(true);
        when(smartEndpointInterceptorMock.handleResponse(messageContext, webServiceEndpoint)).thenReturn(true);
        when(endpointInterceptorMock.handleFault(messageContext, webServiceEndpoint)).thenReturn(true);
        when(smartEndpointInterceptorMock.handleFault(messageContext, webServiceEndpoint)).thenReturn(true);

        Assert.assertTrue(delegatingEndpointInterceptor.handleRequest(messageContext, webServiceEndpoint));
        Assert.assertTrue(delegatingEndpointInterceptor.handleResponse(messageContext, webServiceEndpoint));
        Assert.assertTrue(delegatingEndpointInterceptor.handleFault(messageContext, webServiceEndpoint));
        delegatingEndpointInterceptor.afterCompletion(messageContext, webServiceEndpoint, ex);

        verify(endpointInterceptorMock).afterCompletion(messageContext, webServiceEndpoint, ex);
        verify(smartEndpointInterceptorMock).afterCompletion(messageContext, webServiceEndpoint, ex);
    }

    @Test
    public void testInterceptSoapMustUnderstand() throws Exception {
        QName soapHeader = new QName("http://citrusframework.org", "soapMustUnderstand", "citrus");

        List<EndpointInterceptor> interceptors = new ArrayList<EndpointInterceptor>();
        interceptors.add(endpointInterceptorMock);
        interceptors.add(smartEndpointInterceptorMock);
        interceptors.add(soapEndpointInterceptorMock);

        SoapMustUnderstandEndpointInterceptor soapMustUnderstandEndpointInterceptor = new SoapMustUnderstandEndpointInterceptor();
        soapMustUnderstandEndpointInterceptor.setAcceptedHeaders(Collections.<String>singletonList(soapHeader.toString()));
        interceptors.add(soapMustUnderstandEndpointInterceptor);

        delegatingEndpointInterceptor.setInterceptors(interceptors);

        reset(endpointInterceptorMock, smartEndpointInterceptorMock, soapEndpointInterceptorMock, soapHeaderElement);

        when(smartEndpointInterceptorMock.shouldIntercept(messageContext, webServiceEndpoint)).thenReturn(false);
        when(endpointInterceptorMock.handleRequest(messageContext, webServiceEndpoint)).thenReturn(true);
        when(soapEndpointInterceptorMock.handleRequest(messageContext, webServiceEndpoint)).thenReturn(true);
        when(endpointInterceptorMock.handleResponse(messageContext, webServiceEndpoint)).thenReturn(true);
        when(soapEndpointInterceptorMock.handleResponse(messageContext, webServiceEndpoint)).thenReturn(true);
        when(endpointInterceptorMock.handleFault(messageContext, webServiceEndpoint)).thenReturn(true);
        when(soapEndpointInterceptorMock.handleFault(messageContext, webServiceEndpoint)).thenReturn(true);

        when(soapHeaderElement.getName()).thenReturn(soapHeader);
        when(soapEndpointInterceptorMock.understands(soapHeaderElement)).thenReturn(false);

        Assert.assertTrue(delegatingEndpointInterceptor.handleRequest(messageContext, webServiceEndpoint));
        Assert.assertTrue(delegatingEndpointInterceptor.handleResponse(messageContext, webServiceEndpoint));
        Assert.assertTrue(delegatingEndpointInterceptor.handleFault(messageContext, webServiceEndpoint));
        delegatingEndpointInterceptor.afterCompletion(messageContext, webServiceEndpoint, ex);
        Assert.assertTrue(delegatingEndpointInterceptor.understands(soapHeaderElement));

        verify(endpointInterceptorMock).afterCompletion(messageContext, webServiceEndpoint, ex);
        verify(soapEndpointInterceptorMock).afterCompletion(messageContext, webServiceEndpoint, ex);
    }
}
