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

package org.citrusframework.ws.message.callback;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.citrusframework.message.Message;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.util.FileUtils;
import org.citrusframework.ws.client.WebServiceEndpointConfiguration;
import org.citrusframework.ws.message.SoapAttachment;
import org.citrusframework.ws.message.SoapMessage;
import org.citrusframework.ws.message.SoapMessageHeaders;
import org.citrusframework.xml.StringSource;
import org.mockito.Mockito;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class SoapResponseMessageCallbackTest extends AbstractTestNGUnitTest {

    private org.springframework.ws.soap.SoapMessage soapResponse = Mockito.mock(org.springframework.ws.soap.SoapMessage.class);
    private SoapEnvelope soapEnvelope = Mockito.mock(SoapEnvelope.class);
    private SoapBody soapBody = Mockito.mock(SoapBody.class);
    private SoapHeader soapHeader = Mockito.mock(SoapHeader.class);

    private String responsePayload = "<testMessage>Hello</testMessage>";

    private String soapResponsePayload = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "<SOAP-ENV:Header/>\n" +
            "<SOAP-ENV:Body>\n" +
            responsePayload +
            "</SOAP-ENV:Body>\n" +
            "</SOAP-ENV:Envelope>";

    @Test
    public void testSoapBody() throws TransformerException, IOException {
        SoapResponseMessageCallback callback = new SoapResponseMessageCallback(new WebServiceEndpointConfiguration(), context);

        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        Set<Attachment> soapAttachments = new HashSet<Attachment>();

        reset(soapResponse, soapEnvelope, soapBody, soapHeader);

        when(soapResponse.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(soapResponsePayload));
        when(soapResponse.getPayloadSource()).thenReturn(new StringSource(responsePayload));
        when(soapResponse.getSoapHeader()).thenReturn(soapHeader);
        when(soapEnvelope.getHeader()).thenReturn(soapHeader);
        when(soapHeader.examineAllHeaderElements()).thenReturn(soapHeaders.iterator());
        when(soapHeader.getSource()).thenReturn(null);

        when(soapResponse.getAttachments()).thenReturn(soapAttachments.iterator());

        when(soapResponse.getSoapAction()).thenReturn("");


        callback.doWithMessage(soapResponse);

        Message responseMessage = callback.getResponse();
        Assert.assertEquals(responseMessage.getPayload(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + responsePayload);
        Assert.assertNull(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION));
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);

    }

    @Test
    public void testSoapAction() throws TransformerException, IOException {
        SoapResponseMessageCallback callback = new SoapResponseMessageCallback(new WebServiceEndpointConfiguration(), context);

        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        Set<Attachment> soapAttachments = new HashSet<Attachment>();

        reset(soapResponse, soapEnvelope, soapBody, soapHeader);

        when(soapResponse.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(soapResponsePayload));
        when(soapResponse.getPayloadSource()).thenReturn(new StringSource(responsePayload));
        when(soapResponse.getSoapHeader()).thenReturn(soapHeader);
        when(soapEnvelope.getHeader()).thenReturn(soapHeader);
        when(soapHeader.examineAllHeaderElements()).thenReturn(soapHeaders.iterator());
        when(soapHeader.getSource()).thenReturn(null);

        when(soapResponse.getSoapAction()).thenReturn("soapOperation");

        when(soapResponse.getAttachments()).thenReturn(soapAttachments.iterator());


        callback.doWithMessage(soapResponse);

        Message responseMessage = callback.getResponse();
        Assert.assertEquals(responseMessage.getPayload(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + responsePayload);
        Assert.assertEquals(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION), "soapOperation");
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);

    }

    @Test
    public void testSoapHeaderContent() throws TransformerException, IOException {
        String soapHeaderContent = "<header>" +
                        		"<operation>unitTest</operation>" +
                        		"<messageId>123456789</messageId>" +
                    		"</header>";

        SoapResponseMessageCallback callback = new SoapResponseMessageCallback(new WebServiceEndpointConfiguration(), context);

        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        Set<Attachment> soapAttachments = new HashSet<Attachment>();

        reset(soapResponse, soapEnvelope, soapBody, soapHeader);

        when(soapResponse.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(soapResponsePayload));
        when(soapResponse.getPayloadSource()).thenReturn(new StringSource(responsePayload));
        when(soapResponse.getSoapHeader()).thenReturn(soapHeader);
        when(soapEnvelope.getHeader()).thenReturn(soapHeader);
        when(soapHeader.examineAllHeaderElements()).thenReturn(soapHeaders.iterator());
        when(soapHeader.getSource()).thenReturn(new StringSource(soapHeaderContent));

        when(soapResponse.getSoapAction()).thenReturn("\"\"");

        when(soapResponse.getAttachments()).thenReturn(soapAttachments.iterator());


        callback.doWithMessage(soapResponse);

        Message responseMessage = callback.getResponse();
        Assert.assertEquals(responseMessage.getPayload(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + responsePayload);
        Assert.assertEquals(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION), "");
        Assert.assertEquals(responseMessage.getHeaderData().size(), 1L);
        Assert.assertEquals(responseMessage.getHeaderData().get(0), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + soapHeaderContent);

    }

    @Test
    public void testSoapHeader() throws TransformerException, IOException {
        SoapResponseMessageCallback callback = new SoapResponseMessageCallback(new WebServiceEndpointConfiguration(), context);

        SoapHeaderElement soapHeaderElement = Mockito.mock(SoapHeaderElement.class);

        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        soapHeaders.add(soapHeaderElement);

        Set<Attachment> soapAttachments = new HashSet<Attachment>();

        reset(soapResponse, soapEnvelope, soapBody, soapHeader, soapHeaderElement);

        when(soapResponse.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(soapResponsePayload));
        when(soapResponse.getPayloadSource()).thenReturn(new StringSource(responsePayload));
        when(soapResponse.getSoapHeader()).thenReturn(soapHeader);
        when(soapEnvelope.getHeader()).thenReturn(soapHeader);
        when(soapHeader.examineAllHeaderElements()).thenReturn(soapHeaders.iterator());
        when(soapHeader.getSource()).thenReturn(null);

        when(soapHeaderElement.getName()).thenReturn(new QName("{http://citrusframework.org}citrus:messageId"));
        when(soapHeaderElement.getText()).thenReturn("123456789");

        when(soapResponse.getSoapAction()).thenReturn("soapOperation");

        when(soapResponse.getAttachments()).thenReturn(soapAttachments.iterator());


        callback.doWithMessage(soapResponse);

        Message responseMessage = callback.getResponse();
        Assert.assertEquals(responseMessage.getPayload(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + responsePayload);
        Assert.assertEquals(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION), "soapOperation");
        Assert.assertEquals(responseMessage.getHeader("{http://citrusframework.org}citrus:messageId"), "123456789");
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);

    }

    @Test
    public void testSoapAttachment() throws TransformerException, IOException {
        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("attContentId");
        attachment.setContent("This is a SOAP attachment" + System.getProperty("line.separator") + "with multi-line");
        attachment.setContentType("plain/text");

        SoapResponseMessageCallback callback = new SoapResponseMessageCallback(new WebServiceEndpointConfiguration(), context);

        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        Set<Attachment> soapAttachments = new HashSet<Attachment>();
        soapAttachments.add(attachment);

        reset(soapResponse, soapEnvelope, soapBody, soapHeader);

        when(soapResponse.getEnvelope()).thenReturn(soapEnvelope);
        when(soapEnvelope.getSource()).thenReturn(new StringSource(soapResponsePayload));
        when(soapResponse.getPayloadSource()).thenReturn(new StringSource(responsePayload));
        when(soapResponse.getSoapHeader()).thenReturn(soapHeader);
        when(soapEnvelope.getHeader()).thenReturn(soapHeader);
        when(soapHeader.examineAllHeaderElements()).thenReturn(soapHeaders.iterator());
        when(soapHeader.getSource()).thenReturn(null);

        when(soapResponse.getSoapAction()).thenReturn("soapOperation");

        when(soapResponse.getAttachments()).thenReturn(soapAttachments.iterator());


        callback.doWithMessage(soapResponse);

        Message responseMessage = callback.getResponse();
        Assert.assertEquals(responseMessage.getPayload(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + responsePayload);
        Assert.assertEquals(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION), "soapOperation");
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);

        Assert.assertTrue(SoapMessage.class.isInstance(responseMessage));

        SoapMessage soapResponseMessage = (SoapMessage) responseMessage;
        Assert.assertEquals(soapResponseMessage.getAttachments().get(0).getContentId(), attachment.getContentId());
        Assert.assertEquals(soapResponseMessage.getAttachments().get(0).getContentType(), attachment.getContentType());
        Assert.assertEquals(FileUtils.readToString(soapResponseMessage.getAttachments().get(0).getInputStream()), attachment.getContent());

    }
}
