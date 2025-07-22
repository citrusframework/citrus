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

package org.citrusframework.actions;

import java.util.List;
import java.util.Map;

import org.citrusframework.TestAction;
import org.citrusframework.validation.HeaderValidator;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.ValidationContextAdapter;
import org.citrusframework.validation.ValidationProcessor;
import org.citrusframework.validation.context.ValidationContext;

public interface ReceiveMessageBuilderFactory<T extends TestAction, M extends ReceiveMessageBuilderFactory<T, M>> extends MessageBuilderFactory<T, M> {

    /**
     * Adds a custom timeout to this message receiving action.
     * @param receiveTimeout
     * @return The modified receive message action builder
     */
    M timeout(long receiveTimeout);

    /**
     * Validate header names with case insensitive keys.
     * @param value
     * @return The modified receive message action builder
     */
    M headerNameIgnoreCase(boolean value);

    /**
     * Adds a validation context.
     * @param validationContext
     * @return The modified receive message action builder
     */
    M validate(ValidationContext.Builder<?, ?> validationContext);

    /**
     * Adds a validation context.
     * @param validationContext
     * @return The modified receive message action builder
     */
    M validate(ValidationContext validationContext);

    /**
     * Adds a validation context.
     * @param adapter
     * @return The modified receive message action builder
     */
    M validate(ValidationContextAdapter adapter);

    /**
     * Sets validation contexts.
     * @param validationContexts
     * @return The modified receive message action builder
     */
    M validate(List<ValidationContext.Builder<?, ?>> validationContexts);

    /**
     * Sets validation contexts.
     * @param validationContexts
     * @return The modified receive message action builder
     */
    M validate(ValidationContext.Builder<?, ?>... validationContexts);

    /**
     * Sets message selector string.
     * @param messageSelector
     * @return The modified receive message action builder
     */
    M selector(String messageSelector);

    /**
     * Sets message selector elements.
     * @param messageSelector
     * @return The modified receive message action builder
     */
    M selector(Map<String, String> messageSelector);

    /**
     * Sets explicit message validators for this receive action.
     * @param validator
     * @return The modified receive message action builder
     */
    M validator(MessageValidator<? extends ValidationContext> validator);

    /**
     * Sets explicit message validators for this receive action.
     * @param validators
     * @return The modified receive message action builder
     */
    M validators(MessageValidator<? extends ValidationContext>... validators);

    /**
     * Sets explicit message validator names for this receive action.
     * @param validators
     * @return The modified receive message action builder
     */
    M validators(String... validators);

    /**
     * Sets explicit header validators for this receive action.
     * @param validators
     * @return The modified receive message action builder
     */
    M validators(HeaderValidator... validators);

    /**
     * Sets explicit message validators for this receive action.
     * @param validators
     * @return The modified receive message action builder
     */
    M validators(List<MessageValidator<? extends ValidationContext>> validators);

    /**
     * Sets explicit message validator by name.
     * @param validatorName
     * @return The modified receive message action builder
     */
    M validator(String validatorName);

    /**
     * Sets explicit header validator for this receive action.
     * @param validator
     * @return The modified receive message action builder
     */
    M validator(HeaderValidator validator);

    /**
     * Adds validation processor to the action for validating
     * the received message with Java code.
     * @param processor
     * @return The modified receive message action builder
     */
    M validate(ValidationProcessor processor);
}
