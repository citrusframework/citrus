/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.model.config.core;

/**
 * Builder for creating schema repository instances.
 *
 * @author Martin.Maher@consol.de
 * @since 2013.04.19
 */
public class SchemaRepositoryBuilder {

    /** Model object */
    private SchemaRepository model = new SchemaRepository();

    /**
     * Default constructor
     */
    public SchemaRepositoryBuilder() {
    }

    /**
     * Set the id.
     * @param id
     * @return
     */
    public SchemaRepositoryBuilder withId(String id) {
        model.setId(id);
        return this;
    }

    /**
     * Add new schema
     * @param schemaRef the schema to reference
     * @return
     */
    public SchemaRepositoryBuilder addSchema(String schemaRef) {
        SchemaRepository.Schemas.Schema schema = new SchemaRepository.Schemas.Schema();
        schema.setRef(schemaRef);
        if(model.getSchemas() == null) {
            model.setSchemas(new SchemaRepository.Schemas());
        }
        model.getSchemas().getSchemas().add(schema);
        return this;
    }

    /**
     * Builds the model.
     * @return
     */
    public SchemaRepository build() {
        return model;
    }
}
