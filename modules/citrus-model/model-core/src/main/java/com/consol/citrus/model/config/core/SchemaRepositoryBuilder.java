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
     * Adds new schema by id and location.
     * @param id
     * @param location
     * @return
     */
    public SchemaRepositoryBuilder addSchema(String id, String location) {
        Schema schema = new Schema();
        schema.setId(id);
        schema.setLocation(location);

        if(model.getSchemas() == null) {
            model.setSchemas(new SchemaRepository.Schemas());
        }

        model.getSchemas().getRevesAndSchemas().add(schema);
        return this;
    }

    /**
     * Adds new schema by instance.
     * @param schema
     * @return
     */
    public SchemaRepositoryBuilder addSchema(Schema schema) {
        if(model.getSchemas() == null) {
            model.setSchemas(new SchemaRepository.Schemas());
        }

        model.getSchemas().getRevesAndSchemas().add(schema);
        return this;
    }

    /**
     * Add new schema reference by id
     * @param schemaId the schema to reference
     * @return
     */
    public SchemaRepositoryBuilder addSchemaReference(String schemaId) {
        SchemaRepository.Schemas.Ref schemaRef = new SchemaRepository.Schemas.Ref();
        schemaRef.setSchema(schemaId);

        if(model.getSchemas() == null) {
            model.setSchemas(new SchemaRepository.Schemas());
        }

        model.getSchemas().getRevesAndSchemas().add(schemaRef);
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
