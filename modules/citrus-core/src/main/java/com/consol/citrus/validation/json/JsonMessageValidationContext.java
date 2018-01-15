/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.validation.json;

import com.consol.citrus.validation.context.DefaultValidationContext;
import com.consol.citrus.validation.context.SchemaValidationContext;

import java.util.HashSet;
import java.util.Set;

/**
 * Validation context holding JSON specific validation information.
 * @author Christoph Deppisch
 * @since 2.3
 */
public class JsonMessageValidationContext extends DefaultValidationContext implements SchemaValidationContext {

    /** Map holding xpath expressions to identify the ignored message elements */
    private Set<String> ignoreExpressions = new HashSet<>();

    /**
     * Should message be validated with its schema definition
     *
     * This is currently disabled by default, because old json tests would fail with a validation exception
     * as soon as a json schema repository is specified and the schema validation is activated.
     */
    private boolean schemaValidation = false;

    /** Explicit schema repository to use for this validation */
    private String schemaRepository;

    /** Explicit schema instance to use for this validation */
    private String schema;

    /**
     * Get ignored message elements.
     * @return the ignoreExpressions
     */
    public Set<String> getIgnoreExpressions() {
        return ignoreExpressions;
    }

    /**
     * Set ignored message elements.
     * @param ignoreExpressions the ignoreExpressions to set
     */
    public void setIgnoreExpressions(Set<String> ignoreExpressions) {
        this.ignoreExpressions = ignoreExpressions;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSchemaValidationEnabled() {
        return schemaValidation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSchemaValidation(boolean schemaValidation) {
        this.schemaValidation = schemaValidation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSchemaRepository() {
        return schemaRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSchemaRepository(String schemaRepository) {
        this.schemaRepository = schemaRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSchema() {
        return schema;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSchema(String schema) {
        this.schema = schema;
    }
}
