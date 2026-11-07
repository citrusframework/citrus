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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import org.apache.camel.catalog.DefaultCamelCatalog;
import org.citrusframework.CitrusVersion;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.json.schema.SimpleJsonSchema;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.validation.context.json.JsonMessageValidationContext;
import org.citrusframework.validation.json.JsonTextMessageValidator;
import org.citrusframework.yaml.actions.YamlTestActionBuilder;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import tools.jackson.databind.JsonNode;

public class CitrusSchemaGeneratorTest {

    private final JsonTextMessageValidator validator = new JsonTextMessageValidator();
    private static final String KNOWN_ACTION = "{\"echo\":{\"message\":\"Hello Citrus\"}}";
    private static final String UNKNOWN_ACTION = "{\"assert-log\":{\"message\":\"Unknown\"}}";

    /** Test cases that nest the action placeholder inside each kind of action container. */
    private static final List<String> NESTED_ACTION_CONTAINERS = List.of(
            "{\"name\":\"Parallel\",\"actions\":[{\"parallel\":{\"actions\":[%s]}}]}",
            "{\"name\":\"Sequential\",\"actions\":[{\"sequential\":{\"actions\":[%s]}}]}",
            "{\"name\":\"Assert\",\"actions\":[{\"assert\":{\"when\":[%s]}}]}",
            "{\"name\":\"Catch\",\"actions\":[{\"catch\":{\"when\":[%s]}}]}",
            "{\"name\":\"Async\",\"actions\":[{\"async\":{\"actions\":[%s]}}]}",
            "{\"name\":\"Conditional\",\"actions\":[{\"conditional\":{\"when\":\"true\",\"actions\":[%s]}}]}",
            "{\"name\":\"Iterate\",\"actions\":[{\"iterate\":{\"condition\":\"i lt 2\",\"actions\":[%s]}}]}",
            "{\"name\":\"Repeat\",\"actions\":[{\"repeat\":{\"until\":\"i gt 2\",\"actions\":[%s]}}]}",
            "{\"name\":\"RepeatOnError\",\"actions\":[{\"repeatOnError\":{\"until\":\"i gt 2\",\"actions\":[%s]}}]}",
            "{\"name\":\"Timer\",\"actions\":[{\"timer\":{\"actions\":[%s]}}]}",
            "{\"name\":\"DoFinally\",\"actions\":[{\"doFinally\":{\"actions\":[%s]}}]}",
            "{\"name\":\"Finally\",\"actions\":[{\"echo\":{\"message\":\"Hello\"}}],\"finally\":[%s]}"
    );

    private final TestContext context = TestContextFactory.newInstance().getObject();
    private SimpleJsonSchema testCaseSchema;

    @BeforeClass
    public void setup() {
        context.setVariable("citrus.version", CitrusVersion.version());
        context.setVariable("camel.version", new DefaultCamelCatalog().getCatalogVersion());

        CitrusSchemaGenerator.main(new String[]{"target/schema"});

        testCaseSchema = new SimpleJsonSchema(Resources.create("target/schema/citrus/citrus-testcase.json"));
        testCaseSchema.initialize();
    }

    @Test
    public void verifyIndexJson() throws IOException {
        verifyGeneratedFile("index.json", context);
    }

    @Test
    public void verifyAgentConfigurationJson() throws IOException {
        verifyGeneratedFile("citrus-agent-configuration.json", context);
    }

    @Test
    public void verifyAggregateEndpointsJson() throws IOException {
        verifyGeneratedFile("citrus-catalog-aggregate-endpoints.json", context);
    }

    @Test
    public void verifyEndpointDescriptions() throws IOException {
        verifyCatalogDescriptions("citrus-catalog-aggregate-endpoints.json");
    }

    @Test
    public void verifyAggregateFunctionsJson() throws IOException {
        verifyGeneratedFile("citrus-catalog-aggregate-functions.json", context);
    }

    @Test
    public void verifyFunctionDescriptions() throws IOException {
        verifyCatalogDescriptions("citrus-catalog-aggregate-functions.json");
    }

    @Test
    public void verifyAggregateInfraServicesJson() throws IOException {
        verifyGeneratedFile("citrus-catalog-aggregate-infra-services.json", context);
    }

    @Test
    public void verifyAggregateTestActionsJson() throws IOException {
        verifyGeneratedFile("citrus-catalog-aggregate-test-actions.json", context);
    }

    @Test
    public void verifyTestActionDescriptions() throws IOException {
        verifyCatalogDescriptions("citrus-catalog-aggregate-test-actions.json");
    }

    @Test
    public void verifyAggregateTestContainersJson() throws IOException {
        verifyGeneratedFile("citrus-catalog-aggregate-test-containers.json", context);
    }

    @Test
    public void verifyAggregateValidationMatcherJson() throws IOException {
        verifyGeneratedFile("citrus-catalog-aggregate-validation-matcher.json", context);
    }

