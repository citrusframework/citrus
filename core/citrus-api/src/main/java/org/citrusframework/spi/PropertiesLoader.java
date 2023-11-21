package org.citrusframework.spi;

import org.citrusframework.exceptions.CitrusRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertiesLoader {

    private PropertiesLoader() {
        // Not intended for instantiation
    }

    public static Properties loadProperties(Resource resource) {
        Properties properties = new Properties();
        try (InputStream inputStream = resource.getInputStream()) {
            System.out.println("Resource: " + resource);
            loadProperties(resource.getLocation(), properties, inputStream);
        } catch (IOException e) {
            throwPropertiesLoadingFailedException(resource.getLocation(), e);
        }
        return properties;
    }

    public static Properties loadProperties(String path) {
        Properties properties = new Properties();
        try (InputStream in = ResourcePathTypeResolver.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new CitrusRuntimeException(String.format("Failed to locate resource path '%s'!", path));
            }

            loadProperties(path, properties, in);
        } catch (IOException e) {
            throwPropertiesLoadingFailedException(path, e);
        }

        return properties;
    }

    private static void throwPropertiesLoadingFailedException(String path, IOException e) {
        throw new CitrusRuntimeException(String.format("Unable to load properties from resource path configuration at '%s'", path), e);
    }

    private static void loadProperties(String path, Properties properties, InputStream in) throws IOException {
        if (path.endsWith(".xml")) {
            properties.loadFromXML(in);
        } else {
            properties.load(in);
        }
    }
}
