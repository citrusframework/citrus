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

package org.citrusframework.camel.util;

import java.util.Map;

import org.citrusframework.CitrusSettings;
import org.citrusframework.util.DefaultTypeConverter;
import org.citrusframework.groovy.util.GroovyTypeConverter;
import org.citrusframework.spring.util.SpringBeanTypeConverter;
import org.citrusframework.util.TypeConversionUtils;
import org.citrusframework.util.TypeConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@Isolated("Mutates global TypeConverter state via System properties")
public class TypeConverterTest {

    @Test
    public void testLookup() {
        try {
            Assertions.assertEquals(DefaultTypeConverter.class, TypeConverter.lookupDefault().getClass());

            System.setProperty(CitrusSettings.TYPE_CONVERTER_PROPERTY, TypeConverter.APACHE_CAMEL);
            TypeConversionUtils.loadDefaultConverter();

            Map<String, TypeConverter> converters = TypeConverter.lookup();
            Assertions.assertEquals(3L, converters.size());
            Assertions.assertEquals(CamelTypeConverter.class, converters.get(TypeConverter.APACHE_CAMEL).getClass());
            Assertions.assertEquals(TypeConverter.lookupDefault(), converters.get(TypeConverter.APACHE_CAMEL));

            Assertions.assertEquals(GroovyTypeConverter.class, converters.get(TypeConverter.GROOVY).getClass());
            Assertions.assertEquals(SpringBeanTypeConverter.class, converters.get(TypeConverter.SPRING).getClass());

            Assertions.assertFalse(TypeConverter.lookup().containsKey(TypeConverter.DEFAULT));
        } finally {
            System.setProperty(CitrusSettings.TYPE_CONVERTER_PROPERTY, CitrusSettings.TYPE_CONVERTER_DEFAULT);
            Assertions.assertEquals(DefaultTypeConverter.class, TypeConverter.lookupDefault().getClass());
            TypeConversionUtils.loadDefaultConverter();
        }
    }
}
