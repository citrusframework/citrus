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

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TypeConverterTest {

    @Test
    public void testLookup() {
        Map<String, TypeConverter> converters = TypeConverter.lookup();
        Assertions.assertEquals(1L, converters.size());
        Assertions.assertEquals(DefaultTypeConverter.class, converters.get(TypeConverter.DEFAULT).getClass());
        Assertions.assertEquals(TypeConverter.lookupDefault(), converters.get(TypeConverter.DEFAULT));

        Assertions.assertFalse(TypeConverter.lookup().containsKey(TypeConverter.SPRING));
        Assertions.assertFalse(TypeConverter.lookup().containsKey(TypeConverter.APACHE_CAMEL));
        Assertions.assertFalse(TypeConverter.lookup().containsKey(TypeConverter.GROOVY));
    }

    @Test
    public void shouldLookupConvertersOnceWhenCalledConcurrently() throws Exception {
        ConcurrencyTestUtils.raceRepeatedly(10_000, 16, TypeConverter.converters::clear, TypeConverter::lookup,
                () -> Assertions.assertEquals(1, TypeConverter.converters.size()));
    }
}
