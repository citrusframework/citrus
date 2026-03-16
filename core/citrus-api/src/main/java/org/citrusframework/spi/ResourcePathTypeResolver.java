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

package org.citrusframework.spi;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jakarta.annotation.Nullable;
import org.citrusframework.util.ClassLoaderHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.file.FileSystems.newFileSystem;
import static java.util.Collections.singletonMap;
import static java.util.Collections.synchronizedList;
import static java.util.Objects.nonNull;
import static org.citrusframework.spi.PropertiesLoader.loadProperties;
import static org.citrusframework.util.ObjectHelper.assertNotNull;

/**
 * Type resolver resolves references via resource path lookup. Provided resource paths should point
 * to a resource in classpath (e.g. META-INF/my/resource/path/file-name). The resolver will try to
 * locate the resource as classpath resource and read the file as property file. By default, the
 * resolver reads the default type resolver property {@link TypeResolver#DEFAULT_TYPE_PROPERTY} and
 * instantiates a new instance for the given type information. Note that, in order to reduce
 * classpath scanning, the resolver caches the results of specific classpath scans.
 * <p>
 * A possible property file content that represents the resource in classpath could look like this:
 * <pre>
 * type=org.citrusframework.MySpecialPojo
 * </pre>
 * <p>
 * Users can define custom property names to read instead of the default
 * {@link TypeResolver#DEFAULT_TYPE_PROPERTY}.
 * <p>
 * Users can define custom property names to read instead of the default
 * {@link TypeResolver#DEFAULT_TYPE_PROPERTY}.
 */
public class ResourcePathTypeResolver implements TypeResolver {

    public static final @Nullable URL ROOT = ResourcePathTypeResolver.class
        .getProtectionDomain()
        .getCodeSource()
        .getLocation();

    private static FileSystem rootFs = null;

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(ResourcePathTypeResolver.class);

    /**
     * Base path for resources
     */
    private final String resourceBasePath;

    /**
     * Resolver resolves all resources for a given path from classpath
     */
    private final ClasspathResourceResolver classpathResourceResolver = new ClasspathResourceResolver();

    /**
     * Zip entries as String, so the archive is read only once
     */
    private final List<String> zipEntriesAsString = synchronizedList(new ArrayList<>());

    /**
     * Cached properties loaded from classpath scans.
     */
    private final Map<String, Properties> resourceProperties = new ConcurrentHashMap<>();

    /**
     * Cached specific type names as resolved from classpath.
     */
    private final Map<String, Map<String, String>> typeCache = new ConcurrentHashMap<>();

    static {
        if (rootIsNotCitrusApiJar()) {
            try {
                rootFs = newFileSystem(new File(ROOT.toString().substring("file:".length())).toPath());
            } catch (IOException e) {
                logger.debug("Failed to create File system from jar '{}'", ROOT, e);
            }
        }
    }

    private static boolean rootIsNotCitrusApiJar() {
        return nonNull(ROOT)
                && ROOT.toString().matches(".*jar(!/)?")
                && !(ROOT.toString().replace("\\", "/").matches(".*/citrus-api-\\d+\\.\\d+\\.\\d+(-.*)?\\.jar"));
    }

    /**
     * Default constructor using META-INF resource base path.
     */
    public ResourcePathTypeResolver() {
        this("META-INF");
    }

    /**
     * Default constructor initializes with given resource path.
     */
    public ResourcePathTypeResolver(String resourceBasePath) {
        if (resourceBasePath.endsWith("/")) {
            this.resourceBasePath = resourceBasePath.substring(0, resourceBasePath.length() - 1);
        } else {
            this.resourceBasePath = resourceBasePath;
        }
    }

    @Override
    public String resolveProperty(String resourcePath, String property) {
        return readAsProperties(resourcePath).getProperty(property);
    }

    @Override
    public <T> T resolve(String resourcePath, String property, Object... initargs) {
        String cacheKey = toCacheKey(resourcePath, property, "NO_KEY_PROPERTY");

        Map<String, String> map = typeCache.computeIfAbsent(
            cacheKey,
            key -> singletonMap(key, resolveProperty(resourcePath, property))
        );

        return (T) ClassLoaderHelper.instantiateType(map.get(cacheKey), initargs);
    }

