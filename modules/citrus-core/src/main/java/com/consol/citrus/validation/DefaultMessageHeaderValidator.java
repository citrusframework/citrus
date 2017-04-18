/*
 * Copyright 2006-2017 the original author or authors.
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
import org.hamcrest.Matcher;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * Basic header message validator provides message header validation. Subclasses only have to add
 * specific logic for message payload validation. This validator is based on a control message.
 * 
 * @author Christoph Deppisch
 */
public class DefaultMessageHeaderValidator extends AbstractMessageValidator<ValidationContext> {

    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage, TestContext context, ValidationContext validationContext) {
        Map<String, Object> controlHeaders = controlMessage.getHeaders();
        Map<String, Object> receivedHeaders = receivedMessage.getHeaders();

        if (CollectionUtils.isEmpty(controlHeaders)) { return; }

        log.debug("Start message header validation ...");

        for (Map.Entry<String, Object> entry : controlHeaders.entrySet()) {
            String headerName = entry.getKey();
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

            if (entry.getValue() instanceof Matcher) {
                Assert.isTrue(((Matcher) entry.getValue()).matches(actualValue),
                        ValidationUtils.buildValueMismatchErrorMessage(
                                "Values not matching for header '" + headerName + "'", entry.getValue(), actualValue));
                continue;
            }

            //check if value expression is variable or function
            String expectedValue = context.replaceDynamicContentInString(String.valueOf(entry.getValue()));

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

        log.info("Message header validation successful: All values OK");
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return true;
    }

    @Override
    protected Class<ValidationContext> getRequiredValidationContextType() {
        return ValidationContext.class;
    }
}
