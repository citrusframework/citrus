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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.ws.client.WebServiceEndpointConfiguration;
import org.citrusframework.ws.message.SoapAttachment;
import org.citrusframework.ws.message.SoapMessageHeaders;
import org.citrusframework.xml.StringResult;
import jakarta.xml.soap.MimeHeader;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPMessage;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.InputStreamSource;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class SoapRequestMessageCallbackTest extends AbstractTestNGUnitTest {

    private final SoapMessage soapRequest = Mockito.mock(SoapMessage.class);
    private final SoapBody soapBody = Mockito.mock(SoapBody.class);
    private final SoapHeader soapHeader = Mockito.mock(SoapHeader.class);

    private final String requestPayload = "<testMessage>Hello</testMessage>";

    @Test
    public void testSoapBody() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(requestPayload);

        SoapRequestMessageCallback callback = new SoapRequestMessageCallback(testMessage, new WebServiceEndpointConfiguration(), context);

        StringResult soapBodyResult = new StringResult();

        reset(soapRequest, soapBody);

        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(soapBodyResult);


        callback.doWithMessage(soapRequest);

        Assert.assertEquals(soapBodyResult.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + requestPayload);

    }

    @Test
    public void testSoapAction() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(requestPayload)
                                                    .setHeader(SoapMessageHeaders.SOAP_ACTION, "soapAction");

        SoapRequestMessageCallback callback = new SoapRequestMessageCallback(testMessage, new WebServiceEndpointConfiguration(), context);

        reset(soapRequest, soapBody);

        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(new StringResult());

        callback.doWithMessage(soapRequest);

        verify(soapRequest).setSoapAction("soapAction");
    }

    @Test
    public void testSoapHeaderContent() throws TransformerException, IOException {
        String soapHeaderContent = "<header>" +
                            		"<operation>unitTest</operation>" +
                            		"<messageId>123456789</messageId>" +
                        		"</header>";

        Message testMessage = new DefaultMessage(requestPayload)
                                                    .addHeaderData(soapHeaderContent);

        SoapRequestMessageCallback callback = new SoapRequestMessageCallback(testMessage, new WebServiceEndpointConfiguration(), context);

        StringResult soapHeaderResult = new StringResult();

        reset(soapRequest, soapBody, soapHeader);

        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(new StringResult());

        when(soapRequest.getSoapHeader()).thenReturn(soapHeader);
        when(soapHeader.getResult()).thenReturn(soapHeaderResult);


        callback.doWithMessage(soapRequest);

        Assert.assertEquals(soapHeaderResult.toString(), soapHeaderContent);

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

        SoapRequestMessageCallback callback = new SoapRequestMessageCallback(testMessage, new WebServiceEndpointConfiguration(), context);

        StringResult soapHeaderResult = new StringResult();

        reset(soapRequest, soapBody, soapHeader);

        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(new StringResult());

        when(soapRequest.getSoapHeader()).thenReturn(soapHeader);
        when(soapHeader.getResult()).thenReturn(soapHeaderResult);

        callback.doWithMessage(soapRequest);

        Assert.assertEquals(soapHeaderResult.toString(), soapHeaderContent + "<AppInfo><appId>123456789</appId></AppInfo>");

    }

    @Test
    public void testSoapHeader() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(requestPayload)
                                                    .setHeader("operation", "unitTest")
                                                    .setHeader("messageId", "123456789");

        SoapRequestMessageCallback callback = new SoapRequestMessageCallback(testMessage, new WebServiceEndpointConfiguration(), context);

        SoapHeaderElement soapHeaderElement = Mockito.mock(SoapHeaderElement.class);

        reset(soapRequest, soapBody, soapHeader, soapHeaderElement);

        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(new StringResult());

        when(soapRequest.getSoapHeader()).thenReturn(soapHeader);
        when(soapHeader.addHeaderElement(eq(new QName("", "operation", "")))).thenReturn(soapHeaderElement);
        when(soapHeader.addHeaderElement(eq(new QName("", "messageId", "")))).thenReturn(soapHeaderElement);

        callback.doWithMessage(soapRequest);

        verify(soapHeaderElement).setText("unitTest");
        verify(soapHeaderElement).setText("123456789");
    }

    @Test
    public void testSoapHeaderQNameString() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(requestPayload)
                                                    .setHeader("{http://www.citrus.com}citrus:operation", "unitTest")
                                                    .setHeader("{http://www.citrus.com}citrus:messageId", "123456789");

        SoapRequestMessageCallback callback = new SoapRequestMessageCallback(testMessage, new WebServiceEndpointConfiguration(), context);

        SoapHeaderElement soapHeaderElement = Mockito.mock(SoapHeaderElement.class);

        reset(soapRequest, soapBody, soapHeader, soapHeaderElement);

        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(new StringResult());

        when(soapRequest.getSoapHeader()).thenReturn(soapHeader);
        when(soapHeader.addHeaderElement(eq(new QName("http://www.citrus.com", "operation", "citrus")))).thenReturn(soapHeaderElement);
        when(soapHeader.addHeaderElement(eq(new QName("http://www.citrus.com", "messageId", "citrus")))).thenReturn(soapHeaderElement);

        callback.doWithMessage(soapRequest);

        verify(soapHeaderElement).setText("unitTest");
        verify(soapHeaderElement).setText("123456789");
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMimeHeader() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(requestPayload)
                                                    .setHeader(SoapMessageHeaders.HTTP_PREFIX + "operation", "unitTest")
                                                    .setHeader(SoapMessageHeaders.HTTP_PREFIX + "messageId", "123456789");

        SoapRequestMessageCallback callback = new SoapRequestMessageCallback(testMessage, new WebServiceEndpointConfiguration(), context);


        SaajSoapMessage saajSoapRequest = Mockito.mock(SaajSoapMessage.class);
        SoapEnvelope soapEnvelope = Mockito.mock(SoapEnvelope.class);
        SOAPMessage saajMessage = Mockito.mock(SOAPMessage.class);

        MimeHeaders mimeHeaders = new MimeHeaders();

        reset(saajSoapRequest, soapBody, soapHeader, soapEnvelope, saajMessage);

        when(saajSoapRequest.getEnvelope()).thenReturn(soapEnvelope);
        when(saajSoapRequest.getSoapBody()).thenReturn(soapBody);

        when(soapEnvelope.getBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(new StringResult());

        when(saajSoapRequest.getSaajMessage()).thenReturn(saajMessage);

        when(saajMessage.getMimeHeaders()).thenReturn(mimeHeaders);

        callback.doWithMessage(saajSoapRequest);

        Iterator it = mimeHeaders.getAllHeaders();
        Assert.assertEquals(((MimeHeader)it.next()).getName(), "operation");
        Assert.assertEquals(((MimeHeader)it.next()).getValue(), "123456789");
        Assert.assertFalse(it.hasNext());

    }

    @Test
    public void testSoapAttachment() throws TransformerException, IOException {
        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("attContentId");
        attachment.setContent("This is a SOAP attachment\nwith multi-line");
        attachment.setContentType("plain/text");

        org.citrusframework.ws.message.SoapMessage testMessage = new org.citrusframework.ws.message.SoapMessage(requestPayload)
                .addAttachment(attachment);

        SoapRequestMessageCallback callback = new SoapRequestMessageCallback(testMessage, new WebServiceEndpointConfiguration(), context);

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

        callback.doWithMessage(soapRequest);

    }
}
