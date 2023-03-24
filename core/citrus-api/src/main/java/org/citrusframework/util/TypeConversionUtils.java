/*
 * Copyright 2006-2016 the original author or authors.
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

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public abstract class TypeConversionUtils {

    /** Type converter delegate used to convert target objects to required type */
    private static TypeConverter typeConverter = TypeConverter.lookupDefault();

    /**
     * Prevent instantiation.
     */
    private TypeConversionUtils() {
        super();
    }

    /**
     * Reload default type converter.
     */
    public static void loadDefaultConverter() {
        typeConverter = TypeConverter.lookupDefault();
    }

    /**
     * Converts target object to required type if necessary.
     *
     * @param target
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T convertIfNecessary(Object target, Class<T> type) {
        return typeConverter.convertIfNecessary(target, type);
    }

    /**
     * Convert value string to required type.
     * @param value
     * @param type
     * @return
     */
    public static <T> T convertStringToType(String value, Class<T> type) {
        return typeConverter.convertStringToType(value, type);
    }

    /**
     * Convert value string to required type or read bean of type from application context.
     * @param value
     * @param type
     * @param context
     * @return
     */
    public static <T> T convertStringToType(String value, Class<T> type, TestContext context) {
        try {
            return convertStringToType(value, type);
        } catch (CitrusRuntimeException e) {
            // try to resolve bean with reference resolver
            if (context.getReferenceResolver() != null && context.getReferenceResolver().isResolvable(value)) {
                Object bean = context.getReferenceResolver().resolve(value, type);
                if (type.isAssignableFrom(bean.getClass())) {
                    return (T) bean;
                }
            }

            throw new CitrusRuntimeException(String.format("Unable to convert '%s' to required type '%s' - also no bean of required type available in application context", value, type.getName()), e.getCause());
        }
    }
}
