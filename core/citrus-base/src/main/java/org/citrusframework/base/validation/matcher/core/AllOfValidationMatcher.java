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

package org.citrusframework.base.validation.matcher.core;

import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.validation.matcher.CombinatorExpressionParser;
import org.citrusframework.validation.matcher.ControlExpressionParser;
import org.citrusframework.validation.matcher.ValidationMatcher;
import org.citrusframework.validation.matcher.ValidationMatcherUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Conjunction combinator matcher. Requires ALL sub-matcher expressions to pass. All sub-matchers
 * are evaluated even when some fail, and every failure is included in the reported error.
 *
 * <p>Usage: {@code @allOf(startsWith('hello'), endsWith('world'))@}</p>
 */
public class AllOfValidationMatcher implements ValidationMatcher, ControlExpressionParser {

    @Override
    public List<String> extractControlValues(String controlExpression, Character delimiter) {
        return CombinatorExpressionParser.splitSubExpressions(controlExpression);
    }

    @Override
    public void validate(String fieldName, String value, List<String> controlParameters, TestContext context)
            throws ValidationException {
        if (controlParameters == null || controlParameters.size() < 2) {
            throw new CitrusRuntimeException(
                    "allOf() requires at least 2 sub-matcher expressions but got: "
                    + (controlParameters == null ? 0 : controlParameters.size()));
        }

        List<String> failures = new ArrayList<>();
        for (String subExpr : controlParameters) {
            try {
                ValidationMatcherUtils.resolveValidationMatcher(fieldName, value,
                        CitrusSettings.VALIDATION_MATCHER_PREFIX + subExpr + CitrusSettings.VALIDATION_MATCHER_SUFFIX,
                        context);
            } catch (ValidationException e) {
                failures.add("'" + subExpr + "' -> " + e.getMessage());
            }
        }

        if (!failures.isEmpty()) {
            throw new ValidationException(
                    "allOf() failed for field '" + fieldName + "' with value '" + value + "': ["
                    + String.join(", ", failures) + "]");
        }
    }
}
