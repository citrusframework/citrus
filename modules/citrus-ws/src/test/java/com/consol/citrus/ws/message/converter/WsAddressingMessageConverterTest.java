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
import com.consol.citrus.ws.addressing.WsAddressingHeaders;
import com.consol.citrus.ws.addressing.WsAddressingVersion;
import com.consol.citrus.ws.client.WebServiceEndpointConfiguration;
import org.easymock.EasyMock;
import org.springframework.ws.soap.*;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.transform.StringResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.transform.TransformerException;
import java.io.IOException;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class WsAddressingMessageConverterTest {

    private SoapMessage soapRequest = EasyMock.createMock(SoapMessage.class);
    private SoapBody soapBody = EasyMock.createMock(SoapBody.class);
    private SoapHeader soapHeader = EasyMock.createMock(SoapHeader.class);

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

        SoapHeaderElement soapHeaderElement = EasyMock.createMock(SoapHeaderElement.class);

        reset(soapRequest, soapBody, soapHeader, soapHeaderElement);

        expect(soapRequest.getSoapBody()).andReturn(soapBody).once();
        expect(soapBody.getPayloadResult()).andReturn(soapBodyResult).once();
        expect(soapRequest.getSoapHeader()).andReturn(soapHeader).times(1);

        soapHeader.addNamespaceDeclaration("wsa", "http://www.w3.org/2005/08/addressing");
        expectLastCall().once();

        expect(soapHeader.addHeaderElement(eq(QNameUtils.createQName("http://www.w3.org/2005/08/addressing", "To", "")))).andReturn(soapHeaderElement).once();
        expect(soapHeader.addHeaderElement(eq(QNameUtils.createQName("http://www.w3.org/2005/08/addressing", "From", "")))).andReturn(soapHeaderElement).once();
        expect(soapHeader.addHeaderElement(eq(QNameUtils.createQName("http://www.w3.org/2005/08/addressing", "Action", "")))).andReturn(soapHeaderElement).once();
        expect(soapHeader.addHeaderElement(eq(QNameUtils.createQName("http://www.w3.org/2005/08/addressing", "MessageID", "")))).andReturn(soapHeaderElement).once();

        expect(soapHeaderElement.getResult()).andReturn(new StringResult()).once();
        soapHeaderElement.setText("Test");
        expectLastCall().once();

        soapHeaderElement.setMustUnderstand(true);
        expectLastCall().once();

        soapHeaderElement.setText("wsAddressing");
        expectLastCall().once();

        soapHeaderElement.setText("urn:uuid:aae36050-2853-4ca8-b879-fe366f97c5a1");
        expectLastCall().once();

        expect(soapHeader.getResult()).andReturn(soapHeaderResult).times(2);

        replay(soapRequest, soapBody, soapHeader, soapHeaderElement);

        messageConverter.convertOutbound(soapRequest, testMessage, new WebServiceEndpointConfiguration());

        Assert.assertEquals(soapBodyResult.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + requestPayload);
        Assert.assertEquals(soapHeaderResult.toString(), "");

        verify(soapRequest, soapBody, soapHeader, soapHeaderElement);
    }
}
