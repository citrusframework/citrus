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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.apicurio.datamodels.openapi.models.OasSchema;
import jakarta.annotation.Nullable;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.model.OasModelHelper;

import static org.citrusframework.openapi.random.RandomConfiguration.RANDOM_CONFIGURATION;

/**
 * Context class for generating random values based on an OpenAPI specification.
 * This class manages the state and configuration needed to generate random values
 * for various schemas defined in the OpenAPI specification.
 */
public class RandomContext {

    private final OpenApiSpecification specification;
    private final RandomModelBuilder randomModelBuilder;

    /**
     * Cache for storing variable during random value generation.
     */
    private final Map<String, Object> contextVariables = new HashMap<>();
    private Map<String, OasSchema> schemaDefinitions;

    /**
     * Constructs a default RandomContext backed by no specification. Note, that this context can not
     * resolve referenced schemas, as no specification is available.
     */
    public RandomContext() {
        this.randomModelBuilder = new RandomModelBuilder(false);
        this.specification = null;
    }

    /**
     * Constructs a new RandomContext with the specified OpenAPI specification and quote option.
     *
     * @param specification the OpenAPI specification
     * @param quote         whether to quote the generated random values
     */
    public RandomContext(OpenApiSpecification specification, boolean quote) {
        this.specification = specification;
        this.randomModelBuilder = new RandomModelBuilder(quote);
    }

    /**
     * Returns the OpenAPI specification associated with this context.
     *
     * @return the OpenAPI specification
     */
    public OpenApiSpecification getSpecification() {
        return specification;
    }

    /**
     * Returns the RandomModelBuilder associated with this context.
     *
     * @return the RandomModelBuilder
     */
    public RandomModelBuilder getRandomModelBuilder() {
        return randomModelBuilder;
    }

    /**
     * Generates random values based on the specified schema.
     *
     * @param schema the schema to generate random values for
     */
    public void generate(OasSchema schema) {
        doGenerate(resolveSchema(schema));
    }

    void doGenerate(OasSchema resolvedSchema) {
        RANDOM_CONFIGURATION.getGenerator(resolvedSchema).generateIntoContext(this, resolvedSchema);
    }

    /**
     * Resolves a schema, handling reference schemas by fetching the referenced schema definition.
     *
     * @param schema the schema to resolve
     * @return the resolved schema
     */
    @Nullable OasSchema resolveSchema(OasSchema schema) {
        if (OasModelHelper.isReferenceType(schema)) {
            if (schemaDefinitions == null) {
                schemaDefinitions = getSchemaDefinitions();
            }
            schema = schemaDefinitions.get(OasModelHelper.getReferenceName(schema.$ref));
        }
        return schema;
    }

    /**
     * Returns the schema definitions from the specified OpenAPI document.
     *
     * @return a map of schema definitions
     */
    Map<String, OasSchema> getSchemaDefinitions() {
        return specification != null ? OasModelHelper.getSchemaDefinitions(specification.getOpenApiDoc(null)) : Collections.emptyMap();
    }

    /**
     * Retrieves a context variable by key, computing its value if necessary using the provided mapping function.
     *
     * @param <T>             the type of the context variable
     * @param key             the key of the context variable
     * @param mappingFunction the function to compute the value if it is not present
     * @return the context variable value
     */
    public <T> T get(String key, Function<String, T> mappingFunction) {
        //noinspection unchecked
        return (T) contextVariables.computeIfAbsent(key, mappingFunction);
    }
}
