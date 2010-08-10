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

package com.consol.citrus.ws;

import static org.easymock.EasyMock.*;

import java.util.*;

import javax.xml.namespace.QName;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.*;
import org.springframework.ws.soap.soap11.Soap11Body;
import org.springframework.ws.soap.soap11.Soap11Fault;
import org.springframework.ws.soap.soap12.Soap12Body;
import org.springframework.ws.soap.soap12.Soap12Fault;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.message.CitrusMessageHeaders;
import com.consol.citrus.message.MessageHandler;
import com.consol.citrus.ws.message.CitrusSoapMessageHeaders;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * @author Christoph Deppisch
 */
public class WebServiceEndpointTest {

    MessageContext messageContext = EasyMock.createMock(MessageContext.class);
    
    @Test
    public void testMessageProcessing() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        final Message<String> requestMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(requestHeaders)
                                .build();
        
        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        final Message<String> responseMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestResponse><Message>Hello World!</Message></TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();
        
        endpoint.setMessageHandler(new MessageHandler() {
            public Message<?> handleMessage(Message<?> message) {
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());
                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());
                
                return responseMessage;
            }
        });
        
        SoapMessage soapRequest = EasyMock.createMock(SoapMessage.class);
        SoapHeader soapRequestHeader = EasyMock.createMock(SoapHeader.class);
        
        SoapMessage soapResponse = EasyMock.createMock(SoapMessage.class);
        
        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapRequest, soapRequestHeader, soapResponse);
        
        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource("<TestRequest><Message>Hello World!</Message></TestRequest>")).once();
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn(null).anyTimes();
        
        expect(soapRequest.getAttachments()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(messageContext.getResponse()).andReturn(soapResponse).once();

        expect(soapResponse.getPayloadResult()).andReturn(soapResponsePayload).once();
        
        replay(messageContext, soapRequest, soapRequestHeader, soapResponse);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapRequest, soapRequestHeader, soapResponse);
    }
    
    @Test
    public void testMessageProcessingWithSoapAction() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(CitrusSoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message<String> requestMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(requestHeaders)
                                .build();
        
        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        final Message<String> responseMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestResponse><Message>Hello World!</Message></TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();
        
        endpoint.setMessageHandler(new MessageHandler() {
            public Message<?> handleMessage(Message<?> message) {
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());
                
                Assert.assertNotNull(message.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION), "sayHello");
                
                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());
                
                return responseMessage;
            }
        });
        
        SoapMessage soapRequest = EasyMock.createMock(SoapMessage.class);
        SoapHeader soapRequestHeader = EasyMock.createMock(SoapHeader.class);
        
        SoapMessage soapResponse = EasyMock.createMock(SoapMessage.class);
        
        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapRequest, soapRequestHeader, soapResponse);
        
        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource("<TestRequest><Message>Hello World!</Message></TestRequest>")).once();
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        expect(soapRequest.getAttachments()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(messageContext.getResponse()).andReturn(soapResponse).once();

        expect(soapResponse.getPayloadResult()).andReturn(soapResponsePayload).once();
        
        replay(messageContext, soapRequest, soapRequestHeader, soapResponse);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapRequest, soapRequestHeader, soapResponse);
    }
    
    @Test
    public void testMessageProcessingWithSoapRequestHeaders() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(CitrusSoapMessageHeaders.SOAP_ACTION, "sayHello");
        requestHeaders.put("Operation", "sayHello");
        final Message<String> requestMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(requestHeaders)
                                .build();
        
        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        final Message<String> responseMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestResponse><Message>Hello World!</Message></TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();
        
        endpoint.setMessageHandler(new MessageHandler() {
            public Message<?> handleMessage(Message<?> message) {
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());
                
                Assert.assertNotNull(message.getHeaders().get("Operation"));
                Assert.assertEquals(message.getHeaders().get("Operation"), "sayHello");
                
                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());
                
                return responseMessage;
            }
        });
        
        SoapMessage soapRequest = EasyMock.createMock(SoapMessage.class);
        SoapHeader soapRequestHeader = EasyMock.createMock(SoapHeader.class);
        Set<SoapHeaderElement> soapRequestHeaders = new HashSet<SoapHeaderElement>();
        SoapHeaderElement soapRequestHeaderEntry = EasyMock.createMock(SoapHeaderElement.class);
        soapRequestHeaders.add(soapRequestHeaderEntry);
        
        SoapMessage soapResponse = EasyMock.createMock(SoapMessage.class);
        
        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapRequest, soapRequestHeader, soapRequestHeaderEntry, soapResponse);
        
        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource("<TestRequest><Message>Hello World!</Message></TestRequest>")).once();
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(soapRequestHeaders.iterator()).once();
        
        expect(soapRequestHeaderEntry.getName()).andReturn(QNameUtils.createQName("http://www.consol.de/citrus", "Operation", "citrus")).once();
        expect(soapRequestHeaderEntry.getText()).andReturn("sayHello").once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        expect(soapRequest.getAttachments()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(messageContext.getResponse()).andReturn(soapResponse).once();

        expect(soapResponse.getPayloadResult()).andReturn(soapResponsePayload).once();
        
        replay(messageContext, soapRequest, soapRequestHeader, soapRequestHeaderEntry, soapResponse);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapRequest, soapRequestHeader, soapRequestHeaderEntry, soapResponse);
    }
    
    @Test
    public void testMessageProcessingWithSoapResponseHeaders() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(CitrusSoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message<String> requestMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(requestHeaders)
                                .build();
        
        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        responseHeaders.put("{http://www.consol.de/citrus}citrus:Operation", "sayHello");
        final Message<String> responseMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestResponse><Message>Hello World!</Message></TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();
        
        endpoint.setMessageHandler(new MessageHandler() {
            public Message<?> handleMessage(Message<?> message) {
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());
                
                Assert.assertNotNull(message.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION), "sayHello");
                
                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());
                
                return responseMessage;
            }
        });
        
        SoapMessage soapRequest = EasyMock.createMock(SoapMessage.class);
        SoapHeader soapRequestHeader = EasyMock.createMock(SoapHeader.class);
        
        SoapMessage soapResponse = EasyMock.createMock(SoapMessage.class);
        SoapHeader soapResponseHeader = EasyMock.createMock(SoapHeader.class);
        
        final SoapHeaderElement soapRequestHeaderEntry = EasyMock.createMock(SoapHeaderElement.class);
        
        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader);
        
        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource("<TestRequest><Message>Hello World!</Message></TestRequest>")).once();
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        expect(soapRequest.getAttachments()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(messageContext.getResponse()).andReturn(soapResponse).once();

        expect(soapResponse.getPayloadResult()).andReturn(soapResponsePayload).once();
        
        expect(soapResponse.getSoapHeader()).andReturn(soapResponseHeader).anyTimes();
        
        expect(soapResponseHeader.addHeaderElement((QName)anyObject())).andAnswer(new IAnswer<SoapHeaderElement>() {
            public SoapHeaderElement answer() throws Throwable {
                QName headerQName = (QName)EasyMock.getCurrentArguments()[0];
                
                Assert.assertEquals(headerQName.getLocalPart(), "Operation");
                Assert.assertEquals(headerQName.getPrefix(), "citrus");
                Assert.assertEquals(headerQName.getNamespaceURI(), "http://www.consol.de/citrus");
                return soapRequestHeaderEntry;
            }
        }).once();
        
        soapRequestHeaderEntry.setText("sayHello");
        expectLastCall().once();
        
        replay(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader);
    }
    
    @Test
    public void testMessageProcessingWithDefaultHeaderQName() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(CitrusSoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message<String> requestMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(requestHeaders)
                                .build();
        
        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        responseHeaders.put("Operation", "sayHello");
        final Message<String> responseMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestResponse><Message>Hello World!</Message></TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();
        
        endpoint.setMessageHandler(new MessageHandler() {
            public Message<?> handleMessage(Message<?> message) {
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());
                
                Assert.assertNotNull(message.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION), "sayHello");
                
                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());
                
                return responseMessage;
            }
        });

        endpoint.setDefaultNamespaceUri("http://www.consol.de/citrus");
        endpoint.setDefaultPrefix("citrus");
        
        SoapMessage soapRequest = EasyMock.createMock(SoapMessage.class);
        SoapHeader soapRequestHeader = EasyMock.createMock(SoapHeader.class);
        
        SoapMessage soapResponse = EasyMock.createMock(SoapMessage.class);
        SoapHeader soapResponseHeader = EasyMock.createMock(SoapHeader.class);
        
        final SoapHeaderElement soapRequestHeaderEntry = EasyMock.createMock(SoapHeaderElement.class);
        
        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader);
        
        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource("<TestRequest><Message>Hello World!</Message></TestRequest>")).once();
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        expect(soapRequest.getAttachments()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(messageContext.getResponse()).andReturn(soapResponse).once();

        expect(soapResponse.getPayloadResult()).andReturn(soapResponsePayload).once();
        
        expect(soapResponse.getSoapHeader()).andReturn(soapResponseHeader).anyTimes();
        
        expect(soapResponseHeader.addHeaderElement((QName)anyObject())).andAnswer(new IAnswer<SoapHeaderElement>() {
            public SoapHeaderElement answer() throws Throwable {
                QName headerQName = (QName)EasyMock.getCurrentArguments()[0];
                
                Assert.assertEquals(headerQName.getLocalPart(), "Operation");
                Assert.assertEquals(headerQName.getPrefix(), "citrus");
                Assert.assertEquals(headerQName.getNamespaceURI(), "http://www.consol.de/citrus");
                return soapRequestHeaderEntry;
            }
        }).once();
        
        soapRequestHeaderEntry.setText("sayHello");
        expectLastCall().once();
        
        replay(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader);
    }
    
    @Test
    public void testMessageProcessingWithDefaultHeaderQNameNoPrefix() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(CitrusSoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message<String> requestMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(requestHeaders)
                                .build();
        
        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        responseHeaders.put("Operation", "sayHello");
        final Message<String> responseMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestResponse><Message>Hello World!</Message></TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();
        
        endpoint.setMessageHandler(new MessageHandler() {
            public Message<?> handleMessage(Message<?> message) {
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());
                
                Assert.assertNotNull(message.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION), "sayHello");
                
                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());
                
                return responseMessage;
            }
        });

        endpoint.setDefaultNamespaceUri("http://www.consol.de/citrus");
        
        SoapMessage soapRequest = EasyMock.createMock(SoapMessage.class);
        SoapHeader soapRequestHeader = EasyMock.createMock(SoapHeader.class);
        
        SoapMessage soapResponse = EasyMock.createMock(SoapMessage.class);
        SoapHeader soapResponseHeader = EasyMock.createMock(SoapHeader.class);
        
        final SoapHeaderElement soapRequestHeaderEntry = EasyMock.createMock(SoapHeaderElement.class);
        
        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader);
        
        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource("<TestRequest><Message>Hello World!</Message></TestRequest>")).once();
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        expect(soapRequest.getAttachments()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(messageContext.getResponse()).andReturn(soapResponse).once();

        expect(soapResponse.getPayloadResult()).andReturn(soapResponsePayload).once();
        
        expect(soapResponse.getSoapHeader()).andReturn(soapResponseHeader).anyTimes();
        
        expect(soapResponseHeader.addHeaderElement((QName)anyObject())).andAnswer(new IAnswer<SoapHeaderElement>() {
            public SoapHeaderElement answer() throws Throwable {
                QName headerQName = (QName)EasyMock.getCurrentArguments()[0];
                
                Assert.assertEquals(headerQName.getLocalPart(), "Operation");
                Assert.assertEquals(headerQName.getPrefix(), "");
                Assert.assertEquals(headerQName.getNamespaceURI(), "http://www.consol.de/citrus");
                return soapRequestHeaderEntry;
            }
        }).once();
        
        soapRequestHeaderEntry.setText("sayHello");
        expectLastCall().once();
        
        replay(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader);
    }
    
    @Test(expectedExceptions = SoapHeaderException.class)
    public void testMessageProcessingMissingNamespaceUri() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(CitrusSoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message<String> requestMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(requestHeaders)
                                .build();
        
        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        responseHeaders.put("Operation", "sayHello");
        final Message<String> responseMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestResponse><Message>Hello World!</Message></TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();
        
        endpoint.setMessageHandler(new MessageHandler() {
            public Message<?> handleMessage(Message<?> message) {
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());
                
                Assert.assertNotNull(message.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION), "sayHello");
                
                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());
                
                return responseMessage;
            }
        });

        SoapMessage soapRequest = EasyMock.createMock(SoapMessage.class);
        SoapHeader soapRequestHeader = EasyMock.createMock(SoapHeader.class);
        
        SoapMessage soapResponse = EasyMock.createMock(SoapMessage.class);
        SoapHeader soapResponseHeader = EasyMock.createMock(SoapHeader.class);
        
        final SoapHeaderElement soapRequestHeaderEntry = EasyMock.createMock(SoapHeaderElement.class);
        
        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader);
        
        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource("<TestRequest><Message>Hello World!</Message></TestRequest>")).once();
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        expect(soapRequest.getAttachments()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(messageContext.getResponse()).andReturn(soapResponse).once();

        expect(soapResponse.getPayloadResult()).andReturn(soapResponsePayload).once();
        
        expect(soapResponse.getSoapHeader()).andReturn(soapResponseHeader).anyTimes();
        
        expect(soapResponseHeader.addHeaderElement((QName)anyObject())).andAnswer(new IAnswer<SoapHeaderElement>() {
            public SoapHeaderElement answer() throws Throwable {
                QName headerQName = (QName)EasyMock.getCurrentArguments()[0];
                
                Assert.assertEquals(headerQName.getLocalPart(), "Operation");
                Assert.assertEquals(headerQName.getPrefix(), "");
                Assert.assertEquals(headerQName.getNamespaceURI(), "");
                return soapRequestHeaderEntry;
            }
        }).once();
        
        soapRequestHeaderEntry.setText("sayHello");
        expectLastCall().once();
        
        replay(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader);
    }
    
    @Test
    public void testMessageProcessingWithSoapAttachment() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(CitrusSoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message<String> requestMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(requestHeaders)
                                .build();
        
        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        final Message<String> responseMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestResponse><Message>Hello World!</Message></TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();
        
        endpoint.setMessageHandler(new MessageHandler() {
            public Message<?> handleMessage(Message<?> message) {
                Assert.assertNotNull(message.getHeaders().get("myContentId"));
                
                Attachment attachment = (Attachment)message.getHeaders().get("myContentId");
                Assert.assertEquals(attachment.getContentId(), "myContentId");
                Assert.assertEquals(attachment.getContentType(), "text/xml");
                
                Assert.assertEquals(message.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION), "sayHello");
                
                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());
                
                return responseMessage;
            }
        });
        
        SoapMessage soapRequest = EasyMock.createMock(SoapMessage.class);
        SoapHeader soapRequestHeader = EasyMock.createMock(SoapHeader.class);
        
        SoapMessage soapResponse = EasyMock.createMock(SoapMessage.class);
        
        StringResult soapResponsePayload = new StringResult();
        
        Set<Attachment> attachments = new HashSet<Attachment>();
        Attachment attachment = EasyMock.createMock(Attachment.class);
        attachments.add(attachment);
        
        reset(messageContext, soapRequest, soapRequestHeader, soapResponse, attachment);
        
        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource("<TestRequest><Message>Hello World!</Message></TestRequest>")).once();
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        expect(soapRequest.getAttachments()).andReturn(attachments.iterator()).once();
        expect(attachment.getContentId()).andReturn("myContentId").anyTimes();
        expect(attachment.getContentType()).andReturn("text/xml").anyTimes();
        
        expect(messageContext.getResponse()).andReturn(soapResponse).once();

        expect(soapResponse.getPayloadResult()).andReturn(soapResponsePayload).once();
        
        replay(messageContext, soapRequest, soapRequestHeader, soapResponse, attachment);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapRequest, soapRequestHeader, soapResponse, attachment);
    }
    
    @Test
    public void testMessageProcessingWithServerSoapFaultInResponse() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(CitrusSoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message<String> requestMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(requestHeaders)
                                .build();
        
        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        responseHeaders.put("citrus_soap_fault", "SERVER,Invalid request");
        final Message<String> responseMessage = MessageBuilder.withPayload("")
                                .copyHeaders(responseHeaders)
                                .build();
        
        endpoint.setMessageHandler(new MessageHandler() {
            public Message<?> handleMessage(Message<?> message) {
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());
                
                Assert.assertNotNull(message.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION), "sayHello");
                
                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());
                
                return responseMessage;
            }
        });
        
        SoapMessage soapRequest = EasyMock.createMock(SoapMessage.class);
        SoapHeader soapRequestHeader = EasyMock.createMock(SoapHeader.class);
        
        SoapMessage soapResponse = EasyMock.createMock(SoapMessage.class);
        SoapHeader soapResponseHeader = EasyMock.createMock(SoapHeader.class);
        SoapBody soapResponseBody = EasyMock.createMock(SoapBody.class);
        final SoapFault soapFault = EasyMock.createMock(SoapFault.class);
        
        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapResponseBody);
        
        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource("<TestRequest><Message>Hello World!</Message></TestRequest>")).once();
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        expect(soapRequest.getAttachments()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(messageContext.getResponse()).andReturn(soapResponse).once();

        expect(soapResponse.getSoapHeader()).andReturn(soapResponseHeader).anyTimes();
        
        expect(soapResponse.getSoapBody()).andReturn(soapResponseBody).once();
        
        expect(soapResponseBody.addServerOrReceiverFault((String)anyObject(), (Locale)anyObject())).andAnswer(new IAnswer<SoapFault>() {
            public SoapFault answer() throws Throwable {
                Assert.assertEquals(EasyMock.getCurrentArguments()[0], "Invalid request");
                
                return soapFault;
            }
        });
        
        replay(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapResponseBody);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapResponseBody);
    }
    
    @Test
    public void testMessageProcessingWithClientSoapFaultInResponse() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(CitrusSoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message<String> requestMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(requestHeaders)
                                .build();
        
        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        responseHeaders.put("citrus_soap_fault", "CLIENT,Invalid request");
        final Message<String> responseMessage = MessageBuilder.withPayload("")
                                .copyHeaders(responseHeaders)
                                .build();
        
        endpoint.setMessageHandler(new MessageHandler() {
            public Message<?> handleMessage(Message<?> message) {
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());
                
                Assert.assertNotNull(message.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION), "sayHello");
                
                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());
                
                return responseMessage;
            }
        });
        
        SoapMessage soapRequest = EasyMock.createMock(SoapMessage.class);
        SoapHeader soapRequestHeader = EasyMock.createMock(SoapHeader.class);
        
        SoapMessage soapResponse = EasyMock.createMock(SoapMessage.class);
        SoapHeader soapResponseHeader = EasyMock.createMock(SoapHeader.class);
        SoapBody soapResponseBody = EasyMock.createMock(SoapBody.class);
        final SoapFault soapFault = EasyMock.createMock(SoapFault.class);
        
        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapResponseBody);
        
        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource("<TestRequest><Message>Hello World!</Message></TestRequest>")).once();
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        expect(soapRequest.getAttachments()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(messageContext.getResponse()).andReturn(soapResponse).once();

        expect(soapResponse.getSoapHeader()).andReturn(soapResponseHeader).anyTimes();
        
        expect(soapResponse.getSoapBody()).andReturn(soapResponseBody).once();
        
        expect(soapResponseBody.addClientOrSenderFault((String)anyObject(), (Locale)anyObject())).andAnswer(new IAnswer<SoapFault>() {
            public SoapFault answer() throws Throwable {
                Assert.assertEquals(EasyMock.getCurrentArguments()[0], "Invalid request");
                
                return soapFault;
            }
        });
        
        replay(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapResponseBody);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapResponseBody);
    }
    
    @Test
    public void testMessageProcessingWithSoapFaultDetail() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(CitrusSoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message<String> requestMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(requestHeaders)
                                .build();
        
        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        responseHeaders.put("citrus_soap_fault", "SERVER,Invalid request");
        final Message<String> responseMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><DetailMessage><text>This request was not OK!</text></DetailMessage>")
                                .copyHeaders(responseHeaders)
                                .build();
        
        endpoint.setMessageHandler(new MessageHandler() {
            public Message<?> handleMessage(Message<?> message) {
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());
                
                Assert.assertNotNull(message.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION), "sayHello");
                
                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());
                
                return responseMessage;
            }
        });
        
        SoapMessage soapRequest = EasyMock.createMock(SoapMessage.class);
        SoapHeader soapRequestHeader = EasyMock.createMock(SoapHeader.class);
        
        SoapMessage soapResponse = EasyMock.createMock(SoapMessage.class);
        SoapHeader soapResponseHeader = EasyMock.createMock(SoapHeader.class);
        SoapBody soapResponseBody = EasyMock.createMock(SoapBody.class);
        final SoapFault soapFault = EasyMock.createMock(SoapFault.class);
        SoapFaultDetail soapFaultDetail = EasyMock.createMock(SoapFaultDetail.class);
        
        StringResult soapFaultResult = new StringResult();
        
        reset(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapResponseBody, soapFault, soapFaultDetail);
        
        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource("<TestRequest><Message>Hello World!</Message></TestRequest>")).once();
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        expect(soapRequest.getAttachments()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(messageContext.getResponse()).andReturn(soapResponse).once();

        expect(soapResponse.getSoapHeader()).andReturn(soapResponseHeader).anyTimes();
        
        expect(soapResponse.getSoapBody()).andReturn(soapResponseBody).once();
        
        expect(soapResponseBody.addServerOrReceiverFault((String)anyObject(), (Locale)anyObject())).andAnswer(new IAnswer<SoapFault>() {
            public SoapFault answer() throws Throwable {
                Assert.assertEquals(EasyMock.getCurrentArguments()[0], "Invalid request");
                
                return soapFault;
            }
        });
        
        expect(soapFault.addFaultDetail()).andReturn(soapFaultDetail).once();
        
        expect(soapFaultDetail.getResult()).andReturn(soapFaultResult).once();
        
        replay(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapResponseBody, soapFault, soapFaultDetail);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapFaultResult.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapResponseBody, soapFault, soapFaultDetail);
    }
    
    @Test
    public void testMessageProcessingWithSoapActionInResponse() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(CitrusSoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message<String> requestMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(requestHeaders)
                                .build();
        
        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        responseHeaders.put(CitrusSoapMessageHeaders.SOAP_ACTION, "answerHello");
        responseHeaders.put(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR, "someCorrelator");
        final Message<String> responseMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestResponse><Message>Hello World!</Message></TestResponse>")
                                .copyHeaders(responseHeaders)
                                .build();
        
        endpoint.setMessageHandler(new MessageHandler() {
            public Message<?> handleMessage(Message<?> message) {
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());
                
                Assert.assertNotNull(message.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION), "sayHello");
                
                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());
                
                return responseMessage;
            }
        });
        
        SoapMessage soapRequest = EasyMock.createMock(SoapMessage.class);
        SoapHeader soapRequestHeader = EasyMock.createMock(SoapHeader.class);
        
        SoapMessage soapResponse = EasyMock.createMock(SoapMessage.class);
        
        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapRequest, soapRequestHeader, soapResponse);
        
        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource("<TestRequest><Message>Hello World!</Message></TestRequest>")).once();
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        expect(soapRequest.getAttachments()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(messageContext.getResponse()).andReturn(soapResponse).once();

        expect(soapResponse.getPayloadResult()).andReturn(soapResponsePayload).once();
        
        soapResponse.setSoapAction("answerHello");
        expectLastCall().once();
        
        replay(messageContext, soapRequest, soapRequestHeader, soapResponse);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapRequest, soapRequestHeader, soapResponse);
    }
    
    @Test
    public void testMessageProcessingWithSoap11FaultInResponse() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(CitrusSoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message<String> requestMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(requestHeaders)
                                .build();
        
        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        responseHeaders.put("citrus_soap_fault", "{http://www.consol.de/citrus}citrus:TEC-1000,Invalid request");
        final Message<String> responseMessage = MessageBuilder.withPayload("")
                                .copyHeaders(responseHeaders)
                                .build();
        
        endpoint.setMessageHandler(new MessageHandler() {
            public Message<?> handleMessage(Message<?> message) {
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());
                
                Assert.assertNotNull(message.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION), "sayHello");
                
                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());
                
                return responseMessage;
            }
        });
        
        SoapMessage soapRequest = EasyMock.createMock(SoapMessage.class);
        SoapHeader soapRequestHeader = EasyMock.createMock(SoapHeader.class);
        
        SoapMessage soapResponse = EasyMock.createMock(SoapMessage.class);
        SoapHeader soapResponseHeader = EasyMock.createMock(SoapHeader.class);
        Soap11Body soapResponseBody = EasyMock.createMock(Soap11Body.class);
        final Soap11Fault soapFault = EasyMock.createMock(Soap11Fault.class);
        
        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapResponseBody);
        
        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource("<TestRequest><Message>Hello World!</Message></TestRequest>")).once();
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        expect(soapRequest.getAttachments()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(messageContext.getResponse()).andReturn(soapResponse).once();

        expect(soapResponse.getSoapHeader()).andReturn(soapResponseHeader).anyTimes();
        
        expect(soapResponse.getSoapBody()).andReturn(soapResponseBody).once();
        
        expect(soapResponseBody.addFault((QName)anyObject(), (String)anyObject(), (Locale)anyObject())).andAnswer(new IAnswer<Soap11Fault>() {
            public Soap11Fault answer() throws Throwable {
                QName faultQName = (QName)EasyMock.getCurrentArguments()[0];
                
                Assert.assertEquals(faultQName.getLocalPart(), "TEC-1000");
                Assert.assertEquals(faultQName.getPrefix(), "citrus");
                Assert.assertEquals(faultQName.getNamespaceURI(), "http://www.consol.de/citrus");
                Assert.assertEquals(EasyMock.getCurrentArguments()[1], "Invalid request");
                
                return soapFault;
            }
        });
        
        replay(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapResponseBody);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapResponseBody);
    }
    
    @Test
    public void testMessageProcessingWithSoap12FaultInResponse() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(CitrusSoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message<String> requestMessage = MessageBuilder.withPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(requestHeaders)
                                .build();
        
        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        responseHeaders.put("citrus_soap_fault", "{http://www.consol.de/citrus}citrus:TEC-1000,Invalid request");
        final Message<String> responseMessage = MessageBuilder.withPayload("")
                                .copyHeaders(responseHeaders)
                                .build();
        
        endpoint.setMessageHandler(new MessageHandler() {
            public Message<?> handleMessage(Message<?> message) {
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());
                
                Assert.assertNotNull(message.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION), "sayHello");
                
                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());
                
                return responseMessage;
            }
        });
        
        SoapMessage soapRequest = EasyMock.createMock(SoapMessage.class);
        SoapHeader soapRequestHeader = EasyMock.createMock(SoapHeader.class);
        
        SoapMessage soapResponse = EasyMock.createMock(SoapMessage.class);
        SoapHeader soapResponseHeader = EasyMock.createMock(SoapHeader.class);
        Soap12Body soapResponseBody = EasyMock.createMock(Soap12Body.class);
        final Soap12Fault soapFault = EasyMock.createMock(Soap12Fault.class);
        
        
        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapResponseBody, soapFault);
        
        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource("<TestRequest><Message>Hello World!</Message></TestRequest>")).once();
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        expect(soapRequest.getAttachments()).andReturn(Collections.emptySet().iterator()).once();
        
        expect(messageContext.getResponse()).andReturn(soapResponse).once();

        expect(soapResponse.getSoapHeader()).andReturn(soapResponseHeader).anyTimes();
        
        expect(soapResponse.getSoapBody()).andReturn(soapResponseBody).once();
        
        expect(soapResponseBody.addServerOrReceiverFault((String)anyObject(), (Locale)anyObject())).andAnswer(new IAnswer<Soap12Fault>() {
            public Soap12Fault answer() throws Throwable {
                Assert.assertEquals(EasyMock.getCurrentArguments()[0], "Invalid request");
                
                return soapFault;
            }
        });
        
        soapFault.addFaultSubcode((QName)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                QName faultQName = (QName)EasyMock.getCurrentArguments()[0];
                
                Assert.assertEquals(faultQName.getLocalPart(), "TEC-1000");
                Assert.assertEquals(faultQName.getPrefix(), "citrus");
                Assert.assertEquals(faultQName.getNamespaceURI(), "http://www.consol.de/citrus");
                return null;
            }
        }).once();
        
        replay(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapResponseBody, soapFault);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapResponseBody, soapFault);
    }
}
