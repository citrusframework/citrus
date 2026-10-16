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

package org.citrusframework.selenium.actions;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.matcher.ValidationMatcherUtils;

/**
 * Shared assertion logic for Selenium element values with validation matcher support.
 *
 * @since 5.1
 */
final class ValueAssertions {

    private ValueAssertions() {
        // prevent instantiation
    }

    /**
     * Validates the given value against the expected value. Returns silently in case the expected
     * value is empty. Expected values may be validation matcher expressions.
     * @param label
     * @param expectedValue
     * @param actualValue
     * @param context
     */
    static void assertValue(String label, String expectedValue, String actualValue, TestContext context) {
        if (!StringUtils.hasText(expectedValue)) {
            return;
        }

        String control = context.replaceDynamicContentInString(expectedValue);

        if (ValidationMatcherUtils.isValidationMatcherExpression(control)) {
            ValidationMatcherUtils.resolveValidationMatcher("payload", actualValue, control, context);
        } else if (!control.equals(actualValue)) {
            throw new ValidationException(String.format("Selenium validation failed, %s expected '%s', but was '%s'", label, control, actualValue));
        }
    }
}
