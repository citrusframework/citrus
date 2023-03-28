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

package org.citrusframework.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.xml.transform.Source;

import org.citrusframework.xml.StringSource;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class DefaultTypeConverterTest {

    private final DefaultTypeConverter converter = DefaultTypeConverter.INSTANCE;

    @Test
    public void testConvertIfNecessary() {
        String payload = "Hello Citrus!";

        Assert.assertEquals(converter.convertIfNecessary("1", Byte.class), Byte.valueOf((byte) 1));
        Assert.assertEquals(converter.convertIfNecessary((byte) 1, Byte.class), Byte.valueOf((byte) 1));
        Assert.assertEquals(converter.convertIfNecessary("1", Short.class), Short.valueOf((short) 1));
        Assert.assertEquals(converter.convertIfNecessary((short) 1, Short.class), Short.valueOf((short) 1));
        Assert.assertEquals(converter.convertIfNecessary("1", Integer.class), Integer.valueOf(1));
        Assert.assertEquals(converter.convertIfNecessary(1, Integer.class), Integer.valueOf(1));
        Assert.assertEquals(converter.convertIfNecessary("1", Long.class), Long.valueOf(1));
        Assert.assertEquals(converter.convertIfNecessary(1L, Long.class), Long.valueOf(1));
        Assert.assertEquals(converter.convertIfNecessary("1", Float.class), Float.valueOf(1.0F));
        Assert.assertEquals(converter.convertIfNecessary(1.0F, Float.class), Float.valueOf(1.0F));
        Assert.assertEquals(converter.convertIfNecessary("1", Double.class), Double.valueOf(1.0D));
        Assert.assertEquals(converter.convertIfNecessary(1.0D, Double.class), Double.valueOf(1.0D));
        Assert.assertEquals(converter.convertIfNecessary("true", Boolean.class), Boolean.TRUE);
        Assert.assertEquals(converter.convertIfNecessary(Boolean.FALSE, Boolean.class), Boolean.FALSE);
        Assert.assertEquals(converter.convertIfNecessary("no", Boolean.class), Boolean.FALSE);
        Assert.assertEquals(converter.convertIfNecessary("[a,b,c]", String[].class).length, 3);
        Assert.assertEquals(converter.convertIfNecessary("[a, b, c]", String[].class).length, 3);
        Assert.assertEquals(converter.convertIfNecessary("[a,b,c]", List.class).size(), 3);
        Assert.assertEquals(converter.convertIfNecessary("[a, b, c]", List.class).size(), 3);
        Assert.assertEquals(converter.convertIfNecessary("{key=value}", Map.class).get("key"), "value");
        Assert.assertEquals(converter.convertIfNecessary("{key1=value1, key2=value2}", Map.class).get("key2"), "value2");
        Assert.assertEquals(converter.convertIfNecessary("{key1=value1,key2=value2}", Map.class).get("key2"), "value2");
        Assert.assertEquals(converter.convertIfNecessary(Arrays.asList("foo", "bar"), String.class), "[foo, bar]");
        Assert.assertEquals(converter.convertIfNecessary(new String[] {"foo", "bar"}, String.class), "[foo, bar]");
        Assert.assertEquals(converter.convertIfNecessary(new Object[] {"foo", "bar"}, String.class), "[foo, bar]");
        Assert.assertEquals(converter.convertIfNecessary(Collections.singletonMap("foo", "bar"), String.class), "{foo=bar}");
        Assert.assertEquals(converter.convertIfNecessary(Arrays.asList(1, 2), String.class), "[1, 2]");
        Assert.assertEquals(converter.convertIfNecessary(new int[] {1, 2}, String.class), "[1, 2]");
        Assert.assertEquals(converter.convertIfNecessary(null, String.class), "null");
        Assert.assertEquals(converter.convertIfNecessary(payload, String.class), payload);
        Assert.assertEquals(converter.convertIfNecessary(payload, InputStream.class).getClass(), ByteArrayInputStream.class);
        Assert.assertEquals(converter.convertIfNecessary(payload, Source.class).getClass(), StringSource.class);
        Assert.assertEquals(converter.convertIfNecessary(payload, byte[].class), payload.getBytes());
        Assert.assertEquals(converter.convertIfNecessary(payload.getBytes(), String.class), Arrays.toString(payload.getBytes()));
        Assert.assertEquals(converter.convertIfNecessary(ByteBuffer.wrap(payload.getBytes()), String.class), payload);
    }

}
