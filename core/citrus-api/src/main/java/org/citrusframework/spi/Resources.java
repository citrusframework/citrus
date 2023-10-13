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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.ReflectionHelper;

/**
 * Helps with resources of type classpath or file system.
 */
public class Resources {

    public static final String CLASSPATH_RESOURCE_PREFIX = "classpath:";
    public static final String FILESYSTEM_RESOURCE_PREFIX = "file:";

    public static final String JAR_RESOURCE_PREFIX = "jar:";
    public static final String HTTP_RESOURCE_PREFIX = "http:";

    public static Resource create(String filePath) {
        if (filePath.startsWith(CLASSPATH_RESOURCE_PREFIX)) {
            return newClasspathResource(filePath);
        } else if (filePath.startsWith(FILESYSTEM_RESOURCE_PREFIX)) {
            return newFileSystemResource(filePath);
        } else if (filePath.startsWith(HTTP_RESOURCE_PREFIX) || filePath.startsWith(JAR_RESOURCE_PREFIX)) {
            try {
                return create(new URL(filePath));
            } catch (MalformedURLException e) {
                throw new CitrusRuntimeException(e);
            }
        }

        Resource file = newFileSystemResource(filePath);
        if (file.exists()) {
            return file;
        }

        return newClasspathResource(filePath);
    }

    public static Resource create(String filePath, Class<?> contextClass) {
        return newClasspathResource(contextClass.getPackageName().replace(".", "/") + "/" + filePath);
    }

    public static Resource create(byte[] content) {
        return new ByteArrayResource(content);
    }

    public static Resource create(File file) {
        return new FileSystemResource(file);
    }

    public static Resource create(URL url) {
        return new UrlResource(url);
    }

    public static Resource newClasspathResource(String filePath) {
        return new ClasspathResource(filePath);
    }

    public static Resource newFileSystemResource(String filePath) {
        return new FileSystemResource(filePath);
    }

    private static String getRawPath(String filePath) {
        if (filePath.startsWith(CLASSPATH_RESOURCE_PREFIX)) {
            return filePath.substring(CLASSPATH_RESOURCE_PREFIX.length());
        }

        if (filePath.startsWith(FILESYSTEM_RESOURCE_PREFIX)) {
            return filePath.substring(FILESYSTEM_RESOURCE_PREFIX.length());
        }

        return filePath;
    }

    /**
     * Resource loaded from classpath.
     */
    public static class ClasspathResource implements Resource {

        private final String location;

        public ClasspathResource(String location) {
            String raw = getRawPath(location);

            if (raw.startsWith("/")) {
                this.location = raw.substring(1);
            } else {
                this.location = raw;
            }
        }

        @Override
        public String getLocation() {
            return location;
        }

        @Override
        public boolean exists() {
            return this.getURI() != null;
        }

        @Override
        public InputStream getInputStream() {
            return ReflectionHelper.class.getClassLoader().getResourceAsStream(location);
        }

        @Override
        public File getFile() {
            if (!exists()) {
                throw new CitrusRuntimeException(String.format("Failed to load classpath resource %s - does not exist", getLocation()));
            }

            return Paths.get(getURI()).toFile();
        }

        public URI getURI() {
            URL url = ReflectionHelper.class.getClassLoader().getResource(location);
            try {
                return url != null ? url.toURI() : null;
            } catch (URISyntaxException e) {
                throw new CitrusRuntimeException("Failed to load classpath resource", e);
            }
        }
    }

    /**
     * Resource with given byte array content.
     */
    public static class ByteArrayResource implements Resource {

        private final byte[] content;

        public ByteArrayResource(byte[] content) {
            this.content = content;
        }

        @Override
        public String getLocation() {
            return "";
        }

        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public File getFile() {
            throw new UnsupportedOperationException("ByteArrayResource does not provide access to a file");
        }
    }

    /**
     * Resource on the file system.
     */
    public static class FileSystemResource implements Resource {

        private final File file;

        public FileSystemResource(String path) {
            this.file = new File(getRawPath(path));
        }

        public FileSystemResource(File file) {
            this.file = file;
        }

        @Override
        public String getLocation() {
            return file.getPath();
        }

        @Override
        public boolean exists() {
            return file.exists();
        }

        @Override
        public InputStream getInputStream() {
            if (!exists()) {
                throw new CitrusRuntimeException(file + " does not exists");
            }

            if (file.isDirectory()) {
                throw new UnsupportedOperationException(file + " is a directory");
            }

            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new CitrusRuntimeException(file + " does not exists", e);
            }
        }

        @Override
        public File getFile() {
            return file;
        }
    }

    public static class UrlResource implements Resource {

        private final URL url;

        public UrlResource(URL url) {
            this.url = url;
        }

        @Override
        public String getLocation() {
            return url.toString();
        }

        @Override
        public boolean exists() {
            URLConnection connection = null;
            try {
                connection = url.openConnection();
                if (connection instanceof HttpURLConnection) {
                    return ((HttpURLConnection) connection).getResponseCode() == HttpURLConnection.HTTP_OK;
                }

                return connection.getContentLengthLong() > 0;
            } catch (IOException e) {
                throw new CitrusRuntimeException(e);
            } finally {
                // close the http connection to avoid
                // leaking gaps in case of an exception
                if (connection instanceof HttpURLConnection) {
                    ((HttpURLConnection) connection).disconnect();
                }
            }
        }

        @Override
        public InputStream getInputStream() {
            URLConnection connection = null;
            try {
                connection = url.openConnection();
                connection.setUseCaches(false);
                return connection.getInputStream();
            } catch (IOException e) {
                throw new CitrusRuntimeException(e);
            } finally {
                // close the http connection to avoid
                // leaking gaps in case of an exception
                if (connection instanceof HttpURLConnection) {
                    ((HttpURLConnection) connection).disconnect();
                }
            }
        }

        @Override
        public File getFile() {
            if (!"file".equals(url.getProtocol())) {
                throw new CitrusRuntimeException("Failed to resolve to absolute file path because it does not reside in the file system: " + url);
            }
            try {
                return new File(url.toURI().getSchemeSpecificPart());
            } catch (URISyntaxException ex) {
                return new File(url.getFile());
            }
        }
    }
}
