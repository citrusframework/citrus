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

package org.citrusframework.validation.json;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.citrusframework.validation.context.DefaultValidationContext;
import org.citrusframework.validation.context.SchemaValidationContext;
import org.citrusframework.validation.context.ValidationContext;

/**
 * Validation context holding JSON specific validation information.
 * @author Christoph Deppisch
 * @since 2.3
 */
public class JsonMessageValidationContext extends DefaultValidationContext implements SchemaValidationContext {

    /** Map holding xpath expressions to identify the ignored message elements */
    private final Set<String> ignoreExpressions;

    /**
     * Should message be validated with its schema definition
     *
     * This is currently disabled by default, because old json tests would fail with a validation exception
     * as soon as a json schema repository is specified and the schema validation is activated.
     */
    private final boolean schemaValidation;

    /** Explicit schema repository to use for this validation */
    private final String schemaRepository;

    /** Explicit schema instance to use for this validation */
    private final String schema;

    /**
     * Default constructor.
     */
    public JsonMessageValidationContext() {
        this(new Builder());
    }

    /**
     * Constructor using fluent builder.
     * @param builder
     */
    public JsonMessageValidationContext(Builder builder) {
        this.ignoreExpressions = builder.ignoreExpressions;
        this.schemaValidation = builder.schemaValidation;
        this.schemaRepository = builder.schemaRepository;
        this.schema = builder.schema;
    }

    /**
     * Fluent builder
     */
    public static final class Builder implements ValidationContext.Builder<JsonMessageValidationContext, Builder>,
            SchemaValidationContext.Builder<Builder> {

        private final Set<String> ignoreExpressions = new HashSet<>();
        private boolean schemaValidation = true;
        private String schemaRepository;
        private String schema;

        public static Builder json() {
            return new Builder();
        }

        public JsonPathMessageValidationContext.Builder expressions() {
            return new JsonPathMessageValidationContext.Builder();
        }

        public JsonPathMessageValidationContext.Builder expression(String path, Object expectedValue) {
            return new JsonPathMessageValidationContext.Builder()
                    .expression(path, expectedValue);
        }

        /**
         * Sets schema validation enabled/disabled for this message.
         *
         * @param enabled
         * @return
         */
        public Builder schemaValidation(final boolean enabled) {
            this.schemaValidation = enabled;
            return this;
        }

        /**
         * Sets explicit schema instance name to use for schema validation.
         *
         * @param schemaName
         * @return
         */
        public Builder schema(final String schemaName) {
            this.schema = schemaName;
            return this;
        }

        /**
         * Sets explicit xsd schema repository instance to use for validation.
         *
         * @param schemaRepository
         * @return
         */
        public Builder schemaRepository(final String schemaRepository) {
            this.schemaRepository = schemaRepository;
            return this;
        }

        /**
         * Adds ignore path expression for message element.
         *
         * @param path
         * @return
         */
        public Builder ignore(final String path) {
            this.ignoreExpressions.add(path);
            return this;
        }

        /**
         * Adds a list of ignore path expressions for message element.
         *
         * @param paths
         * @return
         */
        public Builder ignore(final List<String> paths) {
            this.ignoreExpressions.addAll(paths);
            return this;
        }

        @Override
        public JsonMessageValidationContext build() {
            return new JsonMessageValidationContext(this);
        }
    }

    /**
     * Get ignored message elements.
     * @return the ignoreExpressions
     */
    public Set<String> getIgnoreExpressions() {
        return ignoreExpressions;
    }

    @Override
    public boolean isSchemaValidationEnabled() {
        return schemaValidation;
    }

    @Override
    public String getSchemaRepository() {
        return schemaRepository;
    }

    @Override
    public String getSchema() {
        return schema;
    }

}
