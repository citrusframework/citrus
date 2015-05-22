/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.arquillian.helper;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * Helper sets field values on target objects using reflection.
 *
 * @author Christoph Deppisch
 * @since 2.2
 */
public final class InjectionHelper {

    /**
     * Prevent instantiation.
     */
    private InjectionHelper() {}

    /**
     * Sets the field value for the given object instance via reflection.
     *
     * @param obj       the object instance
     * @param fieldName the field name
     * @param value     the value to set
     */
    public static void inject(Object obj, String fieldName, Object value) throws IllegalAccessException {
        Field field = ReflectionUtils.findField(obj.getClass(), fieldName);
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, obj, value);
    }
}
