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

import com.consol.citrus.message.*;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.message.SoapAttachment;
import com.consol.citrus.ws.client.WebServiceEndpointConfiguration;
import com.consol.citrus.ws.message.SoapMessage;
import com.consol.citrus.ws.message.SoapMessageHeaders;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.core.io.InputStreamSource;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.*;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.*;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class SoapMessageConverterTest {

    private SoapMessageFactory soapMessageFactory = EasyMock.createMock(SoapMessageFactory.class);
    private org.springframework.ws.soap.SoapMessage soapRequest = EasyMock.createMock(org.springframework.ws.soap.SoapMessage.class);
    private org.springframework.ws.soap.SoapMessage soapResponse = EasyMock.createMock(org.springframework.ws.soap.SoapMessage.class);
    private SoapEnvelope soapEnvelope = EasyMock.createMock(SoapEnvelope.class);
    private SoapBody soapBody = EasyMock.createMock(SoapBody.class);
    private SoapHeader soapHeader = EasyMock.createMock(SoapHeader.class);

    private String requestPayload = "<testMessage>Hello</testMessage>";
    private String responsePayload = "<testMessage>Hello</testMessage>";

    @Test
    public void testOutboundSoapMessageCreation() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(requestPayload);

        WebServiceEndpointConfiguration endpointConfiguration = new WebServiceEndpointConfiguration();
        endpointConfiguration.setMessageFactory(soapMessageFactory);

        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        StringResult soapBodyResult = new StringResult();

        reset(soapMessageFactory, soapRequest, soapBody);

        expect(soapMessageFactory.createWebServiceMessage()).andReturn(soapRequest).once();
        expect(soapRequest.getSoapBody()).andReturn(soapBody).once();
        expect(soapBody.getPayloadResult()).andReturn(soapBodyResult).once();

        replay(soapMessageFactory, soapRequest, soapBody);

        soapMessageConverter.convertOutbound(testMessage, endpointConfiguration);

        Assert.assertEquals(soapBodyResult.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + requestPayload);

        verify(soapMessageFactory, soapRequest, soapBody);
    }

    @Test
    public void testOutboundSoapBody() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(requestPayload);

        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        StringResult soapBodyResult = new StringResult();

        reset(soapRequest, soapBody);

        expect(soapRequest.getSoapBody()).andReturn(soapBody).once();
        expect(soapBody.getPayloadResult()).andReturn(soapBodyResult).once();

        replay(soapRequest, soapBody);

        soapMessageConverter.convertOutbound(soapRequest, testMessage, new WebServiceEndpointConfiguration());

        Assert.assertEquals(soapBodyResult.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + requestPayload);

        verify(soapRequest, soapBody);
    }

    @Test
    public void testOutboundSoapAction() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(requestPayload)
                .setHeader(SoapMessageHeaders.SOAP_ACTION, "soapAction");

        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        reset(soapRequest, soapBody);

        expect(soapRequest.getSoapBody()).andReturn(soapBody).once();
        expect(soapBody.getPayloadResult()).andReturn(new StringResult()).once();

        soapRequest.setSoapAction("soapAction");
        expectLastCall().once();

        replay(soapRequest, soapBody);

        soapMessageConverter.convertOutbound(soapRequest, testMessage, new WebServiceEndpointConfiguration());

        verify(soapRequest, soapBody);
    }

    @Test
    public void testOutboundSoapHeaderContent() throws TransformerException, IOException {
        String soapHeaderContent = "<header>" +
                "<operation>unitTest</operation>" +
                "<messageId>123456789</messageId>" +
                "</header>";

        Message testMessage = new DefaultMessage(requestPayload)
                .addHeaderData(soapHeaderContent);

        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        StringResult soapHeaderResult = new StringResult();

        reset(soapRequest, soapBody, soapHeader);

        expect(soapRequest.getSoapBody()).andReturn(soapBody).once();
        expect(soapBody.getPayloadResult()).andReturn(new StringResult()).once();

        expect(soapRequest.getSoapHeader()).andReturn(soapHeader).once();
        expect(soapHeader.getResult()).andReturn(soapHeaderResult).once();

        replay(soapRequest, soapBody, soapHeader);

        soapMessageConverter.convertOutbound(soapRequest, testMessage, new WebServiceEndpointConfiguration());

        Assert.assertEquals(soapHeaderResult.toString(), soapHeaderContent);

        verify(soapRequest, soapBody, soapHeader);
    }

    @Test
    public void testMultipleOutboundSoapHeaderContent() throws TransformerException, IOException {
        String soapHeaderContent = "<header>" +
                "<operation>unitTest</operation>" +
                "<messageId>123456789</messageId>" +
                "</header>";

        Message testMessage = new DefaultMessage(requestPayload)
                .addHeaderData(soapHeaderContent)
                .addHeaderData("<AppInfo><appId>123456789</appId></AppInfo>");

        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        StringResult soapHeaderResult = new StringResult();

        reset(soapRequest, soapBody, soapHeader);

        expect(soapRequest.getSoapBody()).andReturn(soapBody).once();
        expect(soapBody.getPayloadResult()).andReturn(new StringResult()).once();

        expect(soapRequest.getSoapHeader()).andReturn(soapHeader).times(2);
        expect(soapHeader.getResult()).andReturn(soapHeaderResult).times(2);

        replay(soapRequest, soapBody, soapHeader);

        soapMessageConverter.convertOutbound(soapRequest, testMessage, new WebServiceEndpointConfiguration());

        Assert.assertEquals(soapHeaderResult.toString(), soapHeaderContent + "<AppInfo><appId>123456789</appId></AppInfo>");

        verify(soapRequest, soapBody, soapHeader);
    }

    @Test
    public void testOutboundSoapHeader() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(requestPayload)
                .setHeader("operation", "unitTest")
                .setHeader("messageId", "123456789");

        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        SoapHeaderElement soapHeaderElement = EasyMock.createMock(SoapHeaderElement.class);

        reset(soapRequest, soapBody, soapHeader, soapHeaderElement);

        expect(soapRequest.getSoapBody()).andReturn(soapBody).once();
        expect(soapBody.getPayloadResult()).andReturn(new StringResult()).once();

        expect(soapRequest.getSoapHeader()).andReturn(soapHeader).times(2);
        expect(soapHeader.addHeaderElement(eq(QNameUtils.createQName("", "operation", "")))).andReturn(soapHeaderElement).once();
        expect(soapHeader.addHeaderElement(eq(QNameUtils.createQName("", "messageId", "")))).andReturn(soapHeaderElement).once();

        soapHeaderElement.setText("unitTest");
        expectLastCall().once();

        soapHeaderElement.setText("123456789");
        expectLastCall().once();

        replay(soapRequest, soapBody, soapHeader, soapHeaderElement);

        soapMessageConverter.convertOutbound(soapRequest, testMessage, new WebServiceEndpointConfiguration());

        verify(soapRequest, soapBody, soapHeader, soapHeaderElement);
    }

    @Test
    public void testOutboundSoapHeaderQNameString() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(requestPayload)
                .setHeader("{http://www.citrus.com}citrus:operation", "unitTest")
                .setHeader("{http://www.citrus.com}citrus:messageId", "123456789");

        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        SoapHeaderElement soapHeaderElement = EasyMock.createMock(SoapHeaderElement.class);

        reset(soapRequest, soapBody, soapHeader, soapHeaderElement);

        expect(soapRequest.getSoapBody()).andReturn(soapBody).once();
        expect(soapBody.getPayloadResult()).andReturn(new StringResult()).once();

        expect(soapRequest.getSoapHeader()).andReturn(soapHeader).times(2);
        expect(soapHeader.addHeaderElement(eq(QNameUtils.createQName("http://www.citrus.com", "operation", "citrus")))).andReturn(soapHeaderElement).once();
        expect(soapHeader.addHeaderElement(eq(QNameUtils.createQName("http://www.citrus.com", "messageId", "citrus")))).andReturn(soapHeaderElement).once();

        soapHeaderElement.setText("unitTest");
        expectLastCall().once();

        soapHeaderElement.setText("123456789");
        expectLastCall().once();

        replay(soapRequest, soapBody, soapHeader, soapHeaderElement);

        soapMessageConverter.convertOutbound(soapRequest, testMessage, new WebServiceEndpointConfiguration());

        verify(soapRequest, soapBody, soapHeader, soapHeaderElement);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testOutboundSoapMimeHeader() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(requestPayload)
                .setHeader(SoapMessageHeaders.HTTP_PREFIX + "operation", "unitTest")
                .setHeader(SoapMessageHeaders.HTTP_PREFIX + "messageId", "123456789");

        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        SaajSoapMessage saajSoapRequest = EasyMock.createMock(SaajSoapMessage.class);
        SoapEnvelope soapEnvelope = EasyMock.createMock(SoapEnvelope.class);
        SOAPMessage saajMessage = EasyMock.createMock(SOAPMessage.class);

        MimeHeaders mimeHeaders = new MimeHeaders();

        reset(saajSoapRequest, soapBody, soapHeader, soapEnvelope, saajMessage);

        expect(saajSoapRequest.getEnvelope()).andReturn(soapEnvelope).once();

        expect(soapEnvelope.getBody()).andReturn(soapBody).once();
        expect(soapBody.getPayloadResult()).andReturn(new StringResult()).once();

        expect(saajSoapRequest.getSaajMessage()).andReturn(saajMessage).times(2);

        expect(saajMessage.getMimeHeaders()).andReturn(mimeHeaders).times(2);

        replay(saajSoapRequest, soapBody, soapHeader, soapEnvelope, saajMessage);

        soapMessageConverter.convertOutbound(saajSoapRequest, testMessage, new WebServiceEndpointConfiguration());

        Iterator it = mimeHeaders.getAllHeaders();
        Assert.assertEquals(((MimeHeader)it.next()).getName(), "operation");
        Assert.assertEquals(((MimeHeader)it.next()).getValue(), "123456789");
        Assert.assertFalse(it.hasNext());

        verify(saajSoapRequest, soapBody, soapHeader, soapEnvelope, saajMessage);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testOutboundSoapMimeHeaderSkipped() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(requestPayload)
                .setHeader(SoapMessageHeaders.HTTP_PREFIX + "operation", "unitTest")
                .setHeader(SoapMessageHeaders.HTTP_PREFIX + "messageId", "123456789");

        WebServiceEndpointConfiguration endpointConfiguration = new WebServiceEndpointConfiguration();
        endpointConfiguration.setHandleMimeHeaders(false);
        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        SaajSoapMessage saajSoapRequest = EasyMock.createMock(SaajSoapMessage.class);
        SoapEnvelope soapEnvelope = EasyMock.createMock(SoapEnvelope.class);
        SOAPMessage saajMessage = EasyMock.createMock(SOAPMessage.class);

        reset(saajSoapRequest, soapBody, soapHeader, soapEnvelope, saajMessage);

        expect(saajSoapRequest.getEnvelope()).andReturn(soapEnvelope).once();

        expect(soapEnvelope.getBody()).andReturn(soapBody).once();
        expect(soapBody.getPayloadResult()).andReturn(new StringResult()).once();

        replay(saajSoapRequest, soapBody, soapHeader, soapEnvelope, saajMessage);

        soapMessageConverter.convertOutbound(saajSoapRequest, testMessage, endpointConfiguration);

        verify(saajSoapRequest, soapBody, soapHeader, soapEnvelope, saajMessage);
    }

    @Test
    public void testOutboundSoapAttachment() throws TransformerException, IOException {
        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("attContentId");
        attachment.setContent("This is a SOAP attachment\nwith multi-line");
        attachment.setContentType("plain/text");

        SoapMessage testMessage = new SoapMessage(requestPayload);
        testMessage.addAttachment(attachment);

        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        reset(soapRequest, soapBody);

        expect(soapRequest.getSoapBody()).andReturn(soapBody).once();
        expect(soapBody.getPayloadResult()).andReturn(new StringResult()).once();

        expect(soapRequest.addAttachment(eq(attachment.getContentId()), (InputStreamSource)anyObject(), eq(attachment.getContentType()))).andAnswer(new IAnswer<Attachment>() {
            public Attachment answer() throws Throwable {
                InputStreamSource contentStream = (InputStreamSource)EasyMock.getCurrentArguments()[1];
                BufferedReader reader = new BufferedReader(new InputStreamReader(contentStream.getInputStream()));

                Assert.assertEquals(reader.readLine(), "This is a SOAP attachment");
                Assert.assertEquals(reader.readLine(), "with multi-line");

                reader.close();
                return null;
            }
        }).once();

        replay(soapRequest, soapBody);

        soapMessageConverter.convertOutbound(soapRequest, testMessage, new WebServiceEndpointConfiguration());

        verify(soapRequest, soapBody);
    }

    @Test
    public void testInboundSoapBody() throws TransformerException, IOException {
        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        StringSource soapBodySource = new StringSource(responsePayload);

        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        Set<Attachment> soapAttachments = new HashSet<Attachment>();

        reset(soapResponse, soapEnvelope, soapBody, soapHeader);

        expect(soapResponse.getEnvelope()).andReturn(soapEnvelope).anyTimes();
        expect(soapResponse.getPayloadSource()).andReturn(soapBodySource).times(2);
        expect(soapResponse.getSoapHeader()).andReturn(soapHeader).anyTimes();
        expect(soapEnvelope.getHeader()).andReturn(soapHeader).anyTimes();
        expect(soapHeader.examineAllHeaderElements()).andReturn(soapHeaders.iterator()).once();
        expect(soapHeader.getSource()).andReturn(null).once();

        expect(soapResponse.getAttachments()).andReturn(soapAttachments.iterator()).once();

        expect(soapResponse.getSoapAction()).andReturn("").anyTimes();

        replay(soapResponse, soapEnvelope, soapBody, soapHeader);

        Message responseMessage = soapMessageConverter.convertInbound(soapResponse, new WebServiceEndpointConfiguration());
        Assert.assertEquals(responseMessage.getPayload(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + responsePayload);
        Assert.assertNull(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION));
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);

        verify(soapResponse, soapEnvelope, soapBody, soapHeader);
    }

    @Test
    public void testInboundSoapAction() throws TransformerException, IOException {
        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        StringSource soapBodySource = new StringSource(responsePayload);

        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        Set<Attachment> soapAttachments = new HashSet<Attachment>();

        reset(soapResponse, soapEnvelope, soapBody, soapHeader);

        expect(soapResponse.getEnvelope()).andReturn(soapEnvelope).anyTimes();
        expect(soapResponse.getPayloadSource()).andReturn(soapBodySource).times(2);
        expect(soapResponse.getSoapHeader()).andReturn(soapHeader).anyTimes();
        expect(soapEnvelope.getHeader()).andReturn(soapHeader).anyTimes();
        expect(soapHeader.examineAllHeaderElements()).andReturn(soapHeaders.iterator()).once();
        expect(soapHeader.getSource()).andReturn(null).once();

        expect(soapResponse.getSoapAction()).andReturn("soapOperation").anyTimes();

        expect(soapResponse.getAttachments()).andReturn(soapAttachments.iterator()).once();

        replay(soapResponse, soapEnvelope, soapBody, soapHeader);

        Message responseMessage = soapMessageConverter.convertInbound(soapResponse, new WebServiceEndpointConfiguration());
        Assert.assertEquals(responseMessage.getPayload(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + responsePayload);
        Assert.assertEquals(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION), "soapOperation");
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);

        verify(soapResponse, soapEnvelope, soapBody, soapHeader);
    }

    @Test
    public void testInboundSoapHeaderContent() throws TransformerException, IOException {
        String soapHeaderContent = "<header>" +
                "<operation>unitTest</operation>" +
                "<messageId>123456789</messageId>" +
                "</header>";

        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        StringSource soapBodySource = new StringSource(responsePayload);

        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        Set<Attachment> soapAttachments = new HashSet<Attachment>();

        reset(soapResponse, soapEnvelope, soapBody, soapHeader);

        expect(soapResponse.getEnvelope()).andReturn(soapEnvelope).anyTimes();
        expect(soapResponse.getPayloadSource()).andReturn(soapBodySource).times(2);
        expect(soapResponse.getSoapHeader()).andReturn(soapHeader).anyTimes();
        expect(soapEnvelope.getHeader()).andReturn(soapHeader).anyTimes();
        expect(soapHeader.examineAllHeaderElements()).andReturn(soapHeaders.iterator()).once();
        expect(soapHeader.getSource()).andReturn(new StringSource(soapHeaderContent)).times(2);

        expect(soapResponse.getSoapAction()).andReturn("\"\"").anyTimes();

        expect(soapResponse.getAttachments()).andReturn(soapAttachments.iterator()).once();

        replay(soapResponse, soapEnvelope, soapBody, soapHeader);

        Message responseMessage = soapMessageConverter.convertInbound(soapResponse, new WebServiceEndpointConfiguration());
        Assert.assertEquals(responseMessage.getPayload(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + responsePayload);
        Assert.assertEquals(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION), "");
        Assert.assertEquals(responseMessage.getHeaderData().size(), 1L);
        Assert.assertEquals(responseMessage.getHeaderData().get(0), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + soapHeaderContent);

        verify(soapResponse, soapEnvelope, soapBody, soapHeader);
    }

    @Test
    public void testInboundSoapHeader() throws TransformerException, IOException {
        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        StringSource soapBodySource = new StringSource(responsePayload);

        SoapHeaderElement soapHeaderElement = EasyMock.createMock(SoapHeaderElement.class);

        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        soapHeaders.add(soapHeaderElement);

        Set<Attachment> soapAttachments = new HashSet<Attachment>();

        reset(soapResponse, soapEnvelope, soapBody, soapHeader, soapHeaderElement);

        expect(soapResponse.getEnvelope()).andReturn(soapEnvelope).anyTimes();
        expect(soapResponse.getPayloadSource()).andReturn(soapBodySource).times(2);
        expect(soapResponse.getSoapHeader()).andReturn(soapHeader).anyTimes();
        expect(soapEnvelope.getHeader()).andReturn(soapHeader).anyTimes();
        expect(soapHeader.examineAllHeaderElements()).andReturn(soapHeaders.iterator()).once();
        expect(soapHeader.getSource()).andReturn(null).once();

        expect(soapHeaderElement.getName()).andReturn(new QName("{http://citrusframework.org}citrus:messageId")).once();
        expect(soapHeaderElement.getText()).andReturn("123456789").once();

        expect(soapResponse.getSoapAction()).andReturn("soapOperation").anyTimes();

        expect(soapResponse.getAttachments()).andReturn(soapAttachments.iterator()).once();

        replay(soapResponse, soapEnvelope, soapBody, soapHeader, soapHeaderElement);

        Message responseMessage = soapMessageConverter.convertInbound(soapResponse, new WebServiceEndpointConfiguration());
        Assert.assertEquals(responseMessage.getPayload(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + responsePayload);
        Assert.assertEquals(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION), "soapOperation");
        Assert.assertEquals(responseMessage.getHeader("{http://citrusframework.org}citrus:messageId"), "123456789");
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);

        verify(soapResponse, soapEnvelope, soapBody, soapHeader, soapHeaderElement);
    }

    @Test
    public void testInboundSoapAttachment() throws TransformerException, IOException {
        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("attContentId");
        attachment.setContent("This is a SOAP attachment" + System.getProperty("line.separator") + "with multi-line");
        attachment.setContentType("plain/text");

        SoapMessageConverter soapMessageConverter = new SoapMessageConverter();

        StringSource soapBodySource = new StringSource(responsePayload);

        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        Set<Attachment> soapAttachments = new HashSet<Attachment>();
        soapAttachments.add(attachment);

        reset(soapResponse, soapEnvelope, soapBody, soapHeader);

        expect(soapResponse.getEnvelope()).andReturn(soapEnvelope).anyTimes();
        expect(soapResponse.getPayloadSource()).andReturn(soapBodySource).times(2);
        expect(soapResponse.getSoapHeader()).andReturn(soapHeader).anyTimes();
        expect(soapEnvelope.getHeader()).andReturn(soapHeader).anyTimes();
        expect(soapHeader.examineAllHeaderElements()).andReturn(soapHeaders.iterator()).once();
        expect(soapHeader.getSource()).andReturn(null).once();

        expect(soapResponse.getSoapAction()).andReturn("soapOperation").anyTimes();

        expect(soapResponse.getAttachments()).andReturn(soapAttachments.iterator()).once();

        replay(soapResponse, soapEnvelope, soapBody, soapHeader);

        Message responseMessage = soapMessageConverter.convertInbound(soapResponse, new WebServiceEndpointConfiguration());
        Assert.assertTrue(SoapMessage.class.isInstance(responseMessage));

        SoapMessage soapResponseMessage = (SoapMessage) responseMessage;
        Assert.assertEquals(soapResponseMessage.getPayload(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + responsePayload);
        Assert.assertEquals(soapResponseMessage.getSoapAction(), "soapOperation");
        Assert.assertEquals(soapResponseMessage.getHeaderData().size(), 0L);

        List<SoapAttachment> attachments = soapResponseMessage.getAttachments();
        Assert.assertEquals(attachments.size(), 1L);
        Assert.assertEquals(attachments.get(0).getContentId(), attachment.getContentId());
        Assert.assertEquals(attachments.get(0).getContentType(), attachment.getContentType());
        Assert.assertEquals(FileUtils.readToString(attachments.get(0).getInputStream()), attachment.getContent());

        verify(soapResponse, soapEnvelope, soapBody, soapHeader);
    }
}
