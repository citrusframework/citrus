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

package com.consol.citrus.validation.context;

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
     * Enable/disable schema validation.
     * @param schemaValidation the schemaValidation to set
     */
    void setSchemaValidation(boolean schemaValidation);

    /**
     * Gets the schemaRepository.
     * @return the schemaRepository the schemaRepository to get.
     */
    String getSchemaRepository();

    /**
     * Sets the schemaRepository.
     * @param schemaRepository the schemaRepository to set
     */
    void setSchemaRepository(String schemaRepository);

    /**
     * Gets the schema.
     * @return the schema the schema to get.
     */
    String getSchema();

    /**
     * Sets the schema.
     * @param schema the schema to set
     */
    void setSchema(String schema);
}
