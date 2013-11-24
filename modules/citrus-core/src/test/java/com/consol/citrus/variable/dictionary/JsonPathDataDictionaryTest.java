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

package com.consol.citrus.variable.dictionary;

import com.consol.citrus.message.MessageType;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class JsonPathDataDictionaryTest extends AbstractTestNGUnitTest {
    @Test
    public void testTranslateExactMatchStrategy() {
        String messagePayload = "{\"TestMessage\":{\"Text\":\"Hello World!\",\"OtherText\":\"No changes\"}}";

        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("TestMessage.Text", "Hello!");

        JsonPathDataDictionary dictionary = new JsonPathDataDictionary();
        dictionary.setMappings(mappings);

        String intercepted = dictionary.interceptMessagePayload(messagePayload, MessageType.JSON.toString(), context);
        Assert.assertEquals(intercepted, "{\"TestMessage\":{\"Text\":\"Hello!\",\"OtherText\":\"No changes\"}}");
    }

    @Test
    public void testTranslateStartsWithStrategy() {
        String messagePayload = "{\"TestMessage\":{\"Text\":\"Hello World!\",\"OtherText\":\"No changes\"}}";

        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("TestMessage.Text", "Hello!");
        mappings.put("TestMessage.Other", "Bye!");

        JsonPathDataDictionary dictionary = new JsonPathDataDictionary();
        dictionary.setMappings(mappings);
        dictionary.setPathMappingStrategy(DataDictionary.PathMappingStrategy.STARTS_WITH);

        String intercepted = dictionary.interceptMessagePayload(messagePayload, MessageType.JSON.toString(), context);
        Assert.assertEquals(intercepted, "{\"TestMessage\":{\"Text\":\"Hello!\",\"OtherText\":\"Bye!\"}}");
    }

    @Test
    public void testTranslateEndsWithStrategy() {
        String messagePayload = "{\"TestMessage\":{\"Text\":\"Hello World!\",\"OtherText\":\"No changes\"}}";

        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("Text", "Hello!");

        JsonPathDataDictionary dictionary = new JsonPathDataDictionary();
        dictionary.setMappings(mappings);
        dictionary.setPathMappingStrategy(DataDictionary.PathMappingStrategy.ENDS_WITH);

        String intercepted = dictionary.interceptMessagePayload(messagePayload, MessageType.JSON.toString(), context);
        Assert.assertEquals(intercepted, "{\"TestMessage\":{\"Text\":\"Hello!\",\"OtherText\":\"Hello!\"}}");
    }

    @Test
    public void testTranslateWithVariables() {
        String messagePayload = "{\"TestMessage\":{\"Text\":\"Hello World!\",\"OtherText\":\"No changes\"}}";

        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("TestMessage.Text", "${helloText}");

        JsonPathDataDictionary dictionary = new JsonPathDataDictionary();
        dictionary.setMappings(mappings);

        context.setVariable("helloText", "Hello!");

        String intercepted = dictionary.interceptMessagePayload(messagePayload, MessageType.JSON.toString(), context);
        Assert.assertEquals(intercepted, "{\"TestMessage\":{\"Text\":\"Hello!\",\"OtherText\":\"No changes\"}}");
    }

    @Test
    public void testTranslateWithArrays() {
        String messagePayload = "{\"TestMessage\":{\"Text\":[\"Hello World!\",\"Hello Galaxy!\"],\"OtherText\":\"No changes\"}}";

        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("TestMessage.Text[0]", "Hello!");
        mappings.put("TestMessage.Text[1]", "Hello Universe!");

        JsonPathDataDictionary dictionary = new JsonPathDataDictionary();
        dictionary.setMappings(mappings);

        String intercepted = dictionary.interceptMessagePayload(messagePayload, MessageType.JSON.toString(), context);
        Assert.assertEquals(intercepted, "{\"TestMessage\":{\"Text\":[\"Hello!\",\"Hello Universe!\"],\"OtherText\":\"No changes\"}}");
    }

    @Test
    public void testTranslateWithArraysAndObjects() {
        String messagePayload = "{\"TestMessage\":{\"Greetings\":[{\"Text\":\"Hello World!\"},{\"Text\":\"Hello Galaxy!\"}],\"OtherText\":\"No changes\"}}";

        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("TestMessage.Greetings[0].Text", "Hello!");
        mappings.put("TestMessage.Greetings[1].Text", "Hello Universe!");

        JsonPathDataDictionary dictionary = new JsonPathDataDictionary();
        dictionary.setMappings(mappings);

        String intercepted = dictionary.interceptMessagePayload(messagePayload, MessageType.JSON.toString(), context);
        Assert.assertEquals(intercepted, "{\"TestMessage\":{\"Greetings\":[{\"Text\":\"Hello!\"},{\"Text\":\"Hello Universe!\"}],\"OtherText\":\"No changes\"}}");
    }

    @Test
    public void testTranslateFromMappingFile() throws Exception {
        String messagePayload = "{\"TestMessage\":{\"Text\":\"Hello World!\",\"OtherText\":\"No changes\"}}";

        JsonPathDataDictionary dictionary = new JsonPathDataDictionary();
        dictionary.setMappingFile(new ClassPathResource("jsonmapping.properties", this.getClass()));
        dictionary.afterPropertiesSet();

        String intercepted = dictionary.interceptMessagePayload(messagePayload, MessageType.JSON.toString(), context);
        Assert.assertEquals(intercepted, "{\"TestMessage\":{\"Text\":\"Hello!\",\"OtherText\":\"No changes\"}}");
    }
}
