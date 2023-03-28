/*
 * Copyright 2006-2012 the original author or authors.
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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class MessageSelectorBuilderTest {

    @Test
    public void testToKeyValueMap() {
        Map<String, String> headerMap = MessageSelectorBuilder.withString("foo = 'bar'").toKeyValueMap();
        
        Assert.assertEquals(headerMap.size(), 1L);
        Assert.assertTrue(headerMap.containsKey("foo"));
        Assert.assertEquals(headerMap.get("foo"), "bar");
        
        headerMap = MessageSelectorBuilder.withString("foo = 'bar' AND operation = 'foo'").toKeyValueMap();
        
        Assert.assertEquals(headerMap.size(), 2L);
        Assert.assertTrue(headerMap.containsKey("foo"));
        Assert.assertEquals(headerMap.get("foo"), "bar");
        Assert.assertTrue(headerMap.containsKey("operation"));
        Assert.assertEquals(headerMap.get("operation"), "foo");
        
        headerMap = MessageSelectorBuilder.withString("foo='bar' AND operation='foo'").toKeyValueMap();
        
        Assert.assertEquals(headerMap.size(), 2L);
        Assert.assertTrue(headerMap.containsKey("foo"));
        Assert.assertEquals(headerMap.get("foo"), "bar");
        Assert.assertTrue(headerMap.containsKey("operation"));
        Assert.assertEquals(headerMap.get("operation"), "foo");
        
        headerMap = MessageSelectorBuilder.withString("foo='bar' AND operation='foo' AND foobar='true'").toKeyValueMap();
        
        Assert.assertEquals(headerMap.size(), 3L);
        Assert.assertTrue(headerMap.containsKey("foo"));
        Assert.assertEquals(headerMap.get("foo"), "bar");
        Assert.assertTrue(headerMap.containsKey("operation"));
        Assert.assertEquals(headerMap.get("operation"), "foo");
        Assert.assertTrue(headerMap.containsKey("foobar"));
        Assert.assertEquals(headerMap.get("foobar"), "true");
        
        headerMap = MessageSelectorBuilder.withString("A='Avalue' AND B='Bvalue' AND N='Nvalue'").toKeyValueMap();
        
        Assert.assertEquals(headerMap.size(), 3L);
        Assert.assertTrue(headerMap.containsKey("A"));
        Assert.assertEquals(headerMap.get("A"), "Avalue");
        Assert.assertTrue(headerMap.containsKey("B"));
        Assert.assertEquals(headerMap.get("B"), "Bvalue");
        Assert.assertTrue(headerMap.containsKey("N"));
        Assert.assertEquals(headerMap.get("N"), "Nvalue");
        
        headerMap = MessageSelectorBuilder.withString("foo='OPERAND' AND bar='ANDROID'").toKeyValueMap();
        
        Assert.assertEquals(headerMap.size(), 2L);
        Assert.assertTrue(headerMap.containsKey("foo"));
        Assert.assertEquals(headerMap.get("foo"), "OPERAND");
        Assert.assertTrue(headerMap.containsKey("bar"));
        Assert.assertEquals(headerMap.get("bar"), "ANDROID");
        
        headerMap = MessageSelectorBuilder.withString("foo='ANDROID'").toKeyValueMap();
        
        Assert.assertEquals(headerMap.size(), 1L);
        Assert.assertTrue(headerMap.containsKey("foo"));
        Assert.assertEquals(headerMap.get("foo"), "ANDROID");
        
        headerMap = MessageSelectorBuilder.withString("xpath://foo[@key='primary']/value='bar' AND operation='foo'").toKeyValueMap();
        
        Assert.assertEquals(headerMap.size(), 2L);
        Assert.assertTrue(headerMap.containsKey("xpath://foo[@key='primary']/value"));
        Assert.assertEquals(headerMap.get("xpath://foo[@key='primary']/value"), "bar");
        Assert.assertTrue(headerMap.containsKey("operation"));
        Assert.assertEquals(headerMap.get("operation"), "foo");
    }
}
