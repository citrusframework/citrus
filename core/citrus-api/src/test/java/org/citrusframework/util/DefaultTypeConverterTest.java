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

package org.citrusframework.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.xml.transform.Source;

import org.citrusframework.api.xml.StringSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.MultiValueMap;

/**
 * @since 2.7
 */
public class DefaultTypeConverterTest {

    private final DefaultTypeConverter converter = DefaultTypeConverter.INSTANCE;

    @Test
    public void testConvertIfNecessary() throws IOException {
        String payload = "Hello Citrus!";

        Assertions.assertEquals(Byte.valueOf((byte) 1), converter.convertIfNecessary("1", Byte.class));
        Assertions.assertEquals(Byte.valueOf((byte) 1), converter.convertIfNecessary((byte) 1, Byte.class));
        Assertions.assertEquals(Short.valueOf((short) 1), converter.convertIfNecessary("1", Short.class));
        Assertions.assertEquals(Short.valueOf((short) 1), converter.convertIfNecessary((short) 1, Short.class));
        Assertions.assertEquals(Integer.valueOf(1), converter.convertIfNecessary("1", Integer.class));
        Assertions.assertEquals(Integer.valueOf(1), converter.convertIfNecessary(1, Integer.class));
        Assertions.assertEquals(Long.valueOf(1), converter.convertIfNecessary("1", Long.class));
        Assertions.assertEquals(Long.valueOf(1), converter.convertIfNecessary(1L, Long.class));
        Assertions.assertEquals(Float.valueOf(1.0F), converter.convertIfNecessary("1", Float.class));
        Assertions.assertEquals(Float.valueOf(1.0F), converter.convertIfNecessary(1.0F, Float.class));
        Assertions.assertEquals(Double.valueOf(1.0D), converter.convertIfNecessary("1", Double.class));
        Assertions.assertEquals(Double.valueOf(1.0D), converter.convertIfNecessary(1.0D, Double.class));
        Assertions.assertEquals(Boolean.TRUE, converter.convertIfNecessary("true", Boolean.class));
        Assertions.assertEquals(Boolean.FALSE, converter.convertIfNecessary(Boolean.FALSE, Boolean.class));
        Assertions.assertEquals(Boolean.FALSE, converter.convertIfNecessary("no", Boolean.class));
        Assertions.assertEquals(3, converter.convertIfNecessary("[a,b,c]", String[].class).length);
        Assertions.assertEquals(3, converter.convertIfNecessary("[a, b, c]", String[].class).length);
        Assertions.assertEquals(3, converter.convertIfNecessary("[a,b,c]", List.class).size());
        Assertions.assertEquals(3, converter.convertIfNecessary("[a, b, c]", List.class).size());
        Assertions.assertEquals("value", converter.convertIfNecessary("{key=value}", Map.class).get("key"));
        Assertions.assertEquals("value2", converter.convertIfNecessary("{key1=value1, key2=value2}", Map.class).get("key2"));
        Assertions.assertEquals("value2", converter.convertIfNecessary("{key1=value1,key2=value2}", Map.class).get("key2"));
        Assertions.assertEquals("[foo, bar]", converter.convertIfNecessary(Arrays.asList("foo", "bar"), String.class));
        Assertions.assertEquals("[foo, bar]", converter.convertIfNecessary(new String[] {"foo", "bar"}, String.class));
        Assertions.assertEquals("[foo, bar]", converter.convertIfNecessary(new Object[] {"foo", "bar"}, String.class));
        Assertions.assertEquals("{foo=bar}", converter.convertIfNecessary(Collections.singletonMap("foo", "bar"), String.class));
        Assertions.assertEquals("{foo=bar}", converter.convertIfNecessary(MultiValueMap.fromSingleValue(Collections.singletonMap("foo", "bar")), String.class));
        Assertions.assertEquals("{foo=[bar, foobar]}", converter.convertIfNecessary(MultiValueMap.fromMultiValue(Collections.singletonMap("foo", List.of("bar", "foobar"))), String.class));
        Assertions.assertArrayEquals("{foo=bar}".getBytes(), converter.convertIfNecessary(MultiValueMap.fromSingleValue(Collections.singletonMap("foo", "bar")), byte[].class));
        Assertions.assertArrayEquals("{foo=[bar, foobar]}".getBytes(), converter.convertIfNecessary(MultiValueMap.fromMultiValue(Collections.singletonMap("foo", List.of("bar", "foobar"))), byte[].class));
        Assertions.assertEquals("[1, 2]", converter.convertIfNecessary(Arrays.asList(1, 2), String.class));
        Assertions.assertEquals("[1, 2]", converter.convertIfNecessary(new int[] {1, 2}, String.class));
        Assertions.assertEquals("null", converter.convertIfNecessary(null, String.class));
        Assertions.assertEquals(payload, converter.convertIfNecessary(payload, String.class));
        Assertions.assertEquals(ByteArrayInputStream.class, converter.convertIfNecessary(payload, InputStream.class).getClass());
        Assertions.assertEquals(StringSource.class, converter.convertIfNecessary(payload, Source.class).getClass());
        Assertions.assertArrayEquals(payload.getBytes(), converter.convertIfNecessary(payload, byte[].class));
        Assertions.assertEquals(Arrays.toString(payload.getBytes()), converter.convertIfNecessary(payload.getBytes(), String.class));
        Assertions.assertEquals(payload, converter.convertIfNecessary(ByteBuffer.wrap(payload.getBytes()), String.class));
        Assertions.assertEquals(Arrays.toString(payload.getBytes()), converter.convertIfNecessary(new ByteArrayInputStream(payload.getBytes()), String.class));
        Assertions.assertEquals(Arrays.toString(payload.getBytes()), converter.convertIfNecessary(new GZIPInputStream(new ByteArrayInputStream(getZipped(payload.getBytes()))), String.class));
    }

    private byte[] getZipped(byte[] in) throws IOException {
        try (ByteArrayOutputStream zipped = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(zipped)) {
                gzipOutputStream.write(in);
                gzipOutputStream.flush();
            }
            return zipped.toByteArray();
        }
    }
}
