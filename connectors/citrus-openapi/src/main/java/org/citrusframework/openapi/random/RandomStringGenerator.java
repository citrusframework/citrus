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

package org.citrusframework.openapi.random;

import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;

import static org.citrusframework.openapi.OpenApiConstants.TYPE_STRING;

/**
 * A generator for producing random strings based on an OpenAPI schema.
 * This class extends the {@link RandomGenerator} and provides a specific implementation
 * for generating random strings with constraints defined in the schema.
 */
public class RandomStringGenerator extends RandomGenerator {

    private static final OasSchema STRING_SCHEMA = new Oas30Schema();

    static {
        STRING_SCHEMA.type = TYPE_STRING;
    }

    public RandomStringGenerator() {
        super(STRING_SCHEMA);
    }

    @Override
    void generateIntoContext(RandomContext randomContext, OasSchema schema) {
        int min = 1;
        int max = 10;

        if (schema.minLength != null && schema.minLength.intValue() > 0) {
            min = schema.minLength.intValue();
        }

        if (schema.maxLength != null && schema.maxLength.intValue() > 0) {
            max = schema.maxLength.intValue();
        }

        randomContext.getRandomModelBuilder().appendSimpleQuoted("citrus:randomString(%s,MIXED,true,%s)".formatted(max, min));
    }
}
