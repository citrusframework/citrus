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

package org.citrusframework.nosql.model;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.nosql.error.NoSqlErrorClass;
import org.citrusframework.nosql.error.NoSqlException;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.expectThrows;
import static org.testng.Assert.assertTrue;

public class NoSqlValueTest {

    @Test
    public void shouldKeepMissingAndNullDistinct() {
        NoSqlValue nullValue = NoSqlValue.nullValue();
        NoSqlValue missing = NoSqlValue.missing();

        assertTrue(nullValue.isNull());
        assertFalse(nullValue.isMissing());
        assertTrue(missing.isMissing());
        assertFalse(missing.isNull());
        assertNotEquals(nullValue, missing);
        assertEquals(nullValue.render(), "null");
        assertEquals(missing.render(), "");
    }

    @Test
    public void shouldOmitMissingEntriesFromJsonCompatibleModel() {
        Map<String, NoSqlValue> values = new LinkedHashMap<>();
        values.put("present", NoSqlValue.string("value"));
        values.put("explicitNull", NoSqlValue.nullValue());
        values.put("absent", NoSqlValue.missing());

        Object jsonCompatible = NoSqlValue.object(values).toJsonCompatible();

        assertTrue(jsonCompatible instanceof Map<?, ?>);
        Map<?, ?> converted = (Map<?, ?>) jsonCompatible;
        assertEquals(converted.size(), 2);
        assertEquals(converted.get("present"), "value");
        assertTrue(converted.containsKey("explicitNull"));
        assertFalse(converted.containsKey("absent"));
    }

    @Test
    public void shouldRenderObjectAsJson() {
        Map<String, NoSqlValue> values = new LinkedHashMap<>();
        values.put("text", NoSqlValue.string("a\"b"));
        values.put("number", NoSqlValue.number(42));
        values.put("flag", NoSqlValue.bool(true));
        NoSqlValue value = NoSqlValue.object(values);

        assertEquals(value.render(), "{\"text\":\"a\\\"b\",\"number\":42,\"flag\":true}");
    }

    @Test
    public void shouldRenderNumbersWithoutScientificNotation() {
        assertEquals(NoSqlValue.number(new BigDecimal("1E+3")).render(), "1000");
        assertEquals(NoSqlValue.number(new BigDecimal("1.50")).render(), "1.5");
        assertEquals(NoSqlValue.number(1), NoSqlValue.number(new BigDecimal("1.0")));
    }

    @Test
    public void shouldCompareBinaryValuesByContent() {
        NoSqlValue first = NoSqlValue.binary(new byte[]{1, 2, 3});
        NoSqlValue second = NoSqlValue.binary(new byte[]{1, 2, 3});

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(first.render(), "AQID");
    }

    @Test
    public void shouldConvertPlainJavaObjects() {
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("name", "orders");
        source.put("count", 2);
        source.put("items", List.of("a", "b"));
        source.put("nothing", null);

        NoSqlValue value = NoSqlValue.of(source);

        assertEquals(value.render(), "{\"name\":\"orders\",\"count\":2,\"items\":[\"a\",\"b\"],\"nothing\":null}");
    }

    @Test
    public void shouldExposeProviderSpecificValuesExplicitly() {
        NoSqlValue value = NoSqlValue.provider("mongodb", "ObjectId", "507f1f77bcf86cd799439011");

        assertEquals(value.render(), "507f1f77bcf86cd799439011");
        assertEquals(value.toJsonCompatible(), "507f1f77bcf86cd799439011");
        assertTrue(value instanceof NoSqlValue.ProviderValue);
    }

    @Test
    public void shouldReturnSingletonMissingAndNullValues() {
        assertSame(NoSqlValue.missing(), NoSqlValue.missing());
        assertSame(NoSqlValue.nullValue(), NoSqlValue.nullValue());
    }

    @Test
    public void shouldRejectUnsupportedValueType() {
        NoSqlException exception = expectThrows(NoSqlException.class, () -> NoSqlValue.of(new Object()));
        assertEquals(exception.getErrorClass(), NoSqlErrorClass.INVALID_PAYLOAD);
    }
}
