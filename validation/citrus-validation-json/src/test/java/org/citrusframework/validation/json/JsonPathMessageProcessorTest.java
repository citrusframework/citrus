/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.validation.json;

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.exceptions.UnknownElementException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class JsonPathMessageProcessorTest extends AbstractTestNGUnitTest {

    @Test
    public void testConstructWithJsonPath() {
        Message message = new DefaultMessage("{ \"TestMessage\": { \"Text\": \"Hello World!\" }}");

        Map<String, Object> jsonPathExpressions = new HashMap<>();
        jsonPathExpressions.put("$.TestMessage.Text", "Hello!");

        JsonPathMessageProcessor processor = new JsonPathMessageProcessor.Builder()
                .expressions(jsonPathExpressions)
                .build();
        processor.processMessage(message, context);
        Assert.assertEquals(message.getPayload(String.class), "{\"TestMessage\":{\"Text\":\"Hello!\"}}");
    }

    @Test
    public void testConstructWithJsonPathMultipleValues() {
        Message message = new DefaultMessage("{ \"TestMessage\": { \"Text\": \"Hello World!\", \"Id\": 1234567}}");

        Map<String, Object> jsonPathExpressions = new HashMap<>();
        jsonPathExpressions.put("$.TestMessage.Text", "Hello!");
        jsonPathExpressions.put("$.TestMessage.Id", "9999999");

        JsonPathMessageProcessor processor = new JsonPathMessageProcessor.Builder()
                .expressions(jsonPathExpressions)
                .build();
        processor.processMessage(message, context);
        Assert.assertEquals(message.getPayload(String.class), "{\"TestMessage\":{\"Text\":\"Hello!\",\"Id\":9999999}}");
    }

    @Test
    public void testConstructWithJsonPathWithArrays() {
        Message message = new DefaultMessage("{ \"TestMessage\": [{ \"Text\": \"Hello World!\" }, { \"Text\": \"Another Hello World!\" }]}");

        Map<String, Object> jsonPathExpressions = new HashMap<>();
        jsonPathExpressions.put("$..Text", "Hello!");

        JsonPathMessageProcessor processor = new JsonPathMessageProcessor.Builder()
                .expressions(jsonPathExpressions)
                .build();
        processor.processMessage(message, context);
        Assert.assertEquals(message.getPayload(String.class), "{\"TestMessage\":[{\"Text\":\"Hello!\"},{\"Text\":\"Hello!\"}]}");
    }

    @Test
    public void testConstructWithJsonPathNoResult() {
        Message message = new DefaultMessage("{ \"TestMessage\": { \"Text\": \"Hello World!\" }}");

        Map<String, Object> jsonPathExpressions = new HashMap<>();
        jsonPathExpressions.put("$.TestMessage.Unknown", "Hello!");

        JsonPathMessageProcessor processor = new JsonPathMessageProcessor.Builder()
                .expressions(jsonPathExpressions)
                .ignoreNotFound(true)
                .build();
        processor.processMessage(message, context);
        Assert.assertEquals(message.getPayload(String.class), "{\"TestMessage\":{\"Text\":\"Hello World!\"}}");
    }

    @Test(expectedExceptions = UnknownElementException.class)
    public void testConstructFailOnUnknownJsonPath() {
        Message message = new DefaultMessage("{ \"TestMessage\": { \"Text\": \"Hello World!\" }}");

        Map<String, Object> jsonPathExpressions = new HashMap<>();
        jsonPathExpressions.put("$.TestMessage.Unknown", "Hello!");

        JsonPathMessageProcessor processor = new JsonPathMessageProcessor.Builder()
                .expressions(jsonPathExpressions)
                .build();
        processor.processMessage(message, context);
    }
}
