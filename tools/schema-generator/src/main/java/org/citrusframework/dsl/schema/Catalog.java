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

package org.citrusframework.dsl.schema;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.Option;
import org.citrusframework.dsl.schema.generator.CitrusModule;
import org.citrusframework.dsl.schema.generator.CitrusSchemaGenerator;
import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.functions.DefaultFunctionLibrary;
import org.citrusframework.functions.Function;
import org.citrusframework.functions.ParameterizedFunction;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.matcher.DefaultValidationMatcherLibrary;
import org.citrusframework.validation.matcher.ParameterizedValidationMatcher;
import org.citrusframework.validation.matcher.ValidationMatcher;
import org.citrusframework.yaml.SchemaProperty;

import static org.citrusframework.yaml.SchemaProperty.Kind.ACTION;
import static org.citrusframework.yaml.SchemaProperty.Kind.CONTAINER;
import static org.citrusframework.yaml.SchemaProperty.Kind.GROUP;
import static org.citrusframework.yaml.SchemaProperty.Kind.PROPERTY;

public class Catalog {

    private final Map<String, CatalogItem> collectedItems = new LinkedHashMap<>();

    public void add(CatalogItem item) {
        String name = Optional.ofNullable(item.schema.group())
                .filter(group -> !group.isBlank())
                .map(group -> "%s-%s".formatted(group, item.name))
                .orElse(item.name);
        collectedItems.put(name, item);
    }

    public Map<String, CatalogEntry> getTestActionCatalog() {
        return collectedItems.entrySet()
                .stream()
                .filter(entry -> entry.getValue().schema.kind() == ACTION || entry.getValue().schema.kind() == GROUP)
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    CatalogItem item = entry.getValue();
                    CitrusModule module = new CitrusModule()
                            .withRequireOneOf(false);
                    if (item.schema.kind() == GROUP) {
                        module.withIgnoreFilter(schema -> schema.kind() != PROPERTY);
                    }

                    JsonNode jsonSchema = CitrusSchemaGenerator.generateSchema(item.type, module, Option.INLINE_ALL_SCHEMAS);
                    return new CatalogEntry(item.schema.kind().getCatalogKind(),
                            entry.getKey(),
                            item.schema.group(),
                            Optional.ofNullable(item.schema.title())
                                    .filter(title -> !title.isEmpty())
                                    .orElse(StringUtils.convertFirstCharToUpperCase(item.name)),
                            item.schema.description(),
                            jsonSchema);
                })
                .collect(Collectors.toMap(
                        item -> item.name,
                        item -> item,
                        (prev, next) -> prev, LinkedHashMap::new));
    }

    public Map<String, CatalogEntry> getTestContainerCatalog() {
        return collectedItems.entrySet()
                .stream()
                .filter(entry -> entry.getValue().schema.kind() == CONTAINER)
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    CatalogItem item = entry.getValue();
                    JsonNode jsonSchema = CitrusSchemaGenerator.generateSchema(item.type, Option.INLINE_ALL_SCHEMAS);
                    return new CatalogEntry(item.schema.kind().getCatalogKind(),
                            entry.getKey(),
                            item.schema.group(),
                            Optional.ofNullable(item.schema.title())
                                    .filter(title -> !title.isEmpty())
                                    .orElse(StringUtils.convertFirstCharToUpperCase(item.name)),
                            item.schema.description(),
                            jsonSchema);
                })
                .collect(Collectors.toMap(
                        item -> item.name,
                        item -> item,
                        (prev, next) -> prev, LinkedHashMap::new));
    }

    public Map<String, CatalogEntry> getEndpointCatalog() {
        Map<String, CatalogEntry> catalog = new LinkedHashMap<>();

        Map<String, EndpointBuilder<?>> endpointBuilders = EndpointBuilder.lookup()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().contains("."))
                .filter(entry -> !entry.getKey().endsWith(".async") && !entry.getKey().endsWith(".sync"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for (Map.Entry<String, EndpointBuilder<?>> builder : endpointBuilders.entrySet()) {
            JsonNode jsonSchema = CitrusSchemaGenerator.generateSchema(builder.getValue().getClass(), Option.INLINE_ALL_SCHEMAS);
            String[] tokens = builder.getKey().split("\\.");
            String group = tokens[0];
            String name = group + StringUtils.convertFirstCharToUpperCase(tokens[1]);
            catalog.put(builder.getKey().replaceAll("\\.", "-"),
                    new CatalogEntry(SchemaProperty.Kind.ENDPOINT.getCatalogKind(),
                            name,
                            group,
                            StringUtils.convertFirstCharToUpperCase(name),
                            null,
                            jsonSchema));
        }

        return catalog;
    }

    public Map<String, CatalogEntry> getFunctionsCatalog() {
        Map<String, CatalogEntry> catalog = new LinkedHashMap<>();

        Map<String, Function> functions = new DefaultFunctionLibrary().getMembers();
        for (Map.Entry<String, Function> function : functions.entrySet()) {
            JsonNode jsonSchema;
            if (function.getValue() instanceof ParameterizedFunction<?> parameterizedFunction) {
                jsonSchema = CitrusSchemaGenerator.generateSchema(parameterizedFunction.getParameters().getClass(), Option.INLINE_ALL_SCHEMAS);
            } else {
                jsonSchema = CitrusSchemaGenerator.generateSchema(function.getValue().getClass(), Option.INLINE_ALL_SCHEMAS);
            }
            catalog.put(function.getKey(),
                    new CatalogEntry(SchemaProperty.Kind.FUNCTION.getCatalogKind(),
                            function.getKey(),
                            "citrus",
                            StringUtils.convertFirstCharToUpperCase(function.getKey()),
                            null,
                            jsonSchema));
        }

        return catalog;
    }

    public Map<String, CatalogEntry> getValidationMatcherCatalog() {
        Map<String, CatalogEntry> catalog = new LinkedHashMap<>();

        Map<String, ValidationMatcher> matchers = new DefaultValidationMatcherLibrary().getMembers();
        for (Map.Entry<String, ValidationMatcher> matcher : matchers.entrySet()) {
            JsonNode jsonSchema;
            if (matcher.getValue() instanceof ParameterizedValidationMatcher<?> parameterizedMatcher) {
                jsonSchema = CitrusSchemaGenerator.generateSchema(parameterizedMatcher.getParameters().getClass(), Option.INLINE_ALL_SCHEMAS);
            } else {
                jsonSchema = CitrusSchemaGenerator.generateSchema(matcher.getValue().getClass(), Option.INLINE_ALL_SCHEMAS);
            }
            catalog.put(matcher.getKey(),
                    new CatalogEntry(SchemaProperty.Kind.VALIDATION_MATCHER.getCatalogKind(),
                            matcher.getKey(),
                            "citrus",
                            StringUtils.convertFirstCharToUpperCase(matcher.getKey()),
                            null,
                            jsonSchema));
        }

        return catalog;
    }

    public record CatalogItem (
        String name,
        Class<?> type,
        SchemaProperty schema
    ){}

    public record CatalogEntry (
        String kind,
        String name,
        String group,
        String title,
        String description,
        JsonNode propertiesSchema
    ){}
}
