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

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.Message;
import org.citrusframework.validation.context.ValidationContext;

/**
 * Base abstract implementation for message validators. Calls method to finds a proper validation context
 * in the list of available validation contexts and performs validation.
 *
 * @author Christoph Deppisch
 */
public abstract class AbstractMessageValidator<T extends ValidationContext> implements MessageValidator<T> {

    @Override
    public final void validateMessage(Message receivedMessage, Message controlMessage, TestContext context,
            List<ValidationContext> validationContexts) throws ValidationException {
        T validationContext = findValidationContext(validationContexts);

        // check if we were able to find a proper validation context
        if (validationContext != null) {
            validateMessage(receivedMessage, controlMessage, context, validationContext);
        }
    }

    /**
     * Validates message with most appropriate validation context.
     * @param receivedMessage
     * @param controlMessage
     * @param context
     * @param validationContext
     */
    public void validateMessage(Message receivedMessage, Message controlMessage, TestContext context, T validationContext) {
    }

    /**
     * Provides class type of most appropriate validation context.
     * @return
     */
    protected abstract Class<T> getRequiredValidationContextType();

    /**
     * Finds the message validation context that is most appropriate for this validator implementation.
     * @param validationContexts
     * @return
     */
    public T findValidationContext(List<ValidationContext> validationContexts) {
        for (ValidationContext validationContext : validationContexts) {
            if (getRequiredValidationContextType().isInstance(validationContext)) {
                return (T) validationContext;
            }
        }

        return null;
    }
}
