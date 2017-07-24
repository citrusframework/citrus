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

import com.consol.citrus.Citrus;
import com.consol.citrus.message.*;
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

    private final String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage><Text>Hello World!</Text><OtherText name=\"foo\">No changes</OtherText></TestMessage>";
    private final String htmlPayload = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">"
            + "<html>"
                + "<head>"
                    + "<title>?</title>"
                + "</head>"
                + "<body>"
                    + "<h1>?</h1>"
                    + "<hr>"
                    + "<form action=\"/\">"
                        + "<input name=\"foo\" type=\"text\">"
                    + "</form>"
                + "</body>"
            + "</html>";

    @Test
    public void testTranslate() throws Exception {
        Message message = new DefaultMessage(payload);

        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("//TestMessage/Text", "Hello!");
        mappings.put("//@name", "bar");
        mappings.put("//something/else", "not_found");

        XpathMappingDataDictionary dictionary = new XpathMappingDataDictionary();
        dictionary.setMappings(mappings);

        Message intercepted = dictionary.interceptMessage(message, Citrus.DEFAULT_MESSAGE_TYPE, context);
        Assert.assertEquals(intercepted.getPayload(String.class).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage>" + System.getProperty("line.separator") +
                "   <Text>Hello!</Text>" + System.getProperty("line.separator") +
                "   <OtherText name=\"bar\">No changes</OtherText>" + System.getProperty("line.separator") +
                "</TestMessage>");
    }
    
    @Test
    public void testTranslateMultipleNodes() throws Exception {
        Message message = new DefaultMessage(payload);

        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("//*[string-length(normalize-space(text())) > 0]", "Hello!");
        mappings.put("//@*", "bar");

        XpathMappingDataDictionary dictionary = new XpathMappingDataDictionary();
        dictionary.setMappings(mappings);

        Message intercepted = dictionary.interceptMessage(message, Citrus.DEFAULT_MESSAGE_TYPE, context);
        Assert.assertEquals(intercepted.getPayload(String.class).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage>" + System.getProperty("line.separator") +
                "   <Text>Hello!</Text>" + System.getProperty("line.separator") +
                "   <OtherText name=\"bar\">Hello!</OtherText>" + System.getProperty("line.separator") +
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

        Message intercepted = dictionary.interceptMessage(message, Citrus.DEFAULT_MESSAGE_TYPE, context);
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

        Message intercepted = dictionary.interceptMessage(message, Citrus.DEFAULT_MESSAGE_TYPE, context);
        Assert.assertEquals(intercepted.getPayload(String.class).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns1:TestMessage xmlns:ns1=\"http://www.foo.bar\">" + System.getProperty("line.separator") +
                "   <ns1:Text>Hello!</ns1:Text>" + System.getProperty("line.separator") +
                "   <ns1:OtherText name=\"bar\">No changes</ns1:OtherText>" + System.getProperty("line.separator") +
                "</ns1:TestMessage>");
    }

    @Test
    public void testTranslateWithVariables() throws Exception {
        Message message = new DefaultMessage(payload);

        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("//TestMessage/Text", "${hello}");
        mappings.put("//@name", "bar");

        context.setVariable("hello", "Hello!");

        XpathMappingDataDictionary dictionary = new XpathMappingDataDictionary();
        dictionary.setMappings(mappings);

        Message intercepted = dictionary.interceptMessage(message, Citrus.DEFAULT_MESSAGE_TYPE, context);
        Assert.assertEquals(intercepted.getPayload(String.class).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage>" + System.getProperty("line.separator") +
                "   <Text>Hello!</Text>" + System.getProperty("line.separator") +
                "   <OtherText name=\"bar\">No changes</OtherText>" + System.getProperty("line.separator") +
                "</TestMessage>");
    }

    @Test
    public void testTranslateFromMappingFile() throws Exception {
        Message message = new DefaultMessage(payload);

        XpathMappingDataDictionary dictionary = new XpathMappingDataDictionary();
        dictionary.setMappingFile(new ClassPathResource("xpathmapping.properties", DataDictionary.class));
        dictionary.afterPropertiesSet();

        Message intercepted = dictionary.interceptMessage(message, Citrus.DEFAULT_MESSAGE_TYPE, context);
        Assert.assertEquals(intercepted.getPayload(String.class).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage>" + System.getProperty("line.separator") +
                "   <Text>Hello!</Text>" + System.getProperty("line.separator") +
                "   <OtherText name=\"bar\">GoodBye!</OtherText>" + System.getProperty("line.separator") +
                "</TestMessage>");
    }

    @Test
    public void testTranslateNoResult() {
        Message message = new DefaultMessage(payload);

        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("//TestMessage/Unknown", "Hello!");
        mappings.put("//@name", "bar");

        XpathMappingDataDictionary dictionary = new XpathMappingDataDictionary();
        dictionary.setMappings(mappings);

        Message intercepted = dictionary.interceptMessage(message, Citrus.DEFAULT_MESSAGE_TYPE, context);
        Assert.assertEquals(intercepted.getPayload(String.class).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage>" + System.getProperty("line.separator") +
                "   <Text>Hello World!</Text>" + System.getProperty("line.separator") +
                "   <OtherText name=\"bar\">No changes</OtherText>" + System.getProperty("line.separator") +
                "</TestMessage>");
    }

    @Test
    public void testTranslateXhtml() throws Exception {
        Message message = new DefaultMessage(htmlPayload);

        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("/xh:html/xh:head/xh:title", "Hello");
        mappings.put("//xh:h1", "Hello Citrus!");

        XpathMappingDataDictionary dictionary = new XpathMappingDataDictionary();
        dictionary.setMappings(mappings);

        NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();
        namespaceContextBuilder.getNamespaceMappings().put("xh", "http://www.w3.org/1999/xhtml");
        dictionary.setNamespaceContextBuilder(namespaceContextBuilder);

        Message intercepted = dictionary.interceptMessage(message, MessageType.XHTML.name(), context);
        Assert.assertTrue(intercepted.getPayload(String.class).trim().contains("<title>Hello</title>"));
        Assert.assertTrue(intercepted.getPayload(String.class).trim().contains("<h1>Hello Citrus!</h1>"));
        Assert.assertTrue(intercepted.getPayload(String.class).trim().contains("<hr />"));
    }
}
