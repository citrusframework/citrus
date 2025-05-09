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

import java.util.List;
import java.util.stream.Collectors;

import org.citrusframework.message.Message;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.ValidationContext;

/**
 * Basic control message validator for all message types. Subclasses only have to add
 * specific logic for message payload validation. This validator is based on a control message.
 *
 */
public class DefaultMessageValidator extends AbstractMessageValidator<ValidationContext> {

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return true;
    }

    @Override
    public ValidationContext findValidationContext(List<ValidationContext> validationContexts) {
        return super.findValidationContext(validationContexts.stream()
                .filter(it -> !(it instanceof HeaderValidationContext))
                .collect(Collectors.toList()));
    }

    @Override
    protected Class<ValidationContext> getRequiredValidationContextType() {
        return ValidationContext.class;
    }
}
