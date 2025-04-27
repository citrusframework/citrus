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

package org.citrusframework.openapi.validation;

import org.citrusframework.openapi.OpenApiSettings;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.validation.context.DefaultValidationContext;
import org.citrusframework.validation.context.SchemaValidationContext;
import org.citrusframework.validation.context.ValidationContext;

/**
 * Validation context holding OpenAPI specific validation information.
 *
 * @since 4.3
 */
public class OpenApiMessageValidationContext extends DefaultValidationContext implements SchemaValidationContext {

    /**
     * Should message be validated with its schema definition. This is enabled with respect to
     * global settings, which are true by default. as only messages processed by open api actions
     * will be processed and validation information will be derived from open api spec.
     *
     * <p>Note that the default registered validation context is used for received messages. This is
     * why the schema validation is initialized with response validation enabled globally.
     */
    private final boolean schemaValidation;

    private final String openApiSpecificationId;

    private final String operationKey;

    public OpenApiMessageValidationContext(Builder builder) {
        super();

        // If not explicitly specified, go for the default.
        this.schemaValidation = builder.schemaValidation != null ? builder.schemaValidation
                : builder.openApiSpecification.isApiRequestValidationEnabled()
                || builder.openApiSpecification.isApiResponseValidationEnabled();
        this.openApiSpecificationId = builder.openApiSpecificationId;
        this.operationKey = builder.operationKey;
    }

    @Override
    public boolean requiresValidator() {
        return true;
    }

    @Override
    public boolean isSchemaValidationEnabled() {
        return schemaValidation;
    }

    @Override
    public String getSchemaRepository() {
        return openApiSpecificationId;
    }

    @Override
    public String getSchema() {
        return operationKey;
    }

    /**
     * Fluent builder
     */
    public static final class Builder implements
            ValidationContext.Builder<OpenApiMessageValidationContext, OpenApiMessageValidationContext.Builder>,
            SchemaValidationContext.Builder<OpenApiMessageValidationContext.Builder> {

        private OpenApiSpecification openApiSpecification;

        private String openApiSpecificationId;

        private String operationKey;

        /**
         * Mapped as object to be able to indicate "not explicitly set" in which case the default is
         * chosen.
         *
         * <p>Note that a message validation context is explicitly created only for send messages,
         * whereas default request validation enabled is chosen as default value.
         */
        private Boolean schemaValidation = OpenApiSettings.isRequestValidationEnabled();

        public static OpenApiMessageValidationContext.Builder openApi(OpenApiSpecification openApiSpecification) {
            Builder builder = new Builder();
            builder.openApiSpecification = openApiSpecification;
            return builder;
        }

        public static OpenApiMessageValidationContext.Builder openApi() {
            return new Builder();
        }

        /**
         * Sets schema validation enabled/disabled for this message.
         */
        public OpenApiMessageValidationContext.Builder schemaValidation(final boolean enabled) {
            this.schemaValidation = enabled;
            return this;
        }

        /**
         * Not used for open api validation. Schema is automatically derived from associated openApiSpecification.
         */
        @Override
        public OpenApiMessageValidationContext.Builder schema(final String schemaName) {
            this.operationKey = schemaName;
            return this;
        }

        /**
         * Not used for open api validation. Schema is automatically derived from associated openApiSpecification.
         */
        @Override
        public OpenApiMessageValidationContext.Builder schemaRepository(final String schemaRepository) {
            openApiSpecificationId = schemaRepository;
            return this;
        }

        @Override
        public OpenApiMessageValidationContext build() {
            return new OpenApiMessageValidationContext(this);
        }
    }
}
