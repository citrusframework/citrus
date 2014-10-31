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

import com.consol.citrus.message.*;
import com.consol.citrus.ws.message.SoapAttachment;
import com.consol.citrus.ws.client.WebServiceEndpointConfiguration;
import com.consol.citrus.ws.message.*;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.core.io.InputStreamSource;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.*;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.transform.StringResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.soap.*;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.Iterator;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class SoapRequestMessageCallbackTest {

    private SoapMessage soapRequest = EasyMock.createMock(SoapMessage.class);
    private SoapBody soapBody = EasyMock.createMock(SoapBody.class);
    private SoapHeader soapHeader = EasyMock.createMock(SoapHeader.class);
    
    private String requestPayload = "<testMessage>Hello</testMessage>";
    
    @Test
    public void testSoapBody() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(requestPayload);

        SoapRequestMessageCallback callback = new SoapRequestMessageCallback(testMessage, new WebServiceEndpointConfiguration());
        
        StringResult soapBodyResult = new StringResult();
        
        reset(soapRequest, soapBody);
        
        expect(soapRequest.getSoapBody()).andReturn(soapBody).once();
        expect(soapBody.getPayloadResult()).andReturn(soapBodyResult).once();
        
        replay(soapRequest, soapBody);
        
        callback.doWithMessage(soapRequest);
        
        Assert.assertEquals(soapBodyResult.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + requestPayload);
        
        verify(soapRequest, soapBody);
    }
    
    @Test
    public void testSoapAction() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(requestPayload)
                                                    .setHeader(SoapMessageHeaders.SOAP_ACTION, "soapAction");

        SoapRequestMessageCallback callback = new SoapRequestMessageCallback(testMessage, new WebServiceEndpointConfiguration());
        
        reset(soapRequest, soapBody);
        
        expect(soapRequest.getSoapBody()).andReturn(soapBody).once();
        expect(soapBody.getPayloadResult()).andReturn(new StringResult()).once();
        
        soapRequest.setSoapAction("soapAction");
        expectLastCall().once();
        
        replay(soapRequest, soapBody);
        
        callback.doWithMessage(soapRequest);
        
        verify(soapRequest, soapBody);
    }
    
    @Test
    public void testSoapHeaderContent() throws TransformerException, IOException {
        String soapHeaderContent = "<header>" + 
                            		"<operation>unitTest</operation>" + 
                            		"<messageId>123456789</messageId>" + 
                        		"</header>";
        
        Message testMessage = new DefaultMessage(requestPayload)
                                                    .addHeaderData(soapHeaderContent);

        SoapRequestMessageCallback callback = new SoapRequestMessageCallback(testMessage, new WebServiceEndpointConfiguration());
        
        StringResult soapHeaderResult = new StringResult();
        
        reset(soapRequest, soapBody, soapHeader);
        
        expect(soapRequest.getSoapBody()).andReturn(soapBody).once();
        expect(soapBody.getPayloadResult()).andReturn(new StringResult()).once();
        
        expect(soapRequest.getSoapHeader()).andReturn(soapHeader).once();
        expect(soapHeader.getResult()).andReturn(soapHeaderResult).once();
        
        replay(soapRequest, soapBody, soapHeader);
        
        callback.doWithMessage(soapRequest);
        
        Assert.assertEquals(soapHeaderResult.toString(), soapHeaderContent);
        
        verify(soapRequest, soapBody, soapHeader);
    }

    @Test
    public void testMultipleSoapHeaderContent() throws TransformerException, IOException {
        String soapHeaderContent = "<header>" +
                "<operation>unitTest</operation>" +
                "<messageId>123456789</messageId>" +
                "</header>";

        Message testMessage = new DefaultMessage(requestPayload)
                .addHeaderData(soapHeaderContent)
                .addHeaderData("<AppInfo><appId>123456789</appId></AppInfo>");

        SoapRequestMessageCallback callback = new SoapRequestMessageCallback(testMessage, new WebServiceEndpointConfiguration());

        StringResult soapHeaderResult = new StringResult();

        reset(soapRequest, soapBody, soapHeader);

        expect(soapRequest.getSoapBody()).andReturn(soapBody).once();
        expect(soapBody.getPayloadResult()).andReturn(new StringResult()).once();

        expect(soapRequest.getSoapHeader()).andReturn(soapHeader).times(2);
        expect(soapHeader.getResult()).andReturn(soapHeaderResult).times(2);

        replay(soapRequest, soapBody, soapHeader);

        callback.doWithMessage(soapRequest);

        Assert.assertEquals(soapHeaderResult.toString(), soapHeaderContent + "<AppInfo><appId>123456789</appId></AppInfo>");

        verify(soapRequest, soapBody, soapHeader);
    }
    
    @Test
    public void testSoapHeader() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(requestPayload)
                                                    .setHeader("operation", "unitTest")
                                                    .setHeader("messageId", "123456789");

        SoapRequestMessageCallback callback = new SoapRequestMessageCallback(testMessage, new WebServiceEndpointConfiguration());
        
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
        
        callback.doWithMessage(soapRequest);
        
        verify(soapRequest, soapBody, soapHeader, soapHeaderElement);
    }
    
    @Test
    public void testSoapHeaderQNameString() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(requestPayload)
                                                    .setHeader("{http://www.citrus.com}citrus:operation", "unitTest")
                                                    .setHeader("{http://www.citrus.com}citrus:messageId", "123456789");

        SoapRequestMessageCallback callback = new SoapRequestMessageCallback(testMessage, new WebServiceEndpointConfiguration());
        
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
        
        callback.doWithMessage(soapRequest);
        
        verify(soapRequest, soapBody, soapHeader, soapHeaderElement);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMimeHeader() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(requestPayload)
                                                    .setHeader(SoapMessageHeaders.HTTP_PREFIX + "operation", "unitTest")
                                                    .setHeader(SoapMessageHeaders.HTTP_PREFIX + "messageId", "123456789");

        SoapRequestMessageCallback callback = new SoapRequestMessageCallback(testMessage, new WebServiceEndpointConfiguration());
        
        
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
        
        callback.doWithMessage(saajSoapRequest);

        Iterator it = mimeHeaders.getAllHeaders();
        Assert.assertEquals(((MimeHeader)it.next()).getName(), "operation");
        Assert.assertEquals(((MimeHeader)it.next()).getValue(), "123456789");
        Assert.assertFalse(it.hasNext());
        
        verify(saajSoapRequest, soapBody, soapHeader, soapEnvelope, saajMessage);
    }
    
    @Test
    public void testSoapAttachment() throws TransformerException, IOException {
        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("attContentId");
        attachment.setContent("This is a SOAP attachment\nwith multi-line");
        attachment.setContentType("plain/text");

        com.consol.citrus.ws.message.SoapMessage testMessage = new com.consol.citrus.ws.message.SoapMessage(requestPayload)
                .addAttachment(attachment);

        SoapRequestMessageCallback callback = new SoapRequestMessageCallback(testMessage, new WebServiceEndpointConfiguration());
        
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
        
        callback.doWithMessage(soapRequest);
        
        verify(soapRequest, soapBody);
    }
}
