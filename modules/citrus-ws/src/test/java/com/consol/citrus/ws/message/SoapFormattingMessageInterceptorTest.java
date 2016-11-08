/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.ws.message;

import com.consol.citrus.message.*;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class SoapFormattingMessageInterceptorTest extends AbstractTestNGUnitTest {

    private SoapFormattingMessageInterceptor messageInterceptor = new SoapFormattingMessageInterceptor();

    @Test
    public void testInterceptMessage() throws Exception {
        SoapMessage message = new SoapMessage("<root>"
                    + "<element attribute='attribute-value'>"
                        + "<sub-element>text-value</sub-element>"
                    + "</element>"
                + "</root>");

        messageInterceptor.interceptMessageConstruction(message, MessageType.XML.name(), context);

        Assert.assertTrue(message.getPayload(String.class).contains(System.lineSeparator()));
    }

    @Test
    public void testInterceptSoapFault() throws Exception {
        SoapFault message = new SoapFault("<root>"
                    + "<element attribute='attribute-value'>"
                        + "<sub-element>text-value</sub-element>"
                    + "</element>"
                + "</root>");

        message.addFaultDetail("<fault-detail><error>Something went wrong</error></fault-detail>");

        messageInterceptor.interceptMessageConstruction(message, MessageType.XML.name(), context);

        Assert.assertTrue(message.getPayload(String.class).contains(System.lineSeparator()));
        Assert.assertEquals(message.getFaultDetails().size(), 1L);
        Assert.assertTrue(message.getFaultDetails().get(0).contains(System.lineSeparator()));
    }

    @Test
    public void testInterceptNonXmlMessage() throws Exception {
        Message message = new DefaultMessage("This is plaintext");
        messageInterceptor.interceptMessageConstruction(message, MessageType.PLAINTEXT.name(), context);
        Assert.assertEquals(message.getPayload(String.class), "This is plaintext");
    }

}