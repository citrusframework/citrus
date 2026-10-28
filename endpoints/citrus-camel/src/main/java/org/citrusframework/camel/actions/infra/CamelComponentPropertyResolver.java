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
 * Only component options of a simple type (e.g. string, number, boolean) are resolved. Object options
 * (e.g. Kafka configuration or Elasticsearch client) require a real object instance and are skipped.
 * <p>
 * Any inspection or catalog failure is logged and does not fail the test. The resolution may return
 * partial results when a failure occurs while processing a single component.
 */
public final class CamelComponentPropertyResolver {

    private static final Logger logger = LoggerFactory.getLogger(CamelComponentPropertyResolver.class);

    /**
     * Pattern matching Camel component schemes in the route source code. Schemes followed by a double slash
     * are treated as absolute URLs (e.g. {@code http://example.com}) and are not considered as component usage.
     */
    private static final Pattern COMPONENT_SCHEME_PATTERN = Pattern.compile("([a-zA-Z][a-zA-Z0-9+.-]*):(?!//)");

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

    /**
     * Java types that can be set from a plain string value.
     */
    private static final Set<String> SIMPLE_JAVA_TYPES = Set.of(
            "java.lang.String",
            "java.lang.Boolean",
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Double",
            "java.lang.Float",
            "java.lang.Short",
            "java.lang.Byte",
            "java.lang.Character",
            "boolean",
            "int",
            "long",
            "double",
            "float",
            "short",
            "byte",
            "char");

    private CamelComponentPropertyResolver() {
        // prevent instantiation of utility class
    }

    /**
     * Resolves matching component properties from given route source code and test variables exposed
     * by Camel infra services using the default Camel catalog.
     * @param sourceCode the Camel route source code
     * @param context the test context holding the exposed infra service variables
     * @return matching component properties as system properties, never {@code null}. May contain partial
     * results when a failure occurred while processing a single component.
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
     * @return matching component properties as system properties, never {@code null}. May contain partial
     * results when a failure occurred while processing a single component.
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
            logger.warn("Failed to auto resolve Camel component properties from integration source: {}", e.getMessage());
            logger.debug("Failed to auto resolve Camel component properties from integration source", e);
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

        List<ComponentModel.ComponentOptionModel> options = model.getComponentOptions().stream()
                .filter(CamelComponentPropertyResolver::isSimpleType)
                .toList();

        Set<String> optionNames = options.stream()
                .map(ComponentModel.ComponentOptionModel::getName)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Map<String, String> aliases = COMPONENT_OPTION_ALIASES.getOrDefault(component, Map.of());

        context.getVariables().forEach((key, value) -> {
            if (key == null || value == null) {
                return;
            }

            String remainder = removeInfraVariablePrefix(key, component);
            if (remainder == null) {
                return;
            }

            String infraKey = toCamelCase(remainder);
            String optionName = resolveOptionName(infraKey, options, optionNames, aliases);
            if (optionName == null) {
                optionName = resolveQualifiedOptionName(remainder, options, optionNames, aliases);
            }

            if (optionName == null) {
                if (model.getComponentOptions().stream()
                        .anyMatch(option -> option.getName().equalsIgnoreCase(infraKey))) {
                    logger.debug("Skipping Camel component property '{}' on component '{}' - only simple types are supported",
                            infraKey, component);
                } else {
                    logger.debug("No matching component option found for infra service property '{}' on component '{}'", key, component);
                }
                return;
            }

            String resolvedValue = context.replaceDynamicContentInString(String.valueOf(value));
            if (resolvedValue == null || resolvedValue.isBlank()) {
                return;
            }

            properties.put("camel.component.%s.%s".formatted(component, optionName), resolvedValue);
            logger.info("Setting Camel component property '{}' from infra service property '{}'",
                    context.getLogModifier().mask("camel.component.%s.%s".formatted(component, optionName)),
                    context.getLogModifier().mask(resolvedValue));
        });
    }

    /**
     * Removes the infra service variable prefix of the given component. Infra services may be started with
     * an implementation qualifier (e.g. {@code kafka.strimzi}) which is part of the variable name
     * (e.g. {@code CITRUS_CAMEL_INFRA_KAFKA_STRIMZI_BOOTSTRAP_SERVERS}).
     * @param key the test variable name
     * @param component the Camel component
     * @return the remaining property name or {@code null} if the variable does not belong to the component
     */
    private static String removeInfraVariablePrefix(String key, String component) {
        for (String token : infraServiceTokens(component)) {
            String prefix = CamelInfraSettings.CAMEL_INFRA_ENV_PREFIX + token + "_";
            if (key.startsWith(prefix)) {
                return key.substring(prefix.length());
            }
        }

        return null;
    }

    /**
     * Resolves the component option for an infra service property that is qualified with the infra service
     * implementation name (e.g. {@code CITRUS_CAMEL_INFRA_KAFKA_STRIMZI_BOOTSTRAP_SERVERS}).
     * @param remainder the property name without the infra service variable prefix
     * @param options the available component options
     * @param optionNames the names of the available component options
     * @param aliases the optional aliases matching infra property keys to component option names
     * @return the matching component option name or {@code null} if no option matches
     */
    private static String resolveQualifiedOptionName(String remainder, List<ComponentModel.ComponentOptionModel> options,
            Set<String> optionNames, Map<String, String> aliases) {
        int separator = remainder.indexOf('_');
        if (separator < 0 || separator == remainder.length() - 1) {
            return null;
        }

        return resolveOptionName(toCamelCase(remainder.substring(separator + 1)), options, optionNames, aliases);
    }

    /**
     * Provides the infra service variable name tokens of the given component. Next to the component name
     * itself the token without any digits is supported too (e.g. component {@code aws2-sqs} matches the
     * infra service {@code aws.sqs} with token {@code AWS_SQS}).
     * @param component the Camel component
     * @return the infra service variable name tokens
     */
    private static List<String> infraServiceTokens(String component) {
        String token = component.replaceAll("[^a-zA-Z0-9]", "_").toUpperCase();
        String normalizedToken = token.replaceAll("[0-9]", "").replaceAll("_+", "_");

        List<String> tokens = new ArrayList<>();
        tokens.add(token);
        if (!tokens.contains(normalizedToken)) {
            tokens.add(normalizedToken);
        }

        return tokens;
    }

    private static String resolveOptionName(String infraKey, List<ComponentModel.ComponentOptionModel> options, Set<String> optionNames,
            Map<String, String> aliases) {
        for (ComponentModel.ComponentOptionModel option : options) {
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

    /**
     * Checks if the given component option is of a simple type that can be set from a plain string value.
     * Object options (e.g. Kafka configuration or Elasticsearch client) require a real object instance
     * and cannot be set via a system property.
     * @param option the component option to check
     * @return true if the option can be set with a plain string value
     */
    private static boolean isSimpleType(ComponentModel.ComponentOptionModel option) {
        if ("object".equalsIgnoreCase(option.getType())) {
            return false;
        }

        String javaType = option.getJavaType();
        if (javaType == null || javaType.isBlank()) {
            return true;
        }

        return SIMPLE_JAVA_TYPES.contains(javaType);
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
