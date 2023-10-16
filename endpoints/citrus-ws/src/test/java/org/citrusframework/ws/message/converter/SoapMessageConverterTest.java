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

package org.citrusframework.ws.message.converter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;

import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.XMLUtils;
import org.citrusframework.ws.client.WebServiceEndpointConfiguration;
import org.citrusframework.ws.message.SoapAttachment;
import org.citrusframework.ws.message.SoapMessage;
import org.citrusframework.ws.message.SoapMessageHeaders;
import org.citrusframework.xml.StringResult;
import org.citrusframework.xml.StringSource;
import jakarta.xml.soap.MimeHeader;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPMessage;
import org.springframework.core.io.InputStreamSource;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @since 2.0
 */
public class SoapMessageConverterTest extends AbstractTestNGUnitTest {

    private final SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

    private static final String XML_PROCESSING_INSTRUCTION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    private SoapMessageFactory soapMessageFactory;
    private org.springframework.ws.soap.SoapMessage soapRequest;
    private org.springframework.ws.soap.SoapMessage soapResponse;
    private SoapEnvelope soapEnvelope;
    private SoapBody soapBody;
    private SoapHeader soapHeader;
    private SoapHeaderElement soapHeaderElement;

    private final String payload = "<testMessage>Hello</testMessage>";

    @BeforeMethod
    public void resetMocks() {
        soapMessageFactory = mock(SoapMessageFactory.class);
        soapRequest = mock(org.springframework.ws.soap.SoapMessage.class);
        soapResponse = mock(org.springframework.ws.soap.SoapMessage.class);
        soapEnvelope = mock(SoapEnvelope.class);
        soapBody = mock(SoapBody.class);
        soapHeader = mock(SoapHeader.class);
        soapHeaderElement = mock(SoapHeaderElement.class);
    }

    @Test
    public void testOutboundSoapMessageCreation() {
        final Message testMessage = new DefaultMessage(payload);

        final WebServiceEndpointConfiguration endpointConfiguration = new WebServiceEndpointConfiguration();
        endpointConfiguration.setMessageFactory(soapMessageFactory);

        final StringResult soapBodyResult = new StringResult();

        when(soapMessageFactory.createWebServiceMessage()).thenReturn(soapRequest);
        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(soapBodyResult);

        soapMessageConverter.convertOutbound(testMessage, endpointConfiguration, context);

        Assert.assertEquals(soapBodyResult.toString(), XML_PROCESSING_INSTRUCTION + payload);

    }

    @Test
    public void testOutboundSoapBody() {
        final Message testMessage = new DefaultMessage(payload);

        final StringResult soapBodyResult = new StringResult();

        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(soapBodyResult);

        soapMessageConverter.convertOutbound(soapRequest, testMessage, new WebServiceEndpointConfiguration(), context);

        Assert.assertEquals(soapBodyResult.toString(), XML_PROCESSING_INSTRUCTION + payload);

    }

    @Test
    public void testOutboundSoapAction() {
        final Message testMessage = new DefaultMessage(payload)
                .setHeader(SoapMessageHeaders.SOAP_ACTION, "soapAction");

        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(new StringResult());

        soapMessageConverter.convertOutbound(soapRequest, testMessage, new WebServiceEndpointConfiguration(), context);
        verify(soapRequest).setSoapAction("soapAction");
    }

    @Test
    public void testOutboundSoapHeaderContent() {
        final String soapHeaderContent = "<header>" +
                "<operation>unitTest</operation>" +
                "<messageId>123456789</messageId>" +
                "</header>";

        final Message testMessage = new DefaultMessage(payload)
                .addHeaderData(soapHeaderContent);

        final StringResult soapHeaderResult = new StringResult();

        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(new StringResult());

        when(soapRequest.getSoapHeader()).thenReturn(soapHeader);
        when(soapHeader.getResult()).thenReturn(soapHeaderResult);

        soapMessageConverter.convertOutbound(soapRequest, testMessage, new WebServiceEndpointConfiguration(), context);

        Assert.assertEquals(soapHeaderResult.toString(), soapHeaderContent);
    }

