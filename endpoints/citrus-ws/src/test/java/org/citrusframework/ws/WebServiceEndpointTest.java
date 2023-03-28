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

package org.citrusframework.ws;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

import org.citrusframework.endpoint.adapter.StaticEndpointAdapter;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.ws.client.WebServiceEndpointConfiguration;
import org.citrusframework.ws.message.SoapAttachment;
import org.citrusframework.ws.message.SoapFault;
import org.citrusframework.ws.message.SoapMessage;
import org.citrusframework.ws.message.SoapMessageHeaders;
import org.citrusframework.ws.server.WebServiceEndpoint;
import org.citrusframework.xml.StringResult;
import org.citrusframework.xml.StringSource;
import jakarta.activation.DataHandler;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPMessage;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapHeaderException;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.soap11.Soap11Body;
import org.springframework.ws.soap.soap11.Soap11Fault;
import org.springframework.ws.soap.soap12.Soap12Body;
import org.springframework.ws.soap.soap12.Soap12Fault;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class WebServiceEndpointTest {

    private MessageContext messageContext = Mockito.mock(MessageContext.class);
    private SoapHeader soapRequestHeader = Mockito.mock(SoapHeader.class);
    private SoapHeader soapResponseHeader = Mockito.mock(SoapHeader.class);
    private SoapHeaderElement soapRequestHeaderEntry = Mockito.mock(SoapHeaderElement.class);
    private org.springframework.ws.soap.SoapMessage soapRequest = Mockito.mock(org.springframework.ws.soap.SoapMessage.class);
    private org.springframework.ws.soap.SoapMessage soapResponse = Mockito.mock(org.springframework.ws.soap.SoapMessage.class);

    private SoapEnvelope soapEnvelope = Mockito.mock(SoapEnvelope.class);
    private SoapBody soapBody = Mockito.mock(SoapBody.class);

    private SaajSoapMessage saajSoapRequest = Mockito.mock(SaajSoapMessage.class);

    private org.springframework.ws.soap.SoapFault soapFault = Mockito.mock(org.springframework.ws.soap.SoapFault.class);
    private SoapFaultDetail soapFaultDetail = Mockito.mock(SoapFaultDetail.class);

    private final String requestPayload = "<TestRequest><Message>Hello World!</Message></TestRequest>";

    @Test
    public void testMessageProcessing() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        final Message requestMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>");

        final Message responseMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestResponse><Message>Hello World!</Message></TestResponse>");

        endpoint.setEndpointAdapter(new StaticEndpointAdapter() {
            public Message handleMessageInternal(Message message) {
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());
                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        StringResult soapResponsePayload = new StringResult();

        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse);

        when(messageContext.getRequest()).thenReturn(soapRequest);
        when(soapRequest.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));

        when(soapRequest.getPayloadSource()).thenReturn(new StringSource(requestPayload));

        when(messageContext.getPropertyNames()).thenReturn(new String[]{});

        when(soapRequest.getSoapHeader()).thenReturn(soapRequestHeader);
        when(soapRequestHeader.getSource()).thenReturn(null);

        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        when(soapRequestHeader.examineAllHeaderElements()).thenReturn(emptyHeaderSet.iterator());

        when(soapRequest.getSoapAction()).thenReturn(null);

        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        when(soapRequest.getAttachments()).thenReturn(emptyAttachmentSet.iterator());

        when(messageContext.getResponse()).thenReturn(soapResponse);

        when(soapResponse.getPayloadResult()).thenReturn(soapResponsePayload);


        endpoint.invoke(messageContext);

        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());

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
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());

                Assert.assertNotNull(message.getHeader(SoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        StringResult soapResponsePayload = new StringResult();

        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse);

        when(messageContext.getRequest()).thenReturn(soapRequest);
        when(soapRequest.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));

        when(soapRequest.getPayloadSource()).thenReturn(new StringSource(requestPayload));

        when(messageContext.getPropertyNames()).thenReturn(new String[]{});

        when(soapRequest.getSoapHeader()).thenReturn(soapRequestHeader);
        when(soapRequestHeader.getSource()).thenReturn(null);

        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        when(soapRequestHeader.examineAllHeaderElements()).thenReturn(emptyHeaderSet.iterator());

        when(soapRequest.getSoapAction()).thenReturn("sayHello");

        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        when(soapRequest.getAttachments()).thenReturn(emptyAttachmentSet.iterator());

        when(messageContext.getResponse()).thenReturn(soapResponse);

        when(soapResponse.getPayloadResult()).thenReturn(soapResponsePayload);


        endpoint.invoke(messageContext);

        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());

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
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());

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

        when(messageContext.getRequest()).thenReturn(soapRequest);
        when(soapRequest.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));

        when(soapRequest.getPayloadSource()).thenReturn(new StringSource(requestPayload));

        when(messageContext.getPropertyNames()).thenReturn(new String[]{});

        when(soapRequest.getSoapHeader()).thenReturn(soapRequestHeader);
        when(soapRequestHeader.getSource()).thenReturn(null);

        when(soapRequestHeader.examineAllHeaderElements()).thenReturn(soapRequestHeaders.iterator());

        when(soapRequestHeaderEntry.getName()).thenReturn(new QName("http://citrusframework.org/citrus", "Operation", "citrus"));
        when(soapRequestHeaderEntry.getText()).thenReturn("sayHello");

        when(soapRequest.getSoapAction()).thenReturn("sayHello");

        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        when(soapRequest.getAttachments()).thenReturn(emptyAttachmentSet.iterator());

        when(messageContext.getResponse()).thenReturn(soapResponse);

        when(soapResponse.getPayloadResult()).thenReturn(soapResponsePayload);

        endpoint.invoke(messageContext);

        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());

    }

    @Test
    public void testMessageProcessingWithMimeRequestHeaders() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        WebServiceEndpointConfiguration endpointConfiguration = new WebServiceEndpointConfiguration();
        endpointConfiguration.setHandleMimeHeaders(true);

        endpoint.setEndpointConfiguration(endpointConfiguration);

        Map<String, Object> requestHeaders = new HashMap<>();
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
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());

                Assert.assertNotNull(message.getHeader("Operation"));
                Assert.assertEquals(message.getHeader("Operation"), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        SOAPMessage soapRequestMessage = Mockito.mock(SOAPMessage.class);
        MimeHeaders mimeHeaders = new MimeHeaders();
        mimeHeaders.addHeader("Host", "localhost:8080");
        mimeHeaders.addHeader("Content-Length", "236");
        mimeHeaders.addHeader("Accept", "text/xml");
        mimeHeaders.addHeader("Accept", "text/html");
        mimeHeaders.addHeader("Accept", "image/gif");
        mimeHeaders.addHeader("Accept", "image/jpeg");
        mimeHeaders.addHeader("Content-Type", "text/xml");

        Set<SoapHeaderElement> soapRequestHeaders = new HashSet<>();
        soapRequestHeaders.add(soapRequestHeaderEntry);

        StringResult soapResponsePayload = new StringResult();

        reset(messageContext, soapEnvelope, soapRequestHeader, soapBody, soapRequestHeaderEntry, soapResponse, saajSoapRequest, soapRequestMessage);

        when(saajSoapRequest.getEnvelope()).thenReturn(soapEnvelope);
        when(saajSoapRequest.getSoapAction()).thenReturn("sayHello");
        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        when(saajSoapRequest.getAttachments()).thenReturn(emptyAttachmentSet.iterator());

        when(saajSoapRequest.getSaajMessage()).thenReturn(soapRequestMessage);
        when(soapRequestMessage.getMimeHeaders()).thenReturn(mimeHeaders);

        when(messageContext.getRequest()).thenReturn(saajSoapRequest);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));

        when(soapEnvelope.getBody()).thenReturn(soapBody);
        when(soapBody.getPayloadSource()).thenReturn(new StringSource(requestPayload));
        when(saajSoapRequest.getPayloadSource()).thenReturn(new StringSource(requestPayload));

        when(saajSoapRequest.getSoapHeader()).thenReturn(soapRequestHeader);
        when(soapRequestHeader.getSource()).thenReturn(null);

        when(messageContext.getPropertyNames()).thenReturn(new String[]{});

        when(soapRequestHeader.examineAllHeaderElements()).thenReturn(soapRequestHeaders.iterator());

        when(soapRequestHeaderEntry.getName()).thenReturn(new QName("http://citrusframework.org/citrus", "Operation", "citrus"));
        when(soapRequestHeaderEntry.getText()).thenReturn("sayHello");

        when(messageContext.getResponse()).thenReturn(soapResponse);

        when(soapResponse.getPayloadResult()).thenReturn(soapResponsePayload);

        endpoint.invoke(messageContext);

        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());

    }

    @Test
    public void testMessageProcessingWithSoapResponseHeaders() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(SoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message requestMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>", requestHeaders);

        Map<String, Object> responseHeaders = new HashMap<String, Object>();
        responseHeaders.put("{http://citrusframework.org/citrus}citrus:Operation", "sayHello");
        final Message responseMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestResponse><Message>Hello World!</Message></TestResponse>", responseHeaders);

        endpoint.setEndpointAdapter(new StaticEndpointAdapter() {
            public Message handleMessageInternal(Message message) {
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());

                Assert.assertNotNull(message.getHeader(SoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        StringResult soapResponsePayload = new StringResult();

        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapRequestHeaderEntry);

        when(messageContext.getRequest()).thenReturn(soapRequest);
        when(soapRequest.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));

        when(soapRequest.getPayloadSource()).thenReturn(new StringSource(requestPayload));

        when(messageContext.getPropertyNames()).thenReturn(new String[]{});

        when(soapRequest.getSoapHeader()).thenReturn(soapRequestHeader);
        when(soapRequestHeader.getSource()).thenReturn(null);

        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        when(soapRequestHeader.examineAllHeaderElements()).thenReturn(emptyHeaderSet.iterator());

        when(soapRequest.getSoapAction()).thenReturn("sayHello");

        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        when(soapRequest.getAttachments()).thenReturn(emptyAttachmentSet.iterator());

        when(messageContext.getResponse()).thenReturn(soapResponse);

        when(soapResponse.getPayloadResult()).thenReturn(soapResponsePayload);

        when(soapResponse.getSoapHeader()).thenReturn(soapResponseHeader);

        doAnswer(new Answer<SoapHeaderElement>() {
            @Override
            public SoapHeaderElement answer(InvocationOnMock invocation) throws Throwable {
                QName headerQName = (QName)invocation.getArguments()[0];

                Assert.assertEquals(headerQName.getLocalPart(), "Operation");
                Assert.assertEquals(headerQName.getPrefix(), "citrus");
                Assert.assertEquals(headerQName.getNamespaceURI(), "http://citrusframework.org/citrus");
                return soapRequestHeaderEntry;
            }
        }).when(soapResponseHeader).addHeaderElement((QName)any());

        endpoint.invoke(messageContext);

        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());

        verify(soapRequestHeaderEntry).setText("sayHello");
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
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());

                Assert.assertNotNull(message.getHeader(SoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        endpoint.setDefaultNamespaceUri("http://citrusframework.org/citrus");
        endpoint.setDefaultPrefix("citrus");

        StringResult soapResponsePayload = new StringResult();

        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader);

        when(messageContext.getRequest()).thenReturn(soapRequest);
        when(soapRequest.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));
        when(soapRequest.getPayloadSource()).thenReturn(new StringSource(requestPayload));

        when(messageContext.getPropertyNames()).thenReturn(new String[]{});

        when(soapRequest.getSoapHeader()).thenReturn(soapRequestHeader);
        when(soapRequestHeader.getSource()).thenReturn(null);

        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        when(soapRequestHeader.examineAllHeaderElements()).thenReturn(emptyHeaderSet.iterator());

        when(soapRequest.getSoapAction()).thenReturn("sayHello");

        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        when(soapRequest.getAttachments()).thenReturn(emptyAttachmentSet.iterator());

        when(messageContext.getResponse()).thenReturn(soapResponse);

        when(soapResponse.getPayloadResult()).thenReturn(soapResponsePayload);

        when(soapResponse.getSoapHeader()).thenReturn(soapResponseHeader);

        doAnswer(new Answer<SoapHeaderElement>() {
            @Override
            public SoapHeaderElement answer(InvocationOnMock invocation) throws Throwable {
                QName headerQName = (QName)invocation.getArguments()[0];

                Assert.assertEquals(headerQName.getLocalPart(), "Operation");
                Assert.assertEquals(headerQName.getPrefix(), "citrus");
                Assert.assertEquals(headerQName.getNamespaceURI(), "http://citrusframework.org/citrus");
                return soapRequestHeaderEntry;
            }
        }).when(soapResponseHeader).addHeaderElement((QName)any());

        endpoint.invoke(messageContext);

        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());

        verify(soapRequestHeaderEntry).setText("sayHello");
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
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());

                Assert.assertNotNull(message.getHeader(SoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        endpoint.setDefaultNamespaceUri("http://citrusframework.org/citrus");

        StringResult soapResponsePayload = new StringResult();

        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader);

        when(messageContext.getRequest()).thenReturn(soapRequest);
        when(soapRequest.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));

        when(soapRequest.getPayloadSource()).thenReturn(new StringSource(requestPayload));

        when(messageContext.getPropertyNames()).thenReturn(new String[]{});

        when(soapRequest.getSoapHeader()).thenReturn(soapRequestHeader);
        when(soapRequestHeader.getSource()).thenReturn(null);

        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        when(soapRequestHeader.examineAllHeaderElements()).thenReturn(emptyHeaderSet.iterator());

        when(soapRequest.getSoapAction()).thenReturn("sayHello");

        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        when(soapRequest.getAttachments()).thenReturn(emptyAttachmentSet.iterator());

        when(messageContext.getResponse()).thenReturn(soapResponse);

        when(soapResponse.getPayloadResult()).thenReturn(soapResponsePayload);

        when(soapResponse.getSoapHeader()).thenReturn(soapResponseHeader);

        doAnswer(new Answer<SoapHeaderElement>() {
            @Override
            public SoapHeaderElement answer(InvocationOnMock invocation) throws Throwable {
                QName headerQName = (QName)invocation.getArguments()[0];

                Assert.assertEquals(headerQName.getLocalPart(), "Operation");
                Assert.assertEquals(headerQName.getPrefix(), "");
                Assert.assertEquals(headerQName.getNamespaceURI(), "http://citrusframework.org/citrus");
                return soapRequestHeaderEntry;
            }
        }).when(soapResponseHeader).addHeaderElement((QName)any());

        endpoint.invoke(messageContext);

        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());

        verify(soapRequestHeaderEntry, times(2)).setText("sayHello");
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
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());

                Assert.assertNotNull(message.getHeader(SoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        StringResult soapResponsePayload = new StringResult();

        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader);

        when(messageContext.getRequest()).thenReturn(soapRequest);
        when(soapRequest.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));

        when(soapRequest.getPayloadSource()).thenReturn(new StringSource(requestPayload));

        when(messageContext.getPropertyNames()).thenReturn(new String[]{});

        when(soapRequest.getSoapHeader()).thenReturn(soapRequestHeader);
        when(soapRequestHeader.getSource()).thenReturn(null);

        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        when(soapRequestHeader.examineAllHeaderElements()).thenReturn(emptyHeaderSet.iterator());

        when(soapRequest.getSoapAction()).thenReturn("sayHello");

        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        when(soapRequest.getAttachments()).thenReturn(emptyAttachmentSet.iterator());

        when(messageContext.getResponse()).thenReturn(soapResponse);

        when(soapResponse.getPayloadResult()).thenReturn(soapResponsePayload);

        when(soapResponse.getSoapHeader()).thenReturn(soapResponseHeader);

        doAnswer(new Answer<SoapHeaderElement>() {
            @Override
            public SoapHeaderElement answer(InvocationOnMock invocation) throws Throwable {
                QName headerQName = (QName)invocation.getArguments()[0];

                Assert.assertEquals(headerQName.getLocalPart(), "Operation");
                Assert.assertEquals(headerQName.getPrefix(), "");
                Assert.assertEquals(headerQName.getNamespaceURI(), "");
                return soapRequestHeaderEntry;
            }
        }).when(soapResponseHeader).addHeaderElement((QName)any());

        endpoint.invoke(messageContext);

        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());

        verify(soapRequestHeaderEntry).setText("sayHello");
    }

    @Test
    public void testMessageProcessingWithSoapAttachment() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<>();
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
        Attachment attachment = Mockito.mock(Attachment.class);
        attachments.add(attachment);

        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, attachment);

        when(messageContext.getRequest()).thenReturn(soapRequest);
        when(soapRequest.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));

        when(soapRequest.getPayloadSource()).thenReturn(new StringSource(requestPayload));

        when(messageContext.getPropertyNames()).thenReturn(new String[]{});

        when(soapRequest.getSoapHeader()).thenReturn(soapRequestHeader);
        when(soapRequestHeader.getSource()).thenReturn(null);

        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        when(soapRequestHeader.examineAllHeaderElements()).thenReturn(emptyHeaderSet.iterator());

        when(soapRequest.getSoapAction()).thenReturn("sayHello");

        when(soapRequest.getAttachments()).thenReturn(attachments.iterator());
        when(attachment.getContentId()).thenReturn("myContentId");
        when(attachment.getContentType()).thenReturn("text/xml");

        when(messageContext.getResponse()).thenReturn(soapResponse);

        when(soapResponse.getPayloadResult()).thenReturn(soapResponsePayload);

        when(attachment.getInputStream()).thenReturn(new ByteArrayInputStream("AttachmentBody".getBytes()));

        endpoint.invoke(messageContext);

        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());
    }

    @Test
    public void testMessageProcessingWithSoapAttachmentInResponse() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        final SoapMessage responseMessage = new SoapMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestResponse><Message>Hello World!</Message></TestResponse>");
        responseMessage.addAttachment(new SoapAttachment("This is an attachment"));
        responseMessage.getAttachments().get(0).setContentId("myAttachment");
        responseMessage.getAttachments().get(0).setContentType("text/plain");

        endpoint.setEndpointAdapter(new StaticEndpointAdapter() {
            public Message handleMessageInternal(Message message) {
                return responseMessage;
            }
        });

        StringResult soapResponsePayload = new StringResult();

        Set<Attachment> attachments = new HashSet<Attachment>();
        Attachment attachment = Mockito.mock(Attachment.class);
        attachments.add(attachment);

        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, attachment);

        when(messageContext.getRequest()).thenReturn(soapRequest);
        when(soapRequest.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));

        when(soapRequest.getPayloadSource()).thenReturn(new StringSource(requestPayload));

        when(messageContext.getPropertyNames()).thenReturn(new String[]{});

        when(soapRequest.getSoapHeader()).thenReturn(soapRequestHeader);
        when(soapRequestHeader.getSource()).thenReturn(null);

        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        when(soapRequestHeader.examineAllHeaderElements()).thenReturn(emptyHeaderSet.iterator());

        when(soapRequest.getSoapAction()).thenReturn("sayHello");

        when(soapRequest.getAttachments()).thenReturn(attachments.iterator());
        when(attachment.getContentId()).thenReturn("myContentId");
        when(attachment.getContentType()).thenReturn("text/xml");

        when(messageContext.getResponse()).thenReturn(soapResponse);

        when(soapResponse.getPayloadResult()).thenReturn(soapResponsePayload);

        when(soapResponse.addAttachment(eq("<myAttachment>"), any(DataHandler.class))).thenReturn(Mockito.mock(Attachment.class));

        when(attachment.getInputStream()).thenReturn(new ByteArrayInputStream("AttachmentBody".getBytes()));

        endpoint.invoke(messageContext);

        verify(soapResponse).addAttachment(eq("<myAttachment>"), any(DataHandler.class));

        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());

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
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());

                Assert.assertNotNull(message.getHeader(SoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        StringResult soapResponsePayload = new StringResult();

        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapBody);

        when(messageContext.getRequest()).thenReturn(soapRequest);
        when(soapRequest.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));

        when(soapRequest.getPayloadSource()).thenReturn(new StringSource(requestPayload));

        when(messageContext.getPropertyNames()).thenReturn(new String[]{});

        when(soapRequest.getSoapHeader()).thenReturn(soapRequestHeader);
        when(soapRequestHeader.getSource()).thenReturn(null);

        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        when(soapRequestHeader.examineAllHeaderElements()).thenReturn(emptyHeaderSet.iterator());

        when(soapRequest.getSoapAction()).thenReturn("sayHello");

        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        when(soapRequest.getAttachments()).thenReturn(emptyAttachmentSet.iterator());

        when(messageContext.getResponse()).thenReturn(soapResponse);

        when(soapResponse.getSoapHeader()).thenReturn(soapResponseHeader);

        when(soapResponse.getSoapBody()).thenReturn(soapBody);

        doAnswer(new Answer<org.springframework.ws.soap.SoapFault>() {
            @Override
            public org.springframework.ws.soap.SoapFault answer(InvocationOnMock invocation) throws Throwable {
                Assert.assertEquals(invocation.getArguments()[0], "Invalid request, because of unknown error");

                return soapFault;
            }
        }).when(soapBody).addServerOrReceiverFault((String)any(), (Locale)any());


        endpoint.invoke(messageContext);

        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());

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
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());

                Assert.assertNotNull(message.getHeader(SoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        StringResult soapResponsePayload = new StringResult();

        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapBody);

        when(messageContext.getRequest()).thenReturn(soapRequest);
        when(soapRequest.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));

        when(soapRequest.getPayloadSource()).thenReturn(new StringSource(requestPayload));

        when(messageContext.getPropertyNames()).thenReturn(new String[]{});

        when(soapRequest.getSoapHeader()).thenReturn(soapRequestHeader);
        when(soapRequestHeader.getSource()).thenReturn(null);

        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        when(soapRequestHeader.examineAllHeaderElements()).thenReturn(emptyHeaderSet.iterator());

        when(soapRequest.getSoapAction()).thenReturn("sayHello");

        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        when(soapRequest.getAttachments()).thenReturn(emptyAttachmentSet.iterator());

        when(messageContext.getResponse()).thenReturn(soapResponse);

        when(soapResponse.getSoapHeader()).thenReturn(soapResponseHeader);

        when(soapResponse.getSoapBody()).thenReturn(soapBody);

        doAnswer(new Answer<org.springframework.ws.soap.SoapFault>() {
            @Override
            public org.springframework.ws.soap.SoapFault answer(InvocationOnMock invocation) throws Throwable {
                Assert.assertEquals(invocation.getArguments()[0], "Invalid request");
                return soapFault;
            }
        }).when(soapBody).addClientOrSenderFault((String)any(), (Locale)any());


        endpoint.invoke(messageContext);

        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());

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
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());

                Assert.assertNotNull(message.getHeader(SoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        StringResult soapFaultResult = new StringResult();

        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapBody, soapFault, soapFaultDetail);

        when(messageContext.getRequest()).thenReturn(soapRequest);
        when(soapRequest.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));

        when(soapRequest.getPayloadSource()).thenReturn(new StringSource(requestPayload));

        when(messageContext.getPropertyNames()).thenReturn(new String[]{});

        when(soapRequest.getSoapHeader()).thenReturn(soapRequestHeader);
        when(soapRequestHeader.getSource()).thenReturn(null);

        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        when(soapRequestHeader.examineAllHeaderElements()).thenReturn(emptyHeaderSet.iterator());

        when(soapRequest.getSoapAction()).thenReturn("sayHello");

        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        when(soapRequest.getAttachments()).thenReturn(emptyAttachmentSet.iterator());

        when(messageContext.getResponse()).thenReturn(soapResponse);

        when(soapResponse.getSoapHeader()).thenReturn(soapResponseHeader);

        when(soapResponse.getSoapBody()).thenReturn(soapBody);

        doAnswer(new Answer<org.springframework.ws.soap.SoapFault>() {
            @Override
            public org.springframework.ws.soap.SoapFault answer(InvocationOnMock invocation) throws Throwable {
                Assert.assertEquals(invocation.getArguments()[0], "Invalid request");

                return soapFault;
            }
        }).when(soapBody).addServerOrReceiverFault((String)any(), (Locale)any());

        when(soapFault.addFaultDetail()).thenReturn(soapFaultDetail);

        when(soapFaultDetail.getResult()).thenReturn(soapFaultResult);


        endpoint.invoke(messageContext);

        Assert.assertEquals(soapFaultResult.toString(), "<DetailMessage><text>This request was not OK!</text></DetailMessage>");

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
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());

                Assert.assertNotNull(message.getHeader(SoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        StringResult soapFaultResult = new StringResult();

        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapBody, soapFault, soapFaultDetail);

        when(messageContext.getRequest()).thenReturn(soapRequest);
        when(soapRequest.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));

        when(soapRequest.getPayloadSource()).thenReturn(new StringSource(requestPayload));

        when(messageContext.getPropertyNames()).thenReturn(new String[]{});

        when(soapRequest.getSoapHeader()).thenReturn(soapRequestHeader);
        when(soapRequestHeader.getSource()).thenReturn(null);

        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        when(soapRequestHeader.examineAllHeaderElements()).thenReturn(emptyHeaderSet.iterator());

        when(soapRequest.getSoapAction()).thenReturn("sayHello");

        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        when(soapRequest.getAttachments()).thenReturn(emptyAttachmentSet.iterator());

        when(messageContext.getResponse()).thenReturn(soapResponse);

        when(soapResponse.getSoapHeader()).thenReturn(soapResponseHeader);

        when(soapResponse.getSoapBody()).thenReturn(soapBody);

        doAnswer(new Answer<org.springframework.ws.soap.SoapFault>() {
            @Override
            public org.springframework.ws.soap.SoapFault answer(InvocationOnMock invocation) throws Throwable {
                Assert.assertEquals(invocation.getArguments()[0], "Invalid request");

                return soapFault;
            }
        }).when(soapBody).addServerOrReceiverFault((String)any(), (Locale)any());

        when(soapFault.addFaultDetail()).thenReturn(soapFaultDetail);

        when(soapFaultDetail.getResult()).thenReturn(soapFaultResult);


        endpoint.invoke(messageContext);

        Assert.assertEquals(soapFaultResult.toString(), "<DetailMessage><text>This request was not OK!</text></DetailMessage><Error><text>This request was not OK!</text></Error>");

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
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());

                Assert.assertNotNull(message.getHeader(SoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });


        StringResult soapResponsePayload = new StringResult();

        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse);

        when(messageContext.getRequest()).thenReturn(soapRequest);
        when(soapRequest.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));

        when(soapRequest.getPayloadSource()).thenReturn(new StringSource(requestPayload));

        when(messageContext.getPropertyNames()).thenReturn(new String[]{});

        when(soapRequest.getSoapHeader()).thenReturn(soapRequestHeader);
        when(soapRequestHeader.getSource()).thenReturn(null);

        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        when(soapRequestHeader.examineAllHeaderElements()).thenReturn(emptyHeaderSet.iterator());

        when(soapRequest.getSoapAction()).thenReturn("sayHello");

        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        when(soapRequest.getAttachments()).thenReturn(emptyAttachmentSet.iterator());

        when(messageContext.getResponse()).thenReturn(soapResponse);

        when(soapResponse.getPayloadResult()).thenReturn(soapResponsePayload);

        endpoint.invoke(messageContext);

        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());

        verify(soapResponse).setSoapAction("answerHello");
    }

    @Test
    public void testMessageProcessingWithSoap11FaultInResponse() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(SoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message requestMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>", requestHeaders);

        final SoapFault responseMessage = new SoapFault();
        responseMessage.faultCode("{http://citrusframework.org/citrus}citrus:TEC-1000");
        responseMessage.faultString("Invalid request");

        endpoint.setEndpointAdapter(new StaticEndpointAdapter() {
            public Message handleMessageInternal(Message message) {
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());

                Assert.assertNotNull(message.getHeader(SoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        Soap11Body soapResponseBody = Mockito.mock(Soap11Body.class);
        final Soap11Fault soapFault = Mockito.mock(Soap11Fault.class);

        StringResult soapResponsePayload = new StringResult();

        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapResponseBody);

        when(messageContext.getRequest()).thenReturn(soapRequest);
        when(soapRequest.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));

        when(soapRequest.getPayloadSource()).thenReturn(new StringSource(requestPayload));

        when(messageContext.getPropertyNames()).thenReturn(new String[]{});

        when(soapRequest.getSoapHeader()).thenReturn(soapRequestHeader);
        when(soapRequestHeader.getSource()).thenReturn(null);

        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        when(soapRequestHeader.examineAllHeaderElements()).thenReturn(emptyHeaderSet.iterator());

        when(soapRequest.getSoapAction()).thenReturn("sayHello");

        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        when(soapRequest.getAttachments()).thenReturn(emptyAttachmentSet.iterator());

        when(messageContext.getResponse()).thenReturn(soapResponse);

        when(soapResponse.getSoapHeader()).thenReturn(soapResponseHeader);

        when(soapResponse.getSoapBody()).thenReturn(soapResponseBody);

        doAnswer(new Answer<org.springframework.ws.soap.SoapFault>() {
            @Override
            public org.springframework.ws.soap.SoapFault answer(InvocationOnMock invocation) throws Throwable {
                QName faultQName = (QName)invocation.getArguments()[0];

                Assert.assertEquals(faultQName.getLocalPart(), "TEC-1000");
                Assert.assertEquals(faultQName.getPrefix(), "citrus");
                Assert.assertEquals(faultQName.getNamespaceURI(), "http://citrusframework.org/citrus");
                Assert.assertEquals(invocation.getArguments()[1], "Invalid request");

                return soapFault;
            }
        }).when(soapResponseBody).addFault((QName)any(), (String)any(), (Locale)any());


        endpoint.invoke(messageContext);

        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());

    }

    @Test
    public void testMessageProcessingWithSoap12FaultInResponse() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        Map<String, Object> requestHeaders = new HashMap<String, Object>();
        requestHeaders.put(SoapMessageHeaders.SOAP_ACTION, "sayHello");
        final Message requestMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>", requestHeaders);

        final SoapFault responseMessage = new SoapFault();
        responseMessage.faultCode("{http://citrusframework.org/citrus}citrus:TEC-1000");
        responseMessage.faultString("Invalid request");

        endpoint.setEndpointAdapter(new StaticEndpointAdapter() {
            public Message handleMessageInternal(Message message) {
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());

                Assert.assertNotNull(message.getHeader(SoapMessageHeaders.SOAP_ACTION));
                Assert.assertEquals(message.getHeader(SoapMessageHeaders.SOAP_ACTION), "sayHello");

                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });

        Soap12Body soapResponseBody = Mockito.mock(Soap12Body.class);
        final Soap12Fault soapFault = Mockito.mock(Soap12Fault.class);

        StringResult soapResponsePayload = new StringResult();

        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse, soapResponseHeader, soapResponseBody, soapFault);

        when(messageContext.getRequest()).thenReturn(soapRequest);
        when(soapRequest.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));

        when(soapRequest.getPayloadSource()).thenReturn(new StringSource(requestPayload));

        when(messageContext.getPropertyNames()).thenReturn(new String[]{});

        when(soapRequest.getSoapHeader()).thenReturn(soapRequestHeader);
        when(soapRequestHeader.getSource()).thenReturn(null);

        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        when(soapRequestHeader.examineAllHeaderElements()).thenReturn(emptyHeaderSet.iterator());

        when(soapRequest.getSoapAction()).thenReturn("sayHello");

        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        when(soapRequest.getAttachments()).thenReturn(emptyAttachmentSet.iterator());

        when(messageContext.getResponse()).thenReturn(soapResponse);

        when(soapResponse.getSoapHeader()).thenReturn(soapResponseHeader);

        when(soapResponse.getSoapBody()).thenReturn(soapResponseBody);

        doAnswer(new Answer<Soap12Fault>() {
            @Override
            public Soap12Fault answer(InvocationOnMock invocation) throws Throwable {
                Assert.assertEquals(invocation.getArguments()[0], "Invalid request");
                return soapFault;
            }
        }).when(soapResponseBody).addServerOrReceiverFault((String)any(), (Locale)any());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                QName faultQName = (QName)invocation.getArguments()[0];

                Assert.assertEquals(faultQName.getLocalPart(), "TEC-1000");
                Assert.assertEquals(faultQName.getPrefix(), "citrus");
                Assert.assertEquals(faultQName.getNamespaceURI(), "http://citrusframework.org/citrus");
                return null;
            }
        }).when(soapFault).addFaultSubcode((QName)any());

        endpoint.invoke(messageContext);

        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());

    }

    @Test
    public void testEmptySoapMessageProcessing() throws Exception {
        WebServiceEndpoint endpoint = new WebServiceEndpoint();

        final Message requestMessage = new DefaultMessage("");
        final Message responseMessage = new DefaultMessage("");

        endpoint.setEndpointAdapter(new StaticEndpointAdapter() {
            public Message handleMessageInternal(Message message) {
                Assert.assertEquals(message.getHeaders().size(), requestMessage.getHeaders().size());
                Assert.assertEquals(message.getPayload(), requestMessage.getPayload());

                return responseMessage;
            }
        });


        StringResult soapResponsePayload = new StringResult();

        reset(messageContext, soapEnvelope, soapRequest, soapRequestHeader, soapResponse);

        when(messageContext.getRequest()).thenReturn(soapRequest);
        when(soapRequest.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Header/><SOAP-ENV:Body></SOAP-ENV:Body></SOAP-ENV:Envelope>"));

        when(soapRequest.getPayloadSource()).thenReturn(null);

        when(messageContext.getPropertyNames()).thenReturn(new String[]{});

        when(soapRequest.getSoapHeader()).thenReturn(soapRequestHeader);
        when(soapRequestHeader.getSource()).thenReturn(null);

        Set<SoapHeaderElement> emptyHeaderSet = Collections.emptySet();
        when(soapRequestHeader.examineAllHeaderElements()).thenReturn(emptyHeaderSet.iterator());

        when(soapRequest.getSoapAction()).thenReturn(null);

        Set<Attachment> emptyAttachmentSet = Collections.emptySet();
        when(soapRequest.getAttachments()).thenReturn(emptyAttachmentSet.iterator());

        when(messageContext.getResponse()).thenReturn(soapResponse);

        endpoint.invoke(messageContext);

        Assert.assertEquals(soapResponsePayload.toString(), responseMessage.getPayload());

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
