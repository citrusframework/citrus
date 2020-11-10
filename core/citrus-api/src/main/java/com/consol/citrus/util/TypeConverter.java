package com.consol.citrus.util;

import java.util.HashMap;
import java.util.Map;

import com.consol.citrus.CitrusSettings;
import com.consol.citrus.spi.ResourcePathTypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 */
public interface TypeConverter {

    /** Logger */
    Logger LOG = LoggerFactory.getLogger(TypeConverter.class);

    /** Type converter resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/type/converter";

    Map<String, TypeConverter> converters = new HashMap<>();

    /**
     * Resolves all available converters from resource path lookup. Scans classpath for converter meta information
     * and instantiates those converters.
     * @return
     */
    static Map<String, TypeConverter> lookup() {
        if (converters.isEmpty()) {
            converters.putAll(new ResourcePathTypeResolver().resolveAll(RESOURCE_PATH));

            if (converters.size() == 0) {
                converters.put("default", new DefaultTypeConverter());
            }

            if (LOG.isDebugEnabled()) {
                converters.forEach((k, v) -> LOG.debug(String.format("Found type converter '%s' as %s", k, v.getClass())));
            }
        }

        return converters;
    }

    /**
     * Lookup default type converter specified by resource path lookup and/or environment settings. In case only a single type converter is loaded
     * via resource path lookup this converter is used regardless of any environment settings. If there are multiple converter implementations
     * on the classpath the environment settings must specify the default.
     *
     * If no converter implementation is given via resource path lookup the default implementation is returned.
     * @return type converter to use by default.
     */
    static TypeConverter lookupDefault() {
        String name = CitrusSettings.getTypeConverter();

        if (lookup().size() == 1) {
            Map.Entry<String, TypeConverter> converterEntry = lookup().entrySet().iterator().next();
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Using type converter '%s'", converterEntry.getKey()));
            }

            return converterEntry.getValue();
        } else if (lookup().containsKey(name)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Using type converter '%s'", name));
            }

            return lookup().get(name);
        }

        LOG.warn(String.format("Missing type converter for name '%s' - using default type converter", name));

        return new DefaultTypeConverter();
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
}
