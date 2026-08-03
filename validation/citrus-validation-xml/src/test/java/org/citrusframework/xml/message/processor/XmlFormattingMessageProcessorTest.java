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

package org.citrusframework.xml.message.processor;

import javax.xml.parsers.DocumentBuilderFactory;

import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.StringReader;

/**
 * @since 2.6.2
 */
public class XmlFormattingMessageProcessorTest extends AbstractTestNGUnitTest {

    private XmlFormattingMessageProcessor messageProcessor = new XmlFormattingMessageProcessor();

    @Test
    public void testProcessMessage() {
        Message message = new DefaultMessage("<root>"
                    + "<element attribute='attribute-value'>"
                        + "<sub-element>text-value</sub-element>"
                    + "</element>"
                + "</root>");
        messageProcessor.process(message, context);

        Assert.assertTrue(message.getPayload(String.class).contains("\n"));
    }

    @Test
    public void testProcessMessageExplicitType() {
        Message message = new DefaultMessage("<root>"
                    + "<element attribute='attribute-value'>"
                        + "<sub-element>text-value</sub-element>"
                    + "</element>"
                + "</root>");
        message.setType(MessageType.XML.name());
        messageProcessor.process(message, context);

        Assert.assertTrue(message.getPayload(String.class).contains("\n"));
    }

    @Test
    public void testProcessDocumentPayload() throws Exception {
        String xml = "<root><element attribute='attribute-value'><sub-element>text-value</sub-element></element></root>";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        Document document = factory.newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));

        Message message = new DefaultMessage(document);
        message.setType(MessageType.XML.name());
        messageProcessor.process(message, context);

        Assert.assertTrue(message.getPayload() instanceof String);
        String result = message.getPayload(String.class);
        Assert.assertTrue(result.contains("root"), "Expected 'root' in: " + result);
        Assert.assertTrue(result.contains("sub-element"), "Expected 'sub-element' in: " + result);
        Assert.assertTrue(result.contains("text-value"), "Expected 'text-value' in: " + result);
    }

    @Test
    public void testProcessNonXmlMessage() {
        Message message = new DefaultMessage("This is plaintext");
        message.setType(MessageType.PLAINTEXT.name());
        messageProcessor.process(message, context);
        Assert.assertEquals(message.getPayload(String.class), "This is plaintext");
    }

    @Test
    public void testBuilder() {
        XmlFormattingMessageProcessor processor = new XmlFormattingMessageProcessor.Builder().build();

        Message message = new DefaultMessage("<root>"
                    + "<element attribute='attribute-value'>"
                        + "<sub-element>text-value</sub-element>"
                    + "</element>"
                + "</root>");
        processor.process(message, context);

        Assert.assertTrue(message.getPayload(String.class).contains("\n"));
    }

    @Test
    public void testLookup() {
        MessageProcessor.Builder<?, ?> builder = MessageProcessor.lookup("xmlPrettyPrint")
                .orElse(null);

        Assert.assertNotNull(builder);
        Assert.assertEquals(builder.getClass(), XmlFormattingMessageProcessor.Builder.class);
    }
}
