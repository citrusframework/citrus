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

package org.citrusframework.validation;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.citrusframework.spi.TypeResolver;
import org.citrusframework.validation.context.SchemaValidationContext;
import org.citrusframework.validation.matcher.ValidationMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

public interface SchemaValidator<T extends SchemaValidationContext> {

    /** Logger */
    Logger logger = LoggerFactory.getLogger(MessageValidator.class);

    /** Schema validator resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/message/schemaValidator";

    /** Type resolver to find custom schema validators on classpath via resource path lookup */
    TypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    /**
     * Resolves all available validators from resource path lookup. Scans classpath for validator meta information
     * and instantiates those validators.
     * @return
     */
    static Map<String, SchemaValidator<? extends SchemaValidationContext>> lookup() {
        Map<String, SchemaValidator<?>> validators = TYPE_RESOLVER.resolveAll("", TypeResolver.DEFAULT_TYPE_PROPERTY, "name");

        if (logger.isDebugEnabled()) {
            validators.forEach((k, v) -> logger.debug(String.format("Found message validator '%s' as %s", k, v.getClass())));
        }

        return validators;
    }

    /**
     * Resolves validator from resource path lookup with given validator resource name. Scans classpath for validator meta information
     * with given name and returns instance of validator. Returns optional instead of throwing exception when no validator
     * could be found.
     * @param validator
     * @return
     */
    static Optional<SchemaValidator<? extends SchemaValidationContext>> lookup(String validator) {
        try {
            SchemaValidator<? extends ValidationMatcher> instance = TYPE_RESOLVER.resolve(validator);
            return Optional.of(instance);
        } catch (CitrusRuntimeException e) {
            logger.warn(String.format("Failed to resolve validator from resource '%s/%s'", RESOURCE_PATH, validator));
        }

        return Optional.empty();
    }
    /**
     * Validates the given message against schemas
     * @param message The message to be validated
     * @param context The test context of the current test execution
     * @param validationContext The context of the validation to be used for the validation
     * @return A report holding the results of the validation
     */
    void validate(Message message, TestContext context, T validationContext);

    /**
     *
     * @param messageType
     * @param message
     * @return true if the message/message type can be validated by this validator
     */
    boolean supportsMessageType(String messageType, Message message);

    /**
     * @param message the message which is subject of validation
     * @param  schemaValidationEnabled flag to indicate whether schema validation is explicitly enabled
     * @return true, if the validator can validate the given message
     */
    boolean canValidate(Message message, boolean schemaValidationEnabled);

    /**
     * Validate the message against the given schemaRepository and schema.
     */
    void validate(Message message, TestContext context, String schemaRepository, String schema);
}
