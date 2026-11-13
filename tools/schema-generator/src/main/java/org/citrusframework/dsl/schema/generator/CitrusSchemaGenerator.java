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

package org.citrusframework.dsl.schema.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.classmate.AnnotationConfiguration;
import com.fasterxml.classmate.AnnotationInclusion;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.generator.TypeContext;
import org.citrusframework.api.agent.CitrusAgentConfiguration;
import org.citrusframework.dsl.schema.Catalog;
import org.citrusframework.dsl.schema.Test;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Generates the full Citrus test case DSL schema based on annotated YAML model classes.
 */
public class CitrusSchemaGenerator {

    private static final String DEFINITIONS = "definitions";
    private static final String REF = "$ref";
    private static final String DEFINITION_REF_PREFIX = "#/definitions/";
    private static final String TEST_ACTIONS_DEFINITION = "TestActions";
    private static final String GENERIC_TEST_ACTION_DEFINITION_PREFIX = "TestActionBuilder_";

    public static void main(String[] args) {
        try {
            Path outputDir = Path.of(args.length > 0 ? args[0] : "target/generated-resources/citrus-catalog");
            if (!outputDir.toFile().exists() && !outputDir.toFile().mkdirs()) {
                throw new RuntimeException("Failed to create output directory: " + outputDir);
            }

            String version = args.length > 1 ? args[1] : "5.3.0-SNAPSHOT";
            String catalogIndex = FileUtils.readToString(Resources.fromClasspath("templates/catalog-index.json")).replaceAll("@version@", version);
            writeFile(outputDir, "index.json", catalogIndex);

            Path workingDir = outputDir.resolve("citrus");
            Path versionWorkingDir = workingDir.resolve(version);
            if (!workingDir.toFile().exists() && !workingDir.toFile().mkdirs()) {
                throw new RuntimeException("Failed to create output directory: " + workingDir);
            }
            if (!versionWorkingDir.toFile().exists() && !versionWorkingDir.toFile().mkdirs()) {
                throw new RuntimeException("Failed to create output directory: " + versionWorkingDir);
            }

            JsonNode agentConfiguration = generateSchema(CitrusAgentConfiguration.class, Option.INLINE_ALL_SCHEMAS);
            writeFile(workingDir, "citrus-agent-configuration.json", agentConfiguration.toString());
            writeFile(versionWorkingDir, "citrus-agent-configuration.json", agentConfiguration.toString());

            String index = FileUtils.readToString(Resources.fromClasspath("templates/index.json")).replaceAll("@version@", version);
            writeFile(workingDir, "index.json", index);
            writeFile(versionWorkingDir, "index.json", index);

            Catalog catalog = new Catalog();
            JsonNode jsonSchema = generateSchema(Test.class, catalog, new CitrusModule());
            writeFile(workingDir, "citrus-testcase.json", jsonSchema.toString());
            writeFile(versionWorkingDir, "citrus-testcase.json", jsonSchema.toString());

            String xsdSchema = FileUtils.readToString(Resources.fromClasspath("org/citrusframework/schema/xml/testcase/citrus-testcase.xsd"));
            writeFile(workingDir, "citrus-testcase.xsd", xsdSchema);
            writeFile(versionWorkingDir, "citrus-testcase.xsd", xsdSchema);

            String actionCatalog = JsonSupport.json().writeValueAsString(catalog.getTestActionCatalog());
            writeFile(workingDir, "citrus-catalog-aggregate-test-actions.json", actionCatalog);
            writeFile(versionWorkingDir, "citrus-catalog-aggregate-test-actions.json", actionCatalog);

            String containerCatalog = JsonSupport.json().writeValueAsString(catalog.getTestContainerCatalog());
            writeFile(workingDir, "citrus-catalog-aggregate-test-containers.json", containerCatalog);
            writeFile(versionWorkingDir, "citrus-catalog-aggregate-test-containers.json", containerCatalog);

            String endpointCatalog = JsonSupport.json().writeValueAsString(catalog.getEndpointCatalog());
            writeFile(workingDir, "citrus-catalog-aggregate-endpoints.json", endpointCatalog);
            writeFile(versionWorkingDir, "citrus-catalog-aggregate-endpoints.json", endpointCatalog);

            String functionsCatalog = JsonSupport.json().writeValueAsString(catalog.getFunctionsCatalog());
            writeFile(workingDir, "citrus-catalog-aggregate-functions.json", functionsCatalog);
            writeFile(versionWorkingDir, "citrus-catalog-aggregate-functions.json", functionsCatalog);

            String validationMatcherCatalog = JsonSupport.json().writeValueAsString(catalog.getValidationMatcherCatalog());
            writeFile(workingDir, "citrus-catalog-aggregate-validation-matcher.json", validationMatcherCatalog);
            writeFile(versionWorkingDir, "citrus-catalog-aggregate-validation-matcher.json", validationMatcherCatalog);

            String infraServiceCatalog = JsonSupport.json().writeValueAsString(catalog.getInfraServiceCatalog());
            writeFile(workingDir, "citrus-catalog-aggregate-infra-services.json", infraServiceCatalog);
            writeFile(versionWorkingDir, "citrus-catalog-aggregate-infra-services.json", infraServiceCatalog);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Citrus DSL schema", e);
        }
    }

