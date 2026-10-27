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
package org.citrusframework.message;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MessageSelectorBuilderTest {

    @Test
    public void testToKeyValueMap() {
        Map<String, String> headerMap = MessageSelectorBuilder.withString("foo = 'bar'").toKeyValueMap();

        Assertions.assertEquals(1L, headerMap.size());
        Assertions.assertTrue(headerMap.containsKey("foo"));
        Assertions.assertEquals("bar", headerMap.get("foo"));

        headerMap = MessageSelectorBuilder.withString("foo = 'bar' AND operation = 'foo'").toKeyValueMap();

        Assertions.assertEquals(2L, headerMap.size());
        Assertions.assertTrue(headerMap.containsKey("foo"));
        Assertions.assertEquals("bar", headerMap.get("foo"));
        Assertions.assertTrue(headerMap.containsKey("operation"));
        Assertions.assertEquals("foo", headerMap.get("operation"));

        headerMap = MessageSelectorBuilder.withString("foo='bar' AND operation='foo'").toKeyValueMap();

        Assertions.assertEquals(2L, headerMap.size());
        Assertions.assertTrue(headerMap.containsKey("foo"));
        Assertions.assertEquals("bar", headerMap.get("foo"));
        Assertions.assertTrue(headerMap.containsKey("operation"));
        Assertions.assertEquals("foo", headerMap.get("operation"));

        headerMap = MessageSelectorBuilder.withString("foo='bar' AND operation='foo' AND foobar='true'").toKeyValueMap();

        Assertions.assertEquals(3L, headerMap.size());
        Assertions.assertTrue(headerMap.containsKey("foo"));
        Assertions.assertEquals("bar", headerMap.get("foo"));
        Assertions.assertTrue(headerMap.containsKey("operation"));
        Assertions.assertEquals("foo", headerMap.get("operation"));
        Assertions.assertTrue(headerMap.containsKey("foobar"));
        Assertions.assertEquals("true", headerMap.get("foobar"));

        headerMap = MessageSelectorBuilder.withString("A='Avalue' AND B='Bvalue' AND N='Nvalue'").toKeyValueMap();

        Assertions.assertEquals(3L, headerMap.size());
        Assertions.assertTrue(headerMap.containsKey("A"));
        Assertions.assertEquals("Avalue", headerMap.get("A"));
        Assertions.assertTrue(headerMap.containsKey("B"));
        Assertions.assertEquals("Bvalue", headerMap.get("B"));
        Assertions.assertTrue(headerMap.containsKey("N"));
        Assertions.assertEquals("Nvalue", headerMap.get("N"));

        headerMap = MessageSelectorBuilder.withString("foo='OPERAND' AND bar='ANDROID'").toKeyValueMap();

        Assertions.assertEquals(2L, headerMap.size());
        Assertions.assertTrue(headerMap.containsKey("foo"));
        Assertions.assertEquals("OPERAND", headerMap.get("foo"));
        Assertions.assertTrue(headerMap.containsKey("bar"));
        Assertions.assertEquals("ANDROID", headerMap.get("bar"));

        headerMap = MessageSelectorBuilder.withString("foo='ANDROID'").toKeyValueMap();

        Assertions.assertEquals(1L, headerMap.size());
        Assertions.assertTrue(headerMap.containsKey("foo"));
        Assertions.assertEquals("ANDROID", headerMap.get("foo"));

        headerMap = MessageSelectorBuilder.withString("xpath://foo[@key='primary']/value='bar' AND operation='foo'").toKeyValueMap();

        Assertions.assertEquals(2L, headerMap.size());
        Assertions.assertTrue(headerMap.containsKey("xpath://foo[@key='primary']/value"));
        Assertions.assertEquals("bar", headerMap.get("xpath://foo[@key='primary']/value"));
        Assertions.assertTrue(headerMap.containsKey("operation"));
        Assertions.assertEquals("foo", headerMap.get("operation"));
    }
}
