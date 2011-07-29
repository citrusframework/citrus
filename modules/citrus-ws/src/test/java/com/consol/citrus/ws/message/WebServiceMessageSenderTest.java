/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.ws.message;

import static org.easymock.EasyMock.*;

import org.easymock.EasyMock;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.ws.client.core.*;
import org.springframework.ws.soap.*;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.adapter.common.endpoint.EndpointUriResolver;
import com.consol.citrus.message.MessageSender.ErrorHandlingStrategy;
import com.consol.citrus.message.*;

/**
 * @author Christoph Deppisch
 */
public class WebServiceMessageSenderTest {

    private WebServiceTemplate webServiceTemplate = EasyMock.createMock(WebServiceTemplate.class);
    private ReplyMessageHandler replyMessageHandler = EasyMock.createMock(ReplyMessageHandler.class);
    
    @Test
    public void testDefaultUri() {
        WebServiceMessageSender messageSender = new WebServiceMessageSender();
        
        messageSender.setReplyMessageHandler(replyMessageHandler);
        messageSender.setWebServiceTemplate(webServiceTemplate);

        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();
        
        reset(webServiceTemplate, replyMessageHandler);
        
        expect(webServiceTemplate.getDefaultUri()).andReturn("http://localhost:8080/request").once();
        
        webServiceTemplate.setFaultMessageResolver(anyObject(FaultMessageResolver.class));
        expectLastCall().once();
        
        expect(webServiceTemplate.sendAndReceive((WebServiceMessageCallback)anyObject(), 
                (WebServiceMessageCallback)anyObject())).andReturn(true).once();
        
        replyMessageHandler.onReplyMessage(anyObject(Message.class));
        expectLastCall().once();
        
        replay(webServiceTemplate, replyMessageHandler);
        
        messageSender.send(requestMessage);
        
        verify(webServiceTemplate, replyMessageHandler);
    }
    
    @Test
    public void testReplyMessageCorrelator() {
        WebServiceMessageSender messageSender = new WebServiceMessageSender();
        
        messageSender.setReplyMessageHandler(replyMessageHandler);
        messageSender.setWebServiceTemplate(webServiceTemplate);

        ReplyMessageCorrelator correlator = EasyMock.createMock(ReplyMessageCorrelator.class);
        messageSender.setCorrelator(correlator);

        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();
        
        reset(webServiceTemplate, replyMessageHandler, correlator);
        
        expect(webServiceTemplate.getDefaultUri()).andReturn("http://localhost:8080/request").once();
        
        webServiceTemplate.setFaultMessageResolver(anyObject(FaultMessageResolver.class));
        expectLastCall().once();
        
        expect(webServiceTemplate.sendAndReceive((WebServiceMessageCallback)anyObject(), 
                (WebServiceMessageCallback)anyObject())).andReturn(true).once();
        
        expect(correlator.getCorrelationKey(requestMessage)).andReturn("correlationKey").once();
        
        replyMessageHandler.onReplyMessage(anyObject(Message.class), eq("correlationKey"));
        expectLastCall().once();
        
        replay(webServiceTemplate, replyMessageHandler, correlator);
        
        messageSender.send(requestMessage);
        
        verify(webServiceTemplate, replyMessageHandler, correlator);
    }
    
    @Test
    public void testEndpointUriResolver() {
        WebServiceMessageSender messageSender = new WebServiceMessageSender();
        
        messageSender.setReplyMessageHandler(replyMessageHandler);
        messageSender.setWebServiceTemplate(webServiceTemplate);
        EndpointUriResolver endpointUriResolver = EasyMock.createMock(EndpointUriResolver.class);
        messageSender.setEndpointResolver(endpointUriResolver);

        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();
        
        reset(webServiceTemplate, replyMessageHandler, endpointUriResolver);
        
        expect(webServiceTemplate.getDefaultUri()).andReturn("http://localhost:8080/request").once();
        
        webServiceTemplate.setFaultMessageResolver(anyObject(FaultMessageResolver.class));
        expectLastCall().once();
        
        
        expect(endpointUriResolver.resolveEndpointUri(requestMessage, "http://localhost:8080/request")).andReturn("http://localhost:8081/new").once();
        
        expect(webServiceTemplate.sendAndReceive(eq("http://localhost:8081/new"), 
                (WebServiceMessageCallback)anyObject(), (WebServiceMessageCallback)anyObject())).andReturn(true).once();
        
        replyMessageHandler.onReplyMessage(anyObject(Message.class));
        expectLastCall().once();
        
        replay(webServiceTemplate, replyMessageHandler, endpointUriResolver);
        
        messageSender.send(requestMessage);
        
        verify(webServiceTemplate, replyMessageHandler, endpointUriResolver);
    }
    
    @Test
    public void testErrorResponseExceptionStrategy() {
        WebServiceMessageSender messageSender = new WebServiceMessageSender();
        
        messageSender.setReplyMessageHandler(replyMessageHandler);
        messageSender.setWebServiceTemplate(webServiceTemplate);
        messageSender.setErrorHandlingStrategy(ErrorHandlingStrategy.THROWS_EXCEPTION);
        
        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();
        
        SoapMessage soapFaultMessage = EasyMock.createMock(SoapMessage.class);
        SoapBody soapBody = EasyMock.createMock(SoapBody.class);
        SoapFault soapFault = EasyMock.createMock(SoapFault.class);
        
        reset(webServiceTemplate, replyMessageHandler, soapFaultMessage, soapBody, soapFault);
        
        expect(webServiceTemplate.getDefaultUri()).andReturn("http://localhost:8080/request").once();
        
        webServiceTemplate.setFaultMessageResolver(anyObject(FaultMessageResolver.class));
        expectLastCall().once();
        
        expect(soapFaultMessage.getSoapBody()).andReturn(soapBody).anyTimes();
        expect(soapFaultMessage.getFaultReason()).andReturn("Internal server error").anyTimes();
        expect(soapBody.getFault()).andReturn(soapFault).once();
        
        replay(soapFaultMessage, soapBody, soapFault);
        
        expect(webServiceTemplate.sendAndReceive((WebServiceMessageCallback)anyObject(), 
                (WebServiceMessageCallback)anyObject())).andThrow(new SoapFaultClientException(soapFaultMessage)).once();
        
        replay(webServiceTemplate, replyMessageHandler);
        
        try {
            messageSender.send(requestMessage);
            Assert.fail("Missing exception due to soap fault");
        } catch (SoapFaultClientException e) {
            verify(webServiceTemplate, replyMessageHandler, soapFaultMessage, soapBody, soapFault);
        }
        
    }
}
