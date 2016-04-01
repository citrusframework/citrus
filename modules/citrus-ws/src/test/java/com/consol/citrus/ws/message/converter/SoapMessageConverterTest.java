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

package com.consol.citrus.ws.message.converter;

import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.ws.client.WebServiceEndpointConfiguration;
import com.consol.citrus.ws.message.*;
import com.consol.citrus.ws.message.SoapMessage;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.InputStreamSource;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.*;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import java.io.*;
import java.util.*;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class SoapMessageConverterTest extends AbstractTestNGUnitTest {

    public static final String XML_PROCESSING_INSTRUCTION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    private SoapMessageFactory soapMessageFactory = Mockito.mock(SoapMessageFactory.class);
    private org.springframework.ws.soap.SoapMessage soapRequest = Mockito.mock(org.springframework.ws.soap.SoapMessage.class);
    private org.springframework.ws.soap.SoapMessage soapResponse = Mockito.mock(org.springframework.ws.soap.SoapMessage.class);
    private SoapEnvelope soapEnvelope = Mockito.mock(SoapEnvelope.class);
    private SoapBody soapBody = Mockito.mock(SoapBody.class);
    private SoapHeader soapHeader = Mockito.mock(SoapHeader.class);
    private SoapHeaderElement soapHeaderElement = Mockito.mock(SoapHeaderElement.class);

    private String payload = "<testMessage>Hello</testMessage>";

    @Test
    public void testOutboundSoapMessageCreation() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(payload);

        WebServiceEndpointConfiguration endpointConfiguration = new WebServiceEndpointConfiguration();
        endpointConfiguration.setMessageFactory(soapMessageFactory);

        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        StringResult soapBodyResult = new StringResult();

        reset(soapMessageFactory, soapRequest, soapBody);

        when(soapMessageFactory.createWebServiceMessage()).thenReturn(soapRequest);
        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(soapBodyResult);

        soapMessageConverter.convertOutbound(testMessage, endpointConfiguration, context);

        Assert.assertEquals(soapBodyResult.toString(), XML_PROCESSING_INSTRUCTION + payload);

    }

    @Test
    public void testOutboundSoapBody() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(payload);

        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        StringResult soapBodyResult = new StringResult();

        reset(soapRequest, soapBody);

        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(soapBodyResult);

        soapMessageConverter.convertOutbound(soapRequest, testMessage, new WebServiceEndpointConfiguration(), context);

        Assert.assertEquals(soapBodyResult.toString(), XML_PROCESSING_INSTRUCTION + payload);

    }

    @Test
    public void testOutboundSoapAction() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(payload)
                .setHeader(SoapMessageHeaders.SOAP_ACTION, "soapAction");

        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        reset(soapRequest, soapBody);

        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(new StringResult());

        soapMessageConverter.convertOutbound(soapRequest, testMessage, new WebServiceEndpointConfiguration(), context);
        verify(soapRequest).setSoapAction("soapAction");
    }

    @Test
    public void testOutboundSoapHeaderContent() throws TransformerException, IOException {
        String soapHeaderContent = "<header>" +
                "<operation>unitTest</operation>" +
                "<messageId>123456789</messageId>" +
                "</header>";

        Message testMessage = new DefaultMessage(payload)
                .addHeaderData(soapHeaderContent);

        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        StringResult soapHeaderResult = new StringResult();

        reset(soapRequest, soapBody, soapHeader);

        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(new StringResult());

        when(soapRequest.getSoapHeader()).thenReturn(soapHeader);
        when(soapHeader.getResult()).thenReturn(soapHeaderResult);

        soapMessageConverter.convertOutbound(soapRequest, testMessage, new WebServiceEndpointConfiguration(), context);

        Assert.assertEquals(soapHeaderResult.toString(), soapHeaderContent);
    }

    @Test
    public void testMultipleOutboundSoapHeaderContent() throws TransformerException, IOException {
        String soapHeaderContent = "<header>" +
                "<operation>unitTest</operation>" +
                "<messageId>123456789</messageId>" +
                "</header>";

        Message testMessage = new DefaultMessage(payload)
                .addHeaderData(soapHeaderContent)
                .addHeaderData("<AppInfo><appId>123456789</appId></AppInfo>");

        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        StringResult soapHeaderResult = new StringResult();

        reset(soapRequest, soapBody, soapHeader);

        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(new StringResult());

        when(soapRequest.getSoapHeader()).thenReturn(soapHeader);
        when(soapHeader.getResult()).thenReturn(soapHeaderResult);

        soapMessageConverter.convertOutbound(soapRequest, testMessage, new WebServiceEndpointConfiguration(), context);

        Assert.assertEquals(soapHeaderResult.toString(), soapHeaderContent + "<AppInfo><appId>123456789</appId></AppInfo>");

    }

    @Test
    public void testOutboundSoapHeader() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(payload)
                .setHeader("operation", "unitTest")
                .setHeader("messageId", "123456789");

        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        reset(soapRequest, soapBody, soapHeader, soapHeaderElement);

        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(new StringResult());

        when(soapRequest.getSoapHeader()).thenReturn(soapHeader);
        when(soapHeader.addHeaderElement(eq(QNameUtils.createQName("", "operation", "")))).thenReturn(soapHeaderElement);
        when(soapHeader.addHeaderElement(eq(QNameUtils.createQName("", "messageId", "")))).thenReturn(soapHeaderElement);

        soapMessageConverter.convertOutbound(soapRequest, testMessage, new WebServiceEndpointConfiguration(), context);

        verify(soapHeaderElement).setText("unitTest");
        verify(soapHeaderElement).setText("123456789");
    }

    @Test
    public void testOutboundSoapHeaderQNameString() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(payload)
                .setHeader("{http://www.citrus.com}citrus:operation", "unitTest")
                .setHeader("{http://www.citrus.com}citrus:messageId", "123456789");

        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        reset(soapRequest, soapBody, soapHeader, soapHeaderElement);

        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(new StringResult());

        when(soapRequest.getSoapHeader()).thenReturn(soapHeader);
        when(soapHeader.addHeaderElement(eq(QNameUtils.createQName("http://www.citrus.com", "operation", "citrus")))).thenReturn(soapHeaderElement);
        when(soapHeader.addHeaderElement(eq(QNameUtils.createQName("http://www.citrus.com", "messageId", "citrus")))).thenReturn(soapHeaderElement);

        soapMessageConverter.convertOutbound(soapRequest, testMessage, new WebServiceEndpointConfiguration(), context);

        verify(soapHeaderElement).setText("unitTest");
        verify(soapHeaderElement).setText("123456789");
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testOutboundSoapMimeHeader() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(payload)
                .setHeader(SoapMessageHeaders.HTTP_PREFIX + "operation", "unitTest")
                .setHeader(SoapMessageHeaders.HTTP_PREFIX + "messageId", "123456789");

        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        SaajSoapMessage saajSoapRequest = Mockito.mock(SaajSoapMessage.class);
        SOAPMessage saajMessage = Mockito.mock(SOAPMessage.class);

        MimeHeaders mimeHeaders = new MimeHeaders();

        reset(saajSoapRequest, soapBody, soapHeader, soapEnvelope, saajMessage);

        when(saajSoapRequest.getEnvelope()).thenReturn(soapEnvelope);

        when(soapEnvelope.getBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(new StringResult());

        when(saajSoapRequest.getSaajMessage()).thenReturn(saajMessage);

        when(saajMessage.getMimeHeaders()).thenReturn(mimeHeaders);

        soapMessageConverter.convertOutbound(saajSoapRequest, testMessage, new WebServiceEndpointConfiguration(), context);

        Iterator it = mimeHeaders.getAllHeaders();
        Assert.assertEquals(((MimeHeader)it.next()).getName(), "operation");
        Assert.assertEquals(((MimeHeader)it.next()).getValue(), "123456789");
        Assert.assertFalse(it.hasNext());

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testOutboundSoapMimeHeaderSkipped() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(payload)
                .setHeader(SoapMessageHeaders.HTTP_PREFIX + "operation", "unitTest")
                .setHeader(SoapMessageHeaders.HTTP_PREFIX + "messageId", "123456789");

        WebServiceEndpointConfiguration endpointConfiguration = new WebServiceEndpointConfiguration();
        endpointConfiguration.setHandleMimeHeaders(false);
        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        SaajSoapMessage saajSoapRequest = Mockito.mock(SaajSoapMessage.class);
        SOAPMessage saajMessage = Mockito.mock(SOAPMessage.class);

        reset(saajSoapRequest, soapBody, soapHeader, soapEnvelope, saajMessage);

        when(saajSoapRequest.getEnvelope()).thenReturn(soapEnvelope);

        when(soapEnvelope.getBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(new StringResult());

        soapMessageConverter.convertOutbound(saajSoapRequest, testMessage, endpointConfiguration, context);

    }

    @Test
    public void testOutboundSoapAttachment() throws TransformerException, IOException {
        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("attContentId");
        attachment.setContent("This is a SOAP attachment\nwith multi-line");
        attachment.setContentType("plain/text");

        SoapMessage testMessage = new SoapMessage(payload);
        testMessage.addAttachment(attachment);

        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        reset(soapRequest, soapBody);

        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(new StringResult());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                InputStreamSource contentStream = (InputStreamSource)invocation.getArguments()[1];
                BufferedReader reader = new BufferedReader(new InputStreamReader(contentStream.getInputStream()));

                Assert.assertEquals(reader.readLine(), "This is a SOAP attachment");
                Assert.assertEquals(reader.readLine(), "with multi-line");

                reader.close();
                return null;
            }
        }).when(soapRequest).addAttachment(eq(attachment.getContentId()), (InputStreamSource)any(), eq(attachment.getContentType()));

        soapMessageConverter.convertOutbound(soapRequest, testMessage, new WebServiceEndpointConfiguration(), context);

    }

    @Test
    public void testInboundSoapBody() throws TransformerException, IOException {
        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        StringSource soapBodySource = new StringSource(payload);

        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        Set<Attachment> soapAttachments = new HashSet<Attachment>();

        reset(soapResponse, soapEnvelope, soapBody, soapHeader);

        when(soapResponse.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));
        when(soapResponse.getPayloadSource()).thenReturn(soapBodySource);
        when(soapResponse.getSoapHeader()).thenReturn(soapHeader);
        when(soapEnvelope.getHeader()).thenReturn(soapHeader);
        when(soapHeader.examineAllHeaderElements()).thenReturn(soapHeaders.iterator());
        when(soapHeader.getSource()).thenReturn(null);

        when(soapResponse.getAttachments()).thenReturn(soapAttachments.iterator());

        when(soapResponse.getSoapAction()).thenReturn("");

        Message responseMessage = soapMessageConverter.convertInbound(soapResponse, new WebServiceEndpointConfiguration(), context);
        Assert.assertEquals(responseMessage.getPayload(), XML_PROCESSING_INSTRUCTION + payload);
        Assert.assertNull(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION));
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);

    }

    @Test
    public void testInboundSoapBodyOnlyRootElement() throws TransformerException, IOException {
        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        StringSource soapBodySource = new StringSource("<testMessage/>");

        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        Set<Attachment> soapAttachments = new HashSet<Attachment>();

        reset(soapResponse, soapEnvelope, soapBody, soapHeader);

        when(soapResponse.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));
        when(soapResponse.getPayloadSource()).thenReturn(soapBodySource);
        when(soapResponse.getSoapHeader()).thenReturn(soapHeader);
        when(soapEnvelope.getHeader()).thenReturn(soapHeader);
        when(soapHeader.examineAllHeaderElements()).thenReturn(soapHeaders.iterator());
        when(soapHeader.getSource()).thenReturn(null);

        when(soapResponse.getAttachments()).thenReturn(soapAttachments.iterator());

        when(soapResponse.getSoapAction()).thenReturn("");

        Message responseMessage = soapMessageConverter.convertInbound(soapResponse, new WebServiceEndpointConfiguration(), context);
        Assert.assertEquals(responseMessage.getPayload(), XML_PROCESSING_INSTRUCTION + "<testMessage/>");
        Assert.assertNull(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION));
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);

    }

    @Test
    public void testInboundSoapAction() throws TransformerException, IOException {
        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        StringSource soapBodySource = new StringSource(payload);

        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        Set<Attachment> soapAttachments = new HashSet<Attachment>();

        reset(soapResponse, soapEnvelope, soapBody, soapHeader);

        when(soapResponse.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));
        when(soapResponse.getPayloadSource()).thenReturn(soapBodySource);
        when(soapResponse.getSoapHeader()).thenReturn(soapHeader);
        when(soapEnvelope.getHeader()).thenReturn(soapHeader);
        when(soapHeader.examineAllHeaderElements()).thenReturn(soapHeaders.iterator());
        when(soapHeader.getSource()).thenReturn(null);

        when(soapResponse.getSoapAction()).thenReturn("soapOperation");

        when(soapResponse.getAttachments()).thenReturn(soapAttachments.iterator());

        Message responseMessage = soapMessageConverter.convertInbound(soapResponse, new WebServiceEndpointConfiguration(), context);
        Assert.assertEquals(responseMessage.getPayload(), XML_PROCESSING_INSTRUCTION + payload);
        Assert.assertEquals(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION), "soapOperation");
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);

    }

    @Test
    public void testInboundSoapHeaderContent() throws TransformerException, IOException {
        String soapHeaderContent = "<header>" +
                "<operation>unitTest</operation>" +
                "<messageId>123456789</messageId>" +
                "</header>";

        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        StringSource soapBodySource = new StringSource(payload);

        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        Set<Attachment> soapAttachments = new HashSet<Attachment>();

        reset(soapResponse, soapEnvelope, soapBody, soapHeader);

        when(soapResponse.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));
        when(soapResponse.getPayloadSource()).thenReturn(soapBodySource);
        when(soapResponse.getSoapHeader()).thenReturn(soapHeader);
        when(soapEnvelope.getHeader()).thenReturn(soapHeader);
        when(soapHeader.examineAllHeaderElements()).thenReturn(soapHeaders.iterator());
        when(soapHeader.getSource()).thenReturn(new StringSource(soapHeaderContent));

        when(soapResponse.getSoapAction()).thenReturn("\"\"");

        when(soapResponse.getAttachments()).thenReturn(soapAttachments.iterator());

        Message responseMessage = soapMessageConverter.convertInbound(soapResponse, new WebServiceEndpointConfiguration(), context);
        Assert.assertEquals(responseMessage.getPayload(), XML_PROCESSING_INSTRUCTION + payload);
        Assert.assertEquals(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION), "");
        Assert.assertEquals(responseMessage.getHeaderData().size(), 1L);
        Assert.assertEquals(responseMessage.getHeaderData().get(0), XML_PROCESSING_INSTRUCTION + soapHeaderContent);

    }

    @Test
    public void testInboundSoapHeader() throws TransformerException, IOException {
        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        StringSource soapBodySource = new StringSource(payload);

        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        soapHeaders.add(soapHeaderElement);

        Set<Attachment> soapAttachments = new HashSet<Attachment>();

        reset(soapResponse, soapEnvelope, soapBody, soapHeader, soapHeaderElement);

        when(soapResponse.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));
        when(soapResponse.getPayloadSource()).thenReturn(soapBodySource);
        when(soapResponse.getSoapHeader()).thenReturn(soapHeader);
        when(soapEnvelope.getHeader()).thenReturn(soapHeader);
        when(soapHeader.examineAllHeaderElements()).thenReturn(soapHeaders.iterator());
        when(soapHeader.getSource()).thenReturn(null);

        when(soapHeaderElement.getName()).thenReturn(new QName("{http://citrusframework.org}citrus:messageId"));
        when(soapHeaderElement.getText()).thenReturn("123456789");

        when(soapResponse.getSoapAction()).thenReturn("soapOperation");

        when(soapResponse.getAttachments()).thenReturn(soapAttachments.iterator());

        Message responseMessage = soapMessageConverter.convertInbound(soapResponse, new WebServiceEndpointConfiguration(), context);
        Assert.assertEquals(responseMessage.getPayload(), XML_PROCESSING_INSTRUCTION + payload);
        Assert.assertEquals(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION), "soapOperation");
        Assert.assertEquals(responseMessage.getHeader("{http://citrusframework.org}citrus:messageId"), "123456789");
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);

    }

    @Test
    public void testInboundSoapAttachment() throws TransformerException, IOException {
        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("attContentId");
        attachment.setContent("This is a SOAP attachment" + System.getProperty("line.separator") + "with multi-line");
        attachment.setContentType("plain/text");

        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        StringSource soapBodySource = new StringSource(payload);

        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        Set<Attachment> soapAttachments = new HashSet<Attachment>();
        soapAttachments.add(attachment);

        reset(soapResponse, soapEnvelope, soapBody, soapHeader);

        when(soapResponse.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));
        when(soapResponse.getPayloadSource()).thenReturn(soapBodySource);
        when(soapResponse.getSoapHeader()).thenReturn(soapHeader);
        when(soapEnvelope.getHeader()).thenReturn(soapHeader);
        when(soapHeader.examineAllHeaderElements()).thenReturn(soapHeaders.iterator());
        when(soapHeader.getSource()).thenReturn(null);

        when(soapResponse.getSoapAction()).thenReturn("soapOperation");

        when(soapResponse.getAttachments()).thenReturn(soapAttachments.iterator());

        Message responseMessage = soapMessageConverter.convertInbound(soapResponse, new WebServiceEndpointConfiguration(), context);
        Assert.assertTrue(SoapMessage.class.isInstance(responseMessage));

        SoapMessage soapResponseMessage = (SoapMessage) responseMessage;
        Assert.assertEquals(soapResponseMessage.getPayload(), XML_PROCESSING_INSTRUCTION + payload);
        Assert.assertEquals(soapResponseMessage.getSoapAction(), "soapOperation");
        Assert.assertEquals(soapResponseMessage.getHeaderData().size(), 0L);

        List<SoapAttachment> attachments = soapResponseMessage.getAttachments();
        Assert.assertEquals(attachments.size(), 1L);
        Assert.assertEquals(attachments.get(0).getContentId(), attachment.getContentId());
        Assert.assertEquals(attachments.get(0).getContentType(), attachment.getContentType());
        Assert.assertEquals(FileUtils.readToString(attachments.get(0).getInputStream()), attachment.getContent());

    }

    @Test
    public void testInboundSoapBodyWithNamespaceTranslation() throws TransformerException, IOException {
        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        StringSource soapBodySource = new StringSource(payload);

        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        Set<Attachment> soapAttachments = new HashSet<Attachment>();

        reset(soapResponse, soapEnvelope, soapBody, soapHeader);

        when(soapResponse.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new DOMSource(XMLUtils.parseMessagePayload(getSoapRequestPayload(payload, "xmlns:foo=\"http://citruframework.org/foo\"")).getFirstChild()));
        when(soapResponse.getPayloadSource()).thenReturn(soapBodySource);
        when(soapResponse.getSoapHeader()).thenReturn(soapHeader);
        when(soapEnvelope.getHeader()).thenReturn(soapHeader);
        when(soapHeader.examineAllHeaderElements()).thenReturn(soapHeaders.iterator());
        when(soapHeader.getSource()).thenReturn(null);

        when(soapResponse.getAttachments()).thenReturn(soapAttachments.iterator());

        when(soapResponse.getSoapAction()).thenReturn("");

       Message responseMessage = soapMessageConverter.convertInbound(soapResponse, new WebServiceEndpointConfiguration(), context);
        Assert.assertEquals(responseMessage.getPayload(), XML_PROCESSING_INSTRUCTION + "<testMessage xmlns:foo=\"http://citruframework.org/foo\">Hello</testMessage>");
        Assert.assertNull(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION));
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);

    }

    @Test
    public void testInboundSoapBodyWithNamespaceTranslationXmlProcessingInstruction() throws TransformerException, IOException {
        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        StringSource soapBodySource = new StringSource(XML_PROCESSING_INSTRUCTION + payload);

        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        Set<Attachment> soapAttachments = new HashSet<Attachment>();

        reset(soapResponse, soapEnvelope, soapBody, soapHeader);

        when(soapResponse.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new DOMSource(XMLUtils.parseMessagePayload(getSoapRequestPayload(payload, "xmlns:foo=\"http://citruframework.org/foo\"")).getFirstChild()));
        when(soapResponse.getPayloadSource()).thenReturn(soapBodySource);
        when(soapResponse.getSoapHeader()).thenReturn(soapHeader);
        when(soapEnvelope.getHeader()).thenReturn(soapHeader);
        when(soapHeader.examineAllHeaderElements()).thenReturn(soapHeaders.iterator());
        when(soapHeader.getSource()).thenReturn(null);

        when(soapResponse.getAttachments()).thenReturn(soapAttachments.iterator());

        when(soapResponse.getSoapAction()).thenReturn("");

        Message responseMessage = soapMessageConverter.convertInbound(soapResponse, new WebServiceEndpointConfiguration(), context);
        Assert.assertEquals(responseMessage.getPayload(), XML_PROCESSING_INSTRUCTION + "<testMessage xmlns:foo=\"http://citruframework.org/foo\">Hello</testMessage>");
        Assert.assertNull(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION));
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);

    }

    @Test
    public void testInboundSoapBodyWithNamespaceTranslationOnlyRootElement() throws TransformerException, IOException {
        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        String payload = "<testMessage xmlns:foo=\"http://citruframework.org/foo\" xmlns:bar=\"http://citruframework.org/bar\" " +
                "other=\"true\"/>";
        StringSource soapBodySource = new StringSource(payload);

        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        Set<Attachment> soapAttachments = new HashSet<Attachment>();

        reset(soapResponse, soapEnvelope, soapBody, soapHeader);

        when(soapResponse.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new DOMSource(XMLUtils.parseMessagePayload(getSoapRequestPayload(payload, "skip=\"true\"", "xmlns:foo=\"http://citruframework.org/foo\"",
                "xmlns:new=\"http://citruframework.org/new\"")).getFirstChild()));
        when(soapResponse.getPayloadSource()).thenReturn(soapBodySource);
        when(soapResponse.getSoapHeader()).thenReturn(soapHeader);
        when(soapEnvelope.getHeader()).thenReturn(soapHeader);
        when(soapHeader.examineAllHeaderElements()).thenReturn(soapHeaders.iterator());
        when(soapHeader.getSource()).thenReturn(null);

        when(soapResponse.getAttachments()).thenReturn(soapAttachments.iterator());

        when(soapResponse.getSoapAction()).thenReturn("");

        Message responseMessage = soapMessageConverter.convertInbound(soapResponse, new WebServiceEndpointConfiguration(), context);
        Assert.assertEquals(responseMessage.getPayload(), XML_PROCESSING_INSTRUCTION + "<testMessage xmlns:foo=\"http://citruframework.org/foo\" xmlns:bar=\"http://citruframework.org/bar\" " +
                "other=\"true\" xmlns:new=\"http://citruframework.org/new\"/>");
        Assert.assertNull(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION));
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);

    }

    @Test
    public void testInboundSoapBodyWithNamespaceTranslationDuplicates() throws TransformerException, IOException {
        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        String payload = "<testMessage xmlns:foo=\"http://citruframework.org/foo\" xmlns:bar=\"http://citruframework.org/bar\" " +
                "other=\"true\">Hello</testMessage>";
        StringSource soapBodySource = new StringSource(payload);

        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        Set<Attachment> soapAttachments = new HashSet<Attachment>();

        reset(soapResponse, soapEnvelope, soapBody, soapHeader);

        when(soapResponse.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new DOMSource(XMLUtils.parseMessagePayload(getSoapRequestPayload(payload, "skip=\"true\"", "xmlns:foo=\"http://citruframework.org/foo\"",
                "xmlns:new=\"http://citruframework.org/new\"")).getFirstChild()));
        when(soapResponse.getPayloadSource()).thenReturn(soapBodySource);
        when(soapResponse.getSoapHeader()).thenReturn(soapHeader);
        when(soapEnvelope.getHeader()).thenReturn(soapHeader);
        when(soapHeader.examineAllHeaderElements()).thenReturn(soapHeaders.iterator());
        when(soapHeader.getSource()).thenReturn(null);

        when(soapResponse.getAttachments()).thenReturn(soapAttachments.iterator());

        when(soapResponse.getSoapAction()).thenReturn("");

        Message responseMessage = soapMessageConverter.convertInbound(soapResponse, new WebServiceEndpointConfiguration(), context);
        Assert.assertEquals(responseMessage.getPayload(), XML_PROCESSING_INSTRUCTION + "<testMessage xmlns:foo=\"http://citruframework.org/foo\" xmlns:bar=\"http://citruframework.org/bar\" " +
                "other=\"true\" xmlns:new=\"http://citruframework.org/new\">Hello</testMessage>");
        Assert.assertNull(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION));
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);

    }

    @Test
    public void testInboundSoapKeepEnvelope() throws TransformerException, IOException {
        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        SaajSoapMessageFactory soapMessageFactory = new SaajSoapMessageFactory();
        soapMessageFactory.afterPropertiesSet();
        WebServiceMessage soapMessage = soapMessageFactory.createWebServiceMessage(new ByteArrayInputStream((XML_PROCESSING_INSTRUCTION + getSoapRequestPayload()).getBytes()));

        WebServiceEndpointConfiguration endpointConfiguration = new WebServiceEndpointConfiguration();
        endpointConfiguration.setKeepSoapEnvelope(true);
        Message responseMessage = soapMessageConverter.convertInbound(soapMessage, endpointConfiguration, context);
        Assert.assertEquals(StringUtils.trimAllWhitespace(responseMessage.getPayload(String.class)), StringUtils.trimAllWhitespace(XML_PROCESSING_INSTRUCTION + getSoapRequestPayload()));
        Assert.assertEquals(responseMessage.getHeaderData().size(), 1L);
        Assert.assertEquals(responseMessage.getHeaderData().get(0), XML_PROCESSING_INSTRUCTION + "<SOAP-ENV:Header xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"/>");
    }

    private String getSoapRequestPayload() {
        return getSoapRequestPayload(payload);
    }

    private String getSoapRequestPayload(String payload, String ... namespaces) {
        return "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" " + StringUtils.arrayToDelimitedString(namespaces, " ") + ">\n" +
                "<SOAP-ENV:Header/>\n" +
                "<SOAP-ENV:Body>\n" +
                    payload +
                "</SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>";
    }
}
