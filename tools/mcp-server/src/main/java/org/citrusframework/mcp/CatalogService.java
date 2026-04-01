/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citrusframework.mcp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.enterprise.context.ApplicationScoped;
import org.citrusframework.CitrusVersion;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.ClassLoaderHelper;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

/**
 * Shared service for loading and caching Citrus catalogs.
 * <p>
 * Catalogs are cached by {@code (citrusVersion, kind)} tuple. The default catalog (no version
 * parameters) is created once at startup.
 */
@ApplicationScoped
public class CatalogService {

    private final ConcurrentMap<CatalogKey, Map<String, ComponentDefinition>> cache = new ConcurrentHashMap<>();
    private final ConcurrentMap<CatalogKey, String> schemas = new ConcurrentHashMap<>();

    /**
     * Default constructor automatically loads the catalog for this current version.
     */
    CatalogService() {
        loadCatalog(CitrusVersion.version());
    }

    /**
     * Load a catalog for the given version. Results are cached by the {@code (citrusVersion, kind)} tuple so that repeated calls with the same parameters do not trigger
     * redundant Maven downloads.
     *
     * @param  version the Citrus version to query, or null for the default
     */
    public void loadCatalog(String version) {
        CatalogIndex index = loadCatalogIndex(version);

        for (Catalog catalog : index.catalogs.values()) {
            CatalogKey key = new CatalogKey(catalog.name(), catalog.version());
            if (!cache.containsKey(key)) {
                cache.put(key, loadComponentDefinitions(catalog));
            }
        }

        for (Catalog catalog : index.schemas.values()) {
            CatalogKey key = new CatalogKey(catalog.name(), catalog.version());
            if (!schemas.containsKey(key)) {
                schemas.put(key, loadSchema(catalog));
            }
        }
    }

    public Map<String, ComponentDefinition> getTestActions() {
        return cache.get(new CatalogKey("actions", getVersion()));
    }

    public Map<String, ComponentDefinition> getTestContainers() {
        return cache.get(new CatalogKey("containers", getVersion()));
    }

    public Map<String, ComponentDefinition> getEndpoints() {
        return cache.get(new CatalogKey("endpoints", getVersion()));
    }

    public Map<String, ComponentDefinition> getFunctions() {
        return cache.get(new CatalogKey("functions", getVersion()));
    }

    public Map<String, ComponentDefinition> getValidationMatcher() {
        return cache.get(new CatalogKey("validationMatcher", getVersion()));
    }

    public String getSchema(String name) {
        return schemas.get(new CatalogKey(name, getVersion()));
    }

    public String getVersion() {
        if (StringUtils.hasText(CitrusVersion.version())) {
            return CitrusVersion.version();
        } else {
            return loadCatalogIndex("").version();
        }
    }

    private CatalogIndex loadCatalogIndex(String version) {
        if (StringUtils.hasText(version)) {
            return JsonSupport.json().readValue(ClassLoaderHelper.getClassLoader()
                    .getResourceAsStream("citrus-catalog/citrus/%s/index.json".formatted(version)), CatalogIndex.class);
        } else {
            return JsonSupport.json().readValue(ClassLoaderHelper.getClassLoader()
                    .getResourceAsStream("citrus-catalog/citrus/index.json"), CatalogIndex.class);
        }
    }

    /**
     * Loads component definitions for this catalog.
     */
    private String loadSchema(Catalog catalog) {
        try {
            return FileUtils.readToString(ClassLoaderHelper.getClassLoader().getResourceAsStream("citrus-catalog/citrus/%s/%s".formatted(catalog.version(), catalog.file())))
                    .replaceAll("\\r?\\n\\s*", "");
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read schema '%s'".formatted(catalog.name()), e);
        }
    }

    /**
     * Loads component definitions for this catalog.
     */
    private Map<String, ComponentDefinition> loadComponentDefinitions(Catalog catalog) {
        return loadComponentDefinitions(catalog.name(), catalog.version(), catalog.file());
    }

    private Map<String, ComponentDefinition> loadComponentDefinitions(String kind, String version, String file) {
        try {
            Map<String, ComponentDefinition> components = new HashMap<>();
            JsonNode raw = JsonSupport.json().readTree(ClassLoaderHelper.getClassLoader().getResourceAsStream("citrus-catalog/citrus/%s/%s".formatted(version, file)));
            raw.propertyNames().forEach(
                    name -> components.put(name, JsonSupport.json().convertValue(raw.get(name), ComponentDefinition.class)));
            return components;
        } catch (JacksonException e) {
            throw new CitrusRuntimeException("Failed to read component aggregate schema definitions of kind '%s'".formatted(kind), e);
        }
    }

    /**
     * Extracts the pure test action name and removes a potential group prefix.
     */
    public String resolveName(String name, String group) {
        if (group == null) {
            return name;
        }

        if (name.startsWith(group + "-")) {
            return name.substring(group.length() + 1);
        }

        return name;
    }

    /**
     * Citrus catalog holds one to many component definition entries in given file path.
     */
    private record CatalogIndex(String name, String runtime, String version, Map<String, Catalog> catalogs, Map<String, Catalog> schemas) {
    }

    /**
     * Citrus catalog holds one to many component definition entries in given file path.
     */
    private record Catalog(String name, String description, String version, String file) {
    }

    /**
     * A key combines component kind and version.
     */
    private record CatalogKey(String kind, String version) {
    }

    /**
     * Citrus component definition provides information about the component.
     * Components may be one of test actions, containers, endpoints, functions or validation matcher.
     */
    public record ComponentDefinition(String kind, String version, String name, String group, String module,
                               String title, String description, JsonNode propertiesSchema) {
    }
}
