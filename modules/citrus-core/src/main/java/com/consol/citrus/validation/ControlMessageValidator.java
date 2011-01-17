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

import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageHeaders;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.functions.FunctionUtils;
import com.consol.citrus.util.MessageUtils;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.context.ValidationContextBuilder;
import com.consol.citrus.variable.VariableUtils;

/**
 * Basic control message validator provides message header validation. Subclasses only have to add
 * specific logic for message payload validation. This validator is based on a control message.
 * 
 * @author Christoph Deppisch
 */
public class ControlMessageValidator extends AbstractMessageValidator<ControlMessageValidationContext> {

    /**
     * Logger
     */
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * Implementation performs message header validation as well as message
     * payload validation.
     */
    public void validateMessage(Message<?> receivedMessage, TestContext context,
            ControlMessageValidationContext validationContext) {
        // validate message payload first
        validateMessagePayload(receivedMessage, validationContext.getControlMessage(), context);
        
        // validate message headers
        validateMessageHeader(validationContext.getControlMessage().getHeaders(), 
                receivedMessage.getHeaders(), 
                context);
    }
    
    /**
     * Validates the message payload with comparison to the control message payload 
     * located in validation context.
     * 
     * @param receivedMessage the received message to check.
     * @param validationContext the current validation context holding the expected control message.
     * @param context the current test context with all variables.
     */
    public void validateMessagePayload(Message<?> receivedMessage, 
            Message<?> controlMessage, TestContext context) throws ValidationException {
    }
    
    
    /**
     * Validates the message header comparing its values to a control header set.
     *
     * @param controlHeaders the expected control headers.
     * @param receivedHeaders the actual headers from message received.
     * @param context the current test context.
     */
    public void validateMessageHeader(MessageHeaders controlHeaders, MessageHeaders receivedHeaders, TestContext context) {
        if (CollectionUtils.isEmpty(controlHeaders)) { return; }

        log.info("Start message header validation");

        for (Entry<String, Object> entry : controlHeaders.entrySet()) {
            String headerName = entry.getKey();
            String expectedValue = entry.getValue().toString();
            String actualValue = null;

            if(MessageUtils.isSpringInternalHeader(headerName)) {
                continue;
            }

            if (VariableUtils.isVariableName(headerName)) {
                headerName = context.getVariable(headerName);
            } else if(context.getFunctionRegistry().isFunction(headerName)) {
                headerName = FunctionUtils.resolveFunction(headerName, context);
            }

            if (!receivedHeaders.containsKey(headerName)) {
                throw new ValidationException("Validation failed: Header element '" + headerName + "' is missing");
            }
            
            if(receivedHeaders.get(headerName) != null) {
                actualValue = receivedHeaders.get(headerName).toString();
            }

            if (VariableUtils.isVariableName(expectedValue)) {
                expectedValue = context.getVariable(expectedValue);
            } else if(context.getFunctionRegistry().isFunction(expectedValue)) {
                expectedValue = FunctionUtils.resolveFunction(expectedValue, context);
            }

            try {
                if(actualValue != null) {
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

            if(log.isDebugEnabled()) {
                log.debug("Validating header element: " + headerName + "='" + expectedValue + "': OK.");
            }
        }

        log.info("Validation of message headers finished successfully: All properties OK");
    }
    
    /**
     * Construct a proper validation context for this validator. Method uses the
     * available context builder implementations searching for an accountable builder supporting
     * {@link ControlMessageValidationContext}. 
     */
    public ControlMessageValidationContext createValidationContext(
            List<ValidationContextBuilder<? extends ValidationContext>> builders,
            TestContext context) {
        for (ValidationContextBuilder<? extends ValidationContext> validationContextBuilder : builders) {
            if (validationContextBuilder.supportsValidationContextType(ControlMessageValidationContext.class)) {
                return (ControlMessageValidationContext) validationContextBuilder.buildValidationContext(context);
            }
        }
        
        return new ControlMessageValidationContext();
    }
    
    /**
     * Checks if the message type is supported.
     */
    public boolean supportsMessageType(String messageType) {
        return true;
    }
}
