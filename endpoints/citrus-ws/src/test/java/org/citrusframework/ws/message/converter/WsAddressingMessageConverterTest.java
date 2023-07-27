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

import java.io.IOException;
import java.util.Collections;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.ws.addressing.WsAddressingHeaders;
import org.citrusframework.ws.addressing.WsAddressingMessageHeaders;
import org.citrusframework.ws.addressing.WsAddressingVersion;
import org.citrusframework.ws.client.WebServiceEndpointConfiguration;
import org.citrusframework.xml.StringResult;
import org.mockito.Mockito;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class WsAddressingMessageConverterTest extends AbstractTestNGUnitTest {

    private final SoapMessage soapRequest = Mockito.mock(SoapMessage.class);
    private final SoapBody soapBody = Mockito.mock(SoapBody.class);
    private final SoapHeader soapHeader = Mockito.mock(SoapHeader.class);

    private final String requestPayload = "<testMessage>Hello</testMessage>";

    private WsAddressingMessageConverter messageConverter;

    @BeforeMethod
    public void setup() {
        WsAddressingHeaders wsAddressingHeaders = new WsAddressingHeaders();
        wsAddressingHeaders.setVersion(WsAddressingVersion.VERSION10);
        wsAddressingHeaders.setAction("wsAddressing");
        wsAddressingHeaders.setFrom("Citrus");
        wsAddressingHeaders.setTo("Test");
        wsAddressingHeaders.setMessageId("urn:uuid:aae36050-2853-4ca8-b879-fe366f97c5a1");

        messageConverter = new WsAddressingMessageConverter(wsAddressingHeaders);
    }

    @Test
    public void testOutboundWsAddressingHeaders() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(requestPayload);

        StringResult soapBodyResult = new StringResult();
        StringResult soapHeaderResult = new StringResult();

        SoapHeaderElement soapHeaderElement = Mockito.mock(SoapHeaderElement.class);

        reset(soapRequest, soapBody, soapHeader, soapHeaderElement);

        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(soapBodyResult);
        when(soapRequest.getSoapHeader()).thenReturn(soapHeader);

        when(soapHeader.addHeaderElement(eq(new QName("http://www.w3.org/2005/08/addressing", "To", "")))).thenReturn(soapHeaderElement);
        when(soapHeader.addHeaderElement(eq(new QName("http://www.w3.org/2005/08/addressing", "From", "")))).thenReturn(soapHeaderElement);
        when(soapHeader.addHeaderElement(eq(new QName("http://www.w3.org/2005/08/addressing", "Action", "")))).thenReturn(soapHeaderElement);
        when(soapHeader.addHeaderElement(eq(new QName("http://www.w3.org/2005/08/addressing", "MessageID", "")))).thenReturn(soapHeaderElement);
        when(soapHeader.examineAllHeaderElements()).thenReturn(Collections.emptyIterator());

        when(soapHeaderElement.getResult()).thenReturn(new StringResult());

        when(soapHeader.getResult()).thenReturn(soapHeaderResult);

        messageConverter.convertOutbound(soapRequest, testMessage, new WebServiceEndpointConfiguration(), context);

        Assert.assertEquals(soapBodyResult.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + requestPayload);
        Assert.assertEquals(soapHeaderResult.toString(), "");

        verify(soapHeader).addNamespaceDeclaration("wsa", "http://www.w3.org/2005/08/addressing");
        verify(soapHeaderElement).setText("Test");
        verify(soapHeaderElement).setText("wsAddressing");
        verify(soapHeaderElement).setText("urn:uuid:aae36050-2853-4ca8-b879-fe366f97c5a1");
    }

    @Test
    public void testOverwriteWsAddressingHeaders() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(requestPayload)
                .setHeader(WsAddressingMessageHeaders.FROM, "customFrom")
                .setHeader(WsAddressingMessageHeaders.TO, "customTo")
                .setHeader(WsAddressingMessageHeaders.ACTION, "customAction")
                .setHeader(WsAddressingMessageHeaders.MESSAGE_ID, "${messageId}");

        context.setVariable("messageId", "urn:custom");

        StringResult soapBodyResult = new StringResult();
        StringResult soapHeaderResult = new StringResult();

        SoapHeaderElement soapHeaderElement = Mockito.mock(SoapHeaderElement.class);

        reset(soapRequest, soapBody, soapHeader, soapHeaderElement);

        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(soapBodyResult);
        when(soapRequest.getSoapHeader()).thenReturn(soapHeader);

        when(soapHeader.addHeaderElement(eq(new QName("http://www.w3.org/2005/08/addressing", "To", "")))).thenReturn(soapHeaderElement);
        when(soapHeader.addHeaderElement(eq(new QName("http://www.w3.org/2005/08/addressing", "From", "")))).thenReturn(soapHeaderElement);
        when(soapHeader.addHeaderElement(eq(new QName("http://www.w3.org/2005/08/addressing", "Action", "")))).thenReturn(soapHeaderElement);
        when(soapHeader.addHeaderElement(eq(new QName("http://www.w3.org/2005/08/addressing", "MessageID", "")))).thenReturn(soapHeaderElement);
        when(soapHeader.examineAllHeaderElements()).thenReturn(Collections.emptyIterator());

        when(soapHeaderElement.getResult()).thenReturn(new StringResult());

        when(soapHeader.getResult()).thenReturn(soapHeaderResult);

        messageConverter.convertOutbound(soapRequest, testMessage, new WebServiceEndpointConfiguration(), context);

        Assert.assertEquals(soapBodyResult.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + requestPayload);
        Assert.assertEquals(soapHeaderResult.toString(), "");

        verify(soapHeader).addNamespaceDeclaration("wsa", "http://www.w3.org/2005/08/addressing");
        verify(soapHeaderElement).setText("customTo");
        verify(soapHeaderElement).setText("customAction");
        verify(soapHeaderElement).setText("urn:custom");
    }
}
