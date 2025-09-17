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

import java.util.concurrent.ThreadLocalRandom;

import io.apicurio.datamodels.openapi.models.OasSchema;
import jakarta.annotation.Nullable;

import static org.citrusframework.openapi.model.OasModelHelper.isArrayType;

/**
 * A generator for producing random arrays based on an OpenAPI schema. This class extends the
 * {@link RandomGenerator} and provides a specific implementation for generating random arrays
 * with constraints defined in the schema.
 *
 * <p>The generator supports arrays with items of a single schema type. If the array's items have
 * different schemas, an {@link UnsupportedOperationException} will be thrown.</p>s
 */
public class RandomArrayGenerator extends RandomGenerator {

    private static void createRandomArrayValueWithSchemaItem(RandomContext randomContext,
                                                             OasSchema schema,
                                                             OasSchema itemsSchema) {
        Number minItems = schema.minItems != null ? schema.minItems : 1;
        Number maxItems = schema.maxItems != null ? schema.maxItems : 10;

        int nItems = ThreadLocalRandom.current()
                .nextInt(minItems.intValue(), maxItems.intValue() + 1);

        randomContext.getRandomModelBuilder().array(() -> {
            for (int i = 0; i < nItems; i++) {
                randomContext.generate(itemsSchema);
            }
        });
    }

    @Override
    public boolean handles(@Nullable OasSchema other) {
        return isArrayType(other);
    }

    @Override
    void generateIntoContext(RandomContext randomContext, OasSchema schema) {
        Object items = schema.items;

        if (items instanceof OasSchema itemsSchema) {
            createRandomArrayValueWithSchemaItem(randomContext, schema, itemsSchema);
        } else {
            throw new UnsupportedOperationException(
                    "Random array creation for an array with items having different schema is currently not supported!");
        }
    }
}
