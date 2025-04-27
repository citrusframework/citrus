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

import java.util.Objects;

import io.apicurio.datamodels.openapi.models.OasSchema;

/**
 * Abstract base class for generators that produce random data based on an OpenAPI schema.
 * Subclasses must implement the {@link #generateIntoContext} method to provide specific random data generation logic.
 *
 * <p>The class provides methods for determining if a generator can handle a given schema,
 * based on the schema type, format, pattern, and enum constraints.
 */
public abstract class RandomGenerator {

    public static final String ANY = "$ANY$";
    public static final RandomGenerator NOOP_RANDOM_GENERATOR = new RandomGenerator() {

        @Override
        void generateIntoContext(RandomContext randomContext, OasSchema schema) {
            // Do nothing
        }
    };

    private final OasSchema schema;

    protected RandomGenerator() {
        this.schema = null;
    }

    protected RandomGenerator(OasSchema schema) {
        this.schema = schema;
    }

    public boolean handles(OasSchema other) {
        if (other == null || schema == null) {
            return false;
        }

        if (ANY.equals(schema.type) || Objects.equals(schema.type, other.type)) {
            if (schema.format != null) {
                return (ANY.equals(schema.format) && other.format != null) || Objects.equals(schema.format, other.format);
            }

            if (schema.pattern != null) {
                return (ANY.equals(schema.pattern) && other.pattern != null) || Objects.equals(schema.pattern, other.pattern);
            }

            return true;
        }

        return false;
    }

    abstract void generateIntoContext(RandomContext randomContext, OasSchema schema);

}
