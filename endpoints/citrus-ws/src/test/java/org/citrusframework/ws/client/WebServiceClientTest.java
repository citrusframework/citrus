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

package org.citrusframework.ws.client;

import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.message.*;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.ws.message.SoapMessage;
import org.mockito.Mockito;
import org.springframework.ws.client.core.*;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class WebServiceClientTest extends AbstractTestNGUnitTest {

    private WebServiceTemplate webServiceTemplate = Mockito.mock(WebServiceTemplate.class);

    @Test
    public void testDefaultUri() {
        WebServiceClient client = new WebServiceClient();
        client.getEndpointConfiguration().setWebServiceTemplate(webServiceTemplate);

        Message requestMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(webServiceTemplate);

        when(webServiceTemplate.sendAndReceive(eq("http://localhost:8081/request"), any(WebServiceMessageCallback.class),
                any(WebServiceMessageCallback.class))).thenReturn(true);

        client.getEndpointConfiguration().setDefaultUri("http://localhost:8081/request");
        client.send(requestMessage, context);

        verify(webServiceTemplate, atLeastOnce()).setDefaultUri("http://localhost:8081/request");
        verify(webServiceTemplate).setFaultMessageResolver(any(FaultMessageResolver.class));
    }

    @Test
    public void testReplyMessageCorrelator() {
        WebServiceClient client = new WebServiceClient();

        client.getEndpointConfiguration().setWebServiceTemplate(webServiceTemplate);

        MessageCorrelator correlator = Mockito.mock(MessageCorrelator.class);
        client.getEndpointConfiguration().setCorrelator(correlator);

        Message requestMessage = new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(webServiceTemplate, correlator);

        when(webServiceTemplate.sendAndReceive(eq("http://localhost:8080/request"), any(WebServiceMessageCallback.class),
                any(WebServiceMessageCallback.class))).thenReturn(true);

        when(correlator.getCorrelationKey(requestMessage)).thenReturn("correlationKey");
        when(correlator.getCorrelationKeyName(any(String.class))).thenReturn("correlationKeyName");

        client.getEndpointConfiguration().setDefaultUri("http://localhost:8080/request");
        client.send(requestMessage, context);

        verify(webServiceTemplate, atLeastOnce()).setDefaultUri("http://localhost:8080/request");
        verify(webServiceTemplate).setFaultMessageResolver(any(FaultMessageResolver.class));
    }

    @Test
    public void testEndpointUriResolver() {
        WebServiceClient client = new WebServiceClient();

        client.getEndpointConfiguration().setWebServiceTemplate(webServiceTemplate);
        EndpointUriResolver endpointUriResolver = Mockito.mock(EndpointUriResolver.class);
        client.getEndpointConfiguration().setEndpointResolver(endpointUriResolver);

        Message requestMessage = new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(webServiceTemplate, endpointUriResolver);

        when(endpointUriResolver.resolveEndpointUri(requestMessage, "http://localhost:8080/request")).thenReturn("http://localhost:8081/new");

        when(webServiceTemplate.sendAndReceive(eq("http://localhost:8081/new"),
                any(WebServiceMessageCallback.class), any(WebServiceMessageCallback.class))).thenReturn(true);

        client.getEndpointConfiguration().setDefaultUri("http://localhost:8080/request");
        client.send(requestMessage, context);

        verify(webServiceTemplate, atLeastOnce()).setDefaultUri("http://localhost:8080/request");
        verify(webServiceTemplate).setFaultMessageResolver(any(FaultMessageResolver.class));
    }

    @Test
    public void testErrorResponseExceptionStrategy() {
        WebServiceClient client = new WebServiceClient();

        client.getEndpointConfiguration().setWebServiceTemplate(webServiceTemplate);
        client.getEndpointConfiguration().setErrorHandlingStrategy(ErrorHandlingStrategy.THROWS_EXCEPTION);

        Message requestMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        org.springframework.ws.soap.SoapMessage soapFaultMessage = Mockito.mock(org.springframework.ws.soap.SoapMessage.class);
        SoapBody soapBody = Mockito.mock(SoapBody.class);
        SoapFault soapFault = Mockito.mock(SoapFault.class);

        reset(webServiceTemplate, soapFaultMessage, soapBody, soapFault);


        when(soapFaultMessage.getSoapBody()).thenReturn(soapBody);
        when(soapFaultMessage.getFaultReason()).thenReturn("Internal server error");
        when(soapBody.getFault()).thenReturn(soapFault);

        doThrow(new SoapFaultClientException(soapFaultMessage)).when(webServiceTemplate).sendAndReceive(eq("http://localhost:8080/request"), any(WebServiceMessageCallback.class),
                any(WebServiceMessageCallback.class));

        try {
            client.getEndpointConfiguration().setDefaultUri("http://localhost:8080/request");
            client.send(requestMessage, context);
            Assert.fail("Missing exception due to soap fault");
        } catch (SoapFaultClientException e) {
            verify(webServiceTemplate, atLeastOnce()).setDefaultUri("http://localhost:8080/request");
            verify(webServiceTemplate).setFaultMessageResolver(any(FaultMessageResolver.class));
        }

    }
}
