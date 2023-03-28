package org.citrusframework.spi;

import java.util.Map;

/**
 * Resolves types by searching for classpath resource mapping files in order to resolve class references at runtime.
 * @author Christoph Deppisch
 */
public interface TypeResolver {

    /** Property name that holds the type information to resolve */
    String DEFAULT_TYPE_PROPERTY = "type";

    /** Property name to mark that multiple types will be present all types are loaded */
    String TYPE_PROPERTY_WILDCARD = "*";

    /**
     * Resolve resource path property file with given name and load given property.
     * @param resourcePath
     * @param property
     * @return
     */
    String resolveProperty(String resourcePath, String property);

    /**
     * Load default type information from given resource path property file and create new instance of given type.
     * @param resourcePath
     * @param initargs
     * @return
     */
    default <T> T resolve(String resourcePath, Object ... initargs) {
        return resolve(resourcePath, DEFAULT_TYPE_PROPERTY, initargs);
    }

    /**
     * Load given property from given resource path property file and create new instance of given type. The type information
     * is read by the given property in the resource file.
     * @param resourcePath
     * @param property
     * @param initargs
     * @return
     */
    <T> T resolve(String resourcePath, String property, Object ... initargs);

    /**
     * Load all resources and create new instance of given type. The type information is read by
     * the given property in the resource file. The keys in the resulting map represent the resource file names.
     * @param <T>
     * @return
     */
    default <T> Map<String, T> resolveAll() {
        return resolveAll("");
    }

    /**
     * Load all resources in given resource path and create new instance of given type. The type information is read by
     * the given property in the resource file. The keys in the resulting map represent the resource file names.
     * @param resourcePath
     * @param <T>
     * @return
     */
    default <T> Map<String, T> resolveAll(String resourcePath) {
        return resolveAll(resourcePath, DEFAULT_TYPE_PROPERTY);
    }

    /**
     * Load all resources in given resource path and create new instance of given type. The type information is read by
     * the given property in the resource file. The keys in the resulting map represent the resource file names.
     * @param resourcePath
     * @param property
     * @param <T>
     * @return
     */
    default <T> Map<String, T> resolveAll(String resourcePath, String property) {
        return resolveAll(resourcePath, property, null);
    }

    /**
     * Load all resources in given resource path and create new instance of given type. The type information is read by
     * the given property in the resource file.
     * @param resourcePath
     * @param property
     * @param keyProperty
     * @param <T>
     * @return
     */
    <T> Map<String, T> resolveAll(String resourcePath, String property, String keyProperty);
}
