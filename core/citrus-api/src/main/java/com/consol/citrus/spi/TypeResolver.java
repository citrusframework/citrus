package com.consol.citrus.spi;

/**
 * Resolves types by searching for classpath resource mapping files in order to resolve class references at runtime.
 * @author Christoph Deppisch
 */
public interface TypeResolver {

    /** Property name that holds the type information to resolve */
    String DEFAULT_TYPE_PROPERTY = "type";

    /**
     * Resolve resource path property file with given name and load given property.
     * @param resourceName
     * @param property
     * @return
     */
    String resolveProperty(String resourceName, String property);

    /**
     * Load default type information from given resource path property file and create new instance of given type.
     * @param resourceName
     * @return
     */
    default <T> T resolve(String resourceName) {
        return resolve(resourceName, DEFAULT_TYPE_PROPERTY);
    }

    /**
     * Load given property from given resource path property file and create new instance of given type.
     * @param resourceName
     * @param property
     * @return
     */
    <T> T resolve(String resourceName, String property);
}
