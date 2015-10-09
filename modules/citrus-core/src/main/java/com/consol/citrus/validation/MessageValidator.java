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
 * Message validator interface. Message validation need specific information like
 * control messages or validation scripts. These validation specific information is 
 * stored in a validation context, which is passed to the validation method.
 * 
 * @author Christoph Deppisch
 */
public interface MessageValidator<T extends ValidationContext> {
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