    @Override
    public <T> Map<String, T> resolveAll(String path, String property, String keyProperty) {
        Map<String, String> typeLookup = typeCache.computeIfAbsent(
            toCacheKey(path, property, keyProperty),
            key -> determineTypeLookup(path, property, keyProperty)
        );

        Map<String, T> resources = new HashMap<>();
        typeLookup.forEach((p, type) -> resources.put(p, (T) ClassLoaderHelper.instantiateType(type)));

        return resources;
    }

    /**
     * Determine the type lookup by performing relevant classpath scans.
     */
    private Map<String, String> determineTypeLookup(String path, String property,
        String keyProperty) {
        String fullPath = getFullResourcePath(path);
        Map<String, String> typeLookup = new HashMap<>();

        try {
            Stream.concat(
                    classpathResourceResolver.getResources(fullPath).stream().filter(Objects::nonNull),
                    resolveAllFromJar(fullPath)
                )
                .forEach(resourcePath -> {
                    Path fileName = resourcePath.getFileName();
                    if (fileName == null) {
                        logger.warn("Skip unsupported resource '{}' for resource lookup",
                            resourcePath);
                        return;
                    }

                    if (property.equals(TYPE_PROPERTY_WILDCARD)) {
                        Properties properties = readAsProperties(fullPath + "/" + fileName);
                        for (Entry<Object, Object> prop : properties.entrySet()) {
                            String type = resolveProperty(fullPath + "/" + fileName,
                                prop.getKey().toString());
                            typeLookup.put(fileName + "." + prop.getKey().toString(), type);
                        }
                    } else {
                        String type = resolveProperty(fullPath + "/" + fileName, property);
                        if (keyProperty != null) {
                            typeLookup.put(
                                resolveProperty(fullPath + "/" + fileName, keyProperty),
                                type);
                        } else {
                            typeLookup.put(fileName.toString(), type);
                        }
                    }
                });
        } catch (IOException e) {
            logger.warn("Failed to resolve resources in '{}'", fullPath, e);
        }

        return typeLookup;
    }

    private String toCacheKey(String path, String property, String keyProperty) {
        return path + "$$$" + property + "$$$" + keyProperty;
    }

    private Stream<Path> resolveAllFromJar(String path) {
        ClassLoader classLoader = assertNotNull(ClassLoaderHelper.getClassLoader());

        if (rootIsNotCitrusApiJar() && nonNull(rootFs)) {
            return getZipEntries().stream()
                .filter(entry -> entry.startsWith(path))
                .map(classLoader::getResource)
                .filter(Objects::nonNull)
                .map(entry -> {
                    String[] split = entry.toString().split("!");
                    try {
                        if (split.length > 1) {
                            return rootFs.getPath(split[1]);
                        }

                        return Paths.get(entry.toURI());
                    } catch (URISyntaxException e) {
                        logger.warn("Failed resolve resource from jar '{}'", entry, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull);
        }

        return Stream.of();
    }

    /**
     * This method needs proper synchronization, because all instances of this class will stream on
     * this array.
     *
     * @return the list of zip entries
     */
    private synchronized List<String> getZipEntries() {
        if (zipEntriesAsString.isEmpty()) {
            try (ZipInputStream in = new ZipInputStream(ROOT.openStream())) {
                ZipEntry entry;
                while ((entry = in.getNextEntry()) != null) {
                    zipEntriesAsString.add(entry.getName());
                }
            } catch (IOException e) {
                logger.warn("Failed to open '{}}'", ROOT, e);
            }
        }

        return zipEntriesAsString;
    }

    /**
     * Read resource from classpath and load content as properties. The properties found on the
     * classpath will be cached.
     */
    private Properties readAsProperties(String resourcePath) {
        return resourceProperties.computeIfAbsent(resourcePath, k -> {
            String path = getFullResourcePath(resourcePath);
            return loadProperties(path);
        });
    }

    /**
     * Combine base resource path and given resource path to proper full resource path.
     */
    private String getFullResourcePath(String resourcePath) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            return resourceBasePath;
        } else if (!resourcePath.startsWith(resourceBasePath)) {
            return resourceBasePath + "/" + resourcePath;
        } else {
            return resourcePath;
        }
    }

    @Override
    public void clearCache() {
        typeCache.clear();
    }
}
