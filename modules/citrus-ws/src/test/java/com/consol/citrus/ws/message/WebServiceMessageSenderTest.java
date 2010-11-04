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
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.ws.client.core.*;
import org.testng.annotations.Test;

import com.consol.citrus.adapter.common.endpoint.EndpointUriResolver;
import com.consol.citrus.message.ReplyMessageHandler;

/**
 * @author Christoph Deppisch
 */
public class WebServiceMessageSenderTest {

    @Test
    public void testDefaultUri() {
        WebServiceMessageSender messageSender = new WebServiceMessageSender();
        
        messageSender.setReplyMessageHandler(new ReplyMessageHandler() {
            public void onReplyMessage(Message<?> replyMessage, String correlationKey) {
            }
            public void onReplyMessage(Message<?> replyMessage) {
            }
        });

        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();
        
        WebServiceTemplate webServiceTemplate = EasyMock.createMock(WebServiceTemplate.class);
        messageSender.setWebServiceTemplate(webServiceTemplate);
        
        reset(webServiceTemplate);
        
        expect(webServiceTemplate.getDefaultUri()).andReturn("http://localhost:8080/request").once();
        
        webServiceTemplate.setFaultMessageResolver(anyObject(FaultMessageResolver.class));
        expectLastCall().once();
        
        expect(webServiceTemplate.sendAndReceive((WebServiceMessageCallback)anyObject(), 
                (WebServiceMessageCallback)anyObject())).andReturn(true).once();
        
        replay(webServiceTemplate);
        
        messageSender.send(requestMessage);
        
        verify(webServiceTemplate);
    }
    
    @Test
    public void testEndpointUriResolver() {
        WebServiceMessageSender messageSender = new WebServiceMessageSender();
        
        messageSender.setReplyMessageHandler(new ReplyMessageHandler() {
            public void onReplyMessage(Message<?> replyMessage, String correlationKey) {
            }
            public void onReplyMessage(Message<?> replyMessage) {
            }
        });

        Message<?> requestMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build();
        
        EndpointUriResolver endpointUriResolver = EasyMock.createMock(EndpointUriResolver.class);
        messageSender.setEndpointResolver(endpointUriResolver);
        
        WebServiceTemplate webServiceTemplate = EasyMock.createMock(WebServiceTemplate.class);
        messageSender.setWebServiceTemplate(webServiceTemplate);
        
        reset(webServiceTemplate, endpointUriResolver);
        
        expect(webServiceTemplate.getDefaultUri()).andReturn("http://localhost:8080/request").once();
        
        webServiceTemplate.setFaultMessageResolver(anyObject(FaultMessageResolver.class));
        expectLastCall().once();
        
        
        expect(endpointUriResolver.resolveEndpointUri(requestMessage, "http://localhost:8080/request")).andReturn("http://localhost:8081/new").once();
        
        expect(webServiceTemplate.sendAndReceive(eq("http://localhost:8081/new"), 
                (WebServiceMessageCallback)anyObject(), (WebServiceMessageCallback)anyObject())).andReturn(true).once();
        
        replay(webServiceTemplate, endpointUriResolver);
        
        messageSender.send(requestMessage);
        
        verify(webServiceTemplate, endpointUriResolver);
    }
}
