/*
 * Copyright the original author or authors.
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

import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.MessageType;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 2.6
 */
public class SoapFormattingMessageProcessorTest extends AbstractUnitTest {

    private SoapFormattingMessageProcessor messageProcessor = new SoapFormattingMessageProcessor();

    @Test
    public void testProcessMessage() {
        SoapMessage message = new SoapMessage("<root>"
                    + "<element attribute='attribute-value'>"
                        + "<sub-element>text-value</sub-element>"
                    + "</element>"
                + "</root>");
        message.setType(MessageType.XML.name());

        messageProcessor.process(message, context);

        Assertions.assertTrue(message.getPayload(String.class).contains("\n"));
    }

    @Test
    public void testProcessSoapFault() {
        SoapFault message = new SoapFault("<root>"
                    + "<element attribute='attribute-value'>"
                        + "<sub-element>text-value</sub-element>"
                    + "</element>"
                + "</root>");
        message.setType(MessageType.XML.name());

        message.addFaultDetail("<fault-detail><error>Something went wrong</error></fault-detail>");

        messageProcessor.process(message, context);

        Assertions.assertTrue(message.getPayload(String.class).contains("\n"));
        Assertions.assertEquals(1L, message.getFaultDetails().size());
        Assertions.assertTrue(message.getFaultDetails().get(0).contains("\n"));
    }

    @Test
    public void testProcessNonXmlMessage() {
        Message message = new DefaultMessage("This is plaintext");
        message.setType(MessageType.PLAINTEXT.name());

        messageProcessor.process(message, context);
        Assertions.assertEquals("This is plaintext", message.getPayload(String.class));
    }

    @Test
    public void testBuilder() {
        SoapFormattingMessageProcessor processor = new SoapFormattingMessageProcessor.Builder().build();

        SoapMessage message = new SoapMessage("<root>"
                    + "<element attribute='attribute-value'>"
                        + "<sub-element>text-value</sub-element>"
                    + "</element>"
                + "</root>");
        message.setType(MessageType.XML.name());

        processor.process(message, context);

        Assertions.assertTrue(message.getPayload(String.class).contains("\n"));
    }

    @Test
    public void testLookup() {
        MessageProcessor.Builder<?, ?> builder = MessageProcessor.lookup("soapPrettyPrint")
                .orElse(null);

        Assertions.assertNotNull(builder);
        Assertions.assertEquals(SoapFormattingMessageProcessor.Builder.class, builder.getClass());
    }
}
