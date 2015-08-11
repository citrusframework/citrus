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

package com.consol.citrus.variable.dictionary.xml;

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.variable.dictionary.DataDictionary;
import com.consol.citrus.xml.namespace.NamespaceContextBuilder;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class XpathMappingDataDictionaryTest extends AbstractTestNGUnitTest {

    @Test
    public void testTranslate() throws Exception {
        Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage><Text>Hello World!</Text><OtherText name=\"foo\">No changes</OtherText></TestMessage>");

        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("//TestMessage/Text", "Hello!");
        mappings.put("//@name", "bar");

        XpathMappingDataDictionary dictionary = new XpathMappingDataDictionary();
        dictionary.setMappings(mappings);

        Message intercepted = dictionary.interceptMessage(message, CitrusConstants.DEFAULT_MESSAGE_TYPE, context);
        Assert.assertEquals(intercepted.getPayload(String.class).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage>" + System.getProperty("line.separator") +
                "   <Text>Hello!</Text>" + System.getProperty("line.separator") +
                "   <OtherText name=\"bar\">No changes</OtherText>" + System.getProperty("line.separator") +
                "</TestMessage>");
    }

    @Test
    public void testTranslateWithNamespaceLookup() throws Exception {
        Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns1:TestMessage xmlns:ns1=\"http://www.foo.bar\"><ns1:Text>Hello World!</ns1:Text><ns1:OtherText name=\"foo\">No changes</ns1:OtherText></ns1:TestMessage>");

        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("//ns1:TestMessage/ns1:Text", "Hello!");
        mappings.put("//@name", "bar");

        XpathMappingDataDictionary dictionary = new XpathMappingDataDictionary();
        dictionary.setMappings(mappings);

        Message intercepted = dictionary.interceptMessage(message, CitrusConstants.DEFAULT_MESSAGE_TYPE, context);
        Assert.assertEquals(intercepted.getPayload(String.class).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns1:TestMessage xmlns:ns1=\"http://www.foo.bar\">" + System.getProperty("line.separator") +
                "   <ns1:Text>Hello!</ns1:Text>" + System.getProperty("line.separator") +
                "   <ns1:OtherText name=\"bar\">No changes</ns1:OtherText>" + System.getProperty("line.separator") +
                "</ns1:TestMessage>");
    }

    @Test
    public void testTranslateWithNamespaceBuilder() throws Exception {
        Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns1:TestMessage xmlns:ns1=\"http://www.foo.bar\"><ns1:Text>Hello World!</ns1:Text><ns1:OtherText name=\"foo\">No changes</ns1:OtherText></ns1:TestMessage>");

        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("//foo:TestMessage/foo:Text", "Hello!");
        mappings.put("//@name", "bar");

        XpathMappingDataDictionary dictionary = new XpathMappingDataDictionary();
        dictionary.setMappings(mappings);

        NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("foo", "http://www.foo.bar");
        namespaceContextBuilder.setNamespaceMappings(namespaces);
        dictionary.setNamespaceContextBuilder(namespaceContextBuilder);

        Message intercepted = dictionary.interceptMessage(message, CitrusConstants.DEFAULT_MESSAGE_TYPE, context);
        Assert.assertEquals(intercepted.getPayload(String.class).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns1:TestMessage xmlns:ns1=\"http://www.foo.bar\">" + System.getProperty("line.separator") +
                "   <ns1:Text>Hello!</ns1:Text>" + System.getProperty("line.separator") +
                "   <ns1:OtherText name=\"bar\">No changes</ns1:OtherText>" + System.getProperty("line.separator") +
                "</ns1:TestMessage>");
    }

    @Test
    public void testTranslateWithVariables() throws Exception {
        Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage><Text>Hello World!</Text><OtherText name=\"foo\">No changes</OtherText></TestMessage>");

        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("//TestMessage/Text", "${hello}");
        mappings.put("//@name", "bar");

        context.setVariable("hello", "Hello!");

        XpathMappingDataDictionary dictionary = new XpathMappingDataDictionary();
        dictionary.setMappings(mappings);

        Message intercepted = dictionary.interceptMessage(message, CitrusConstants.DEFAULT_MESSAGE_TYPE, context);
        Assert.assertEquals(intercepted.getPayload(String.class).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage>" + System.getProperty("line.separator") +
                "   <Text>Hello!</Text>" + System.getProperty("line.separator") +
                "   <OtherText name=\"bar\">No changes</OtherText>" + System.getProperty("line.separator") +
                "</TestMessage>");
    }

    @Test
    public void testTranslateFromMappingFile() throws Exception {
        Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage><Text>Hello World!</Text><OtherText name=\"foo\">No changes</OtherText></TestMessage>");

        XpathMappingDataDictionary dictionary = new XpathMappingDataDictionary();
        dictionary.setMappingFile(new ClassPathResource("xpathmapping.properties", DataDictionary.class));
        dictionary.afterPropertiesSet();

        Message intercepted = dictionary.interceptMessage(message, CitrusConstants.DEFAULT_MESSAGE_TYPE, context);
        Assert.assertEquals(intercepted.getPayload(String.class).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage>" + System.getProperty("line.separator") +
                "   <Text>Hello!</Text>" + System.getProperty("line.separator") +
                "   <OtherText name=\"bar\">GoodBye!</OtherText>" + System.getProperty("line.separator") +
                "</TestMessage>");
    }
}