    @Test
    public void testMultipleOutboundSoapHeaderContent() {
        final String soapHeaderContent = "<header>" +
                "<operation>unitTest</operation>" +
                "<messageId>123456789</messageId>" +
                "</header>";

        final Message testMessage = new DefaultMessage(payload)
                .addHeaderData(soapHeaderContent)
                .addHeaderData("<AppInfo><appId>123456789</appId></AppInfo>");

        final StringResult soapHeaderResult = new StringResult();

        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(new StringResult());

        when(soapRequest.getSoapHeader()).thenReturn(soapHeader);
        when(soapHeader.getResult()).thenReturn(soapHeaderResult);

        soapMessageConverter.convertOutbound(soapRequest, testMessage, new WebServiceEndpointConfiguration(), context);

        Assert.assertEquals(soapHeaderResult.toString(), soapHeaderContent + "<AppInfo><appId>123456789</appId></AppInfo>");

    }

    @Test
    public void testOutboundSoapHeader() {
        final Message testMessage = new DefaultMessage(payload)
                .setHeader("operation", "unitTest")
                .setHeader("messageId", "123456789");

        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(new StringResult());

        when(soapRequest.getSoapHeader()).thenReturn(soapHeader);
        when(soapHeader.addHeaderElement(eq(new QName("", "operation", "")))).thenReturn(soapHeaderElement);
        when(soapHeader.addHeaderElement(eq(new QName("", "messageId", "")))).thenReturn(soapHeaderElement);

        soapMessageConverter.convertOutbound(soapRequest, testMessage, new WebServiceEndpointConfiguration(), context);

        verify(soapHeaderElement).setText("unitTest");
        verify(soapHeaderElement).setText("123456789");
    }

