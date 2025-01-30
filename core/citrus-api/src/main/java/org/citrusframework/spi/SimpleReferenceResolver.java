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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * Simple object registry holding in memory key value store to bind and obtain object references by name.
 */
public class SimpleReferenceResolver implements ReferenceResolver, ReferenceRegistry {

    private final ConcurrentHashMap<String, Object> objectStore = new ConcurrentHashMap<>();

    @Override
    public <T> T resolve(Class<T> type) {
        return objectStore.values().stream()
                .filter(v -> v != null && type.isAssignableFrom(v.getClass()))
                .map(type::cast)
                .findFirst()
                .orElseThrow(() -> new CitrusRuntimeException(String.format("Unable to find bean reference for type '%s'", type)));
    }

    @Override
    public Object resolve(String name) {
        if (!objectStore.containsKey(name)) {
            throw new CitrusRuntimeException(String.format("Unable to find bean reference for name '%s'", name));
        }

        return objectStore.get(name);
    }

    @Override
    public <T> T resolve(String name, Class<T> type) {
        return objectStore.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> name.equals(entry.getKey()) && type.isAssignableFrom(entry.getValue().getClass()))
                .map(Map.Entry::getValue)
                .map(type::cast)
                .findFirst()
                .orElseThrow(() -> new CitrusRuntimeException(String.format("Unable to find bean reference for name '%s'", name)));
    }

    @Override
    public <T> Map<String, T> resolveAll(Class<T> type) {
        return objectStore.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> type.isAssignableFrom(entry.getValue().getClass()))
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> type.cast(entry.getValue())));
    }

    @Override
    public boolean isResolvable(String name) {
        return objectStore.containsKey(name);
    }

    @Override
    public boolean isResolvable(Class<?> type) {
        return objectStore.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .anyMatch(entry -> type.isAssignableFrom(entry.getValue().getClass()));
    }

    @Override
    public boolean isResolvable(String name, Class<?> type) {
        return objectStore.containsKey(name) && type.isAssignableFrom(objectStore.get(name).getClass());
    }

    @Override
    public void bind(String name, Object value) {
        if (value != null) {
            this.objectStore.put(name, value);
        }
    }
}
