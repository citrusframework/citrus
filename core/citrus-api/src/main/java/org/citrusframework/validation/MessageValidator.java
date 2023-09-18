/*
 * Copyright 2006-2010 the original author or authors.
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.Message;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.citrusframework.spi.TypeResolver;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.matcher.ValidationMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message validator interface. Message validation need specific information like
 * control messages or validation scripts. These validation specific information is
 * stored in a validation context, which is passed to the validation method.
 *
 * @author Christoph Deppisch
 */
public interface MessageValidator<T extends ValidationContext> {

    /** Logger */
    Logger logger = LoggerFactory.getLogger(MessageValidator.class);

    /** Message validator resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/message/validator";

    /** Type resolver to find custom message validators on classpath via resource path lookup */
    TypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    /**
     * Resolves all available validators from resource path lookup. Scans classpath for validator meta information
     * and instantiates those validators.
     * @return
     */
    static Map<String, MessageValidator<? extends ValidationContext>> lookup() {
        Map<String, MessageValidator<?>> validators = TYPE_RESOLVER.resolveAll("", TypeResolver.DEFAULT_TYPE_PROPERTY, "name");

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
    static Optional<MessageValidator<? extends ValidationContext>> lookup(String validator) {
        try {
            MessageValidator<? extends ValidationMatcher> instance = TYPE_RESOLVER.resolve(validator);
            return Optional.of(instance);
        } catch (CitrusRuntimeException e) {
            logger.warn(String.format("Failed to resolve validator from resource '%s/%s'", RESOURCE_PATH, validator));
        }

        return Optional.empty();
    }

    /**
     * Validates a message with given test context and validation context.
     * @param receivedMessage the message to validate.
     * @param controlMessage the expected control message.
     * @param context the current test context.
     * @param validationContexts list of available validation contexts.
     */
    void validateMessage(Message receivedMessage,
                         Message controlMessage,
                                TestContext context,
                                List<ValidationContext> validationContexts)
                                throws ValidationException;

    /**
     * Checks if this message validator is capable of this message type. XML message validators may only apply to this message
     * type while JSON message validator implementations do not and vice versa. This check is called by the {@link MessageValidatorRegistry}
     * in order to find a proper message validator for a message.
     *
     * @param messageType the message type representation as String (e.g. xml, json, csv, plaintext).
     * @param message the message object
     * @return true if this message validator is capable of validating the message type.
     */
    boolean supportsMessageType(String messageType, Message message);
}
