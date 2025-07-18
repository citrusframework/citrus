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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import org.citrusframework.openapi.model.OasModelHelper;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * A generator for producing random composite schemas based on an OpenAPI schema. This class extends
 * the {@link RandomGenerator} and provides a specific implementation for generating composite schemas
 * with constraints defined in the schema.
 *
 * <p>The generator supports composite schemas, which include `allOf`, `anyOf`, and `oneOf` constructs.</p>
 */
public class RandomCompositeGenerator extends RandomGenerator {

    private static void createOneOf(RandomContext randomContext, List<OasSchema> schemas) {
        int schemaIndex = ThreadLocalRandom.current().nextInt(schemas.size());
        randomContext.getRandomModelBuilder().object(() -> randomContext.generate(schemas.get(schemaIndex)));
    }

    private static void createAnyOf(RandomContext randomContext, Oas30Schema schema) {
        randomContext.getRandomModelBuilder().object(() -> {
            boolean anyAdded = false;
            for (OasSchema oneSchema : schema.anyOf) {
                if (ThreadLocalRandom.current().nextBoolean()) {
                    randomContext.generate(oneSchema);
                    anyAdded = true;
                }
            }

            // Add at least one
            if (!anyAdded) {
                createOneOf(randomContext, schema.anyOf);
            }
        });
    }

    private static Map<String, Object> createAllOf(RandomContext randomContext, OasSchema schema) {
        Map<String, Object> allOf = new HashMap<>();

        randomContext.getRandomModelBuilder().object(() -> {
            for (OasSchema oneSchema : schema.allOf) {
                randomContext.generate(oneSchema);
            }
        });

        return allOf;
    }

    @Override
    public boolean handles(OasSchema other) {
        return OasModelHelper.isCompositeSchema(other);
    }

    @Override
    void generateIntoContext(RandomContext randomContext, OasSchema schema) {
        if (!isEmpty(schema.allOf)) {
            createAllOf(randomContext, schema);
        } else if (schema instanceof Oas30Schema oas30Schema && !isEmpty(oas30Schema.anyOf)) {
            createAnyOf(randomContext, oas30Schema);
        } else if (schema instanceof Oas30Schema oas30Schema && !isEmpty(oas30Schema.oneOf)) {
            createOneOf(randomContext, oas30Schema.oneOf);
        }
    }
}