    public static JsonNode generateSchema(Class<?> type, Option... options) {
        return generateSchema(type, null, new CitrusModule(), options);
    }

    public static JsonNode generateSchema(Class<?> type, CitrusModule module, Option... options) {
        return generateSchema(type, null, module, options);
    }

    public static JsonNode generateSchema(Class<?> type, Catalog catalog, Option... options) {
        return generateSchema(type, catalog, new CitrusModule(), options);
    }

    public static JsonNode generateSchema(Class<?> type, Catalog catalog, CitrusModule module, Option... options) {
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_7, OptionPreset.PLAIN_JSON)
                .with(module)
                .with(
                    Option.GETTER_METHODS,
                    Option.MAP_VALUES_AS_ADDITIONAL_PROPERTIES,
                    Option.PLAIN_DEFINITION_KEYS,
                    Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT
                );

        for (Option option : options) {
            configBuilder.with(option);
        }

        SchemaGeneratorConfig config = configBuilder.build();
        AnnotationConfiguration.StdConfiguration annotationConfig = new AnnotationConfiguration.StdConfiguration(AnnotationInclusion.INCLUDE_AND_INHERIT_IF_INHERITED);
        TypeContext typeContext = new CitrusModule.TypeContextWrapper(annotationConfig, config)
                .withCatalog(catalog);
        SchemaGenerator generator = new SchemaGenerator(config, typeContext);
        JsonNode jsonSchema = generator.generateSchema(type);
        useStrictTestActions(jsonSchema);
        return jsonSchema;
    }

    /**
     * Nested YAML actions use the runtime TestActions supplier type, which Victools represents as an open generic
     * TestActionBuilder schema. When the schema holds the aggregate TestActions definition, point those references to
     * it instead so nested actions have the same known action names and additionalProperties=false constraint as
     * top-level actions. Schemas without definitions (e.g. inlined catalog schemas) are left untouched.
     */
    private static void useStrictTestActions(JsonNode jsonSchema) {
        if (!(jsonSchema.path(DEFINITIONS) instanceof ObjectNode definitions) || !definitions.has(TEST_ACTIONS_DEFINITION)) {
            return;
        }

        List<String> genericDefinitions = new ArrayList<>();
        definitions.properties().forEach(entry -> {
            if (entry.getKey().startsWith(GENERIC_TEST_ACTION_DEFINITION_PREFIX)) {
                genericDefinitions.add(entry.getKey());
            }
        });

        Set<String> genericReferences = new HashSet<>();
        genericDefinitions.forEach(name -> genericReferences.add(DEFINITION_REF_PREFIX + name));

        replaceGenericTestActionReferences(jsonSchema, genericReferences);
        definitions.remove(genericDefinitions);

        List<String> remaining = new ArrayList<>();
        collectGenericTestActionReferences(jsonSchema, remaining);
        if (!remaining.isEmpty()) {
            throw new IllegalStateException("Unresolved generic test action references in schema: " + remaining);
        }
    }

    private static void replaceGenericTestActionReferences(JsonNode node, Set<String> genericReferences) {
        if (node instanceof ObjectNode objectNode) {
            if (genericReferences.contains(objectNode.path(REF).asString(""))) {
                objectNode.put(REF, DEFINITION_REF_PREFIX + TEST_ACTIONS_DEFINITION);
            }
            objectNode.properties().forEach(entry -> replaceGenericTestActionReferences(entry.getValue(), genericReferences));
        } else if (node.isArray()) {
            node.forEach(item -> replaceGenericTestActionReferences(item, genericReferences));
        }
    }

    private static void collectGenericTestActionReferences(JsonNode node, List<String> found) {
        if (node instanceof ObjectNode objectNode) {
            String ref = objectNode.path(REF).asString("");
            if (ref.startsWith(DEFINITION_REF_PREFIX + GENERIC_TEST_ACTION_DEFINITION_PREFIX)) {
                found.add(ref);
            }
            objectNode.properties().forEach(entry -> collectGenericTestActionReferences(entry.getValue(), found));
        } else if (node.isArray()) {
            node.forEach(item -> collectGenericTestActionReferences(item, found));
        }
    }

    private static void writeFile(Path dir, String name, String content) throws IOException {
        Path outputFile = dir.resolve(name);
        Files.writeString(outputFile, content,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }
}
