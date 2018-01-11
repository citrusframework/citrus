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
 * @since 1.3.1
 */
public class SchemaRepositoryModelBuilder {

    /** Model object */
    private SchemaRepositoryModel model = new SchemaRepositoryModel();

    /**
     * Default constructor
     */
    public SchemaRepositoryModelBuilder() {
    }

    /**
     * Set the id.
     * @param id
     * @return
     */
    public SchemaRepositoryModelBuilder withId(String id) {
        model.setId(id);
        return this;
    }

    /**
     * Adds new schema by id and location.
     * @param id
     * @param location
     * @return
     */
    public SchemaRepositoryModelBuilder addSchema(String id, String location) {
        SchemaModel schema = new SchemaModel();
        schema.setId(id);
        schema.setLocation(location);

        if (model.getSchemas() == null) {
            model.setSchemas(new SchemaRepositoryModel.Schemas());
        }

        model.getSchemas().getSchemas().add(schema);
        return this;
    }

    /**
     * Adds new schema by instance.
     * @param schema
     * @return
     */
    public SchemaRepositoryModelBuilder addSchema(SchemaModel schema) {
        if (model.getSchemas() == null) {
            model.setSchemas(new SchemaRepositoryModel.Schemas());
        }

        model.getSchemas().getSchemas().add(schema);
        return this;
    }

    /**
     * Add new schema reference by id
     * @param schemaId the schema to reference
     * @return
     */
    public SchemaRepositoryModelBuilder addSchemaReference(String schemaId) {
        SchemaRepositoryModel.Schemas.Reference schemaRef = new SchemaRepositoryModel.Schemas.Reference();
        schemaRef.setSchema(schemaId);

        if (model.getSchemas() == null) {
            model.setSchemas(new SchemaRepositoryModel.Schemas());
        }

        model.getSchemas().getReferences().add(schemaRef);
        return this;
    }

    /**
     * Builds the model.
     * @return
     */
    public SchemaRepositoryModel build() {
        return model;
    }
}
