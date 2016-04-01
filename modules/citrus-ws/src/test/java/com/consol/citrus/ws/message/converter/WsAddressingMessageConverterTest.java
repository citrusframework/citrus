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
import com.consol.citrus.ws.addressing.WsAddressingHeaders;
import com.consol.citrus.ws.addressing.WsAddressingVersion;
import com.consol.citrus.ws.client.WebServiceEndpointConfiguration;
import org.mockito.Mockito;
import org.springframework.ws.soap.*;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.transform.StringResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.transform.TransformerException;
import java.io.IOException;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class WsAddressingMessageConverterTest extends AbstractTestNGUnitTest {

    private SoapMessage soapRequest = Mockito.mock(SoapMessage.class);
    private SoapBody soapBody = Mockito.mock(SoapBody.class);
    private SoapHeader soapHeader = Mockito.mock(SoapHeader.class);

    private String requestPayload = "<testMessage>Hello</testMessage>";

    @Test
    public void testOutboundWsAddressingHeaders() throws TransformerException, IOException {
        Message testMessage = new DefaultMessage(requestPayload);

        WsAddressingHeaders wsAddressingHeaders = new WsAddressingHeaders();
        wsAddressingHeaders.setVersion(WsAddressingVersion.VERSION10);
        wsAddressingHeaders.setAction("wsAddressing");
        wsAddressingHeaders.setFrom("Citrus");
        wsAddressingHeaders.setTo("Test");
        wsAddressingHeaders.setMessageId("urn:uuid:aae36050-2853-4ca8-b879-fe366f97c5a1");

        WsAddressingMessageConverter messageConverter = new WsAddressingMessageConverter(wsAddressingHeaders);

        StringResult soapBodyResult = new StringResult();
        StringResult soapHeaderResult = new StringResult();

        SoapHeaderElement soapHeaderElement = Mockito.mock(SoapHeaderElement.class);

        reset(soapRequest, soapBody, soapHeader, soapHeaderElement);

        when(soapRequest.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getPayloadResult()).thenReturn(soapBodyResult);
        when(soapRequest.getSoapHeader()).thenReturn(soapHeader);

        when(soapHeader.addHeaderElement(eq(QNameUtils.createQName("http://www.w3.org/2005/08/addressing", "To", "")))).thenReturn(soapHeaderElement);
        when(soapHeader.addHeaderElement(eq(QNameUtils.createQName("http://www.w3.org/2005/08/addressing", "From", "")))).thenReturn(soapHeaderElement);
        when(soapHeader.addHeaderElement(eq(QNameUtils.createQName("http://www.w3.org/2005/08/addressing", "Action", "")))).thenReturn(soapHeaderElement);
        when(soapHeader.addHeaderElement(eq(QNameUtils.createQName("http://www.w3.org/2005/08/addressing", "MessageID", "")))).thenReturn(soapHeaderElement);

        when(soapHeaderElement.getResult()).thenReturn(new StringResult());

        when(soapHeader.getResult()).thenReturn(soapHeaderResult);

        messageConverter.convertOutbound(soapRequest, testMessage, new WebServiceEndpointConfiguration(), context);

        Assert.assertEquals(soapBodyResult.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + requestPayload);
        Assert.assertEquals(soapHeaderResult.toString(), "");

        verify(soapHeader).addNamespaceDeclaration("wsa", "http://www.w3.org/2005/08/addressing");
        verify(soapHeaderElement).setText("Test");
        verify(soapHeaderElement).setMustUnderstand(true);
        verify(soapHeaderElement).setText("wsAddressing");
        verify(soapHeaderElement).setText("urn:uuid:aae36050-2853-4ca8-b879-fe366f97c5a1");
    }
}
