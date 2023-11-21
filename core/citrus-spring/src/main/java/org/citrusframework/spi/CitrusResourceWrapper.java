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

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class CitrusResourceWrapper implements Resource {

    private final org.springframework.core.io.Resource delegate;

    public CitrusResourceWrapper(org.springframework.core.io.Resource delegate) {
        this.delegate = delegate;
    }

    public static CitrusResourceWrapper from(org.springframework.core.io.Resource resource) {
        return new CitrusResourceWrapper(resource);
    }

    @Override
    public String getLocation() {
        try {
            if (delegate instanceof ClassPathResource classPathResource) {
                return classPathResource.getURI().toString();
            }

            return delegate.getFile().getPath();
        } catch (IOException e) {
            return delegate.toString();
        }
    }

    @Override
    public boolean exists() {
        return delegate.exists();
    }

    @Override
    public InputStream getInputStream() {
        try {
            return delegate.getInputStream();
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    @Override
    public File getFile() {
        try {
            return delegate.getFile();
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Spring bean converter implementation able to convert from Spring resource to Citrus resource implementation.
     */
    public static class ResourceConverter implements Converter<org.springframework.core.io.Resource, Resource>, ConditionalConverter {
        @Override
        public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
            return org.springframework.core.io.Resource.class.isAssignableFrom(sourceType.getObjectType()) && Resource.class.isAssignableFrom(targetType.getObjectType());
        }

        @Override
        public Resource convert(org.springframework.core.io.Resource source) {
            return new CitrusResourceWrapper(source);
        }
    }
}
