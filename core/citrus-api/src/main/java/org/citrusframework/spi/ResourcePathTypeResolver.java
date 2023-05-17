package org.citrusframework.spi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Type resolver resolves references via resource path lookup. Provided resource paths should point to a resource in classpath
 * (e.g. META-INF/my/resource/path/file-name). The resolver will try to locate the resource as classpath resource and read the file as property
 * file. By default the resolver reads the default type resolver property {@link TypeResolver#DEFAULT_TYPE_PROPERTY} and instantiates a new instance
 * for the given type information.
 *
 * A possible property file content that represents the resource in classpath could look like this:
 * type=org.citrusframework.MySpecialPojo
 *
 * Users can define custom property names to read instead of the default {@link TypeResolver#DEFAULT_TYPE_PROPERTY}.
 * @author Christoph Deppisch
 */
public class ResourcePathTypeResolver implements TypeResolver {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(ResourcePathTypeResolver.class);

    /** Supported static instance field in target - used as a fallback to the default constructor */
    private static final String INSTANCE = "INSTANCE";

    /** Base path for resources */
    private final String resourceBasePath;

    /** Zip entries as String, so the archive is read only once */
    private final List<String> zipEntriesAsString = new ArrayList<>();

    /**
     * Default constructor using META-INF resource base path.
     */
    public ResourcePathTypeResolver() {
        this("META-INF");
    }

    /**
     * Default constructor initializes with given resource path.
     * @param resourceBasePath
     */
    public ResourcePathTypeResolver(String resourceBasePath) {
        if (resourceBasePath.endsWith("/")) {
            this.resourceBasePath = resourceBasePath.substring(0, resourceBasePath.length() -1);
        } else {
            this.resourceBasePath = resourceBasePath;
        }
    }

    @Override
    public String resolveProperty(String resourcePath, String property) {
        return readAsProperties(resourcePath).getProperty(property);
    }

    @Override
    public <T> T resolve(String resourcePath, String property, Object ... initargs) {
        String type = resolveProperty(resourcePath, property);

        try {
            if (initargs.length == 0) {
                return (T) Class.forName(type).getDeclaredConstructor().newInstance();
            } else {
                return (T) getConstructor(Class.forName(type), initargs).newInstance(initargs);
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException |
                NoSuchMethodException | InvocationTargetException e) {

            try {
                if (Arrays.stream(Class.forName(type).getFields()).anyMatch(f -> f.getName().equals(INSTANCE) &&
                        Modifier.isStatic(f.getModifiers()))) {
                    return (T) Class.forName(type).getField(INSTANCE).get(null);
                }
            } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e1) {
                throw new CitrusRuntimeException(String.format("Failed to resolve classpath resource of type '%s'", type), e1);
            }

            LOG.warn(String.format("Neither static instance nor accessible default constructor (%s) is given on type '%s'",
                    Arrays.toString(getParameterTypes(initargs)), type));
            throw new CitrusRuntimeException(String.format("Failed to resolve classpath resource of type '%s'", type), e);
        }
    }

    @Override
    public <T> Map<String, T> resolveAll(String resourcePath, String property, String keyProperty) {
        Map<String, T> resources = new HashMap<>();
        final String path = getFullResourcePath(resourcePath);

        try {
            Stream.concat(
                    Stream.of(new PathMatchingResourcePatternResolver().getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + path + "/*")),
                    resolveAllFromJar(path))
                .forEach(file -> {
                        Optional<String> resourceName = Optional.ofNullable(file.getFilename());
                        if (resourceName.isEmpty()) {
                            LOG.warn(String.format("Skip unsupported resource '%s' for resource lookup", file));
                            return;
                        }

                        if (property.equals(TYPE_PROPERTY_WILDCARD)) {
                            Properties properties = readAsProperties(path + "/" + resourceName.get());
                            for (Map.Entry<Object, Object> prop : properties.entrySet()) {
                                T resource = resolve(path + "/" + resourceName.get(), prop.getKey().toString());
                                resources.put(resourceName.get() + "." + prop.getKey().toString(), resource);
                            }
                        } else {
                            T resource = resolve(path + "/" + resourceName.get(), property);

                            if (keyProperty != null) {
                                resources.put(resolveProperty(path + "/" + resourceName.get(), keyProperty), resource);
                            } else {
                                resources.put(resourceName.get(), resource);
                            }
                        }
                    });
        } catch (IOException e) {
            LOG.warn(String.format("Failed to resolve resources in '%s'", path), e);
        }

        return resources;
    }

    private Stream<Resource> resolveAllFromJar(String path) {
        String rootAsString = ResourcePathTypeResolver.class.getProtectionDomain().getCodeSource().getLocation().toString();
        ClassLoader classLoader = Objects.requireNonNull(ResourcePathTypeResolver.class.getClassLoader());
        if (rootAsString.endsWith(".jar") && !rootAsString.matches(".*" + File.separator + "citrus-api-\\d+\\.\\d+\\.\\d+(-.*)?\\.jar")) {
            return getZipEntries().stream()
                .filter(entry -> entry.startsWith(path))
                .map(classLoader::getResource)
                .filter(Objects::nonNull)
                .map(UrlResource::new);
        }
        return Stream.of();
    }

    private List<String> getZipEntries() {
        if (zipEntriesAsString.isEmpty()) {
            URL root = ResourcePathTypeResolver.class.getProtectionDomain().getCodeSource().getLocation();
            try (ZipInputStream in = new ZipInputStream(root.openStream())) {
                ZipEntry entry;
                while ((entry = in.getNextEntry()) != null) {
                    zipEntriesAsString.add(entry.getName());
                }
            } catch (IOException e) {
                LOG.warn(String.format("Failed to open '%s'", root), e);
            }
        }
        return zipEntriesAsString;
    }

    /**
     * Gets the constructor best matching the given parameter types.
     * @param type
     * @param initargs
     * @return
     */
    private Constructor<?> getConstructor(Class<?> type, Object[] initargs) {
        final Class<?>[] parameterTypes = getParameterTypes(initargs);

        Optional<Constructor<?>> exactMatch = Arrays.stream(type.getDeclaredConstructors())
                .filter(constructor -> Arrays.equals(constructor.getParameterTypes(), parameterTypes))
                .findFirst();

        if (exactMatch.isPresent()) {
            return exactMatch.get();
        }

        Optional<Constructor<?>> match = Arrays.stream(type.getDeclaredConstructors())
                .filter(constructor -> {
                    if (constructor.getParameterCount() != parameterTypes.length) {
                        return false;
                    }

                    for (int i = 0; i < parameterTypes.length; i++) {
                        if (!constructor.getParameterTypes()[i].isAssignableFrom(parameterTypes[i])) {
                            return false;
                        }
                    }

                    return true;
                })
                .findFirst();

        if (match.isPresent()) {
            return match.get();
        }

        throw new IllegalArgumentException(String.format("No matching constructor found for type %s and parameters %s",
                type.getName(), Arrays.toString(parameterTypes)));
    }

    /**
     * Read resource from classpath and load content as properties.
     * @param resourcePath
     * @return
     */
    private Properties readAsProperties(String resourcePath) {
        String path = getFullResourcePath(resourcePath);

        InputStream in = ResourcePathTypeResolver.class.getClassLoader().getResourceAsStream(path);
        if (in == null) {
            throw new CitrusRuntimeException(String.format("Failed to locate resource path '%s'", path));
        }

        try {
            Properties config = new Properties();
            config.load(in);

            return config;
        } catch (IOException e) {
            throw new CitrusRuntimeException(String.format("Unable to load properties from resource path configuration at '%s'", path), e);
        }
    }

    /**
     * Combine base resource path and given resource path to proper full resource path.
     * @param resourcePath
     * @return
     */
    private String getFullResourcePath(String resourcePath) {
        if (resourcePath == null || resourcePath.length() == 0) {
            return resourceBasePath;
        } else if (!resourcePath.startsWith(resourceBasePath)) {
            return resourceBasePath + "/" + resourcePath;
        } else {
            return resourcePath;
        }
    }

    /**
     * Get types of init arguments.
     * @param initargs
     * @return
     */
    private Class<?>[] getParameterTypes(Object... initargs) {
        return Arrays.stream(initargs).map(Object::getClass).toArray(Class[]::new);
    }
}
