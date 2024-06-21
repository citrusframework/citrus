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

package org.citrusframework.openapi;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.apicurio.datamodels.openapi.models.OasSchema;
import java.util.Map;
import org.citrusframework.openapi.model.OasModelHelper;
import org.citrusframework.spi.Resources;
import org.testng.Assert;
import org.testng.annotations.Test;

// TODO: Add more tests
public class OpenApiTestDataGeneratorTest {

    private final OpenApiSpecification pingSpec = OpenApiSpecification.from(
        Resources.create("classpath:org/citrusframework/openapi/ping/ping-api.yaml"));

    // TODO: fix this by introducing mature validation
    @Test
    public void failsToValidateAnyOf() throws JsonProcessingException {

        Map<String, OasSchema> schemaDefinitions = OasModelHelper.getSchemaDefinitions(
            pingSpec.getOpenApiDoc(null));
        assertNotNull(schemaDefinitions);
        assertFalse(schemaDefinitions.isEmpty());
        Assert.assertEquals(schemaDefinitions.size(), 15);

        Assert.assertThrows(() -> OpenApiTestDataGenerator.createValidationExpression(
            schemaDefinitions.get("PingRespType"), schemaDefinitions, true, pingSpec));
    }
}
