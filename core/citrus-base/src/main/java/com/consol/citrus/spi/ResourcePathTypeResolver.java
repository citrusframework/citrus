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
public class ResourcePathTypeResolver implements TypeResolver {

    private final String resourcePath;

    /**
     * Default constructor initializes with given resource path.
     * @param resourcePath
     */
    public ResourcePathTypeResolver(String resourcePath) {
        if (resourcePath.endsWith("/")) {
            this.resourcePath = resourcePath;
        } else {
            this.resourcePath = resourcePath + "/";
        }
    }

    @Override
    public String resolveProperty(String resourceName, String property) {
        String uri = resourcePath + resourceName;

        InputStream in = ResourcePathTypeResolver.class.getClassLoader().getResourceAsStream(uri);
        if (in == null) {
            throw new CitrusRuntimeException(String.format("Failed to locate resource path '%s'", uri));
        }

        try {
            Properties config = new Properties();
            config.load(in);

            return config.getProperty(property);
        } catch (IOException e) {
            throw new CitrusRuntimeException(String.format("Unable to load properties from resource path configuration at '%s'", uri), e);
        }
    }

    @Override
    public <T> T resolve(String resourceName, String property) {
        String type = resolveProperty(resourceName, property);
        try {
            return (T) Class.forName(type).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new CitrusRuntimeException(String.format("Failed to resolve classpath resource of type '%s'", type), e);
        }
    }
}
