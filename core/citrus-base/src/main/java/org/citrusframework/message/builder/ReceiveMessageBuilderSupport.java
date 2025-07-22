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

package org.citrusframework.message.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.ReceiveMessageBuilderFactory;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.validation.HeaderValidator;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.ValidationContextAdapter;
import org.citrusframework.validation.ValidationProcessor;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.variable.VariableExtractor;

public class ReceiveMessageBuilderSupport<T extends ReceiveMessageAction, B extends ReceiveMessageAction.ReceiveMessageActionBuilder<T, S, B>, S extends ReceiveMessageBuilderSupport<T, B, S>>
        extends MessageBuilderSupport<T, B, S> implements ReceiveMessageBuilderFactory<T, S> {

    private final List<MessageProcessor> controlMessageProcessors = new ArrayList<>();

    private boolean headerNameIgnoreCase = false;

    protected ReceiveMessageBuilderSupport(B delegate) {
        super(delegate);
    }

    @Override
    public S timeout(final long receiveTimeout) {
        delegate.timeout(receiveTimeout);
        return self;
    }

    @Override
    public S headerNameIgnoreCase(final boolean value) {
        this.headerNameIgnoreCase = value;
        return self;
    }

    @Override
    public S validate(final ValidationContext.Builder<?, ?> validationContext) {
        delegate.validate(validationContext);
        return self;
    }

    @Override
    public S validate(final ValidationContext validationContext) {
        return validate((ValidationContext.Builder) () -> validationContext);
    }

    @Override
    public S validate(final ValidationContextAdapter adapter) {
        return validate(adapter.asValidationContext());
    }

    @Override
    public S validate(final List<ValidationContext.Builder<?, ?>> validationContexts) {
        delegate.validate(validationContexts);
        return self;
    }

    @Override
    public S validate(ValidationContext.Builder<?, ?>... validationContexts) {
        return validate(Arrays.asList(validationContexts));
    }

    @Override
    public S selector(final String messageSelector) {
        delegate.selector(messageSelector);
        return self;
    }

    @Override
    public S selector(final Map<String, String> messageSelector) {
        delegate.selector(messageSelector);
        return self;
    }

    @Override
    public S validator(final MessageValidator<? extends ValidationContext> validator) {
        delegate.validator(validator);
        return self;
    }

    @Override
    @SafeVarargs
    public final S validators(final MessageValidator<? extends ValidationContext>... validators) {
        return validators(Arrays.asList(validators));
    }

    @Override
    public final S validators(final String... validators) {
        delegate.validators(validators);
        return self;
    }

    @Override
    public final S validators(final HeaderValidator... validators) {
        delegate.validators(validators);
        return self;
    }

    @Override
    public S validators(final List<MessageValidator<? extends ValidationContext>> validators) {
        delegate.validators(validators);
        return self;
    }

    @Override
    public S validator(final String validatorName) {
        delegate.validator(validatorName);
        return self;
    }

    @Override
    public S validator(final HeaderValidator validator) {
        delegate.validator(validator);
        return self;
    }

    @Override
    public S validate(final ValidationProcessor processor) {
        delegate.validate(processor);
        return self;
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
