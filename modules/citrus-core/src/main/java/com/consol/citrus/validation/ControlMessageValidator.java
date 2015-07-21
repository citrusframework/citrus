/*
 * Copyright 2006-2011 the original author or authors.
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
import com.consol.citrus.message.*;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.matcher.ValidationMatcherUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Basic control message validator provides message header validation. Subclasses only have to add
 * specific logic for message payload validation. This validator is based on a control message.
 * 
 * @author Christoph Deppisch
 */
public class ControlMessageValidator<T extends ControlMessageValidationContext> extends AbstractMessageValidator<T> {

    /**
     * Logger
     */
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * Implementation performs message header validation as well as message
     * payload validation.
     */
    public void validateMessage(Message receivedMessage, TestContext context,
            T validationContext) {
        log.info("Start message validation ...");

        Message controlMessage = validationContext.getControlMessage(context);

        // validate message payload first
        validateMessagePayload(receivedMessage,
                controlMessage,
                validationContext,
                context);

        // validate message headers
        validateMessageHeader(controlMessage.copyHeaders(),
                receivedMessage.copyHeaders(),
                validationContext,
                context);

        log.info("Message validation successful: All values OK");
    }
    
    /**
     * Validates the message payload with comparison to the control message payload 
     * located in validation context.
     * 
     * @param receivedMessage the received message to check.
     * @param controlMessage the expected control message.
     * @param validationContext the current validation context
     * @param context the current test context with all variables.
     */
    public void validateMessagePayload(Message receivedMessage,
            Message controlMessage, T validationContext, TestContext context) throws ValidationException {
    }
    
    
    /**
     * Validates the message header comparing its values to a control header set.
     *
     * @param controlHeaders the expected control headers.
     * @param receivedHeaders the actual headers from message received.
     * @param validationContext the current validation context
     * @param context the current test context.
     */
    public void validateMessageHeader(Map<String, Object> controlHeaders, Map<String, Object> receivedHeaders,
                                      T validationContext, TestContext context) {
        if (CollectionUtils.isEmpty(controlHeaders)) { return; }

        log.info("Start message header validation");

        for (Entry<String, Object> entry : controlHeaders.entrySet()) {
            String headerName = entry.getKey();
            String expectedValue = entry.getValue().toString();
            String actualValue = null;

            if (MessageHeaderUtils.isSpringInternalHeader(headerName) ||
                    headerName.startsWith(MessageHeaders.MESSAGE_PREFIX)) {
                continue;
            }
            //check if header expression is variable or function
            headerName = context.resolveDynamicValue(headerName);

            if (!receivedHeaders.containsKey(headerName)) {
                throw new ValidationException("Validation failed: Header element '" + headerName + "' is missing");
            }

            if (receivedHeaders.get(headerName) != null) {
                actualValue = receivedHeaders.get(headerName).toString();
            }
            
            //check if value expression is variable or function
            expectedValue = context.replaceDynamicContentInString(expectedValue);

            try {
                if (actualValue != null) {
                    if (ValidationMatcherUtils.isValidationMatcherExpression(expectedValue)) {
                        ValidationMatcherUtils.resolveValidationMatcher(headerName, actualValue, 
                                expectedValue, context);
                        continue;
                    }
                    
                    Assert.isTrue(expectedValue != null,
                            "Values not equal for header element '"
                                + headerName + "', expected '"
                                + null + "' but was '"
                                + actualValue + "'");

                    Assert.isTrue(actualValue.equals(expectedValue),
                            "Values not equal for header element '"
                                + headerName + "', expected '"
                                + expectedValue + "' but was '"
                                + actualValue + "'");
                } else {
                    Assert.isTrue(expectedValue == null || expectedValue.length() == 0,
                            "Values not equal for header element '"
                                + headerName + "', expected '"
                                + expectedValue + "' but was '"
                                + null + "'");
                }
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Validation failed:", e);
            }

            if (log.isDebugEnabled()) {
                log.debug("Validating header element: " + headerName + "='" + expectedValue + "': OK.");
            }
        }

        log.info("Validation of message headers finished successfully: All properties OK");
    }

    @Override
    public T findValidationContext(List<ValidationContext> validationContexts) {
        for (ValidationContext validationContext : validationContexts) {
            if (validationContext instanceof ControlMessageValidationContext) {
                return (T) validationContext;
            }
        }
        
        return null;
    }
    
    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return true;
    }
}