    @Test
    public void verifyValidationMatcherDescriptions() throws IOException {
        verifyCatalogDescriptions("citrus-catalog-aggregate-validation-matcher.json");
    }

    @Test
    public void verifyUnknownTestActionsRejected() {
        assertInvalidTestCase("""
                {"name":"UnknownActionTest","actions":[%s]}
                """.formatted(UNKNOWN_ACTION));

        for (String container : NESTED_ACTION_CONTAINERS) {
            assertInvalidTestCase(container.formatted(UNKNOWN_ACTION));
        }
    }

    @Test
    public void verifyKnownTestActionsAccepted() {
        assertValidTestCase("""
                {"name":"KnownActionTest","actions":[%s]}
                """.formatted(KNOWN_ACTION));
        assertValidTestCase("""
                {"name":"CustomActionTest","actions":[{"action":{"ref":"myCustomAction"}}]}
                """);
        assertValidTestCase("""
                {"name":"PurgeChannelsTest","actions":[{"purgeChannels":{"channel":"testChannel"}}]}
                """);

        for (String container : NESTED_ACTION_CONTAINERS) {
            assertValidTestCase(container.formatted(KNOWN_ACTION));
        }
    }

    @Test
    public void verifyNoGenericTestActionReferences() throws IOException {
        String schema = FileUtils.readToString(Resources.create("target/schema/citrus/citrus-testcase.json"));
        Assert.assertFalse(schema.contains("TestActionBuilder_"),
                "Test case schema must not reference the open generic TestActionBuilder definition");
    }

    @Test
    public void verifyRuntimeYamlActionsInSchema() throws IOException {
        JsonNode schema = JsonSupport.json().readTree(FileUtils.readToString(
                Resources.create("target/schema/citrus/citrus-testcase.json")));
        JsonNode schemaActions = schema.path("definitions").path("TestActions").path("properties");

        List<String> missing = YamlTestActionBuilder.lookup().keySet().stream()
                .filter(name -> !schemaActions.has(name))
                .sorted()
                .toList();

        Assert.assertTrue(missing.isEmpty(), "Runtime YAML test actions missing from citrus-testcase.json: " + missing);
    }

    private void verifyGeneratedFile(String fileName, TestContext context) throws IOException {
        Message generated = new DefaultMessage(FileUtils.readToString(Resources.create("target/schema/citrus/" + fileName)));
        Message control = new DefaultMessage(FileUtils.readToString(Resources.create("control/" + fileName)));
        validator.validateMessage(generated, control, context, new JsonMessageValidationContext());
    }

    private void verifyCatalogDescriptions(String fileName) throws IOException {
        JsonNode catalog = JsonSupport.json().readTree(FileUtils.readToString(
                Resources.create("target/schema/citrus/" + fileName)));
        List<String> missing = new ArrayList<>();

        for (Map.Entry<String, JsonNode> entry : catalog.properties()) {
            if (entry.getValue().path("description").asText().isBlank()) {
                missing.add(entry.getKey());
            }

            JsonNode propertiesSchema = entry.getValue().path("propertiesSchema");
            for (Map.Entry<String, JsonNode> property : propertiesSchema.path("properties").properties()) {
                if (!hasDescription(property.getKey(), property.getValue(), propertiesSchema)) {
                    missing.add("%s.%s".formatted(entry.getKey(), property.getKey()));
                }
            }
        }

        Assert.assertTrue(missing.isEmpty(), "Missing descriptions in %s: %s".formatted(fileName, missing));
    }

    /**
     * Properties declared in a oneOf group (e.g. createEndpoint types or HTTP methods) may only carry their
     * description inside the oneOf branch, so look there when the top-level property has none.
     */
    private boolean hasDescription(String propertyName, JsonNode property, JsonNode propertiesSchema) {
        if (!property.path("description").asText().isBlank()) {
            return true;
        }

        for (JsonNode anyOf : propertiesSchema.path("anyOf")) {
            for (JsonNode branch : anyOf.path("oneOf")) {
                JsonNode branchProperty = branch.path("properties").path(propertyName);
                if (!branchProperty.isMissingNode() && !branchProperty.path("description").asText().isBlank()) {
                    return true;
                }
            }
        }

        return false;
    }

    private void assertInvalidTestCase(String testCase) {
        Assert.assertFalse(validateTestCase(testCase).isEmpty(), "Expected test case to be rejected: " + testCase);
    }

    private void assertValidTestCase(String testCase) {
        List<Error> errors = validateTestCase(testCase);
        Assert.assertTrue(errors.isEmpty(), "Expected test case to be accepted: " + errors);
    }

    private List<Error> validateTestCase(String testCase) {
        return testCaseSchema.getSchema().validate(testCase, InputFormat.JSON);
    }

}
