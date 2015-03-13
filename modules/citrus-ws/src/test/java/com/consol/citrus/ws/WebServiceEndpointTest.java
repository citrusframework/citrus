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

import com.consol.citrus.endpoint.adapter.StaticEndpointAdapter;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.ws.client.WebServiceEndpointConfiguration;
import com.consol.citrus.ws.message.SoapFault;
import com.consol.citrus.ws.message.SoapMessage;
import com.consol.citrus.ws.message.*;
import com.consol.citrus.ws.server.WebServiceEndpoint;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.*;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.soap11.Soap11Body;
import org.springframework.ws.soap.soap11.Soap11Fault;
import org.springframework.ws.soap.soap12.Soap12Body;
import org.springframework.ws.soap.soap12.Soap12Fault;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.namespace.QName;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayInputStream;
import java.util.*;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class WebServiceEndpointTest {

    private MessageContext messageContext = EasyMock.createMock(MessageContext.class);
    private SoapHeader soapRequestHeader = EasyMock.createMock(SoapHeader.class);
    private SoapHeader soapResponseHeader = EasyMock.createMock(SoapHeader.class);
    private SoapHeaderElement soapRequestHeaderEntry = EasyMock.createMock(SoapHeaderElement.class);
    private org.springframework.ws.soap.SoapMessage soapRequest = EasyMock.createMock(org.springframework.ws.soap.SoapMessage.class);
    private org.springframework.ws.soap.SoapMessage soapResponse = EasyMock.createMock(org.springframework.ws.soap.SoapMessage.class);

    private SoapEnvelope soapEnvelope = EasyMock.createMock(SoapEnvelope.class);
    private SoapBody soapBody = EasyMock.createMock(SoapBody.class);

    private SaajSoapMessage saajSoapRequest = EasyMock.createMock(SaajSoapMessage.class);

    private org.springframework.ws.soap.SoapFault soapFault = EasyMock.createMock(org.springframework.ws.soap.SoapFault.class);
    private SoapFaultDetail soapFaultDetail = EasyMock.createMock(SoapFaultDetail.class);

    private String requestPayload = "<TestRequest><Message>Hello World!</Message></TestRequest>";

    @Test
    public void testMessageProcessing() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        final Message requestMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>");

        final Message responseMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestResponse><Message>Hello World!</Message></TestResponse>");

        endpoint.setEndpointAdapter(new StaticEndpointAdapter() {
            public Message handleMessageInternal(Message message) {
                Assert.assertEquals(message.copyHeaders().size(), requestMessage.copyHeaders().size());
                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse);

        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        expect(soapRequest.getEnvelope()).andReturn(soapEnvelope).once();
        expect(soapEnvelope.getSource()).andReturn(new StringSource(getSoapRequestPayload())).once();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource(requestPayload)).times(2);
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        expect(soapRequestHeader.getSource()).andReturn(null).once();
        
        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(emptyHeaderSet.iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn(null).anyTimes();
        
        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        expect(soapRequest.getAttachments()).andReturn(emptyAttachmentSet.iterator()).once();
        
        expect(messageContext.getResponse()).andReturn(soapResponse).once();

        expect(soapResponse.getPayloadResult()).andReturn(soapResponsePayload).once();
        
        replay(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse);
    }

    @Test
    public void testMessageProcessingWithSoapAction() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(SoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message requestMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>", requestHeaders);

        final Message responseMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestResponse><Message>Hello World!</Message></TestResponse>");

        endpoint.setEndpointAdapter(new StaticEndpointAdapter() {
            public Message handleMessageInternal(Message message) {
                Assert.assertEquals(message.copyHeaders().size(), requestMessage.copyHeaders().size());

                Assert.assertNotNull(message.getHeader(SoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse);

        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        expect(soapRequest.getEnvelope()).andReturn(soapEnvelope).once();
        expect(soapEnvelope.getSource()).andReturn(new StringSource(getSoapRequestPayload())).once();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource(requestPayload)).times(2);
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        expect(soapRequestHeader.getSource()).andReturn(null).once();
        
        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(emptyHeaderSet.iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        expect(soapRequest.getAttachments()).andReturn(emptyAttachmentSet.iterator()).once();
        
        expect(messageContext.getResponse()).andReturn(soapResponse).once();

        expect(soapResponse.getPayloadResult()).andReturn(soapResponsePayload).once();
        
        replay(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse);
    }
    
    @Test
    public void testMessageProcessingWithSoapRequestHeaders() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(SoapMessageHeaders.SOAP_ACTION, "sayHello");
        requestHeaders.put("Operation", "sayHello");
        final Message requestMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>", requestHeaders);

        final Message responseMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestResponse><Message>Hello World!</Message></TestResponse>");

        endpoint.setEndpointAdapter(new StaticEndpointAdapter() {
            public Message handleMessageInternal(Message message) {
                Assert.assertEquals(message.copyHeaders().size(), requestMessage.copyHeaders().size());

                Assert.assertNotNull(message.getHeader("Operation"));
                Assert.assertEquals(message.getHeader("Operation"), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        Set<SoapHeaderElement> soapRequestHeaders = new HashSet<SoapHeaderElement>();
        soapRequestHeaders.add(soapRequestHeaderEntry);

        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapRequestHeaderEntry, soapResponse);

        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        expect(soapRequest.getEnvelope()).andReturn(soapEnvelope).once();
        expect(soapEnvelope.getSource()).andReturn(new StringSource(getSoapRequestPayload())).once();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource(requestPayload)).times(2);
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        expect(soapRequestHeader.getSource()).andReturn(null).once();
        
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(soapRequestHeaders.iterator()).once();
        
        expect(soapRequestHeaderEntry.getName()).andReturn(QNameUtils.createQName("http://www.consol.de/citrus", "Operation", "citrus")).once();
        expect(soapRequestHeaderEntry.getText()).andReturn("sayHello").once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        expect(soapRequest.getAttachments()).andReturn(emptyAttachmentSet.iterator()).once();
        
        expect(messageContext.getResponse()).andReturn(soapResponse).once();

        expect(soapResponse.getPayloadResult()).andReturn(soapResponsePayload).once();
        
        replay(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapRequestHeaderEntry, soapResponse);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapRequestHeaderEntry, soapResponse);
    }
    
    @Test
    public void testMessageProcessingWithMimeRequestHeaders() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        WebServiceEndpointConfiguration endpointConfiguration = new WebServiceEndpointConfiguration();
        endpointConfiguration.setHandleMimeHeaders(true);

        endpoint.setEndpointConfiguration(endpointConfiguration);

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(SoapMessageHeaders.SOAP_ACTION, "sayHello");
        requestHeaders.put("Operation", "sayHello");
        requestHeaders.put("Host", "localhost:8080");
        requestHeaders.put("Content-Length", "236");
        requestHeaders.put("Accept", "text/xml, text/html, image/gif, image/jpeg");
        requestHeaders.put("Content-Type", "text/xml");
        
        final Message requestMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>", requestHeaders);

        final Message responseMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestResponse><Message>Hello World!</Message></TestResponse>");

        endpoint.setEndpointAdapter(new StaticEndpointAdapter() {
            public Message handleMessageInternal(Message message) {
                Assert.assertEquals(message.copyHeaders().size(), requestMessage.copyHeaders().size());

                Assert.assertNotNull(message.getHeader("Operation"));
                Assert.assertEquals(message.getHeader("Operation"), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });
        
        SOAPMessage soapRequestMessage = EasyMock.createMock(SOAPMessage.class);
        MimeHeaders mimeHeaders = new MimeHeaders();
        mimeHeaders.addHeader("Host", "localhost:8080");
        mimeHeaders.addHeader("Content-Length", "236");
        mimeHeaders.addHeader("Accept", "text/xml");
        mimeHeaders.addHeader("Accept", "text/html");
        mimeHeaders.addHeader("Accept", "image/gif");
        mimeHeaders.addHeader("Accept", "image/jpeg");
        mimeHeaders.addHeader("Content-Type", "text/xml");
        
        Set<SoapHeaderElement> soapRequestHeaders = new HashSet<SoapHeaderElement>();
        soapRequestHeaders.add(soapRequestHeaderEntry);

        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapEnvelope, soapRequestHeader, soapBody, soapRequestHeaderEntry, soapResponse, saajSoapRequest, soapRequestMessage);

        expect(saajSoapRequest.getEnvelope()).andReturn(soapEnvelope).anyTimes();
        expect(saajSoapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        expect(saajSoapRequest.getAttachments()).andReturn(emptyAttachmentSet.iterator()).once();
        
        expect(saajSoapRequest.getSaajMessage()).andReturn(soapRequestMessage).once();
        expect(soapRequestMessage.getMimeHeaders()).andReturn(mimeHeaders).once();

        expect(messageContext.getRequest()).andReturn(saajSoapRequest).anyTimes();
        expect(soapEnvelope.getSource()).andReturn(new StringSource(getSoapRequestPayload())).once();

        expect(soapEnvelope.getBody()).andReturn(soapBody).times(2);
        expect(soapBody.getPayloadSource()).andReturn(new StringSource(requestPayload)).times(2);
        
        expect(soapEnvelope.getHeader()).andReturn(soapRequestHeader).once();
        expect(soapRequestHeader.getSource()).andReturn(null).once();
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(soapRequestHeaders.iterator()).once();
        
        expect(soapRequestHeaderEntry.getName()).andReturn(QNameUtils.createQName("http://www.consol.de/citrus", "Operation", "citrus")).once();
        expect(soapRequestHeaderEntry.getText()).andReturn("sayHello").once();
        
        expect(messageContext.getResponse()).andReturn(soapResponse).once();

        expect(soapResponse.getPayloadResult()).andReturn(soapResponsePayload).once();
        
        replay(messageContext, soapEnvelope, soapRequestHeader, soapBody, soapRequestHeaderEntry, soapResponse, saajSoapRequest, soapRequestMessage);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapEnvelope, soapRequestHeader, soapBody, soapRequestHeaderEntry, soapResponse, saajSoapRequest, soapRequestMessage);
    }
    
    @Test
    public void testMessageProcessingWithSoapResponseHeaders() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(SoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message requestMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>", requestHeaders);

        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        responseHeaders.put("{http://www.consol.de/citrus}citrus:Operation", "sayHello");
        final Message responseMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestResponse><Message>Hello World!</Message></TestResponse>", responseHeaders);

        endpoint.setEndpointAdapter(new StaticEndpointAdapter() {
            public Message handleMessageInternal(Message message) {
                Assert.assertEquals(message.copyHeaders().size(), requestMessage.copyHeaders().size());

                Assert.assertNotNull(message.getHeader(SoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapRequestHeaderEntry);

        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        expect(soapRequest.getEnvelope()).andReturn(soapEnvelope).once();
        expect(soapEnvelope.getSource()).andReturn(new StringSource(getSoapRequestPayload())).once();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource(requestPayload)).times(2);
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        expect(soapRequestHeader.getSource()).andReturn(null).once();
        
        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(emptyHeaderSet.iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        expect(soapRequest.getAttachments()).andReturn(emptyAttachmentSet.iterator()).once();
        
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
        
        replay(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapRequestHeaderEntry);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapRequestHeaderEntry);
    }
    
    @Test
    public void testMessageProcessingWithDefaultHeaderQName() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(SoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message requestMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>", requestHeaders);

        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        responseHeaders.put("Operation", "sayHello");
        final Message responseMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestResponse><Message>Hello World!</Message></TestResponse>", responseHeaders);

        endpoint.setEndpointAdapter(new StaticEndpointAdapter() {
            public Message handleMessageInternal(Message message) {
                Assert.assertEquals(message.copyHeaders().size(), requestMessage.copyHeaders().size());

                Assert.assertNotNull(message.getHeader(SoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        endpoint.setDefaultNamespaceUri("http://www.consol.de/citrus");
        endpoint.setDefaultPrefix("citrus");

        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader);

        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        expect(soapRequest.getEnvelope()).andReturn(soapEnvelope).once();
        expect(soapEnvelope.getSource()).andReturn(new StringSource(getSoapRequestPayload())).once();
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource(requestPayload)).times(2);
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        expect(soapRequestHeader.getSource()).andReturn(null).once();
        
        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(emptyHeaderSet.iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        expect(soapRequest.getAttachments()).andReturn(emptyAttachmentSet.iterator()).once();
        
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
        
        replay(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader);
    }
    
    @Test
    public void testMessageProcessingWithDefaultHeaderQNameNoPrefix() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(SoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message requestMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>", requestHeaders);

        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        responseHeaders.put("Operation", "sayHello");
        final Message responseMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestResponse><Message>Hello World!</Message></TestResponse>", responseHeaders);

        endpoint.setEndpointAdapter(new StaticEndpointAdapter() {
            public Message handleMessageInternal(Message message) {
                Assert.assertEquals(message.copyHeaders().size(), requestMessage.copyHeaders().size());

                Assert.assertNotNull(message.getHeader(SoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        endpoint.setDefaultNamespaceUri("http://www.consol.de/citrus");

        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader);

        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        expect(soapRequest.getEnvelope()).andReturn(soapEnvelope).once();
        expect(soapEnvelope.getSource()).andReturn(new StringSource(getSoapRequestPayload())).once();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource(requestPayload)).times(2);
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        expect(soapRequestHeader.getSource()).andReturn(null).once();
        
        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(emptyHeaderSet.iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        expect(soapRequest.getAttachments()).andReturn(emptyAttachmentSet.iterator()).once();
        
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
        
        replay(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader);
    }
    
    @Test(expectedExceptions = SoapHeaderException.class)
    public void testMessageProcessingMissingNamespaceUri() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(SoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message requestMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>", requestHeaders);

        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        responseHeaders.put("Operation", "sayHello");
        final Message responseMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestResponse><Message>Hello World!</Message></TestResponse>", responseHeaders);

        endpoint.setEndpointAdapter(new StaticEndpointAdapter() {
            public Message handleMessageInternal(Message message) {
                Assert.assertEquals(message.copyHeaders().size(), requestMessage.copyHeaders().size());

                Assert.assertNotNull(message.getHeader(SoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader);

        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        expect(soapRequest.getEnvelope()).andReturn(soapEnvelope).once();
        expect(soapEnvelope.getSource()).andReturn(new StringSource(getSoapRequestPayload())).once();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource(requestPayload)).times(2);
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        expect(soapRequestHeader.getSource()).andReturn(null).once();
        
        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(emptyHeaderSet.iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        expect(soapRequest.getAttachments()).andReturn(emptyAttachmentSet.iterator()).once();
        
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
        
        replay(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader);
    }
    
    @Test
    public void testMessageProcessingWithSoapAttachment() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(SoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message requestMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>", requestHeaders);

        final Message responseMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestResponse><Message>Hello World!</Message></TestResponse>");

        endpoint.setEndpointAdapter(new StaticEndpointAdapter() {
            public Message handleMessageInternal(Message message) {
                Assert.assertTrue(SoapMessage.class.isInstance(message));

                SoapMessage soapMessage = (SoapMessage) message;

                Assert.assertEquals(soapMessage.getAttachments().size(), 1L);

                Attachment attachment = soapMessage.getAttachments().get(0);
                Assert.assertEquals(attachment.getContentId(), "myContentId");
                Assert.assertEquals(attachment.getContentType(), "text/xml");

                Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });


        StringResult soapResponsePayload = new StringResult();
        
        Set<Attachment> attachments = new HashSet<Attachment>();
        Attachment attachment = EasyMock.createMock(Attachment.class);
        attachments.add(attachment);
        
        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, attachment);

        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        expect(soapRequest.getEnvelope()).andReturn(soapEnvelope).once();
        expect(soapEnvelope.getSource()).andReturn(new StringSource(getSoapRequestPayload())).once();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource(requestPayload)).times(2);
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        expect(soapRequestHeader.getSource()).andReturn(null).once();
        
        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(emptyHeaderSet.iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        expect(soapRequest.getAttachments()).andReturn(attachments.iterator()).once();
        expect(attachment.getContentId()).andReturn("myContentId").anyTimes();
        expect(attachment.getContentType()).andReturn("text/xml").anyTimes();
        
        expect(messageContext.getResponse()).andReturn(soapResponse).once();

        expect(soapResponse.getPayloadResult()).andReturn(soapResponsePayload).once();
        
        expect(attachment.getInputStream()).andReturn(new ByteArrayInputStream("AttachmentBody".getBytes())).once();

        replay(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, attachment);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, attachment);
    }
    
    @Test
    public void testMessageProcessingWithServerSoapFaultInResponse() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(SoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message requestMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>", requestHeaders);

        final SoapFault responseMessage = new SoapFault();
        responseMessage.faultCode("SERVER");
        responseMessage.faultString("Invalid request, because of unknown error");

        endpoint.setEndpointAdapter(new StaticEndpointAdapter() {
            public Message handleMessageInternal(Message message) {
                Assert.assertEquals(message.copyHeaders().size(), requestMessage.copyHeaders().size());

                Assert.assertNotNull(message.getHeader(SoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapBody);

        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        expect(soapRequest.getEnvelope()).andReturn(soapEnvelope).once();
        expect(soapEnvelope.getSource()).andReturn(new StringSource(getSoapRequestPayload())).once();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource(requestPayload)).times(2);
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        expect(soapRequestHeader.getSource()).andReturn(null).once();
        
        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(emptyHeaderSet.iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        expect(soapRequest.getAttachments()).andReturn(emptyAttachmentSet.iterator()).once();
        
        expect(messageContext.getResponse()).andReturn(soapResponse).once();

        expect(soapResponse.getSoapHeader()).andReturn(soapResponseHeader).anyTimes();
        
        expect(soapResponse.getSoapBody()).andReturn(soapBody).once();
        
        expect(soapBody.addServerOrReceiverFault((String)anyObject(), (Locale)anyObject())).andAnswer(new IAnswer<org.springframework.ws.soap.SoapFault>() {
            public org.springframework.ws.soap.SoapFault answer() throws Throwable {
                Assert.assertEquals(EasyMock.getCurrentArguments()[0], "Invalid request, because of unknown error");

                return soapFault;
            }
        });
        
        replay(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapBody);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapBody);
    }
    
    @Test
    public void testMessageProcessingWithClientSoapFaultInResponse() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(SoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message requestMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>", requestHeaders);

        final SoapFault responseMessage = new SoapFault();
        responseMessage.faultCode("CLIENT");
        responseMessage.faultString("Invalid request");

        endpoint.setEndpointAdapter(new StaticEndpointAdapter() {
            public Message handleMessageInternal(Message message) {
                Assert.assertEquals(message.copyHeaders().size(), requestMessage.copyHeaders().size());

                Assert.assertNotNull(message.getHeader(SoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapBody);

        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        expect(soapRequest.getEnvelope()).andReturn(soapEnvelope).once();
        expect(soapEnvelope.getSource()).andReturn(new StringSource(getSoapRequestPayload())).once();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource(requestPayload)).times(2);
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        expect(soapRequestHeader.getSource()).andReturn(null).once();
        
        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(emptyHeaderSet.iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        expect(soapRequest.getAttachments()).andReturn(emptyAttachmentSet.iterator()).once();
        
        expect(messageContext.getResponse()).andReturn(soapResponse).once();

        expect(soapResponse.getSoapHeader()).andReturn(soapResponseHeader).anyTimes();
        
        expect(soapResponse.getSoapBody()).andReturn(soapBody).once();
        
        expect(soapBody.addClientOrSenderFault((String)anyObject(), (Locale)anyObject())).andAnswer(new IAnswer<org.springframework.ws.soap.SoapFault>() {
            public org.springframework.ws.soap.SoapFault answer() throws Throwable {
                Assert.assertEquals(EasyMock.getCurrentArguments()[0], "Invalid request");

                return soapFault;
            }
        });
        
        replay(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapBody);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapBody);
    }
    
    @Test
    public void testMessageProcessingWithSoapFaultDetail() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(SoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message requestMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>", requestHeaders);

        final SoapFault responseMessage = new SoapFault();
        responseMessage.setPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ResponseMessage><text>This request was not OK!</text></ResponseMessage>");
        responseMessage.faultCode("SERVER");
        responseMessage.faultString("Invalid request");
        responseMessage.addFaultDetail("<DetailMessage><text>This request was not OK!</text></DetailMessage>");

        endpoint.setEndpointAdapter(new StaticEndpointAdapter() {
            public Message handleMessageInternal(Message message) {
                Assert.assertEquals(message.copyHeaders().size(), requestMessage.copyHeaders().size());

                Assert.assertNotNull(message.getHeader(SoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        StringResult soapFaultResult = new StringResult();
        
        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapBody, soapFault, soapFaultDetail);

        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        expect(soapRequest.getEnvelope()).andReturn(soapEnvelope).once();
        expect(soapEnvelope.getSource()).andReturn(new StringSource(getSoapRequestPayload())).once();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource(requestPayload)).times(2);
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        expect(soapRequestHeader.getSource()).andReturn(null).once();
        
        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(emptyHeaderSet.iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        expect(soapRequest.getAttachments()).andReturn(emptyAttachmentSet.iterator()).once();
        
        expect(messageContext.getResponse()).andReturn(soapResponse).once();

        expect(soapResponse.getSoapHeader()).andReturn(soapResponseHeader).anyTimes();
        
        expect(soapResponse.getSoapBody()).andReturn(soapBody).once();
        
        expect(soapBody.addServerOrReceiverFault((String)anyObject(), (Locale)anyObject())).andAnswer(new IAnswer<org.springframework.ws.soap.SoapFault>() {
            public org.springframework.ws.soap.SoapFault answer() throws Throwable {
                Assert.assertEquals(EasyMock.getCurrentArguments()[0], "Invalid request");

                return soapFault;
            }
        });
        
        expect(soapFault.addFaultDetail()).andReturn(soapFaultDetail).once();
        
        expect(soapFaultDetail.getResult()).andReturn(soapFaultResult).once();
        
        replay(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapBody, soapFault, soapFaultDetail);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapFaultResult.toString(), "<DetailMessage><text>This request was not OK!</text></DetailMessage>");
        
        verify(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapBody, soapFault, soapFaultDetail);
    }
    
    @Test
    public void testMessageProcessingWithMultipleSoapFaultDetails() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(SoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message requestMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>", requestHeaders);

        final SoapFault responseMessage = new SoapFault();
        responseMessage.setPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ResponseMessage><text>This request was not OK!</text></ResponseMessage>");
        responseMessage.faultCode("SERVER");
        responseMessage.faultString("Invalid request");
        responseMessage.addFaultDetail("<DetailMessage><text>This request was not OK!</text></DetailMessage>");
        responseMessage.addFaultDetail("<Error><text>This request was not OK!</text></Error>");

        endpoint.setEndpointAdapter(new StaticEndpointAdapter() {
            public Message handleMessageInternal(Message message) {
                Assert.assertEquals(message.copyHeaders().size(), requestMessage.copyHeaders().size());

                Assert.assertNotNull(message.getHeader(SoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        StringResult soapFaultResult = new StringResult();
        
        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapBody, soapFault, soapFaultDetail);

        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        expect(soapRequest.getEnvelope()).andReturn(soapEnvelope).once();
        expect(soapEnvelope.getSource()).andReturn(new StringSource(getSoapRequestPayload())).once();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource(requestPayload)).times(2);
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        expect(soapRequestHeader.getSource()).andReturn(null).once();
        
        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(emptyHeaderSet.iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        expect(soapRequest.getAttachments()).andReturn(emptyAttachmentSet.iterator()).once();
        
        expect(messageContext.getResponse()).andReturn(soapResponse).once();

        expect(soapResponse.getSoapHeader()).andReturn(soapResponseHeader).anyTimes();
        
        expect(soapResponse.getSoapBody()).andReturn(soapBody).once();
        
        expect(soapBody.addServerOrReceiverFault((String)anyObject(), (Locale)anyObject())).andAnswer(new IAnswer<org.springframework.ws.soap.SoapFault>() {
            public org.springframework.ws.soap.SoapFault answer() throws Throwable {
                Assert.assertEquals(EasyMock.getCurrentArguments()[0], "Invalid request");

                return soapFault;
            }
        });
        
        expect(soapFault.addFaultDetail()).andReturn(soapFaultDetail).once();
        
        expect(soapFaultDetail.getResult()).andReturn(soapFaultResult).times(2);
        
        replay(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapBody, soapFault, soapFaultDetail);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapFaultResult.toString(), "<DetailMessage><text>This request was not OK!</text></DetailMessage><Error><text>This request was not OK!</text></Error>");
        
        verify(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapBody, soapFault, soapFaultDetail);
    }
    
    @Test
    public void testMessageProcessingWithSoapActionInResponse() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(SoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message requestMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>", requestHeaders);

        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        responseHeaders.put(SoapMessageHeaders.SOAP_ACTION, "answerHello");
        final Message responseMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestResponse><Message>Hello World!</Message></TestResponse>", responseHeaders);

        endpoint.setEndpointAdapter(new StaticEndpointAdapter() {
            public Message handleMessageInternal(Message message) {
                Assert.assertEquals(message.copyHeaders().size(), requestMessage.copyHeaders().size());

                Assert.assertNotNull(message.getHeader(SoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });


        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse);

        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        expect(soapRequest.getEnvelope()).andReturn(soapEnvelope).once();
        expect(soapEnvelope.getSource()).andReturn(new StringSource(getSoapRequestPayload())).once();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource(requestPayload)).times(2);
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        expect(soapRequestHeader.getSource()).andReturn(null).once();
        
        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(emptyHeaderSet.iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        expect(soapRequest.getAttachments()).andReturn(emptyAttachmentSet.iterator()).once();
        
        expect(messageContext.getResponse()).andReturn(soapResponse).once();

        expect(soapResponse.getPayloadResult()).andReturn(soapResponsePayload).once();
        
        soapResponse.setSoapAction("answerHello");
        expectLastCall().once();
        
        replay(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse);
    }
    
    @Test
    public void testMessageProcessingWithSoap11FaultInResponse() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(SoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message requestMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>", requestHeaders);

        final SoapFault responseMessage = new SoapFault();
        responseMessage.faultCode("{http://www.consol.de/citrus}citrus:TEC-1000");
        responseMessage.faultString("Invalid request");

        endpoint.setEndpointAdapter(new StaticEndpointAdapter() {
            public Message handleMessageInternal(Message message) {
                Assert.assertEquals(message.copyHeaders().size(), requestMessage.copyHeaders().size());

                Assert.assertNotNull(message.getHeader(SoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        Soap11Body soapResponseBody = EasyMock.createMock(Soap11Body.class);
        final Soap11Fault soapFault = EasyMock.createMock(Soap11Fault.class);
        
        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapResponseBody);

        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        expect(soapRequest.getEnvelope()).andReturn(soapEnvelope).once();
        expect(soapEnvelope.getSource()).andReturn(new StringSource(getSoapRequestPayload())).once();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource(requestPayload)).times(2);
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        expect(soapRequestHeader.getSource()).andReturn(null).once();
        
        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(emptyHeaderSet.iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        expect(soapRequest.getAttachments()).andReturn(emptyAttachmentSet.iterator()).once();
        
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
        
        replay(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapResponseBody);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapResponseBody);
    }
    
    @Test
    public void testMessageProcessingWithSoap12FaultInResponse() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(SoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message requestMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>", requestHeaders);

        final SoapFault responseMessage = new SoapFault();
        responseMessage.faultCode("{http://www.consol.de/citrus}citrus:TEC-1000");
        responseMessage.faultString("Invalid request");

        endpoint.setEndpointAdapter(new StaticEndpointAdapter() {
            public Message handleMessageInternal(Message message) {
                Assert.assertEquals(message.copyHeaders().size(), requestMessage.copyHeaders().size());

                Assert.assertNotNull(message.getHeader(SoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        Soap12Body soapResponseBody = EasyMock.createMock(Soap12Body.class);
        final Soap12Fault soapFault = EasyMock.createMock(Soap12Fault.class);
        
        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapResponseBody, soapFault);

        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        expect(soapRequest.getEnvelope()).andReturn(soapEnvelope).once();
        expect(soapEnvelope.getSource()).andReturn(new StringSource(getSoapRequestPayload())).once();
        
        expect(soapRequest.getPayloadSource()).andReturn(new StringSource(requestPayload)).times(2);
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        expect(soapRequestHeader.getSource()).andReturn(null).once();
        
        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(emptyHeaderSet.iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn("sayHello").anyTimes();
        
        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        expect(soapRequest.getAttachments()).andReturn(emptyAttachmentSet.iterator()).once();
        
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
        
        replay(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapResponseBody, soapFault);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapResponseBody, soapFault);
    }
    
    @Test
    public void testEmptySoapMessageProcessing() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        final Message requestMessage = new DefaultMessage("");
        final Message responseMessage = new DefaultMessage("");

        endpoint.setEndpointAdapter(new StaticEndpointAdapter() {
            public Message handleMessageInternal(Message message) {
                Assert.assertEquals(message.copyHeaders().size(), requestMessage.copyHeaders().size());
                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });


        StringResult soapResponsePayload = new StringResult();
        
        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse);

        expect(messageContext.getRequest()).andReturn(soapRequest).anyTimes();
        expect(soapRequest.getEnvelope()).andReturn(soapEnvelope).once();
        expect(soapEnvelope.getSource()).andReturn(new StringSource("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Header/><SOAP-ENV:Body></SOAP-ENV:Body></SOAP-ENV:Envelope>")).once();
        
        expect(soapRequest.getPayloadSource()).andReturn(null).once();
        
        expect(messageContext.getPropertyNames()).andReturn(new String[]{}).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapRequestHeader).once();
        expect(soapRequestHeader.getSource()).andReturn(null).once();
        
        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        expect(soapRequestHeader.examineAllHeaderElements()).andReturn(emptyHeaderSet.iterator()).once();
        
        expect(soapRequest.getSoapAction()).andReturn(null).anyTimes();
        
        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        expect(soapRequest.getAttachments()).andReturn(emptyAttachmentSet.iterator()).once();
        
        expect(messageContext.getResponse()).andReturn(soapResponse).once();

        replay(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse);
        
        endpoint.invoke(messageContext);
        
        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
        
        verify(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse);
    }

    private String getSoapRequestPayload() {
        return "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                    "<SOAP-ENV:Header/>\n" +
                    "<SOAP-ENV:Body>\n" +
                        requestPayload +
                    "</SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>";
    }
}
