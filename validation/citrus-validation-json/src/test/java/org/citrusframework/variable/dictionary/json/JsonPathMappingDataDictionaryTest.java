/*
 * Copyright 2006-2017 the original author or authors.
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

package org.citrusframework.variable.dictionary.json;

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
 * @since 2.7
 */
public class JsonPathMappingDataDictionaryTest extends UnitTestSupport {
    @Test
    public void testTranslateExactMatchStrategy() {
        Message message = new DefaultMessage("{\"TestMessage\":{\"Text\":\"Hello World!\",\"OtherText\":\"No changes\", \"OtherNumber\": 10}}");

        Map<String, String> mappings = new HashMap<>();
        mappings.put("$.Something.Else", "NotFound");
        mappings.put("$.TestMessage.Text", "Hello!");

        JsonPathMappingDataDictionary dictionary = new JsonPathMappingDataDictionary();
        dictionary.setMappings(mappings);

        dictionary.processMessage(message, context);
        Assert.assertEquals(message.getPayload(String.class), "{\"TestMessage\":{\"Text\":\"Hello!\",\"OtherText\":\"No changes\",\"OtherNumber\":10}}");
    }

    @Test
    public void testTranslateMultipleNodes() {
        Message message = new DefaultMessage("[" +
                    "{\"TestMessage\":{\"Text\":\"Hello World!\",\"OtherText\":\"No changes\", \"OtherNumber\": 10}}, " +
                    "{\"TestMessage\":{\"Text\":\"Hello World!\",\"OtherText\":\"No changes\", \"OtherNumber\": 10}}" +
                "]");

        Map<String, String> mappings = new HashMap<>();
        mappings.put("$.Something.Else", "NotFound");
        mappings.put("$..Text", "Hello!");

        JsonPathMappingDataDictionary dictionary = new JsonPathMappingDataDictionary();
        dictionary.setMappings(mappings);

        dictionary.processMessage(message, context);
        Assert.assertEquals(message.getPayload(String.class), "[" +
                    "{\"TestMessage\":{\"Text\":\"Hello!\",\"OtherText\":\"No changes\",\"OtherNumber\":10}}," +
                    "{\"TestMessage\":{\"Text\":\"Hello!\",\"OtherText\":\"No changes\",\"OtherNumber\":10}}" +
                "]");
    }

    @Test
    public void testTranslateWithVariables() {
        Message message = new DefaultMessage("{\"TestMessage\":{\"Text\":\"Hello World!\",\"OtherText\":\"No changes\"}}");

        Map<String, String> mappings = new HashMap<>();
        mappings.put("$.TestMessage.Text", "${helloText}");

        JsonPathMappingDataDictionary dictionary = new JsonPathMappingDataDictionary();
        dictionary.setMappings(mappings);

        context.setVariable("helloText", "Hello!");

        dictionary.processMessage(message, context);
        Assert.assertEquals(message.getPayload(String.class), "{\"TestMessage\":{\"Text\":\"Hello!\",\"OtherText\":\"No changes\"}}");
    }

    @Test
    public void testTranslateWithArrays() {
        Message message = new DefaultMessage("{\"TestMessage\":{\"Text\":[\"Hello World!\",\"Hello Galaxy!\"],\"OtherText\":\"No changes\"}}");

        Map<String, String> mappings = new HashMap<>();
        mappings.put("$.TestMessage.Text[0]", "Hello!");
        mappings.put("$.TestMessage.Text[1]", "Hello Universe!");

        JsonPathMappingDataDictionary dictionary = new JsonPathMappingDataDictionary();
        dictionary.setMappings(mappings);

        dictionary.processMessage(message, context);
        Assert.assertEquals(message.getPayload(String.class), "{\"TestMessage\":{\"Text\":[\"Hello!\",\"Hello Universe!\"],\"OtherText\":\"No changes\"}}");
    }

    @Test
    public void testTranslateWithArraysAndObjects() {
        Message message = new DefaultMessage("{\"TestMessage\":{\"Greetings\":[{\"Text\":\"Hello World!\"},{\"Text\":\"Hello Galaxy!\"}],\"OtherText\":\"No changes\"}}");

        Map<String, String> mappings = new HashMap<>();
        mappings.put("$.TestMessage.Greetings[0].Text", "Hello!");
        mappings.put("$.TestMessage.Greetings[1].Text", "Hello Universe!");

        JsonPathMappingDataDictionary dictionary = new JsonPathMappingDataDictionary();
        dictionary.setMappings(mappings);

        dictionary.processMessage(message, context);
        Assert.assertEquals(message.getPayload(String.class), "{\"TestMessage\":{\"Greetings\":[{\"Text\":\"Hello!\"},{\"Text\":\"Hello Universe!\"}],\"OtherText\":\"No changes\"}}");
    }

    @Test
    public void testTranslateFromMappingFile() throws Exception {
        Message message = new DefaultMessage("{\"TestMessage\":{\"Text\":\"Hello World!\",\"OtherText\":\"No changes\"}}");

        JsonPathMappingDataDictionary dictionary = new JsonPathMappingDataDictionary();
        dictionary.setMappingFile(new ClassPathResource("jsonmapping.properties", DataDictionary.class));
        dictionary.initialize();

        dictionary.processMessage(message, context);
        Assert.assertEquals(message.getPayload(String.class), "{\"TestMessage\":{\"Text\":\"Hello!\",\"OtherText\":\"No changes\"}}");
    }

    @Test
    public void testTranslateWithNullValues() {
        Message message = new DefaultMessage("{\"TestMessage\":{\"Text\":null,\"OtherText\":null}}");

        Map<String, String> mappings = new HashMap<>();
        mappings.put("$.TestMessage.Text", "Hello!");

        JsonPathMappingDataDictionary dictionary = new JsonPathMappingDataDictionary();
        dictionary.setMappings(mappings);

        dictionary.processMessage(message, context);
        Assert.assertEquals(message.getPayload(String.class), "{\"TestMessage\":{\"Text\":\"Hello!\",\"OtherText\":null}}");
    }

    @Test
    public void testTranslateWithNumberValues() {
        Message message = new DefaultMessage("{\"TestMessage\":{\"Number\":0,\"OtherNumber\":100}}");

        Map<String, String> mappings = new HashMap<>();
        mappings.put("$.TestMessage.Number", "99");

        JsonPathMappingDataDictionary dictionary = new JsonPathMappingDataDictionary();
        dictionary.setMappings(mappings);

        dictionary.processMessage(message, context);
        Assert.assertEquals(message.getPayload(String.class), "{\"TestMessage\":{\"Number\":99,\"OtherNumber\":100}}");
    }

    @Test
    public void testTranslateNoResult() {
        Message message = new DefaultMessage("{\"TestMessage\":{\"Text\":\"Hello World!\",\"OtherText\":\"No changes\"}}");

        Map<String, String> mappings = new HashMap<>();
        mappings.put("$.Something.Else", "NotFound");

        JsonPathMappingDataDictionary dictionary = new JsonPathMappingDataDictionary();
        dictionary.setMappings(mappings);

        dictionary.processMessage(message, context);
        Assert.assertEquals(message.getPayload(String.class), "{\"TestMessage\":{\"Text\":\"Hello World!\",\"OtherText\":\"No changes\"}}");
    }
}
