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

package org.citrusframework.kubernetes.actions;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.context.json.JsonMessageValidationContext;
import org.citrusframework.validation.context.json.JsonPathMessageValidationContext;

/**
 * Validates serialized Kubernetes command results against an expected JSON result and/or JsonPath expressions.
 */
public class KubernetesResultValidator {

    public static final String DEFAULT_JSON_MESSAGE_VALIDATOR = "defaultJsonMessageValidator";
    public static final String DEFAULT_JSON_PATH_MESSAGE_VALIDATOR = "defaultJsonPathMessageValidator";

    private final String commandResult;
    private final Map<String, Object> commandResultExpressions;
    private final MessageValidator<? extends ValidationContext> jsonMessageValidator;
    private final MessageValidator<? extends ValidationContext> jsonPathMessageValidator;

    public KubernetesResultValidator(String commandResult, Map<String, Object> commandResultExpressions,
                                     MessageValidator<? extends ValidationContext> jsonMessageValidator,
                                     MessageValidator<? extends ValidationContext> jsonPathMessageValidator) {
        this.commandResult = commandResult;
        this.commandResultExpressions = commandResultExpressions;
        this.jsonMessageValidator = jsonMessageValidator;
        this.jsonPathMessageValidator = jsonPathMessageValidator;
    }

    /**
     * Indicates whether result validation has been configured.
     */
    public boolean hasValidation() {
        return StringUtils.hasText(commandResult) || !commandResultExpressions.isEmpty();
    }

    /**
     * Validate the given serialized command result.
     */
    public void validate(String commandResultJson, TestContext context) {
        DefaultMessage actual = new DefaultMessage(commandResultJson);
        DefaultMessage expected = new DefaultMessage(commandResult);

        if (StringUtils.hasText(commandResult)) {
            getMessageValidator(context).validateMessage(actual, expected, context,
                    Collections.singletonList(new JsonMessageValidationContext()));
        }

        if (!commandResultExpressions.isEmpty()) {
            JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext.Builder()
                    .expressions(commandResultExpressions)
                    .build();
            getPathValidator(context).validateMessage(actual, expected, context,
                    Collections.singletonList(validationContext));
        }
    }

    /**
     * Find proper JSON message validator. Uses several strategies to lookup default JSON message validator.
     */
    @SuppressWarnings("unchecked")
    private MessageValidator<? extends ValidationContext> getMessageValidator(TestContext context) {
        if (jsonMessageValidator != null) {
            return jsonMessageValidator;
        }

        Optional<MessageValidator<? extends ValidationContext>> defaultJsonMessageValidator = context.getMessageValidatorRegistry().findMessageValidator(DEFAULT_JSON_MESSAGE_VALIDATOR);

        if (defaultJsonMessageValidator.isEmpty()
                && context.getReferenceResolver().isResolvable(DEFAULT_JSON_MESSAGE_VALIDATOR)) {
            defaultJsonMessageValidator = Optional.of(context.getReferenceResolver().resolve(DEFAULT_JSON_MESSAGE_VALIDATOR, MessageValidator.class));
        }

        if (defaultJsonMessageValidator.isEmpty()) {
            defaultJsonMessageValidator = MessageValidator.lookup("json");
        }

        if (defaultJsonMessageValidator.isPresent()) {
            return defaultJsonMessageValidator.get();
        }

        throw new CitrusRuntimeException("Unable to locate proper JSON message validator - please add validator to project");
    }

    /**
     * Find proper JSON path message validator. Uses several strategies to lookup default JSON path message validator.
     */
    @SuppressWarnings("unchecked")
    private MessageValidator<? extends ValidationContext> getPathValidator(TestContext context) {
        if (jsonPathMessageValidator != null) {
            return jsonPathMessageValidator;
        }

        Optional<MessageValidator<? extends ValidationContext>> defaultJsonPathMessageValidator = context.getMessageValidatorRegistry().findMessageValidator(DEFAULT_JSON_PATH_MESSAGE_VALIDATOR);

        if (defaultJsonPathMessageValidator.isEmpty()
                && context.getReferenceResolver().isResolvable(DEFAULT_JSON_PATH_MESSAGE_VALIDATOR)) {
            defaultJsonPathMessageValidator = Optional.of(context.getReferenceResolver().resolve(DEFAULT_JSON_PATH_MESSAGE_VALIDATOR, MessageValidator.class));
        }

        if (defaultJsonPathMessageValidator.isEmpty()) {
            defaultJsonPathMessageValidator = MessageValidator.lookup("json-path");
        }

        if (defaultJsonPathMessageValidator.isPresent()) {
            return defaultJsonPathMessageValidator.get();
        }

        throw new CitrusRuntimeException("Unable to locate proper JSON path message validator - please add validator to project");
    }
}
