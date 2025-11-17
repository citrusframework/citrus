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

import com.fasterxml.classmate.AnnotationConfiguration;
import com.fasterxml.classmate.AnnotationInclusion;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.generator.TypeContext;
import org.citrusframework.agent.CitrusAgentConfiguration;
import org.citrusframework.agent.connector.yaml.YamlSupport;
import org.citrusframework.dsl.schema.Catalog;
import org.citrusframework.dsl.schema.Test;
import org.citrusframework.message.MessagePayloadUtils;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;

/**
 * Generates the full Citrus test case DSL schema based on annotated YAML model classes.
 */
public class CitrusSchemaGenerator {

    public static void main(String[] args) {
        try {
            Path outputDir = Path.of(args.length > 0 ? args[0] : "target/generated-resources/citrus-catalog");
            if (!outputDir.toFile().exists() && !outputDir.toFile().mkdirs()) {
                throw new RuntimeException("Failed to create output directory: " + outputDir);
            }

            String version = args.length > 1 ? args[1] : "4.9.0-SNAPSHOT";
            String catalogIndex = FileUtils.readToString(Resources.fromClasspath("templates/catalog-index.json")).replaceAll("@version@", version);
            writeFile(outputDir, "index.json", catalogIndex);

            JsonNode agentConfiguration = generateSchema(CitrusAgentConfiguration.class, Option.INLINE_ALL_SCHEMAS);
            writeFile(outputDir, "citrus-agent-configuration.json", agentConfiguration.toPrettyString());

            Path workingDir = outputDir.resolve("citrus/" + version);
            if (!workingDir.toFile().exists() && !workingDir.toFile().mkdirs()) {
                throw new RuntimeException("Failed to create output directory: " + workingDir);
            }

            String index = FileUtils.readToString(Resources.fromClasspath("templates/index.json")).replaceAll("@version@", version);
            writeFile(workingDir, "index.json", index);

            Catalog catalog = new Catalog();
            JsonNode jsonSchema = generateSchema(Test.class, catalog, new CitrusModule());
            writeFile(workingDir, "citrus-testcase.json", jsonSchema.toPrettyString() + "\n");

            String xsdSchema = FileUtils.readToString(Resources.fromClasspath("org/citrusframework/schema/xml/testcase/citrus-testcase.xsd"));
            writeFile(workingDir, "citrus-testcase.xsd", xsdSchema);

            String actionCatalog = MessagePayloadUtils.prettyPrintJson(YamlSupport.json().writeValueAsString(catalog.getTestActionCatalog()));
            writeFile(workingDir, "citrus-catalog-aggregate-test-actions.json", actionCatalog + "\n");

            String containerCatalog = MessagePayloadUtils.prettyPrintJson(YamlSupport.json().writeValueAsString(catalog.getTestContainerCatalog()));
            writeFile(workingDir, "citrus-catalog-aggregate-test-containers.json", containerCatalog + "\n");

            String endpointCatalog = MessagePayloadUtils.prettyPrintJson(YamlSupport.json().writeValueAsString(catalog.getEndpointCatalog()));
            writeFile(workingDir, "citrus-catalog-aggregate-endpoints.json", endpointCatalog + "\n");

            String functionsCatalog = MessagePayloadUtils.prettyPrintJson(YamlSupport.json().writeValueAsString(catalog.getFunctionsCatalog()));
            writeFile(workingDir, "citrus-catalog-aggregate-functions.json", functionsCatalog + "\n");

            String validationMatcherCatalog = MessagePayloadUtils.prettyPrintJson(YamlSupport.json().writeValueAsString(catalog.getValidationMatcherCatalog()));
            writeFile(workingDir, "citrus-catalog-aggregate-validation-matcher.json", validationMatcherCatalog + "\n");
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
                    Option.PLAIN_DEFINITION_KEYS
                );

        for (Option option : options) {
            configBuilder.with(option);
        }

        SchemaGeneratorConfig config = configBuilder.build();
        AnnotationConfiguration.StdConfiguration annotationConfig = new AnnotationConfiguration.StdConfiguration(AnnotationInclusion.INCLUDE_AND_INHERIT_IF_INHERITED);
        TypeContext typeContext = new CitrusModule.TypeContextWrapper(annotationConfig, config)
                .withCatalog(catalog);
        SchemaGenerator generator = new SchemaGenerator(config, typeContext);
        return generator.generateSchema(type);
    }

    private static void writeFile(Path dir, String name, String content) throws IOException {
        Path outputFile = dir.resolve(name);
        Files.writeString(outputFile, content,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }
}
