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

package com.consol.citrus.util;

import org.springframework.util.MultiValueMap;
import org.springframework.xml.transform.StringSource;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.transform.Source;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class TypeConversionUtilsTest {

    @Test
    public void testConvertIfNecessary() {
        String payload = "Hello Citrus!";
        
        Assert.assertEquals(TypeConversionUtils.convertIfNecessary("[a,b,c]", String[].class).length, 3);
        Assert.assertEquals(TypeConversionUtils.convertIfNecessary("[a, b, c]", String[].class).length, 3);
        Assert.assertEquals(TypeConversionUtils.convertIfNecessary("[a,b,c]", List.class).size(), 3);
        Assert.assertEquals(TypeConversionUtils.convertIfNecessary("[a, b, c]", List.class).size(), 3);
        Assert.assertEquals(TypeConversionUtils.convertIfNecessary("{key=value}", Map.class).get("key"), "value");
        Assert.assertEquals(TypeConversionUtils.convertIfNecessary("{key1=value1, key2=value2}", Map.class).get("key2"), "value2");
        Assert.assertEquals(TypeConversionUtils.convertIfNecessary("{key1=value1,key2=value2}", Map.class).get("key2"), "value2");
        Assert.assertEquals(TypeConversionUtils.convertIfNecessary("{key=[value]}", MultiValueMap.class).getFirst("key"), new String[] {"value"});
        Assert.assertEquals(TypeConversionUtils.convertIfNecessary("{key=[value1,value2]}", MultiValueMap.class).get("key").getClass(), LinkedList.class);
        Assert.assertEquals(TypeConversionUtils.convertIfNecessary("{key=[value1,value2]}", MultiValueMap.class).getFirst("key"), new String[] {"value1", "value2"});
        Assert.assertEquals(TypeConversionUtils.convertIfNecessary(payload, InputStream.class).getClass(), ByteArrayInputStream.class);
        Assert.assertEquals(TypeConversionUtils.convertIfNecessary(payload, Source.class).getClass(), StringSource.class);
        Assert.assertEquals(TypeConversionUtils.convertIfNecessary(payload, byte[].class), payload.getBytes());
        Assert.assertEquals(TypeConversionUtils.convertIfNecessary(payload.getBytes(), String.class), Arrays.toString(payload.getBytes()));
        Assert.assertEquals(TypeConversionUtils.convertIfNecessary(ByteBuffer.wrap(payload.getBytes()), String.class), payload);
    }

}