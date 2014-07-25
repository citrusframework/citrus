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

package com.consol.citrus.ws.client;

import com.consol.citrus.endpoint.resolver.EndpointUriResolver;
import com.consol.citrus.message.ErrorHandlingStrategy;
import com.consol.citrus.message.ReplyMessageCorrelator;
import org.easymock.EasyMock;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.ws.client.core.*;
import org.springframework.ws.soap.*;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class WebServiceClientTest {

    private WebServiceTemplate webServiceTemplate = EasyMock.createMock(WebServiceTemplate.class);

    @Test
    public void testDefaultUri() {
        WebServiceClient client = new WebServiceClient();
        client.getEndpointConfiguration().setWebServiceTemplate(webServiceTemplate);

        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();

        reset(webServiceTemplate);

        webServiceTemplate.setDefaultUri("http://localhost:8081/request");
        expectLastCall().once();

        webServiceTemplate.setFaultMessageResolver(anyObject(FaultMessageResolver.class));
        expectLastCall().once();

        expect(webServiceTemplate.sendAndReceive(eq("http://localhost:8081/request"), (WebServiceMessageCallback)anyObject(),
                (WebServiceMessageCallback)anyObject())).andReturn(true).once();

        replay(webServiceTemplate);

        client.getEndpointConfiguration().setDefaultUri("http://localhost:8081/request");
        client.send(requestMessage);

        verify(webServiceTemplate);
    }

    @Test
    public void testReplyMessageCorrelator() {
        WebServiceClient client = new WebServiceClient();

        client.getEndpointConfiguration().setWebServiceTemplate(webServiceTemplate);

        ReplyMessageCorrelator correlator = EasyMock.createMock(ReplyMessageCorrelator.class);
        client.getEndpointConfiguration().setCorrelator(correlator);

        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();

        reset(webServiceTemplate, correlator);

        webServiceTemplate.setDefaultUri("http://localhost:8080/request");
        expectLastCall().once();

        webServiceTemplate.setFaultMessageResolver(anyObject(FaultMessageResolver.class));
        expectLastCall().once();

        expect(webServiceTemplate.sendAndReceive(eq("http://localhost:8080/request"), (WebServiceMessageCallback)anyObject(),
                (WebServiceMessageCallback)anyObject())).andReturn(true).once();

        expect(correlator.getCorrelationKey(requestMessage)).andReturn("correlationKey").once();

        replay(webServiceTemplate, correlator);

        client.getEndpointConfiguration().setDefaultUri("http://localhost:8080/request");
        client.send(requestMessage);

        verify(webServiceTemplate, correlator);
    }

    @Test
    public void testEndpointUriResolver() {
        WebServiceClient client = new WebServiceClient();

        client.getEndpointConfiguration().setWebServiceTemplate(webServiceTemplate);
        EndpointUriResolver endpointUriResolver = EasyMock.createMock(EndpointUriResolver.class);
        client.getEndpointConfiguration().setEndpointResolver(endpointUriResolver);

        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();

        reset(webServiceTemplate, endpointUriResolver);

        webServiceTemplate.setDefaultUri("http://localhost:8080/request");
        expectLastCall().once();

        webServiceTemplate.setFaultMessageResolver(anyObject(FaultMessageResolver.class));
        expectLastCall().once();

        expect(endpointUriResolver.resolveEndpointUri(requestMessage, "http://localhost:8080/request")).andReturn("http://localhost:8081/new").once();

        expect(webServiceTemplate.sendAndReceive(eq("http://localhost:8081/new"),
                (WebServiceMessageCallback)anyObject(), (WebServiceMessageCallback)anyObject())).andReturn(true).once();

        replay(webServiceTemplate, endpointUriResolver);

        client.getEndpointConfiguration().setDefaultUri("http://localhost:8080/request");
        client.send(requestMessage);

        verify(webServiceTemplate, endpointUriResolver);
    }

    @Test
    public void testErrorResponseExceptionStrategy() {
        WebServiceClient client = new WebServiceClient();

        client.getEndpointConfiguration().setWebServiceTemplate(webServiceTemplate);
        client.getEndpointConfiguration().setErrorHandlingStrategy(ErrorHandlingStrategy.THROWS_EXCEPTION);

        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();

        SoapMessage soapFaultMessage = EasyMock.createMock(SoapMessage.class);
        SoapBody soapBody = EasyMock.createMock(SoapBody.class);
        SoapFault soapFault = EasyMock.createMock(SoapFault.class);

        reset(webServiceTemplate, soapFaultMessage, soapBody, soapFault);

        webServiceTemplate.setDefaultUri("http://localhost:8080/request");
        expectLastCall().once();

        webServiceTemplate.setFaultMessageResolver(anyObject(FaultMessageResolver.class));
        expectLastCall().once();

        expect(soapFaultMessage.getSoapBody()).andReturn(soapBody).anyTimes();
        expect(soapFaultMessage.getFaultReason()).andReturn("Internal server error").anyTimes();
        expect(soapBody.getFault()).andReturn(soapFault).once();

        replay(soapFaultMessage, soapBody, soapFault);

        expect(webServiceTemplate.sendAndReceive(eq("http://localhost:8080/request"), (WebServiceMessageCallback)anyObject(),
                (WebServiceMessageCallback)anyObject())).andThrow(new SoapFaultClientException(soapFaultMessage)).once();

        replay(webServiceTemplate);

        try {
            client.getEndpointConfiguration().setDefaultUri("http://localhost:8080/request");
            client.send(requestMessage);
            Assert.fail("Missing exception due to soap fault");
        } catch (SoapFaultClientException e) {
            verify(webServiceTemplate, soapFaultMessage, soapBody, soapFault);
        }

    }
}
