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

package com.consol.citrus.ws.message.callback;

import com.consol.citrus.message.Message;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.message.SoapAttachment;
import com.consol.citrus.ws.client.WebServiceEndpointConfiguration;
import com.consol.citrus.ws.message.SoapMessage;
import com.consol.citrus.ws.message.SoapMessageHeaders;
import org.easymock.EasyMock;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.*;
import org.springframework.xml.transform.StringSource;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class SoapResponseMessageCallbackTest {

    private org.springframework.ws.soap.SoapMessage soapResponse = EasyMock.createMock(org.springframework.ws.soap.SoapMessage.class);
    private SoapEnvelope soapEnvelope = EasyMock.createMock(SoapEnvelope.class);
    private SoapBody soapBody = EasyMock.createMock(SoapBody.class);
    private SoapHeader soapHeader = EasyMock.createMock(SoapHeader.class);

    private String responsePayload = "<testMessage>Hello</testMessage>";

    private String soapResponsePayload = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "<SOAP-ENV:Header/>\n" +
            "<SOAP-ENV:Body>\n" +
            responsePayload +
            "</SOAP-ENV:Body>\n" +
            "</SOAP-ENV:Envelope>";
    
    @Test
    public void testSoapBody() throws TransformerException, IOException {
        SoapResponseMessageCallback callback = new SoapResponseMessageCallback(new WebServiceEndpointConfiguration());
        
        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        Set<Attachment> soapAttachments = new HashSet<Attachment>();
        
        reset(soapResponse, soapEnvelope, soapBody, soapHeader);

        expect(soapResponse.getEnvelope()).andReturn(soapEnvelope).anyTimes();
        expect(soapEnvelope.getSource()).andReturn(new StringSource(soapResponsePayload)).once();
        expect(soapResponse.getPayloadSource()).andReturn(new StringSource(responsePayload)).times(2);
        expect(soapResponse.getSoapHeader()).andReturn(soapHeader).anyTimes();
        expect(soapEnvelope.getHeader()).andReturn(soapHeader).anyTimes();
        expect(soapHeader.examineAllHeaderElements()).andReturn(soapHeaders.iterator()).once();
        expect(soapHeader.getSource()).andReturn(null).once();
        
        expect(soapResponse.getAttachments()).andReturn(soapAttachments.iterator()).once();
        
        expect(soapResponse.getSoapAction()).andReturn("").anyTimes();
        
        replay(soapResponse, soapEnvelope, soapBody, soapHeader);
        
        callback.doWithMessage(soapResponse);
        
        Message responseMessage = callback.getResponse();
        Assert.assertEquals(responseMessage.getPayload(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + responsePayload);
        Assert.assertNull(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION));
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);
        
        verify(soapResponse, soapEnvelope, soapBody, soapHeader);
    }
    
    @Test
    public void testSoapAction() throws TransformerException, IOException {
        SoapResponseMessageCallback callback = new SoapResponseMessageCallback(new WebServiceEndpointConfiguration());
        
        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        Set<Attachment> soapAttachments = new HashSet<Attachment>();
        
        reset(soapResponse, soapEnvelope, soapBody, soapHeader);
        
        expect(soapResponse.getEnvelope()).andReturn(soapEnvelope).anyTimes();
        expect(soapEnvelope.getSource()).andReturn(new StringSource(soapResponsePayload)).once();
        expect(soapResponse.getPayloadSource()).andReturn(new StringSource(responsePayload)).times(2);
        expect(soapResponse.getSoapHeader()).andReturn(soapHeader).anyTimes();
        expect(soapEnvelope.getHeader()).andReturn(soapHeader).anyTimes();
        expect(soapHeader.examineAllHeaderElements()).andReturn(soapHeaders.iterator()).once();
        expect(soapHeader.getSource()).andReturn(null).once();
        
        expect(soapResponse.getSoapAction()).andReturn("soapOperation").anyTimes();
        
        expect(soapResponse.getAttachments()).andReturn(soapAttachments.iterator()).once();
        
        replay(soapResponse, soapEnvelope, soapBody, soapHeader);
        
        callback.doWithMessage(soapResponse);
        
        Message responseMessage = callback.getResponse();
        Assert.assertEquals(responseMessage.getPayload(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + responsePayload);
        Assert.assertEquals(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION), "soapOperation");
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);
        
        verify(soapResponse, soapEnvelope, soapBody, soapHeader);
    }
    
    @Test
    public void testSoapHeaderContent() throws TransformerException, IOException {
        String soapHeaderContent = "<header>" + 
                        		"<operation>unitTest</operation>" + 
                        		"<messageId>123456789</messageId>" + 
                    		"</header>";
        
        SoapResponseMessageCallback callback = new SoapResponseMessageCallback(new WebServiceEndpointConfiguration());
        
        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        Set<Attachment> soapAttachments = new HashSet<Attachment>();
        
        reset(soapResponse, soapEnvelope, soapBody, soapHeader);

        expect(soapResponse.getEnvelope()).andReturn(soapEnvelope).anyTimes();
        expect(soapEnvelope.getSource()).andReturn(new StringSource(soapResponsePayload)).once();
        expect(soapResponse.getPayloadSource()).andReturn(new StringSource(responsePayload)).times(2);
        expect(soapResponse.getSoapHeader()).andReturn(soapHeader).anyTimes();
        expect(soapEnvelope.getHeader()).andReturn(soapHeader).anyTimes();
        expect(soapHeader.examineAllHeaderElements()).andReturn(soapHeaders.iterator()).once();
        expect(soapHeader.getSource()).andReturn(new StringSource(soapHeaderContent)).times(2);
        
        expect(soapResponse.getSoapAction()).andReturn("\"\"").anyTimes();
        
        expect(soapResponse.getAttachments()).andReturn(soapAttachments.iterator()).once();
        
        replay(soapResponse, soapEnvelope, soapBody, soapHeader);
        
        callback.doWithMessage(soapResponse);
        
        Message responseMessage = callback.getResponse();
        Assert.assertEquals(responseMessage.getPayload(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + responsePayload);
        Assert.assertEquals(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION), "");
        Assert.assertEquals(responseMessage.getHeaderData().size(), 1L);
        Assert.assertEquals(responseMessage.getHeaderData().get(0), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + soapHeaderContent);
        
        verify(soapResponse, soapEnvelope, soapBody, soapHeader);
    }
    
    @Test
    public void testSoapHeader() throws TransformerException, IOException {
        SoapResponseMessageCallback callback = new SoapResponseMessageCallback(new WebServiceEndpointConfiguration());
        
        SoapHeaderElement soapHeaderElement = EasyMock.createMock(SoapHeaderElement.class);
        
        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        soapHeaders.add(soapHeaderElement);
        
        Set<Attachment> soapAttachments = new HashSet<Attachment>();
        
        reset(soapResponse, soapEnvelope, soapBody, soapHeader, soapHeaderElement);

        expect(soapResponse.getEnvelope()).andReturn(soapEnvelope).anyTimes();
        expect(soapEnvelope.getSource()).andReturn(new StringSource(soapResponsePayload)).once();
        expect(soapResponse.getPayloadSource()).andReturn(new StringSource(responsePayload)).times(2);
        expect(soapResponse.getSoapHeader()).andReturn(soapHeader).anyTimes();
        expect(soapEnvelope.getHeader()).andReturn(soapHeader).anyTimes();
        expect(soapHeader.examineAllHeaderElements()).andReturn(soapHeaders.iterator()).once();
        expect(soapHeader.getSource()).andReturn(null).once();
        
        expect(soapHeaderElement.getName()).andReturn(new QName("{http://citrusframework.org}citrus:messageId")).once();
        expect(soapHeaderElement.getText()).andReturn("123456789").once();
        
        expect(soapResponse.getSoapAction()).andReturn("soapOperation").anyTimes();
        
        expect(soapResponse.getAttachments()).andReturn(soapAttachments.iterator()).once();
        
        replay(soapResponse, soapEnvelope, soapBody, soapHeader, soapHeaderElement);
        
        callback.doWithMessage(soapResponse);
        
        Message responseMessage = callback.getResponse();
        Assert.assertEquals(responseMessage.getPayload(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + responsePayload);
        Assert.assertEquals(responseMessage.getHeader(SoapMessageHeaders.SOAP_ACTION), "soapOperation");
        Assert.assertEquals(responseMessage.getHeader("{http://citrusframework.org}citrus:messageId"), "123456789");
        Assert.assertEquals(responseMessage.getHeaderData().size(), 0L);
        
        verify(soapResponse, soapEnvelope, soapBody, soapHeader, soapHeaderElement);
    }
    
    @Test
    public void testSoapAttachment() throws TransformerException, IOException {
        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("attContentId");
        attachment.setContent("This is a SOAP attachment" + System.getProperty("line.separator") + "with multi-line");
        attachment.setContentType("plain/text");
        
        SoapResponseMessageCallback callback = new SoapResponseMessageCallback(new WebServiceEndpointConfiguration());
        
        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        Set<Attachment> soapAttachments = new HashSet<Attachment>();
        soapAttachments.add(attachment);
        
        reset(soapResponse, soapEnvelope, soapBody, soapHeader);

        expect(soapResponse.getEnvelope()).andReturn(soapEnvelope).anyTimes();
        expect(soapEnvelope.getSource()).andReturn(new StringSource(soapResponsePayload)).once();
        expect(soapResponse.getPayloadSource()).andReturn(new StringSource(responsePayload)).times(2);
        expect(soapResponse.getSoapHeader()).andReturn(soapHeader).anyTimes();
        expect(soapEnvelope.getHeader()).andReturn(soapHeader).anyTimes();
        expect(soapHeader.examineAllHeaderElements()).andReturn(soapHeaders.iterator()).once();
        expect(soapHeader.getSource()).andReturn(null).once();
        
        expect(soapResponse.getSoapAction()).andReturn("soapOperation").anyTimes();
        
        expect(soapResponse.getAttachments()).andReturn(soapAttachments.iterator()).once();
        
        replay(soapResponse, soapEnvelope, soapBody, soapHeader);
        
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

        verify(soapResponse, soapEnvelope, soapBody, soapHeader);
    }
}
