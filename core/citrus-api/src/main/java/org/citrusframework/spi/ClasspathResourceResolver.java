/*
 *  Copyright 2023-2024 the original author or authors.
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements. See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.spi;

import static org.citrusframework.spi.Resources.CLASSPATH_RESOURCE_PREFIX;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolver finds all resources in given classpath resource path.
 */
public class ClasspathResourceResolver {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(ClasspathResourceResolver.class);

    public Set<Path> getClasses(String path) throws IOException {
        Set<Path> resources = new LinkedHashSet<>(16);
        for (ClassLoader classLoader : getClassLoaders()) {
            findResources(path, classLoader, resources, name -> name.endsWith(".class"));
        }
        return resources;
    }

    public Set<Path> getResources(String path) throws IOException {
        Set<Path> resources = new LinkedHashSet<>(16);

        if (path.endsWith("/*")) {
            path = path.substring(0, path.length() - 1);
        } else if (path.endsWith(".*")) {
            path = path.substring(0, path.length() - 2);
        }

        if (path.startsWith(CLASSPATH_RESOURCE_PREFIX)) {
            path = path.substring(CLASSPATH_RESOURCE_PREFIX.length());
        }

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        for (ClassLoader classLoader : getClassLoaders()) {
            findResources(path, classLoader, resources, name -> !name.endsWith(".class"));
        }
        return resources;
    }

    public Set<Path> getResources(String path, String fileNamePattern) throws IOException {
        return getResources(path).stream()
            .filter(resource -> resource.getFileName().toString().matches(fileNamePattern))
            .collect(Collectors.toSet());
    }

    private void findResources(String path, ClassLoader classLoader, Set<Path> result,
        Predicate<String> filter) throws IOException {
        String resourcePath;
        // If the URL is a jar, the URLClassloader.getResources() seems to require a trailing slash.  The
        // trailing slash is harmless for other URLs
        if (!path.isEmpty() && !path.endsWith("/")) {
            resourcePath = path.replace(".", "/") + "/";
        } else {
            resourcePath = path.replace(".", "/");
        }

        Enumeration<URL> urls = classLoader.getResources(resourcePath);

        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            try {
                String urlPath = parseUrlPath(url);
                if (urlPath == null) {
                    continue;
                }

                logger.debug("Scanning for resources in: {}", urlPath);

                File file = new File(urlPath);
                if (file.isDirectory()) {
                    loadResourcesInDirectory(resourcePath, file, result, filter);
                } else {
                    loadResourcesInJar(classLoader, resourcePath, urlPath, result, filter);
                }
            } catch (IOException e) {
                logger.debug("Failed to read entries in url: {}", url, e);
            }
        }
    }

    private void loadResourcesInJar(ClassLoader classLoader, String path,
        String urlPath, Set<Path> resources, Predicate<String> filter)
        throws IOException {

        String[] split = urlPath.split("!");

        if (split.length == 1) {
            readFromJarStream(classLoader, path, urlPath, resources, filter, new FileInputStream(split[0]));
        } else if (split.length == 2) {
            loadFromNestedJar(classLoader, path, urlPath, resources, filter, split[0], split[1]);
        } else {
            throw new CitrusRuntimeException("Unable to load urlPath from : "+urlPath);
        }

    }

    /**
     * Load resources from a nested jar, also known as fat jar. These are typically used in spring
     * boot applications.
     */
    private static void loadFromNestedJar(ClassLoader classLoader, String path, String urlPath,
        Set<Path> resources, Predicate<String> filter, String baseJar, String nestedJar) throws IOException {
        try (JarFile jarFile = new JarFile(baseJar)) {
            JarEntry jarEntry = jarFile.getJarEntry(nestedJar.startsWith("/") ? nestedJar.substring(1) : nestedJar);
            readFromJarStream(classLoader, path, urlPath, resources, filter, jarFile.getInputStream(jarEntry));
        }
    }

    private static void readFromJarStream(ClassLoader classLoader, String path, String urlPath,
        Set<Path> resources, Predicate<String> filter, InputStream jarInputStream) {
        List<String> entries = new ArrayList<>();
        try (JarInputStream jarStream = new JarInputStream(jarInputStream)) {
            JarEntry entry;
            while ((entry = jarStream.getNextJarEntry()) != null) {
                final String name = entry.getName().trim();
                if (!entry.isDirectory() && filter.test(name) && name.startsWith(path)) {
                        entries.add(name);
                }
            }

            for (String name : entries) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Found resource: {} in {}", name.substring(path.length()),
                        urlPath);
                }
                URL url = classLoader.getResource(name);
                if (url != null) {
                    resources.add(Paths.get(name));
                }
            }
        } catch (IOException e) {
            logger.warn("Cannot search jar file '{} due to an IOException: {}", urlPath,
                e.getMessage(), e);
        }
    }


    private void loadResourcesInDirectory(String path, File location, Set<Path> result,
        Predicate<String> filter) {
        File[] files = location.listFiles();
        if (files == null || files.length == 0) {
            return;
        }

        StringBuilder builder;
        for (File file : files) {
            builder = new StringBuilder(100);
            String name = file.getName().trim();

            if (file.isDirectory()) {
                loadResourcesInDirectory(builder.append(path).append(name).append("/").toString(),
                    file, result, filter);
            } else if (file.isFile() && file.exists() && filter.test(name)) {
                logger.trace("Found resource: {} as {}", name, file.toURI());
                result.add(Paths.get(builder.append(path).append(name).toString()));
            }
        }
    }

    private String parseUrlPath(URL url) {
        String urlPath = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8);

        urlPath = removeNestedProtocol(urlPath);

        // osgi bundles should be skipped
        if (url.toString().startsWith("bundle:") || urlPath.startsWith("bundle:")) {
            logger.trace("Skipping OSGi bundle: {}", url);
            return null;
        }

        // bundle resource should be skipped
        if (url.toString().startsWith("bundleresource:") || urlPath.startsWith("bundleresource:")) {
            logger.trace("Skipping bundleresource: {}", url);
            return null;
        }

        // else it may be in a JAR, grab the path to the jar
        return urlPath.contains("!") ? urlPath.substring(0, urlPath.lastIndexOf("!")) : urlPath;
    }

    /**
     * Removes any nested protocol from the URL path, particularly addressing cases when dealing with
     * Spring Boot fat JARs.
     * <p>
     * Two common cases are:
     * 1. 'jar:file:/path' - for nested URLs in Spring Boot versions up to 3.1.x.
     * 2. 'jar:nested:/path' - for nested URLs in Spring Boot versions starting from 3.2.x.
     */
    private static String removeNestedProtocol(String urlPath) {
        int protocolSeparatorIndex = urlPath.indexOf(':');
        if (protocolSeparatorIndex > -1 && protocolSeparatorIndex < urlPath.indexOf('/')) {
            urlPath = urlPath.substring(protocolSeparatorIndex+1);
        }
        return urlPath;
    }

    private Set<ClassLoader> getClassLoaders() {
        Set<ClassLoader> classLoaders = new LinkedHashSet<>();
        try {
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            if (ccl != null) {
                classLoaders.add(ccl);
            }
        } catch (Exception e) {
            logger.warn(
                "Cannot add ContextClassLoader from current thread due {}. This exception will be ignored",
                e.getMessage());
        }

        classLoaders.add(ClasspathResourceResolver.class.getClassLoader());
        return classLoaders;
    }
}
