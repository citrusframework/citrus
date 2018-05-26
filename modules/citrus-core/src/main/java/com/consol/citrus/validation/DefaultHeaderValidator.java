/*
 * Copyright 2006-2018 the original author or authors.
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
import com.consol.citrus.util.TypeConversionUtils;
import com.consol.citrus.validation.context.HeaderValidationContext;
import com.consol.citrus.validation.matcher.ValidationMatcherUtils;
import org.hamcrest.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public class DefaultHeaderValidator implements HeaderValidator {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(DefaultHeaderValidator.class);

    @Override
    public void validateHeader(String headerName, Object receivedValue, Object controlValue, TestContext context, HeaderValidationContext validationContext) {
        if (controlValue instanceof Matcher) {
            Assert.isTrue(((Matcher) controlValue).matches(receivedValue),
                    ValidationUtils.buildValueMismatchErrorMessage(
                            "Values not matching for header '" + headerName + "'", controlValue, receivedValue));
            return;
        }

        String expectedValue = Optional.ofNullable(controlValue)
                .map(value -> TypeConversionUtils.convertIfNecessary(value, String.class))
                .map(context::replaceDynamicContentInString)
                .orElse("");

        try {
            if (receivedValue != null) {
                String receivedValueString = TypeConversionUtils.convertIfNecessary(receivedValue, String.class);
                if (ValidationMatcherUtils.isValidationMatcherExpression(expectedValue)) {
                    ValidationMatcherUtils.resolveValidationMatcher(headerName, receivedValueString,
                            expectedValue, context);
                    return;
                }

                Assert.isTrue(receivedValueString.equals(expectedValue),
                        "Values not equal for header element '"
                                + headerName + "', expected '"
                                + expectedValue + "' but was '"
                                + receivedValue + "'");
            } else {
                Assert.isTrue(!StringUtils.hasText(expectedValue),
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

    @Override
    public boolean supports(String headerName, Class<?> type) {
        return true;
    }
}
