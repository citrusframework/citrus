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

package org.citrusframework.openapi.actions;

import org.citrusframework.context.TestContext;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class OpenApiPayloadBuilderTest {

    private TestContext context;

    @BeforeClass
    public void setUp() {
        context = new TestContext();
    }

    @Test
    public void testBuildPayloadWithMultiValueMap() {
        // Given
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("key1", "value1");
        multiValueMap.add("key2", "Hello ${user}, welcome!");
        multiValueMap.add("key2", "Another ${user} message");
        multiValueMap.add("${k3}", "a");
        multiValueMap.add("${k3}", "b");
        multiValueMap.add("${k3}", "${user}");

        context.setVariable("user", "John");
        context.setVariable("k3", "key3");

        OpenApiPayloadBuilder payloadBuilder = new OpenApiPayloadBuilder(multiValueMap);

        // When
        Object payload = payloadBuilder.buildPayload(context);

        // Then
        assertTrue(payload instanceof MultiValueMap);
        MultiValueMap<String, String> result = (MultiValueMap<String, String>) payload;

        assertEquals(result.get("key1").get(0), "value1");
        assertEquals(result.get("key2").get(0), "Hello John, welcome!");
        assertEquals(result.get("key2").get(1), "Another John message");
        assertEquals(result.get("key3").get(0), "a");
        assertEquals(result.get("key3").get(1), "b");
        assertEquals(result.get("key3").get(2), "John");
    }

    @Test
    public void testBuildPayloadWithPlainObject() {
        // Given
        String simplePayload = "This is a simple ${message}";
        context.setVariable("message", "test");

        OpenApiPayloadBuilder payloadBuilder = new OpenApiPayloadBuilder(simplePayload);

        // When
        Object payload = payloadBuilder.buildPayload(context);

        // Then
        assertTrue(payload instanceof String);
        assertEquals(payload, "This is a simple test");
    }
}