    @Test
    public void testOutboundSoapHeaderQNameString() {
        final Message testMessage = new DefaultMessage(payload)
                .setHeader("{http://www.citrus.com}citrus:operation", "unitTest")
                .setHeader("{http://www.citrus.com}citrus:messageId", "123456789");

        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(new StringResult());

        when(soapRequest.getSoapHeader()).thenReturn(soapHeader);
        when(soapHeader.addHeaderElement(eq(new QName("http://www.citrus.com", "operation", "citrus")))).thenReturn(soapHeaderElement);
        when(soapHeader.addHeaderElement(eq(new QName("http://www.citrus.com", "messageId", "citrus")))).thenReturn(soapHeaderElement);

        soapMessageConverter.convertOutbound(soapRequest, testMessage, new WebServiceEndpointConfiguration(), context);

        verify(soapHeaderElement).setText("unitTest");
        verify(soapHeaderElement).setText("123456789");
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testOutboundSoapMimeHeader() {
        final Message testMessage = new DefaultMessage(payload)
                .setHeader(SoapMessageHeaders.HTTP_PREFIX + "operation", "unitTest")
                .setHeader(SoapMessageHeaders.HTTP_PREFIX + "messageId", "123456789");

        final SaajSoapMessage saajSoapRequest = mock(SaajSoapMessage.class);
        final SOAPMessage saajMessage = mock(SOAPMessage.class);

        final MimeHeaders mimeHeaders = new MimeHeaders();

        when(saajSoapRequest.getEnvelope()).thenReturn(soapEnvelope);
        when(saajSoapRequest.getSoapBody()).thenReturn(soapBody);

        when(soapEnvelope.getBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(new StringResult());

        when(saajSoapRequest.getSaajMessage()).thenReturn(saajMessage);

        when(saajMessage.getMimeHeaders()).thenReturn(mimeHeaders);

        soapMessageConverter.convertOutbound(saajSoapRequest, testMessage, new WebServiceEndpointConfiguration(), context);

        final Iterator it = mimeHeaders.getAllHeaders();
        Assert.assertEquals(((MimeHeader)it.next()).getName(), "operation");
        Assert.assertEquals(((MimeHeader)it.next()).getValue(), "123456789");
        Assert.assertFalse(it.hasNext());

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testOutboundSoapMimeHeaderSkipped() {
        final Message testMessage = new DefaultMessage(payload)
                .setHeader(SoapMessageHeaders.HTTP_PREFIX + "operation", "unitTest")
                .setHeader(SoapMessageHeaders.HTTP_PREFIX + "messageId", "123456789");

        final WebServiceEndpointConfiguration endpointConfiguration = new WebServiceEndpointConfiguration();
        endpointConfiguration.setHandleMimeHeaders(false);

        final SaajSoapMessage saajSoapRequest = mock(SaajSoapMessage.class);

        when(saajSoapRequest.getEnvelope()).thenReturn(soapEnvelope);
        when(saajSoapRequest.getSoapBody()).thenReturn(soapBody);

        when(soapEnvelope.getBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(new StringResult());

        soapMessageConverter.convertOutbound(saajSoapRequest, testMessage, endpointConfiguration, context);

    }

    @Test
    public void testOutboundSoapAttachment() {
        final SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("attContentId");
        attachment.setContent("This is a SOAP attachment\nwith multi-line");
        attachment.setContentType("plain/text");

        final SoapMessage testMessage = new SoapMessage(payload);
        testMessage.addAttachment(attachment);

        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(new StringResult());

        doAnswer(invocation -> {
            InputStreamSource contentStream = (InputStreamSource)invocation.getArguments()[1];
            BufferedReader reader = new BufferedReader(new InputStreamReader(contentStream.getInputStream()));

            Assert.assertEquals(reader.readLine(), "This is a SOAP attachment");
            Assert.assertEquals(reader.readLine(), "with multi-line");

            reader.close();
            return null;
        }).when(soapRequest).addAttachment(eq("<attContentId>"), any(InputStreamSource.class), eq(attachment.getContentType()));

        soapMessageConverter.convertOutbound(soapRequest, testMessage, new WebServiceEndpointConfiguration(), context);

        verify(soapRequest).addAttachment(eq("<attContentId>"), any(InputStreamSource.class), eq(attachment.getContentType()));

    }

    @Test
    public void testInboundSoapBody() {
        final StringSource soapBodySource = new StringSource(payload);

        when(soapResponse.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));
        when(soapResponse.getPayloadSource()).thenReturn(soapBodySource);
        when(soapResponse.getSoapHeader()).thenReturn(soapHeader);
        when(soapEnvelope.getHeader()).thenReturn(soapHeader);
        when(soapHeader.examineAllHeaderElements()).thenReturn(new HashSet<SoapHeaderElement>().iterator());
        when(soapHeader.getSource()).thenReturn(null);

        when(soapResponse.getAttachments()).thenReturn(new HashSet<Attachment>().iterator());

        when(soapResponse.getSoapAction()).thenReturn("");

        final Message responseMessage = soapMessageConverter.convertInbound(soapResponse, new WebServiceEndpointConfiguration(), context);
        Assert.assertEquals(responseMessage.getPayload(), XML_PROCESSING_INSTRUCTION + payload);
        Assert.assertNull(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION));
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);

    }

    @Test
    public void testInboundSoapBodyOnlyRootElement() {
        final StringSource soapBodySource = new StringSource("<testMessage/>");

        when(soapResponse.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));
        when(soapResponse.getPayloadSource()).thenReturn(soapBodySource);
        when(soapResponse.getSoapHeader()).thenReturn(soapHeader);
        when(soapEnvelope.getHeader()).thenReturn(soapHeader);
        when(soapHeader.examineAllHeaderElements()).thenReturn(new HashSet<SoapHeaderElement>().iterator());
        when(soapHeader.getSource()).thenReturn(null);

        when(soapResponse.getAttachments()).thenReturn(new HashSet<Attachment>().iterator());

        when(soapResponse.getSoapAction()).thenReturn("");

        final Message responseMessage = soapMessageConverter.convertInbound(soapResponse, new WebServiceEndpointConfiguration(), context);
        Assert.assertEquals(responseMessage.getPayload(), XML_PROCESSING_INSTRUCTION + "<testMessage/>");
        Assert.assertNull(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION));
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);

    }

    @Test
    public void testInboundSoapAction() {
        final StringSource soapBodySource = new StringSource(payload);

        when(soapResponse.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));
        when(soapResponse.getPayloadSource()).thenReturn(soapBodySource);
        when(soapResponse.getSoapHeader()).thenReturn(soapHeader);
        when(soapEnvelope.getHeader()).thenReturn(soapHeader);
        when(soapHeader.examineAllHeaderElements()).thenReturn(new HashSet<SoapHeaderElement>().iterator());
        when(soapHeader.getSource()).thenReturn(null);

        when(soapResponse.getSoapAction()).thenReturn("soapOperation");

        when(soapResponse.getAttachments()).thenReturn(new HashSet<Attachment>().iterator());

        final Message responseMessage = soapMessageConverter.convertInbound(soapResponse, new WebServiceEndpointConfiguration(), context);
        Assert.assertEquals(responseMessage.getPayload(), XML_PROCESSING_INSTRUCTION + payload);
        Assert.assertEquals(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION), "soapOperation");
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);

    }

    @Test
    public void testInboundSoapHeaderContent() {
        final String soapHeaderContent = "<header>" +
                "<operation>unitTest</operation>" +
                "<messageId>123456789</messageId>" +
                "</header>";

        final StringSource soapBodySource = new StringSource(payload);

        when(soapResponse.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));
        when(soapResponse.getPayloadSource()).thenReturn(soapBodySource);
        when(soapResponse.getSoapHeader()).thenReturn(soapHeader);
        when(soapEnvelope.getHeader()).thenReturn(soapHeader);
        when(soapHeader.examineAllHeaderElements()).thenReturn(new HashSet<SoapHeaderElement>().iterator());
        when(soapHeader.getSource()).thenReturn(new StringSource(soapHeaderContent));

        when(soapResponse.getSoapAction()).thenReturn("\"\"");

        when(soapResponse.getAttachments()).thenReturn(new HashSet<Attachment>().iterator());

        final Message responseMessage = soapMessageConverter.convertInbound(soapResponse, new WebServiceEndpointConfiguration(), context);
        Assert.assertEquals(responseMessage.getPayload(), XML_PROCESSING_INSTRUCTION + payload);
        Assert.assertEquals(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION), "");
        Assert.assertEquals(responseMessage.getHeaderData().size(), 1L);
        Assert.assertEquals(responseMessage.getHeaderData().get(0), XML_PROCESSING_INSTRUCTION + soapHeaderContent);

    }

    @Test
    public void testInboundSoapHeader() {
        final StringSource soapBodySource = new StringSource(payload);

        final Set<SoapHeaderElement> soapHeaders = new HashSet<>();
        soapHeaders.add(soapHeaderElement);

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

        when(soapResponse.getAttachments()).thenReturn(new HashSet<Attachment>().iterator());

        final Message responseMessage = soapMessageConverter.convertInbound(soapResponse, new WebServiceEndpointConfiguration(), context);
        Assert.assertEquals(responseMessage.getPayload(), XML_PROCESSING_INSTRUCTION + payload);
        Assert.assertEquals(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION), "soapOperation");
        Assert.assertEquals(responseMessage.getHeader("{http://citrusframework.org}citrus:messageId"), "123456789");
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);

    }

    @Test
    public void testInboundSoapAttachment() throws IOException {
        final SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("attContentId");
        attachment.setContent("This is a SOAP attachment" + System.getProperty("line.separator") + "with multi-line");
        attachment.setContentType("plain/text");

        final StringSource soapBodySource = new StringSource(payload);

        final Set<Attachment> soapAttachments = new HashSet<>();
        soapAttachments.add(attachment);

        when(soapResponse.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(getSoapRequestPayload()));
        when(soapResponse.getPayloadSource()).thenReturn(soapBodySource);
        when(soapResponse.getSoapHeader()).thenReturn(soapHeader);
        when(soapEnvelope.getHeader()).thenReturn(soapHeader);
        when(soapHeader.examineAllHeaderElements()).thenReturn(new HashSet<SoapHeaderElement>().iterator());
        when(soapHeader.getSource()).thenReturn(null);

        when(soapResponse.getSoapAction()).thenReturn("soapOperation");

        when(soapResponse.getAttachments()).thenReturn(soapAttachments.iterator());

        final SoapMessage responseMessage = soapMessageConverter.convertInbound(soapResponse, new WebServiceEndpointConfiguration(), context);
        Assert.assertNotNull(responseMessage);

        Assert.assertEquals(responseMessage.getPayload(), XML_PROCESSING_INSTRUCTION + payload);
        Assert.assertEquals(responseMessage.getSoapAction(), "soapOperation");
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);

        final List<SoapAttachment> attachments = responseMessage.getAttachments();
        Assert.assertEquals(attachments.size(), 1L);
        Assert.assertEquals(attachments.get(0).getContentId(), attachment.getContentId());
        Assert.assertEquals(attachments.get(0).getContentType(), attachment.getContentType());
        Assert.assertEquals(FileUtils.readToString(attachments.get(0).getInputStream()), attachment.getContent());

    }

    @Test
    public void testInboundSoapBodyWithNamespaceTranslation() {
        final StringSource soapBodySource = new StringSource(payload);

        when(soapResponse.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new DOMSource(XMLUtils.parseMessagePayload(getSoapRequestPayload(payload, "xmlns:foo=\"http://citruframework.org/foo\"")).getFirstChild()));
        when(soapResponse.getPayloadSource()).thenReturn(soapBodySource);
        when(soapResponse.getSoapHeader()).thenReturn(soapHeader);
        when(soapEnvelope.getHeader()).thenReturn(soapHeader);
        when(soapHeader.examineAllHeaderElements()).thenReturn(new HashSet<SoapHeaderElement>().iterator());
        when(soapHeader.getSource()).thenReturn(null);

        when(soapResponse.getAttachments()).thenReturn(new HashSet<Attachment>().iterator());

        when(soapResponse.getSoapAction()).thenReturn("");

       final Message responseMessage = soapMessageConverter.convertInbound(soapResponse, new WebServiceEndpointConfiguration(), context);
        Assert.assertEquals(responseMessage.getPayload(), XML_PROCESSING_INSTRUCTION + "<testMessage xmlns:foo=\"http://citruframework.org/foo\">Hello</testMessage>");
        Assert.assertNull(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION));
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);

    }

    @Test
    public void testInboundSoapBodyWithNamespaceTranslationXmlProcessingInstruction() {
        final StringSource soapBodySource = new StringSource(XML_PROCESSING_INSTRUCTION + payload);

        when(soapResponse.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new DOMSource(XMLUtils.parseMessagePayload(getSoapRequestPayload(payload, "xmlns:foo=\"http://citruframework.org/foo\"")).getFirstChild()));
        when(soapResponse.getPayloadSource()).thenReturn(soapBodySource);
        when(soapResponse.getSoapHeader()).thenReturn(soapHeader);
        when(soapEnvelope.getHeader()).thenReturn(soapHeader);
        when(soapHeader.examineAllHeaderElements()).thenReturn(new HashSet<SoapHeaderElement>().iterator());
        when(soapHeader.getSource()).thenReturn(null);

        when(soapResponse.getAttachments()).thenReturn(new HashSet<Attachment>().iterator());

        when(soapResponse.getSoapAction()).thenReturn("");

        final Message responseMessage = soapMessageConverter.convertInbound(soapResponse, new WebServiceEndpointConfiguration(), context);
        Assert.assertEquals(responseMessage.getPayload(), XML_PROCESSING_INSTRUCTION + "<testMessage xmlns:foo=\"http://citruframework.org/foo\">Hello</testMessage>");
        Assert.assertNull(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION));
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);

    }

    @Test
    public void testInboundSoapBodyWithNamespaceTranslationOnlyRootElement() {
        final String payload = "<testMessage xmlns:foo=\"http://citruframework.org/foo\" xmlns:bar=\"http://citruframework.org/bar\" " +
                "other=\"true\"/>";
        final StringSource soapBodySource = new StringSource(payload);

        when(soapResponse.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new DOMSource(XMLUtils.parseMessagePayload(getSoapRequestPayload(payload, "skip=\"true\"", "xmlns:foo=\"http://citruframework.org/foo\"",
                "xmlns:new=\"http://citruframework.org/new\"")).getFirstChild()));
        when(soapResponse.getPayloadSource()).thenReturn(soapBodySource);
        when(soapResponse.getSoapHeader()).thenReturn(soapHeader);
        when(soapEnvelope.getHeader()).thenReturn(soapHeader);
        when(soapHeader.examineAllHeaderElements()).thenReturn(new HashSet<SoapHeaderElement>().iterator());
        when(soapHeader.getSource()).thenReturn(null);

        when(soapResponse.getAttachments()).thenReturn(new HashSet<Attachment>().iterator());

        when(soapResponse.getSoapAction()).thenReturn("");

        final Message responseMessage = soapMessageConverter.convertInbound(soapResponse, new WebServiceEndpointConfiguration(), context);
        Assert.assertEquals(responseMessage.getPayload(), XML_PROCESSING_INSTRUCTION + "<testMessage xmlns:foo=\"http://citruframework.org/foo\" xmlns:bar=\"http://citruframework.org/bar\" " +
                "other=\"true\" xmlns:new=\"http://citruframework.org/new\"/>");
        Assert.assertNull(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION));
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);

    }

    @Test
    public void testInboundSoapBodyWithNamespaceTranslationDuplicates() {
        final String payload = "<testMessage xmlns:foo=\"http://citruframework.org/foo\" xmlns:bar=\"http://citruframework.org/bar\" " +
                "other=\"true\">Hello</testMessage>";
        final StringSource soapBodySource = new StringSource(payload);

        when(soapResponse.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new DOMSource(XMLUtils.parseMessagePayload(getSoapRequestPayload(payload, "skip=\"true\"", "xmlns:foo=\"http://citruframework.org/foo\"",
                "xmlns:new=\"http://citruframework.org/new\"")).getFirstChild()));
        when(soapResponse.getPayloadSource()).thenReturn(soapBodySource);
        when(soapResponse.getSoapHeader()).thenReturn(soapHeader);
        when(soapEnvelope.getHeader()).thenReturn(soapHeader);
        when(soapHeader.examineAllHeaderElements()).thenReturn(new HashSet<SoapHeaderElement>().iterator());
        when(soapHeader.getSource()).thenReturn(null);

        when(soapResponse.getAttachments()).thenReturn(new HashSet<Attachment>().iterator());

        when(soapResponse.getSoapAction()).thenReturn("");

        final Message responseMessage = soapMessageConverter.convertInbound(soapResponse, new WebServiceEndpointConfiguration(), context);
        Assert.assertEquals(responseMessage.getPayload(), XML_PROCESSING_INSTRUCTION + "<testMessage xmlns:foo=\"http://citruframework.org/foo\" xmlns:bar=\"http://citruframework.org/bar\" " +
                "other=\"true\" xmlns:new=\"http://citruframework.org/new\">Hello</testMessage>");
        Assert.assertNull(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION));
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);

    }

    @Test
    public void testInboundSoapKeepEnvelope() throws IOException {
        final SaajSoapMessageFactory soapMessageFactory = new SaajSoapMessageFactory();
        soapMessageFactory.afterPropertiesSet();
        final WebServiceMessage soapMessage = soapMessageFactory.createWebServiceMessage(new ByteArrayInputStream((XML_PROCESSING_INSTRUCTION + getSoapRequestPayload()).getBytes()));

        final WebServiceEndpointConfiguration endpointConfiguration = new WebServiceEndpointConfiguration();
        endpointConfiguration.setKeepSoapEnvelope(true);
        final Message responseMessage = soapMessageConverter.convertInbound(soapMessage, endpointConfiguration, context);
        Assert.assertEquals(responseMessage.getPayload(String.class).replaceAll("\\s", ""), (XML_PROCESSING_INSTRUCTION + getSoapRequestPayload()).replaceAll("\\s", ""));
        Assert.assertEquals(responseMessage.getHeaderData().size(), 1L);
        Assert.assertEquals(responseMessage.getHeaderData().get(0), XML_PROCESSING_INSTRUCTION + "<SOAP-ENV:Header xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"/>");
    }

    @Test
    public void testEmptyOutboundSoapBodyNotParsed(){

        //GIVEN
        final DefaultMessage emptyMessage = new DefaultMessage("");
        final WebServiceEndpointConfiguration endpointConfiguration = new WebServiceEndpointConfiguration();
        endpointConfiguration.setMessageFactory(soapMessageFactory);
        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(new StringResult());

        //WHEN
        soapMessageConverter.convertOutbound(soapRequest, emptyMessage, endpointConfiguration, context);

        //THEN
        verify(soapRequest, never()).getPayloadResult();
    }

    private String getSoapRequestPayload() {
        return getSoapRequestPayload(payload);
    }

    private String getSoapRequestPayload(final String payload, final String ... namespaces) {
        return "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" " + String.join(" ", namespaces) + ">\n" +
                "<SOAP-ENV:Header/>\n" +
                "<SOAP-ENV:Body>\n" +
                    payload +
                "</SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>";
    }
}
