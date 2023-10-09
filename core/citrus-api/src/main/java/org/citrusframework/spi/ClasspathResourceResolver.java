/*
 *  Copyright 2023 the original author or authors.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolver finds all resources in given classpath resource path.
 */
public class ClasspathResourceResolver {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(ClasspathResourceResolver.class);

    public Set<Path> getResources(String path) throws IOException {
        Set<Path> resources = new LinkedHashSet<>(16);
        for (ClassLoader classLoader : getClassLoaders()) {
            findResources(path, classLoader, resources);
        }
        return resources;
    }

    private void findResources(String path, ClassLoader classLoader, Set<Path> result) throws IOException {
        String resourcePath;
        // If the URL is a jar, the URLClassloader.getResources() seems to require a trailing slash.  The
        // trailing slash is harmless for other URLs
        if (!path.isEmpty() && !path.endsWith("/")) {
            resourcePath = path + "/";
        } else {
            resourcePath = path;
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
                    loadResourcesInDirectory(resourcePath, file, result);
                } else {
                    loadResourcesInJar(classLoader, resourcePath, new FileInputStream(file), urlPath, result);
                }
            } catch (IOException e) {
                logger.debug("Cannot read entries in url: {}", url, e);
            }
        }
    }

    private void loadResourcesInJar(ClassLoader classLoader, String path, FileInputStream jarInputStream,
                                    String urlPath, Set<Path> resources) {
        List<String> entries = new ArrayList<>();
        try (JarInputStream jarStream = new JarInputStream(jarInputStream);) {
            JarEntry entry;
            while ((entry = jarStream.getNextJarEntry()) != null) {
                final String name = entry.getName().trim();
                if (!entry.isDirectory() && !name.endsWith(".class")) {
                    // name is FQN so it must start with package name
                    if (name.startsWith(path)) {
                        entries.add(name);
                    }
                }
            }

            for (String name : entries) {
                String shortName = name.substring(path.length());
                logger.debug("Found resource: {}", shortName);
                URL url = classLoader.getResource(name);
                if (url !=  null) {
                    try {
                        resources.add(Paths.get(url.toURI()));
                    } catch (FileSystemNotFoundException ex) {
                        // If the file system was not found, assume it's a custom file system that needs to be installed.
                        FileSystems.newFileSystem(url.toURI(), Map.of(), classLoader);
                        resources.add(Paths.get(url.toURI()));
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            logger.warn("Cannot search jar file '{} due to an IOException: {}", urlPath, e.getMessage(), e);
        }
    }

    private void loadResourcesInDirectory(String path, File location, Set<Path> result) {
        File[] files = location.listFiles();
        if (files == null || files.length == 0) {
            return;
        }

        StringBuilder builder;
        for (File file : files) {
            builder = new StringBuilder(100);
            String name = file.getName();
            name = name.trim();
            builder.append(path).append(name);
            String packageOrClass = path == null ? name : builder.toString();

            if (file.isDirectory()) {
                loadResourcesInDirectory(packageOrClass, file, result);
            } else if (file.isFile() && file.exists() && !name.endsWith(".class")) {
                logger.debug("Found resource: {}", name);
                result.add(Paths.get(file.toURI()));
            }
        }
    }

    private String parseUrlPath(URL url) {
        String urlPath = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8);

        if (urlPath.startsWith("file:")) {
            try {
                urlPath = new URI(url.getFile()).getPath();
            } catch (URISyntaxException e) {
                // do nothing
            }

            if (urlPath.startsWith("file:")) {
                urlPath = urlPath.substring(5);
            }
        }

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
        return urlPath.contains("!") ? urlPath.substring(0, urlPath.indexOf("!")) : urlPath;
    }

    private Set<ClassLoader> getClassLoaders() {
        Set<ClassLoader> classLoaders = new LinkedHashSet<>();
        try {
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            if (ccl != null) {
                classLoaders.add(ccl);
            }
        } catch (Exception e) {
            logger.warn("Cannot add ContextClassLoader from current thread due {}. This exception will be ignored", e.getMessage());
        }

        classLoaders.add(ClasspathResourceResolver.class.getClassLoader());
        return classLoaders;
    }
}
