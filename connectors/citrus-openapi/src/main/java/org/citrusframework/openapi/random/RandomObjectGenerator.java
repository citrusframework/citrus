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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import org.citrusframework.openapi.util.OpenApiUtils;

import static org.citrusframework.openapi.OpenApiConstants.TYPE_OBJECT;

/**
 * A generator for producing random objects based on an OpenAPI schema. This class extends
 * the {@link RandomGenerator} and provides a specific implementation for generating objects
 * with properties defined in the schema.
 * <p>
 * The generator supports object schemas and prevents recursion by keeping track of the
 * schemas being processed.</p>
 */
public class RandomObjectGenerator extends RandomGenerator {

    private static final String OBJECT_STACK = "OBJECT_STACK";

    private static final OasSchema OBJECT_SCHEMA = new Oas30Schema();

    static {
        OBJECT_SCHEMA.type = TYPE_OBJECT;
    }

    public RandomObjectGenerator() {
        super(OBJECT_SCHEMA);
    }

    @Override
    void generate(RandomContext randomContext, OasSchema schema) {
        Deque<OasSchema> objectStack = randomContext.get(OBJECT_STACK, k -> new ArrayDeque<>());

        if (objectStack.contains(schema)) {
            // If we have already created this schema, we are very likely in a recursion and need to stop.
            return;
        }

        objectStack.push(schema);
        randomContext.getRandomModelBuilder().object(() -> {
            if (schema.properties != null) {
                for (Map.Entry<String, OasSchema> entry : schema.properties.entrySet()) {
                    if (randomContext.getSpecification().isGenerateOptionalFields()
                            || OpenApiUtils.isRequired(schema, entry.getKey())) {
                        randomContext.getRandomModelBuilder()
                                .property(entry.getKey(), () -> randomContext.generate(entry.getValue()));
                    }
                }
            }
        });

        objectStack.pop();
    }
}
