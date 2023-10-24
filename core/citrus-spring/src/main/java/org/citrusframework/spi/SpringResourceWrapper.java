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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.citrusframework.util.FileUtils;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalConverter;
import org.springframework.core.convert.converter.Converter;

public class SpringResourceWrapper implements org.springframework.core.io.Resource {

    private final Resource delegate;

    public SpringResourceWrapper(Resource delegate) {
        this.delegate = delegate;
    }

    public static SpringResourceWrapper from(Resource resource) {
        return new SpringResourceWrapper(resource);
    }

    @Override
    public boolean exists() {
        return delegate.exists();
    }

    @Override
    public URL getURL() throws IOException {
        return delegate.getURL();
    }

    @Override
    public URI getURI() throws IOException {
        return delegate.getURI();
    }

    @Override
    public File getFile() throws IOException {
        return delegate.getFile();
    }

    @Override
    public long contentLength() throws IOException {
        try {
            return delegate.getFile().length();
        } catch (Exception e) {
            try {
                return Files.size(Path.of(delegate.getLocation()));
            } catch (NoSuchFileException ex) {
                throw new FileNotFoundException(ex.getMessage());
            }
        }
    }

    @Override
    public long lastModified() throws IOException {
        try {
            return delegate.getFile().lastModified();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public org.springframework.core.io.Resource createRelative(String relativePath) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFilename() {
        return FileUtils.getFileName(delegate.getLocation());
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return delegate.getInputStream();
    }

    /**
     * Spring bean converter able to convert from Citrus resource to Spring resource implementation.
     */
    public static class ResourceConverter implements Converter<Resource, org.springframework.core.io.Resource>, ConditionalConverter {
        @Override
        public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
            return Resource.class.isAssignableFrom(sourceType.getObjectType()) && org.springframework.core.io.Resource.class.isAssignableFrom(targetType.getObjectType());
        }

        @Override
        public org.springframework.core.io.Resource convert(Resource source) {
            return new SpringResourceWrapper(source);
        }
    }
}
