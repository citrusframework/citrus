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

package org.citrusframework.openapi.util;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class RandomModelBuilderTest {

    private RandomModelBuilder builder;

    @BeforeMethod
    public void setUp() {
        builder = new RandomModelBuilder();
    }

    @Test
    public void testInitialState() {
        String text = builder.toString();
        assertEquals(text, "");
    }

    @Test
    public void testAppendSimple() {
        builder.appendSimple("testValue");
        String json = builder.toString();
        assertEquals(json, "testValue");
    }

    @Test
    public void testObjectWithProperties() {
        builder.object(() -> {
            builder.property("key1", () -> builder.appendSimple("\"value1\""));
            builder.property("key2", () -> builder.appendSimple("\"value2\""));
        });
        String json = builder.toString();
        assertEquals(json, "{\"key1\": \"value1\",\"key2\": \"value2\"}");
    }

    @Test
    public void testNestedObject() {
        builder.object(() ->
            builder.property("outerKey", () -> builder.object(() ->
                builder.property("innerKey", () -> builder.appendSimple("\"innerValue\""))
            ))
        );
        String json = builder.toString();
        assertEquals(json, "{\"outerKey\": {\"innerKey\": \"innerValue\"}}");
    }

    @Test
    public void testArray() {
        builder.array(() -> {
            builder.appendSimple("\"value1\"");
            builder.appendSimple("\"value2\"");
            builder.appendSimple("\"value3\"");
        });
        String json = builder.toString();
        assertEquals(json, "[\"value1\",\"value2\",\"value3\"]");
    }

    @Test
    public void testNestedArray() {
        builder.array(() -> {
            builder.appendSimple("\"value1\"");
            builder.array(() -> {
                builder.appendSimple("\"nestedValue1\"");
                builder.appendSimple("\"nestedValue2\"");
            });
            builder.appendSimple("\"value2\"");
        });
        String json = builder.toString();
        assertEquals(json, "[\"value1\",[\"nestedValue1\",\"nestedValue2\"],\"value2\"]");
    }

    @Test
    public void testMixedStructure() {
        builder.object(() -> {
            builder.property("key1", () -> builder.array(() -> {
                builder.appendSimple("\"value1\"");
                builder.object(() ->
                    builder.property("nestedKey", () -> builder.appendSimple("\"nestedValue\""))
                );
            }));
            builder.property("key2", () -> builder.appendSimple("\"value2\""));
        });
        String json = builder.toString();
        assertEquals(json, "{\"key1\": [\"value1\",{\"nestedKey\": \"nestedValue\"}],\"key2\": \"value2\"}");
    }

    @Test
    public void testIllegalStateOnEmptyDeque() {

        builder.deque.clear();

        Exception exception = expectThrows(IllegalStateException.class, () ->
            builder.property("key", () -> builder.appendSimple("value"))
        );
        assertEquals(exception.getMessage(), "Encountered empty stack!");

        exception = expectThrows(IllegalStateException.class, () ->
            builder.object(() -> {})
        );
        assertEquals(exception.getMessage(), "Encountered empty stack!");

        exception = expectThrows(IllegalStateException.class, () ->
            builder.array(() -> {})
        );
        assertEquals(exception.getMessage(), "Encountered empty stack!");
    }
}
