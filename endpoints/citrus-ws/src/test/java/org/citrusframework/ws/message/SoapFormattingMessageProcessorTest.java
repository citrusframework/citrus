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

package org.citrusframework.ws.message;

import org.citrusframework.message.*;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class SoapFormattingMessageProcessorTest extends AbstractTestNGUnitTest {

    private SoapFormattingMessageProcessor messageProcessor = new SoapFormattingMessageProcessor();

    @Test
    public void testProcessMessage() throws Exception {
        SoapMessage message = new SoapMessage("<root>"
                    + "<element attribute='attribute-value'>"
                        + "<sub-element>text-value</sub-element>"
                    + "</element>"
                + "</root>");
        message.setType(MessageType.XML.name());

        messageProcessor.process(message, context);

        Assert.assertTrue(message.getPayload(String.class).contains(System.lineSeparator()));
    }

    @Test
    public void testProcessSoapFault() throws Exception {
        SoapFault message = new SoapFault("<root>"
                    + "<element attribute='attribute-value'>"
                        + "<sub-element>text-value</sub-element>"
                    + "</element>"
                + "</root>");
        message.setType(MessageType.XML.name());

        message.addFaultDetail("<fault-detail><error>Something went wrong</error></fault-detail>");

        messageProcessor.process(message, context);

        Assert.assertTrue(message.getPayload(String.class).contains(System.lineSeparator()));
        Assert.assertEquals(message.getFaultDetails().size(), 1L);
        Assert.assertTrue(message.getFaultDetails().get(0).contains(System.lineSeparator()));
    }

    @Test
    public void testProcessNonXmlMessage() throws Exception {
        Message message = new DefaultMessage("This is plaintext");
        message.setType(MessageType.PLAINTEXT.name());

        messageProcessor.process(message, context);
        Assert.assertEquals(message.getPayload(String.class), "This is plaintext");
    }

}
