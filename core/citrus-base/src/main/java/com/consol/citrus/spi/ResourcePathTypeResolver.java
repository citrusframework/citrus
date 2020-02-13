package com.consol.citrus.spi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Type resolver looks for a resource path mapping file in classpath and loads the file as configuration properties.
 * The properties should container a default type that resolves to a class for this type resolver.
 * @author Christoph Deppisch
 */
public class ResourcePathTypeResolver<T> {

    /** Property name that holds the type information to resolve */
    public static final String DEFAULT_TYPE_PROPERTY = "type";

    private final String resourcePath;
    private final Class<T> type;

    /**
     * Default constructor initializes with given resource path.
     * @param resourcePath
     */
    public ResourcePathTypeResolver(String resourcePath, Class<T> type) {
        this.type = type;
        if (resourcePath.endsWith("/")) {
            this.resourcePath = resourcePath;
        } else {
            this.resourcePath = resourcePath + "/";
        }
    }

    /**
     * Resolve resource path property file with given name and load the type default property.
     * @param name
     * @return
     */
    public T resolve(String name) {
        String uri = resourcePath + name;

        InputStream in = ResourcePathTypeResolver.class.getResourceAsStream(uri);
        if (in == null) {
            throw new CitrusRuntimeException(String.format("Failed to locate resource path '%s'", uri));
        }

        try {
            Properties config = new Properties();
            config.load(in);
            return loadClass(config.getProperty(DEFAULT_TYPE_PROPERTY));
        } catch (IOException e) {
            throw new CitrusRuntimeException(String.format("Unable to load properties from resource path configuration at '%s'", uri), e);
        }
    }

    /**
     * Load class with given name and cast to target type.
     * @param className
     * @return
     */
    private T loadClass(String className) {
        try {
            return type.cast(Class.forName(className).newInstance());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new CitrusRuntimeException(String.format("Failed to resolve classpath resource of type '%s'", className), e);
        }
    }
}
