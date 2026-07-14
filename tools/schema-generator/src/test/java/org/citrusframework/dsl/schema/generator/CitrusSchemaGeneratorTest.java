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

import org.apache.camel.catalog.DefaultCamelCatalog;
import org.citrusframework.CitrusVersion;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.validation.json.JsonMessageValidationContext;
import org.citrusframework.validation.json.JsonTextMessageValidator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CitrusSchemaGeneratorTest {

    private final JsonTextMessageValidator validator = new JsonTextMessageValidator();
    private final TestContext context = TestContextFactory.newInstance().getObject();

    @BeforeClass
    public void setup() {
        context.setVariable("citrus.version", CitrusVersion.version());
        context.setVariable("camel.version", new DefaultCamelCatalog().getCatalogVersion());

        CitrusSchemaGenerator.main(new String[]{"target/schema"});
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
    public void verifyAggregateFunctionsJson() throws IOException {
        verifyGeneratedFile("citrus-catalog-aggregate-functions.json", context);
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
    public void verifyAggregateTestContainersJson() throws IOException {
        verifyGeneratedFile("citrus-catalog-aggregate-test-containers.json", context);
    }

    @Test
    public void verifyAggregateValidationMatcherJson() throws IOException {
        verifyGeneratedFile("citrus-catalog-aggregate-validation-matcher.json", context);
    }

    private void verifyGeneratedFile(String fileName, TestContext context) throws IOException {
        Message generated = new DefaultMessage(FileUtils.readToString(Resources.create("target/schema/citrus/" + fileName)));
        Message control = new DefaultMessage(FileUtils.readToString(Resources.create("control/" + fileName)));
        validator.validateMessage(generated, control, context, new JsonMessageValidationContext());
    }

}
