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

package com.consol.citrus.validation;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.Message;
import com.consol.citrus.validation.context.ValidationContext;

import java.util.List;

/**
 * Base abstract implementation for message validators. Calls method to finds a proper validation context
 * in the list of available validation contexts and performs validation.
 * 
 * @author Christoph Deppisch
 */
public abstract class AbstractMessageValidator<T extends ValidationContext> implements MessageValidator<T> {

    /**
     * Try to find proper validation context in list and perform message validation.
     */
    public void validateMessage(Message receivedMessage, TestContext context,
            List<ValidationContext> validationContexts) throws ValidationException {
        T validationContext = findValidationContext(validationContexts);
        
        // check if we were able to find a proper validation context
        if (validationContext != null) {
            validateMessage(receivedMessage, context, validationContext);
        }
    }
}
