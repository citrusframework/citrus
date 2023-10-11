/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.validation.context;

/**
 * This context holds the basic information for the validation of messages against schemas
 * @since 2.7.3
 */
public interface SchemaValidationContext {

    /**
     * Is schema validation enabled.
     * @return the schemaValidation
     */
    boolean isSchemaValidationEnabled();

    /**
     * Gets the schemaRepository.
     * @return the schemaRepository to get.
     */
    String getSchemaRepository();

    /**
     * Gets the schema.
     * @return the schema to get.
     */
    String getSchema();

    /**
     * Fluent builder
     * @param <B>
     */
    interface Builder<B> {

        /**
         * Sets schema validation enabled/disabled for this message.
         * @param enabled
         * @return
         */
        B schemaValidation(final boolean enabled);

        /**
         * Sets explicit schema instance name to use for schema validation.
         * @param schemaName
         * @return
         */
        B schema(final String schemaName);

        /**
         * Sets explicit xsd schema repository instance to use for validation.
         * @param schemaRepository
         * @return
         */
        B schemaRepository(final String schemaRepository);
    }
}
