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

package org.citrusframework.camel.context;

import java.util.Map;
import java.util.Set;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.SimpleReferenceResolver;
import org.apache.camel.CamelContext;

public class CamelReferenceResolver implements ReferenceResolver {

    private CamelContext camelContext;

    private ReferenceResolver fallback = new SimpleReferenceResolver();

    public CamelReferenceResolver() {
        super();
    }

    /**
     * Constructor initializes with given Camel context.
     * @param camelContext
     */
    public CamelReferenceResolver(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @Override
    public <T> T resolve(Class<T> type) {
        Set<T> components = camelContext.getRegistry().findByType(type);

        if (components.isEmpty()) {
            throw new CitrusRuntimeException(String.format("Unable to find bean reference for type '%s'", type));
        }

        return components.iterator().next();
    }

    @Override
    public <T> T resolve(String name, Class<T> type) {
        T component = camelContext.getRegistry().lookupByNameAndType(name, type);

        if (component != null) {
            return component;
        }

        if (fallback.isResolvable(name)) {
            return fallback.resolve(name, type);
        }
        throw new CitrusRuntimeException(String.format("Unable to find bean reference for name '%s'", name));
    }

    @Override
    public Object resolve(String name) {
        Object component = camelContext.getRegistry().lookupByName(name);

        if (component != null) {
            return component;
        }

        if (fallback.isResolvable(name)) {
            return fallback.resolve(name);
        }

        throw new CitrusRuntimeException(String.format("Unable to find bean reference for name '%s'", name));
    }

    @Override
    public <T> Map<String, T> resolveAll(Class<T> requiredType) {
        return camelContext.getRegistry().findByTypeWithName(requiredType);
    }

    @Override
    public boolean isResolvable(String name) {
        return camelContext.getRegistry().lookupByName(name) != null || fallback.isResolvable(name);
    }

    @Override
    public boolean isResolvable(Class<?> type) {
        return !camelContext.getRegistry().findByType(type).isEmpty() || fallback.isResolvable(type);
    }

    @Override
    public boolean isResolvable(String name, Class<?> type) {
        return camelContext.getRegistry().lookupByNameAndType(name, type) != null || fallback.isResolvable(name, type);
    }

    /**
     * Specifies the fallback.
     * @param fallback
     */
    public CamelReferenceResolver withFallback(ReferenceResolver fallback) {
        this.fallback = fallback;
        return this;
    }

    @Override
    public void bind(String name, Object value) {
        camelContext.getRegistry().bind(name, value);
        fallback.bind(name, value);
    }

    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public ReferenceResolver getFallback() {
        return fallback;
    }

    /**
     * Specifies the fallback.
     * @param fallback
     */
    public void setFallback(ReferenceResolver fallback) {
        this.fallback = fallback;
    }
}
