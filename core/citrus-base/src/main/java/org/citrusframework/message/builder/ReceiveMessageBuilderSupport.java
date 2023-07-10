/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.message.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.validation.HeaderValidator;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.ValidationContextAdapter;
import org.citrusframework.validation.ValidationProcessor;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.variable.VariableExtractor;

/**
 * @author Christoph Deppisch
 */
public class ReceiveMessageBuilderSupport<T extends ReceiveMessageAction, B extends ReceiveMessageAction.ReceiveMessageActionBuilder<T, S, B>, S extends ReceiveMessageBuilderSupport<T, B, S>>
        extends MessageBuilderSupport<T, B, S> {

    private final List<MessageProcessor> controlMessageProcessors = new ArrayList<>();

    private boolean headerNameIgnoreCase = false;

    protected ReceiveMessageBuilderSupport(B delegate) {
        super(delegate);
    }

    /**
     * Adds a custom timeout to this message receiving action.
     *
     * @param receiveTimeout
     * @return The modified receive message action builder
     */
    public S timeout(final long receiveTimeout) {
        delegate.timeout(receiveTimeout);
        return self;
    }

    /**
     * Validate header names with case insensitive keys.
     *
     * @param value
     * @return The modified receive message action builder
     */
    public S headerNameIgnoreCase(final boolean value) {
        this.headerNameIgnoreCase = value;
        return self;
    }

    /**
     * Adds a validation context.
     * @param validationContext
     * @return The modified receive message action builder
     */
    public S validate(final ValidationContext.Builder<?, ?> validationContext) {
        delegate.validate(validationContext);
        return self;
    }

    /**
     * Adds a validation context.
     * @param validationContext
     * @return The modified receive message action builder
     */
    public S validate(final ValidationContext validationContext) {
        return validate((ValidationContext.Builder) () -> validationContext);
    }

    /**
     * Adds a validation context.
     * @param adapter
     * @return The modified receive message action builder
     */
    public S validate(final ValidationContextAdapter adapter) {
        return validate(adapter.asValidationContext());
    }

    /**
     * Sets validation contexts.
     * @param validationContexts
     * @return The modified receive message action builder
     */
    public S validate(final List<ValidationContext.Builder<?, ?>> validationContexts) {
        delegate.validate(validationContexts);
        return self;
    }

    /**
     * Sets validation contexts.
     * @param validationContexts
     * @return The modified receive message action builder
     */
    public S validate(ValidationContext.Builder<?, ?> ... validationContexts) {
        return validate(Arrays.asList(validationContexts));
    }

    /**
     * Sets message selector string.
     *
     * @param messageSelector
     * @return The modified receive message action builder
     */
    public S selector(final String messageSelector) {
        delegate.selector(messageSelector);
        return self;
    }

    /**
     * Sets message selector elements.
     *
     * @param messageSelector
     * @return The modified receive message action builder
     */
    public S selector(final Map<String, String> messageSelector) {
        delegate.selector(messageSelector);
        return self;
    }

    /**
     * Sets explicit message validators for this receive action.
     *
     * @param validator
     * @return The modified receive message action builder
     */
    public S validator(final MessageValidator<? extends ValidationContext> validator) {
        delegate.validator(validator);
        return self;
    }

    /**
     * Sets explicit message validators for this receive action.
     *
     * @param validators
     * @return The modified receive message action builder
     */
    @SafeVarargs
    public final S validators(final MessageValidator<? extends ValidationContext>... validators) {
        return validators(Arrays.asList(validators));
    }

    /**
     * Sets explicit message validator names for this receive action.
     *
     * @param validators
     * @return The modified receive message action builder
     */
    public final S validators(final String... validators) {
        delegate.validators(validators);
        return self;
    }

    /**
     * Sets explicit header validators for this receive action.
     *
     * @param validators
     * @return The modified receive message action builder
     */
    public final S validators(final HeaderValidator... validators) {
        delegate.validators(validators);
        return self;
    }

    /**
     * Sets explicit message validators for this receive action.
     *
     * @param validators
     * @return The modified receive message action builder
     */
    public S validators(final List<MessageValidator<? extends ValidationContext>> validators) {
        delegate.validators(validators);
        return self;
    }

    /**
     * Sets explicit message validator by name.
     *
     * @param validatorName
     * @return The modified receive message action builder
     */
    public S validator(final String validatorName) {
        delegate.validator(validatorName);
        return self;
    }

    /**
     * Sets explicit header validator for this receive action.
     *
     * @param validator
     * @return The modified receive message action builder
     */
    public S validator(final HeaderValidator validator) {
        delegate.validator(validator);
        return self;
    }

    /**
     * Adds validation processor to the receive action for validating
     * the received message with Java code.
     *
     * @param processor
     * @return The modified receive message action builder
     */
    public B validate(final ValidationProcessor processor) {
        return delegate.validate(processor);
    }

    @Override
    public S process(MessageProcessor processor) {
        if (processor instanceof VariableExtractor) {
            super.process(processor);
        } else {
            this.controlMessageProcessors.add(processor);
        }
        return self;
    }

    public List<MessageProcessor> getControlMessageProcessors() {
        return controlMessageProcessors;
    }

    public boolean isHeaderNameIgnoreCase() {
        return headerNameIgnoreCase;
    }
}
