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

import io.apicurio.datamodels.openapi.models.OasSchema;
import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.openapi.random.RandomContext;

/**
 * Generates proper payloads and validation expressions based on Open API specification rules.
 */
public final class OpenApiTestDataGenerator {

    private OpenApiTestDataGenerator() {
        // Static access only
    }

    /**
     * Creates payload from schema for outbound message.
     */
    public static String createOutboundPayload(OasSchema schema, OpenApiSpecification specification) {
        RandomContext randomContext = new RandomContext(specification, true);
        randomContext.generate(schema);
        return randomContext.getRandomModelBuilder().write();
    }

    /**
     * Use test variable with given name if present or create value from schema with random values
     */
    public static String createRandomValueExpression(String name, OasSchema schema, OpenApiSpecification specification, TestContext context) {
        if (context.getVariables().containsKey(name)) {
            return CitrusSettings.VARIABLE_PREFIX + name + CitrusSettings.VARIABLE_SUFFIX;
        }

        RandomContext randomContext = new RandomContext(specification, false);
        randomContext.generate(schema);
        return randomContext.getRandomModelBuilder().write();
    }

    /**
     * Use test variable with given name (if present) or create random value expression using
     * functions according to schema type and format.
     */
    public static String createRandomValueExpression(String name, OasSchema schema, TestContext context) {
        if (context.getVariables().containsKey(name)) {
            return CitrusSettings.VARIABLE_PREFIX + name + CitrusSettings.VARIABLE_SUFFIX;
        }

        return createRandomValueExpression(schema);
    }

    public static String createRandomValueExpression(OasSchema schema) {
        RandomContext randomContext = new RandomContext();
        randomContext.generate(schema);
        return randomContext.getRandomModelBuilder().write();
    }
}
