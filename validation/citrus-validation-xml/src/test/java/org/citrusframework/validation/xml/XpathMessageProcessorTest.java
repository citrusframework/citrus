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

package org.citrusframework.validation.xml;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class XpathMessageProcessorTest extends AbstractUnitTest {

    private final Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage><Text>Hello World!</Text></TestMessage>");

    private final Message messageNamespace = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns0:TestMessage xmlns:ns0=\"http://citrusframework.org/test\">" +
                "<ns0:Text>Hello World!</ns0:Text>" +
            "</ns0:TestMessage>");

    @Test
    public void testConstructWithXPath() {
        final Map<String, Object> xPathExpressions = new HashMap<>();
        xPathExpressions.put("/TestMessage/Text", "Hello!");

        XpathMessageProcessor processor = new XpathMessageProcessor.Builder()
                .expressions(xPathExpressions)
                .build();
        processor.processMessage(message, context);

        Assertions.assertTrue(message.getPayload(String.class).replaceAll("\\s", "")
                .endsWith("<TestMessage><Text>Hello!</Text></TestMessage>"));
    }

    @Test
    public void testConstructWithXPathAndDefaultNamespace() {
        final Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage xmlns=\"http://citrusframework.org/test\">" +
                "<Text>Hello World!</Text>" +
                "</TestMessage>");

        final Map<String, Object> xPathExpressions = new HashMap<>();
        xPathExpressions.put("/:TestMessage/:Text", "Hello!");

        XpathMessageProcessor processor = new XpathMessageProcessor.Builder()
                .expressions(xPathExpressions)
                .build();
        processor.processMessage(message, context);

        Assertions.assertTrue(message.getPayload(String.class).replaceAll("\\s", "")
                .contains("<Text>Hello!</Text>"));
    }

    @Test
    public void testConstructWithXPathAndNamespace() {
        final Map<String, Object> xPathExpressions = new HashMap<>();
        xPathExpressions.put("/ns0:TestMessage/ns0:Text", "Hello!");

        XpathMessageProcessor processor = new XpathMessageProcessor.Builder()
                .expressions(xPathExpressions)
                .build();
        processor.processMessage(messageNamespace, context);

        Assertions.assertTrue(messageNamespace.getPayload(String.class).replaceAll("\\s", "")
                .contains("<ns0:Text>Hello!</ns0:Text>"));
    }

    @Test
    public void testConstructWithXPathAndGlobalNamespace() {
        context.getNamespaceContextBuilder().getNamespaceMappings().put("global", "http://citrusframework.org/test");

        final Map<String, Object> xPathExpressions = new HashMap<>();
        xPathExpressions.put("/global:TestMessage/global:Text", "Hello!");

        XpathMessageProcessor processor = new XpathMessageProcessor.Builder()
                .expressions(xPathExpressions)
                .build();
        processor.processMessage(messageNamespace, context);

        Assertions.assertTrue(messageNamespace.getPayload(String.class).replaceAll("\\s", "")
                .contains("<ns0:Text>Hello!</ns0:Text>"));
    }

    @Test
    public void testConstructWithXPathAndNestedNamespace() {
        final Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns0:TestMessage xmlns:ns0=\"http://citrusframework.org/test\">" +
                "<ns1:Text xmlns:ns1=\"http://citrusframework.org/test/text\">Hello World!</ns1:Text>" +
                "</ns0:TestMessage>");

        final Map<String, Object> xPathExpressions = new HashMap<>();
        xPathExpressions.put("/ns0:TestMessage/ns1:Text", "Hello!");

        XpathMessageProcessor processor = new XpathMessageProcessor.Builder()
                .expressions(xPathExpressions)
                .build();
        processor.processMessage(message, context);

        Assertions.assertTrue(message.getPayload(String.class).replaceAll("\\s", "")
                .contains("<ns1:Textxmlns:ns1=\"http://citrusframework.org/test/text\">Hello!</ns1:Text>"));
    }

    @Test
    public void testConstructWithInvalidXPath() {
        final Map<String, Object> xPathExpressions = new HashMap<>();
        xPathExpressions.put(".Invalid/Unknown", "Hello!");

        XpathMessageProcessor processor = new XpathMessageProcessor.Builder()
                .expressions(xPathExpressions)
                .build();
        CitrusRuntimeException thrown = assertThrows(CitrusRuntimeException.class, () -> processor.processMessage(message, context));
        assertThat(thrown).hasMessageMatching("Can not evaluate xpath expression.*");
    }

    @Test
    public void testConstructWithXPathNoResult() {
        final Map<String, Object> xPathExpressions = new HashMap<>();
        xPathExpressions.put("/TestMessage/Unknown", "Hello!");

        XpathMessageProcessor processor = new XpathMessageProcessor.Builder()
                .expressions(xPathExpressions)
                .build();
        CitrusRuntimeException thrown = assertThrows(CitrusRuntimeException.class, () -> processor.processMessage(message, context));
        assertThat(thrown).hasMessageMatching("No result for XPath expression.*");
    }

    @Test
    public void testConstructWithXPathAndInvalidGlobalNamespace() {
        final Map<String, Object> xPathExpressions = new HashMap<>();
        xPathExpressions.put("/global:TestMessage/global:Text", "Hello!");

        XpathMessageProcessor processor = new XpathMessageProcessor.Builder()
                .expressions(xPathExpressions)
                .build();
        CitrusRuntimeException thrown = assertThrows(CitrusRuntimeException.class, () -> processor.processMessage(messageNamespace, context));
        assertThat(thrown).hasMessageMatching("Can not evaluate xpath expression.*");
    }

    @Test
    public void testAddTextToEmptyElement(){

        //GIVEN
        final Message message = new DefaultMessage(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<TestMessage>" +
                    "<Text></Text>" +
                "</TestMessage>");
        final Map<String, Object> xPathExpression = Collections.singletonMap("//TestMessage/Text", "foobar");

        //WHEN
        XpathMessageProcessor processor = new XpathMessageProcessor.Builder()
                .expressions(xPathExpression)
                .build();
        processor.processMessage(message, context);

        //THEN
        Assertions.assertTrue(message.getPayload(String.class).replaceAll("\\s", "")
                .contains("<Text>foobar</Text>"));
    }
}
