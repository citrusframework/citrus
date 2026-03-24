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

package org.citrusframework.jbang.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.JsonSupport;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.ClassLoaderHelper;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

public interface CodeAnalyzer {

    /**
     * Inspects the given source code and provides detailed information about it as a result.
     */
    default ScanResult scan(Resource sourceFile) throws IOException {
        return scan(FileUtils.getFileName(sourceFile.location()), FileUtils.readToString(sourceFile));
    }

    /**
     * Inspects the given source code and provides detailed information about it as a result.
     */
    default ScanResult scan(Path sourceFile) throws IOException {
        String code = Files.readString(sourceFile);
        return scan(sourceFile.getFileName().toString(), code);
    }

    /**
     * Inspects the given source code and provides detailed information about it as a result.
     */
    default ScanResult scan(String fileName, String code) throws IOException {
        Set<String> modules = scanModules(code);
        Set<String> dependencies = scanDependencies(code);
        Set<String> actions = scanTestActions(code, modules);
        Set<String> containers = scanTestContainers(code, modules);
        Set<String> endpoints = scanEndpoints(code, modules);
        Set<String> functions = scanFunctions(code, modules);
        Set<String> validationMatcher = scanValidationMatcher(code, modules);

        return new ScanResult(fileName,
                modules.toArray(String[]::new),
                dependencies.toArray(String[]::new),
                actions.toArray(String[]::new),
                containers.toArray(String[]::new),
                endpoints.toArray(String[]::new),
                functions.toArray(String[]::new),
                validationMatcher.toArray(String[]::new)
        );
    }

    Set<String> scanModules(String code);

    Set<String> scanDependencies(String code);

    Set<String> scanTestActions(String code, Set<String> modules);

    Set<String> scanTestContainers(String code, Set<String> modules);

    Set<String> scanEndpoints(String code, Set<String> modules);

    default Set<String> scanFunctions(String code, Set<String> modules) {
        Map<String, ComponentDefinition> components = getKnownFunctions();

        Set<String> items = new HashSet<>();
        for (Map.Entry<String, ComponentDefinition> entry : components.entrySet()) {
            ComponentDefinition component = components.get(entry.getKey());
            String name = entry.getKey();

            if (code.contains("%s:%s(".formatted(component.group(), name))) {
                items.add("%s:%s".formatted(component.group(), name));
                if (StringUtils.hasText(component.module())) {
                    modules.add(component.module());
                }
            }
        }

        return items;
    }

    default Set<String> scanValidationMatcher(String code, Set<String> modules) {
        Map<String, ComponentDefinition> components = getKnownValidationMatcher();

        Set<String> items = new HashSet<>();
        for (Map.Entry<String, ComponentDefinition> entry : components.entrySet()) {
            ComponentDefinition component = components.get(entry.getKey());
            String name = entry.getKey();

            if (component.group().equals("citrus")) {
                if (code.contains("@%s(".formatted(name)) || code.contains("@%s@".formatted(name))) {
                    items.add(name);
                    if (StringUtils.hasText(component.module())) {
                        modules.add(component.module());
                    }
                    continue;
                }
            }

            if (code.contains("@%s:%s(".formatted(component.group(), name)) || code.contains("@%s:%s@".formatted(component.group(), name))) {
                items.add(name);
                if (StringUtils.hasText(component.module())) {
                    modules.add(component.module());
                }
            }
        }

        return items;
    }

    default Map<String, ComponentDefinition> getKnownTestActions() {
        return getComponentDefinitions("test-actions");
    }

    default Map<String, ComponentDefinition> getKnownTestContainers() {
        return getComponentDefinitions("test-containers");
    }

    default Map<String, ComponentDefinition> getKnownEndpoints() {
        return getComponentDefinitions("endpoints");
    }

    default Map<String, ComponentDefinition> getKnownFunctions() {
        return getComponentDefinitions("functions");
    }

    default Map<String, ComponentDefinition> getKnownValidationMatcher() {
        return getComponentDefinitions("validation-matcher");
    }

    default Map<String, ComponentDefinition> getComponentDefinitions(String kind) {
        try {
            Map<String, ComponentDefinition> components = new HashMap<>();
            JsonNode raw = JsonSupport.json().readTree(ClassLoaderHelper.getClassLoader().getResourceAsStream("citrus-catalog/citrus/citrus-catalog-aggregate-%s.json".formatted(kind)));
            raw.propertyNames().forEach(
                    name -> components.put(name, JsonSupport.json().convertValue(raw.get(name), ComponentDefinition.class)));
            return components;
        } catch (JacksonException e) {
            throw new CitrusRuntimeException("Failed to read component aggregate schema definitions of kind '%s'".formatted(kind), e);
        }
    }

    default String resolveName(String name, String group, Map<String, ComponentDefinition> components) {
        if (group == null) {
            return name;
        }

        if (components.containsKey(group)) {
            return resolveName(group, components.get(group).group(), components);
        }

        return group;
    }

    /**
     * Analyze result contains detailed information about the inspected test code,
     * such as required modules and used endpoints.
     */
    record ScanResult(String name, String[] modules, String[] dependencies, String[] actions,
                      String[] containers, String[] endpoints, String[] functions, String[] validationMatcher) {
    }

    /**
     * Citrus component definition provides information about the component.
     * Components may be one of test actions, containers, endpoints, functions or validation matcher.
     */
    record ComponentDefinition(String kind, String version, String name, String group, String module,
                               String title, String description, JsonNode propertiesSchema) {
    }
}
