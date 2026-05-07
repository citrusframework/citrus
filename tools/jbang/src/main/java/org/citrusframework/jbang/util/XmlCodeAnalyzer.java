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

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.citrusframework.jbang.CitrusJBangMain;
import org.citrusframework.util.StringUtils;

public class XmlCodeAnalyzer implements CodeAnalyzer {

    private static final Pattern DEPS_MODELINE_PATTERN = Pattern.compile("^<!--\\s*deps:\\s+(.+)\\s*-->$", Pattern.MULTILINE);
    private static final Pattern MODULES_MODELINE_PATTERN = Pattern.compile("^<!--\\s*modules:\\s+(.+)\\s*-->$", Pattern.MULTILINE);

    private static final Pattern CAMEL_ENDPOINT_PATTERN = Pattern.compile("^\\s*<(send|receive) endpoint=\"camel:([^\\s:?]+).*$", Pattern.MULTILINE);

    @Override
    public Set<String> scanModules(String code) {
        Set<String> items = new HashSet<>();

        Matcher matcher = MODULES_MODELINE_PATTERN.matcher(code);
        while (matcher.find()) {
            String modules = matcher.group(1);
            if (modules.contains(",")) {
                for (String module : modules.split(",")) {
                    items.add(module.trim().startsWith(MODULE_PREFIX) ? module.trim() : MODULE_PREFIX + module.trim());
                }
            } else {
                items.add(modules.startsWith(MODULE_PREFIX) ? modules : MODULE_PREFIX + modules);
            }
        }

        return items;
    }

    @Override
    public Set<String> scanDependencies(String code) {
        Set<String> items = new HashSet<>();

        Matcher matcher = DEPS_MODELINE_PATTERN.matcher(code);
        while (matcher.find()) {
            String dependencies = matcher.group(1);
            if (dependencies.contains(",")) {
                for (String dependency : dependencies.split(",")) {
                    items.add(dependency.trim());
                }
            } else {
                items.add(dependencies.trim());
            }
        }

        // Special handling of Camel endpoint URIs
        matcher = CAMEL_ENDPOINT_PATTERN.matcher(code);
        while (matcher.find()) {
            String componentName = matcher.group(2);
            items.add("org.apache.camel:camel-%s:%s".formatted(componentName, CitrusJBangMain.Settings.getCamelVersion()));
        }

        return items;
    }

    @Override
    public Set<String> scanTestActions(String code, Set<String> modules) {
        Map<String, ComponentDefinition> components = getKnownTestActions();

        Set<String> items = new HashSet<>();
        Set<Map.Entry<String, ComponentDefinition>> testGroups = components.entrySet()
                .stream()
                .filter(entry -> "testAction".equals(entry.getValue().kind()))
                .collect(Collectors.toSet());
        for (Map.Entry<String, ComponentDefinition> entry : testGroups) {
            String[] tokens = entry.getKey().split("-");

            boolean allMatch = true;
            for (int i = 0; i < tokens.length && allMatch; i++) {
                allMatch = code.contains("<%s>".formatted(tokens[i])) || code.contains("<%s ".formatted(tokens[i]));
            }

            if (allMatch) {
                ComponentDefinition component = entry.getValue();
                items.add(entry.getKey());
                if (StringUtils.hasText(component.module())) {
                    modules.add(component.module());
                } else if (component.group() != null) {
                    ComponentDefinition parentComponent = components.get(component.group());
                    if (parentComponent != null && parentComponent.module() != null) {
                        modules.add(parentComponent.module());
                    }
                }

                if (entry.getKey().contains("-jbang-")) {
                    // add jbang connector as it often is a provided scope dependency
                    modules.add("citrus-jbang-connector");
                }
            }
        }

        return items;
    }

    @Override
    public Set<String> scanTestContainers(String code, Set<String> modules) {
        Map<String, ComponentDefinition> actions = getKnownTestActions();
        Map<String, ComponentDefinition> components = getKnownTestContainers();

        Set<String> items = new HashSet<>();
        for (Map.Entry<String, ComponentDefinition> entry : components.entrySet()) {
            ComponentDefinition component = components.get(entry.getKey());
            String name = resolveName(entry.getKey(), component.group(), actions);

            if (code.contains("<%s>".formatted(name)) || code.contains("<%s ".formatted(name))) {
                items.add(name);
                if (StringUtils.hasText(component.module())) {
                    modules.add(component.module());
                }
            }
        }

        return items;
    }

    @Override
    public Set<String> scanEndpoints(String code, Set<String> modules) {
        Map<String, ComponentDefinition> components = getKnownEndpoints();

        Set<String> items = new HashSet<>();

        if (code.contains("<endpoints>") && code.indexOf("<endpoints>") < code.indexOf("<actions>")) {
            String endpointDefs = code.substring(code.indexOf("<endpoints>"), code.indexOf("<actions>"));
            for (Map.Entry<String, ComponentDefinition> entry : components.entrySet()) {
                ComponentDefinition component = components.get(entry.getKey());
                String name = Optional.ofNullable(component.group()).orElse(entry.getKey());

                if (endpointDefs.contains("<%s>".formatted(name)) ||
                        endpointDefs.contains("<endpoint type=\"%s\"".formatted(name)) ||
                        endpointDefs.contains("<endpoint type=\"%s:".formatted(name)) ||
                        endpointDefs.contains("<endpoint uri=\"%s:".formatted(name))) {
                    items.add(name);
                    if (StringUtils.hasText(component.module())) {
                        modules.add(component.module());
                    }
                }
            }
        }

        for (Map.Entry<String, ComponentDefinition> entry : components.entrySet()) {
            ComponentDefinition component = components.get(entry.getKey());
            String name = Optional.ofNullable(component.group()).orElse(entry.getKey());

            if (code.contains("endpoint=\"%s".formatted(name))) {
                items.add(name);
                if (StringUtils.hasText(component.module())) {
                    modules.add(component.module());
                }
            }
        }

        return items;
    }
}
