/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.util;

import java.util.Map;

import com.consol.citrus.CitrusSettings;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class TypeConverterTest {

    @Test
    public void testLookup() {
        try {
            Assert.assertEquals(TypeConverter.lookupDefault().getClass(), DefaultTypeConverter.class);

            System.setProperty(CitrusSettings.TYPE_CONVERTER_PROPERTY, TypeConverter.GROOVY);
            TypeConversionUtils.loadDefaultConverter();

            Map<String, TypeConverter> converters = TypeConverter.lookup();
            Assert.assertEquals(converters.size(), 2L);
            Assert.assertEquals(converters.get(TypeConverter.GROOVY).getClass(), GroovyTypeConverter.class);
            Assert.assertEquals(converters.get(TypeConverter.GROOVY), TypeConverter.lookupDefault());

            Assert.assertEquals(converters.get(TypeConverter.SPRING).getClass(), SpringBeanTypeConverter.class);

            Assert.assertFalse(TypeConverter.lookup().containsKey(TypeConverter.DEFAULT));
        } finally {
            System.setProperty(CitrusSettings.TYPE_CONVERTER_PROPERTY, CitrusSettings.TYPE_CONVERTER_DEFAULT);
            Assert.assertEquals(TypeConverter.lookupDefault().getClass(), DefaultTypeConverter.class);
            TypeConversionUtils.loadDefaultConverter();
        }
    }
}
