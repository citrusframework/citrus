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
package com.consol.citrus.message;

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
        
        Assert.assertTrue(headerMap.containsKey("foo"));
        Assert.assertEquals(headerMap.get("foo"), "bar");
        
        headerMap = MessageSelectorBuilder.withString("foo = 'bar' AND operation = 'foo'").toKeyValueMap();
        
        Assert.assertTrue(headerMap.containsKey("foo"));
        Assert.assertEquals(headerMap.get("foo"), "bar");
        Assert.assertTrue(headerMap.containsKey("operation"));
        Assert.assertEquals(headerMap.get("operation"), "foo");
        
        headerMap = MessageSelectorBuilder.withString("foo='bar' AND operation='foo'").toKeyValueMap();
        
        Assert.assertTrue(headerMap.containsKey("foo"));
        Assert.assertEquals(headerMap.get("foo"), "bar");
        Assert.assertTrue(headerMap.containsKey("operation"));
        Assert.assertEquals(headerMap.get("operation"), "foo");
        
        headerMap = MessageSelectorBuilder.withString("xpath://foo[@key='primary']/value='bar' AND operation='foo'").toKeyValueMap();
        
        Assert.assertTrue(headerMap.containsKey("xpath://foo[@key='primary']/value"));
        Assert.assertEquals(headerMap.get("xpath://foo[@key='primary']/value"), "bar");
        Assert.assertTrue(headerMap.containsKey("operation"));
        Assert.assertEquals(headerMap.get("operation"), "foo");
    }
}
