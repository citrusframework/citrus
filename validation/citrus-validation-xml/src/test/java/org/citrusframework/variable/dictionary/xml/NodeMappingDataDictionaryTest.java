/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.variable.dictionary.xml;

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.variable.dictionary.DataDictionary;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class NodeMappingDataDictionaryTest extends UnitTestSupport {

    @Test
    public void testTranslateExactMatchStrategy() {
        Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage><Text>Hello World!</Text><OtherText>No changes</OtherText></TestMessage>");

        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("Something.Else", "NotFound!");
        mappings.put("TestMessage.Text", "Hello!");

        NodeMappingDataDictionary dictionary = new NodeMappingDataDictionary();
        dictionary.setMappings(mappings);

        dictionary.processMessage(message, context);
        Assert.assertEquals(message.getPayload(String.class).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.getProperty("line.separator") +
                "<TestMessage>" + System.getProperty("line.separator") +
                "    <Text>Hello!</Text>" + System.getProperty("line.separator") +
                "    <OtherText>No changes</OtherText>" + System.getProperty("line.separator") +
                "</TestMessage>");
    }

    @Test
    public void testTranslateStartsWithStrategy() {
        Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage><Text>Hello World!</Text><OtherText>Good Bye!</OtherText></TestMessage>");

        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("TestMessage.Text", "Hello!");
        mappings.put("TestMessage.Other", "Bye!");

        NodeMappingDataDictionary dictionary = new NodeMappingDataDictionary();
        dictionary.setMappings(mappings);
        dictionary.setPathMappingStrategy(DataDictionary.PathMappingStrategy.STARTS_WITH);

        dictionary.processMessage(message, context);
        Assert.assertEquals(message.getPayload(String.class).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.getProperty("line.separator") +
                "<TestMessage>" + System.getProperty("line.separator") +
                "    <Text>Hello!</Text>" + System.getProperty("line.separator") +
                "    <OtherText>Bye!</OtherText>" + System.getProperty("line.separator") +
                "</TestMessage>");
    }

    @Test
    public void testTranslateEndsWithStrategy() {
        Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage><Text>Hello World!</Text><OtherText>Good Bye!</OtherText></TestMessage>");

        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("Text", "Hello!");

        NodeMappingDataDictionary dictionary = new NodeMappingDataDictionary();
        dictionary.setMappings(mappings);
        dictionary.setPathMappingStrategy(DataDictionary.PathMappingStrategy.ENDS_WITH);

        dictionary.processMessage(message, context);
        Assert.assertEquals(message.getPayload(String.class).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.getProperty("line.separator") +
                "<TestMessage>" + System.getProperty("line.separator") +
                "    <Text>Hello!</Text>" + System.getProperty("line.separator") +
                "    <OtherText>Hello!</OtherText>" + System.getProperty("line.separator") +
                "</TestMessage>");
    }

    @Test
    public void testTranslateAttributes() {
        Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage><Text name=\"helloText\">Hello World!</Text><OtherText name=\"goodbyeText\">No changes</OtherText></TestMessage>");

        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("TestMessage.Text", "Hello!");
        mappings.put("TestMessage.Text.name", "newName");

        NodeMappingDataDictionary dictionary = new NodeMappingDataDictionary();
        dictionary.setMappings(mappings);

        dictionary.processMessage(message, context);
        Assert.assertEquals(message.getPayload(String.class).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.getProperty("line.separator") +
                "<TestMessage>" + System.getProperty("line.separator") +
                "    <Text name=\"newName\">Hello!</Text>" + System.getProperty("line.separator") +
                "    <OtherText name=\"goodbyeText\">No changes</OtherText>" + System.getProperty("line.separator") +
                "</TestMessage>");
    }

    @Test
    public void testTranslateMultipleAttributes() {
        Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage><Text name=\"helloText\">Hello World!</Text><OtherText name=\"goodbyeText\">No changes</OtherText></TestMessage>");

        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("name", "newName");

        NodeMappingDataDictionary dictionary = new NodeMappingDataDictionary();
        dictionary.setMappings(mappings);
        dictionary.setPathMappingStrategy(DataDictionary.PathMappingStrategy.ENDS_WITH);

        dictionary.processMessage(message, context);
        Assert.assertEquals(message.getPayload(String.class).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.getProperty("line.separator") +
                "<TestMessage>" + System.getProperty("line.separator") +
                "    <Text name=\"newName\">Hello World!</Text>" + System.getProperty("line.separator") +
                "    <OtherText name=\"newName\">No changes</OtherText>" + System.getProperty("line.separator") +
                "</TestMessage>");
    }

    @Test
    public void testTranslateWithVariables() {
        Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage><Text name=\"\">Hello World!</Text><OtherText>No changes</OtherText></TestMessage>");

        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("TestMessage.Text", "${newText}");
        mappings.put("TestMessage.Text.name", "citrus:upperCase('text')");

        context.setVariable("newText", "Hello!");

        NodeMappingDataDictionary dictionary = new NodeMappingDataDictionary();
        dictionary.setMappings(mappings);

        dictionary.processMessage(message, context);
        Assert.assertEquals(message.getPayload(String.class).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.getProperty("line.separator") +
                "<TestMessage>" + System.getProperty("line.separator") +
                "    <Text name=\"TEXT\">Hello!</Text>" + System.getProperty("line.separator") +
                "    <OtherText>No changes</OtherText>" + System.getProperty("line.separator") +
                "</TestMessage>");
    }

    @Test
    public void testTranslateFromMappingFile() throws Exception {
        Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage><Text name=\"\">Hello World!</Text><OtherText>No changes</OtherText></TestMessage>");

        context.setVariable("newText", "Hello!");

        NodeMappingDataDictionary dictionary = new NodeMappingDataDictionary();
        dictionary.setMappingFile(new ClassPathResource("mapping.properties", DataDictionary.class));
        dictionary.initialize();

        dictionary.processMessage(message, context);
        Assert.assertEquals(message.getPayload(String.class).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.getProperty("line.separator") +
                "<TestMessage>" + System.getProperty("line.separator") +
                "    <Text name=\"newName\">Hello!</Text>" + System.getProperty("line.separator") +
                "    <OtherText>No changes</OtherText>" + System.getProperty("line.separator") +
                "</TestMessage>");
    }

    @Test
    public void testTranslateWithNestedAndEmptyElements() {
        Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage><Text><value></value></Text><OtherText></OtherText></TestMessage>");

        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("TestMessage.Text.value", "Hello!");

        NodeMappingDataDictionary dictionary = new NodeMappingDataDictionary();
        dictionary.setMappings(mappings);

        dictionary.processMessage(message, context);
        Assert.assertEquals(message.getPayload(String.class).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.getProperty("line.separator") +
                "<TestMessage>" + System.getProperty("line.separator") +
                "    <Text>" + System.getProperty("line.separator") +
                "        <value>Hello!</value>" + System.getProperty("line.separator") +
                "    </Text>" + System.getProperty("line.separator") +
                "    <OtherText/>" + System.getProperty("line.separator") +
                "</TestMessage>");
    }

    @Test
    public void testTranslateNoResult() {
        Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage><Text>Hello World!</Text><OtherText>No changes</OtherText></TestMessage>");

        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("Something.Else", "NotFound!");

        NodeMappingDataDictionary dictionary = new NodeMappingDataDictionary();
        dictionary.setMappings(mappings);

        dictionary.processMessage(message, context);
        Assert.assertEquals(message.getPayload(String.class).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.getProperty("line.separator") +
                "<TestMessage>" + System.getProperty("line.separator") +
                "    <Text>Hello World!</Text>" + System.getProperty("line.separator") +
                "    <OtherText>No changes</OtherText>" + System.getProperty("line.separator") +
                "</TestMessage>");
    }
}
