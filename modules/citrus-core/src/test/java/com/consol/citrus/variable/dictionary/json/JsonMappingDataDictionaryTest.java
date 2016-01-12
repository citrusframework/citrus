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

package com.consol.citrus.variable.dictionary.json;

import com.consol.citrus.message.*;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.variable.dictionary.DataDictionary;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class JsonMappingDataDictionaryTest extends AbstractTestNGUnitTest {
    @Test
    public void testTranslateExactMatchStrategy() {
        Message message = new DefaultMessage("{\"TestMessage\":{\"Text\":\"Hello World!\",\"OtherText\":\"No changes\"}}");

        Map<String, String> mappings = new HashMap<>();
        mappings.put("Something.Else", "NotFound");
        mappings.put("TestMessage.Text", "Hello!");

        JsonMappingDataDictionary dictionary = new JsonMappingDataDictionary();
        dictionary.setMappings(mappings);

        Message intercepted = dictionary.interceptMessage(message, MessageType.JSON.toString(), context);
        Assert.assertEquals(intercepted.getPayload(String.class), "{\"TestMessage\":{\"Text\":\"Hello!\",\"OtherText\":\"No changes\"}}");
    }

    @Test
    public void testTranslateStartsWithStrategy() {
        Message message = new DefaultMessage("{\"TestMessage\":{\"Text\":\"Hello World!\",\"OtherText\":\"No changes\"}}");

        Map<String, String> mappings = new HashMap<>();
        mappings.put("TestMessage.Text", "Hello!");
        mappings.put("TestMessage.Other", "Bye!");

        JsonMappingDataDictionary dictionary = new JsonMappingDataDictionary();
        dictionary.setMappings(mappings);
        dictionary.setPathMappingStrategy(DataDictionary.PathMappingStrategy.STARTS_WITH);

        Message intercepted = dictionary.interceptMessage(message, MessageType.JSON.toString(), context);
        Assert.assertEquals(intercepted.getPayload(String.class), "{\"TestMessage\":{\"Text\":\"Hello!\",\"OtherText\":\"Bye!\"}}");
    }

    @Test
    public void testTranslateEndsWithStrategy() {
        Message message = new DefaultMessage("{\"TestMessage\":{\"Text\":\"Hello World!\",\"OtherText\":\"No changes\"}}");

        Map<String, String> mappings = new HashMap<>();
        mappings.put("Text", "Hello!");

        JsonMappingDataDictionary dictionary = new JsonMappingDataDictionary();
        dictionary.setMappings(mappings);
        dictionary.setPathMappingStrategy(DataDictionary.PathMappingStrategy.ENDS_WITH);

        Message intercepted = dictionary.interceptMessage(message, MessageType.JSON.toString(), context);
        Assert.assertEquals(intercepted.getPayload(String.class), "{\"TestMessage\":{\"Text\":\"Hello!\",\"OtherText\":\"Hello!\"}}");
    }

    @Test
    public void testTranslateWithVariables() {
        Message message = new DefaultMessage("{\"TestMessage\":{\"Text\":\"Hello World!\",\"OtherText\":\"No changes\"}}");

        Map<String, String> mappings = new HashMap<>();
        mappings.put("TestMessage.Text", "${helloText}");

        JsonMappingDataDictionary dictionary = new JsonMappingDataDictionary();
        dictionary.setMappings(mappings);

        context.setVariable("helloText", "Hello!");

        Message intercepted = dictionary.interceptMessage(message, MessageType.JSON.toString(), context);
        Assert.assertEquals(intercepted.getPayload(String.class), "{\"TestMessage\":{\"Text\":\"Hello!\",\"OtherText\":\"No changes\"}}");
    }

    @Test
    public void testTranslateWithArrays() {
        Message message = new DefaultMessage("{\"TestMessage\":{\"Text\":[\"Hello World!\",\"Hello Galaxy!\"],\"OtherText\":\"No changes\"}}");

        Map<String, String> mappings = new HashMap<>();
        mappings.put("TestMessage.Text[0]", "Hello!");
        mappings.put("TestMessage.Text[1]", "Hello Universe!");

        JsonMappingDataDictionary dictionary = new JsonMappingDataDictionary();
        dictionary.setMappings(mappings);

        Message intercepted = dictionary.interceptMessage(message, MessageType.JSON.toString(), context);
        Assert.assertEquals(intercepted.getPayload(String.class), "{\"TestMessage\":{\"Text\":[\"Hello!\",\"Hello Universe!\"],\"OtherText\":\"No changes\"}}");
    }

    @Test
    public void testTranslateWithArraysAndObjects() {
        Message message = new DefaultMessage("{\"TestMessage\":{\"Greetings\":[{\"Text\":\"Hello World!\"},{\"Text\":\"Hello Galaxy!\"}],\"OtherText\":\"No changes\"}}");

        Map<String, String> mappings = new HashMap<>();
        mappings.put("TestMessage.Greetings[0].Text", "Hello!");
        mappings.put("TestMessage.Greetings[1].Text", "Hello Universe!");

        JsonMappingDataDictionary dictionary = new JsonMappingDataDictionary();
        dictionary.setMappings(mappings);

        Message intercepted = dictionary.interceptMessage(message, MessageType.JSON.toString(), context);
        Assert.assertEquals(intercepted.getPayload(String.class), "{\"TestMessage\":{\"Greetings\":[{\"Text\":\"Hello!\"},{\"Text\":\"Hello Universe!\"}],\"OtherText\":\"No changes\"}}");
    }

    @Test
    public void testTranslateFromMappingFile() throws Exception {
        Message message = new DefaultMessage("{\"TestMessage\":{\"Text\":\"Hello World!\",\"OtherText\":\"No changes\"}}");

        JsonMappingDataDictionary dictionary = new JsonMappingDataDictionary();
        dictionary.setMappingFile(new ClassPathResource("jsonmapping.properties", DataDictionary.class));
        dictionary.afterPropertiesSet();

        Message intercepted = dictionary.interceptMessage(message, MessageType.JSON.toString(), context);
        Assert.assertEquals(intercepted.getPayload(String.class), "{\"TestMessage\":{\"Text\":\"Hello!\",\"OtherText\":\"No changes\"}}");
    }

    @Test
    public void testTranslateWithNullValues() {
        Message message = new DefaultMessage("{\"TestMessage\":{\"Text\":null,\"OtherText\":null}}");

        Map<String, String> mappings = new HashMap<>();
        mappings.put("TestMessage.Text", "Hello!");

        JsonMappingDataDictionary dictionary = new JsonMappingDataDictionary();
        dictionary.setMappings(mappings);

        Message intercepted = dictionary.interceptMessage(message, MessageType.JSON.toString(), context);
        Assert.assertEquals(intercepted.getPayload(String.class), "{\"TestMessage\":{\"Text\":\"Hello!\",\"OtherText\":null}}");
    }

    @Test
    public void testTranslateNoResult() {
        Message message = new DefaultMessage("{\"TestMessage\":{\"Text\":\"Hello World!\",\"OtherText\":\"No changes\"}}");

        Map<String, String> mappings = new HashMap<>();
        mappings.put("Something.Else", "NotFound");

        JsonMappingDataDictionary dictionary = new JsonMappingDataDictionary();
        dictionary.setMappings(mappings);

        Message intercepted = dictionary.interceptMessage(message, MessageType.JSON.toString(), context);
        Assert.assertEquals(intercepted.getPayload(String.class), "{\"TestMessage\":{\"Text\":\"Hello World!\",\"OtherText\":\"No changes\"}}");
    }
}
