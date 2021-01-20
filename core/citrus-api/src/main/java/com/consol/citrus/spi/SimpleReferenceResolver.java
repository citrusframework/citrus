package com.consol.citrus.spi;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Simple object registry holding in memory key value store to bind and obtain object references by name.
 * @author Christoph Deppisch
 */
public class SimpleReferenceResolver implements ReferenceResolver, ReferenceRegistry {

    private final Map<String, Object> objectStore = new HashMap<>();

    @Override
    public <T> T resolve(Class<T> type) {
        return objectStore.values().stream()
                .filter(type::isInstance)
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
                .filter(entry -> name.equals(entry.getKey()) && type.isInstance(entry.getValue()))
                .map(Map.Entry::getValue)
                .map(type::cast)
                .findFirst()
                .orElseThrow(() -> new CitrusRuntimeException(String.format("Unable to find bean reference for name '%s'", name)));
    }

    @Override
    public <T> Map<String, T> resolveAll(Class<T> type) {
        return objectStore.entrySet().stream()
                .filter(entry -> type.isInstance(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> type.cast(entry.getValue())));
    }

    @Override
    public boolean isResolvable(String name) {
        return objectStore.containsKey(name);
    }

    @Override
    public boolean isResolvable(Class<?> type) {
        return objectStore.entrySet().stream()
                .anyMatch(entry -> type.isInstance(entry.getValue()));
    }

    @Override
    public boolean isResolvable(String name, Class<?> type) {
        return objectStore.containsKey(name) && type.equals(objectStore.get(name).getClass());
    }

    @Override
    public void bind(String name, Object value) {
        this.objectStore.put(name, value);
    }
}
