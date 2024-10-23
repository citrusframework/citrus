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

import org.citrusframework.CitrusSettings;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public interface TypeConverter {

    /** Logger */
    Logger logger = LoggerFactory.getLogger(TypeConverter.class);

    /** Type converter resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/type/converter";

    Map<String, TypeConverter> converters = new HashMap<>();

    String DEFAULT = "default";
    String SPRING = "spring";
    String APACHE_CAMEL = "camel";
    String GROOVY = "groovy";

    /**
     * Resolves all available converters from resource path lookup. Scans classpath for converter meta information
     * and instantiates those converters.
     * @return
     */
    static Map<String, TypeConverter> lookup() {
        if (converters.isEmpty()) {
            converters.putAll(new ResourcePathTypeResolver().resolveAll(RESOURCE_PATH));

            if (converters.isEmpty()) {
                converters.put(DEFAULT, DefaultTypeConverter.INSTANCE);
            }

            if (logger.isDebugEnabled()) {
                converters.forEach((k, v) -> logger.debug("Found type converter '{}' as {}", k, v.getClass()));
            }
        }

        return converters;
    }

    /**
     * Lookup default type converter specified by resource path lookup and/or environment settings. In case only a single type converter is loaded
     * via resource path lookup this converter is used regardless of any environment settings. If there are multiple converter implementations
     * on the classpath the environment settings must specify the default.
     * <p>
     * If no converter implementation is given via resource path lookup the default implementation is returned.
     * @return type converter to use by default.
     */
    static TypeConverter lookupDefault() {
        return lookupDefault(DefaultTypeConverter.INSTANCE);
    }

    /**
     * Lookup default type converter specified by resource path lookup and/or environment settings. In case only a single type converter is loaded
     * via resource path lookup this converter is used regardless of any environment settings. If there are multiple converter implementations
     * on the classpath the environment settings must specify the default.
     * <p>
     * If no converter implementation is given via resource path lookup the default implementation is returned.
     *
     * @param defaultTypeConverter the fallback default converter
     * @return type converter to use by default.
     */
    static TypeConverter lookupDefault(TypeConverter defaultTypeConverter) {
        String name = CitrusSettings.getTypeConverter();

        Map<String, TypeConverter> converters = lookup();
        if (converters.size() == 1) {
            Map.Entry<String, TypeConverter> converterEntry = converters.entrySet().iterator().next();
            if (logger.isDebugEnabled()) {
                logger.debug("Using type converter '{}'", converterEntry.getKey());
            }

            return converterEntry.getValue();
        } else if (converters.containsKey(name)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Using type converter '{}'", name);
            }

            return converters.get(name);
        }

        if (!CitrusSettings.TYPE_CONVERTER_DEFAULT.equals(name)) {
            logger.warn("Missing type converter for name '{}' - using default type converter", name);
        }

        return defaultTypeConverter;
    }

    /**
     * Converts target object to required type if necessary.
     *
     * @param target
     * @param type
     * @param <T>
     * @return
     */
    <T> T convertIfNecessary(Object target, Class<T> type);

    /**
     * Converts String value object to given type.
     * @param value
     * @param type
     * @param <T>
     * @return
     */
    <T> T convertStringToType(String value, Class<T> type);

    default String asNormalizedArrayString(Object target) {
        return convertIfNecessary(target, String.class).replaceAll("^\\[", "").replaceAll("]$", "").replaceAll(",\\s", ",");
    }
}
