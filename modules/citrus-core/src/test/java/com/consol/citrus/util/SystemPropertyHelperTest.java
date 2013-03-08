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

package com.consol.citrus.util;

import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class SystemPropertyHelperTest {

    @Test
    public void testSingleProperty() {
        Assert.assertNull(System.getProperty("citrus.foo"));
        
        new SystemPropertyHelper("citrus.foo", "bar");
        
        Assert.assertEquals(System.getProperty("citrus.foo"), "bar");
    }
    
    @Test
    public void testMultipleProperties() {
        Assert.assertNull(System.getProperty("citrus.foo.A"));
        Assert.assertNull(System.getProperty("citrus.foo.B"));
        Assert.assertNull(System.getProperty("citrus.foo.C"));
        
        Properties props = new Properties();
        props.put("citrus.foo.A", "A");
        props.put("citrus.foo.B", "B");
        props.put("citrus.foo.C", "C");
        
        new SystemPropertyHelper(props);
        
        Assert.assertEquals(System.getProperty("citrus.foo.A"), "A");
        Assert.assertEquals(System.getProperty("citrus.foo.B"), "B");
        Assert.assertEquals(System.getProperty("citrus.foo.C"), "C");
    }
}
