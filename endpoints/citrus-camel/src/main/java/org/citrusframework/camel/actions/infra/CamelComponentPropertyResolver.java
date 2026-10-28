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

package org.citrusframework.camel.actions.infra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.camel.catalog.CamelCatalog;
import org.apache.camel.catalog.DefaultCamelCatalog;
import org.apache.camel.tooling.model.ComponentModel;
import org.citrusframework.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves Camel component properties from given route source code and test variables that have been
 * exposed by Camel infrastructure services (e.g. {@code CITRUS_CAMEL_INFRA_KAFKA_BOOTSTRAP_SERVERS}).
 * The resolver inspects the route source for used component schemes, looks up available component
 * properties in the Camel catalog and matches them against the exposed infra service variables.
 * Matching properties are returned as system properties using Camel component configuration syntax
 * (e.g. {@code camel.component.kafka.brokers=<bootstrap-servers>}).
 * <p>
 * Any inspection or catalog failure results in an empty result, so the resolution never fails the test.
 */
public final class CamelComponentPropertyResolver {

    private static final Logger logger = LoggerFactory.getLogger(CamelComponentPropertyResolver.class);

    private static final Pattern COMPONENT_SCHEME_PATTERN = Pattern.compile("([a-zA-Z][a-zA-Z0-9+.-]*):");

    private static final CamelCatalog CATALOG = new DefaultCamelCatalog();

    /**
     * Optional aliases matching infra service property keys to component option names.
     * Maps component scheme to a map of camelCase infra property key and actual component option name.
     */
    private static final Map<String, Map<String, String>> COMPONENT_OPTION_ALIASES = Map.of(
            "kafka", Map.of("bootstrapServers", "brokers"),
            "pulsar", Map.of("pulsarBrokerUrl", "serviceUrl"),
            "elasticsearch", Map.of("elasticSearchHost", "hostAddresses", "username", "user"),
            "nats", Map.of("serviceAddress", "servers"));

    private CamelComponentPropertyResolver() {
        // prevent instantiation of utility class
    }

    /**
     * Resolves matching component properties from given route source code and test variables exposed
     * by Camel infra services using the default Camel catalog.
     * @param sourceCode the Camel route source code
     * @param context the test context holding the exposed infra service variables
     * @return matching component properties as system properties, never {@code null}
     */
    public static Map<String, String> resolveProperties(String sourceCode, TestContext context) {
        return resolveProperties(sourceCode, CATALOG, context);
    }

    /**
     * Resolves matching component properties from given route source code and test variables exposed
     * by Camel infra services.
     * @param sourceCode the Camel route source code
     * @param catalog the Camel catalog used to detect components and look up component properties
     * @param context the test context holding the exposed infra service variables
     * @return matching component properties as system properties, never {@code null}
     */
    public static Map<String, String> resolveProperties(String sourceCode, CamelCatalog catalog, TestContext context) {
        Map<String, String> properties = new HashMap<>();

        if (sourceCode == null || sourceCode.isBlank()) {
            return properties;
        }

        try {
            for (String component : detectComponents(sourceCode, catalog)) {
                applyComponentProperties(component, catalog, context, properties);
            }
        } catch (Exception e) {
            logger.warn("Failed to auto resolve Camel component properties from integration source", e);
        }

        return properties;
    }

    private static List<String> detectComponents(String sourceCode, CamelCatalog catalog) {
        Set<String> knownComponents = new HashSet<>(catalog.findComponentNames());
        List<String> detected = new ArrayList<>();

        Matcher matcher = COMPONENT_SCHEME_PATTERN.matcher(sourceCode);
        while (matcher.find()) {
            String scheme = matcher.group(1);
            if (knownComponents.contains(scheme) && !detected.contains(scheme)) {
                detected.add(scheme);
            }
        }

        return detected;
    }

    private static void applyComponentProperties(String component, CamelCatalog catalog, TestContext context,
            Map<String, String> properties) {
        ComponentModel model = catalog.componentModel(component);
        if (model == null) {
            logger.debug("No component model found in Camel catalog for component '{}'", component);
            return;
        }

        Set<String> optionNames = model.getComponentOptions().stream()
                .map(ComponentModel.ComponentOptionModel::getName)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        String variablePrefix = CamelInfraSettings.CAMEL_INFRA_ENV_PREFIX +
                component.replaceAll("[^a-zA-Z0-9]", "_").toUpperCase() + "_";

        Map<String, String> aliases = COMPONENT_OPTION_ALIASES.getOrDefault(component, Map.of());

        context.getVariables().forEach((key, value) -> {
            if (key == null || !key.startsWith(variablePrefix) || value == null) {
                return;
            }

            String infraKey = toCamelCase(key.substring(variablePrefix.length()));
            String optionName = resolveOptionName(infraKey, model, optionNames, aliases);
            if (optionName == null) {
                logger.debug("No matching component option found for infra service property '{}' on component '{}'", key, component);
                return;
            }

            String resolvedValue = context.replaceDynamicContentInString(String.valueOf(value));
            if (resolvedValue == null || resolvedValue.isBlank()) {
                return;
            }

            properties.put("camel.component.%s.%s".formatted(component, optionName), resolvedValue);
        });
    }

    private static String resolveOptionName(String infraKey, ComponentModel model, Set<String> optionNames,
            Map<String, String> aliases) {
        for (ComponentModel.ComponentOptionModel option : model.getComponentOptions()) {
            if (option.getName().equalsIgnoreCase(infraKey)) {
                return option.getName();
            }
        }

        String alias = aliases.get(infraKey);
        if (alias != null && optionNames.contains(alias.toLowerCase())) {
            return alias;
        }

        return null;
    }

    private static String toCamelCase(String key) {
        StringBuilder builder = new StringBuilder();
        boolean capitalize = false;

        for (char c : key.toCharArray()) {
            if (c == '_' || c == '-' || c == '.') {
                capitalize = true;
            } else if (capitalize) {
                builder.append(Character.toUpperCase(c));
                capitalize = false;
            } else {
                builder.append(Character.toLowerCase(c));
            }
        }

        return builder.toString();
    }
}
