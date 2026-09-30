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

import java.util.List;

import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.validation.matcher.CombinatorExpressionParser;
import org.citrusframework.validation.matcher.ControlExpressionParser;
import org.citrusframework.validation.matcher.ValidationMatcher;
import org.citrusframework.validation.matcher.ValidationMatcherUtils;

/**
 * Negation combinator matcher. Accepts exactly one sub-matcher expression and passes when that
 * sub-matcher fails, and fails when that sub-matcher passes.
 *
 * <p>Usage: {@code @not(contains('error'))@}</p>
 */
public class NotValidationMatcher implements ValidationMatcher, ControlExpressionParser {

    @Override
    public List<String> extractControlValues(String controlExpression, Character delimiter) {
        return CombinatorExpressionParser.splitSubExpressions(controlExpression);
    }

    @Override
    public void validate(String fieldName, String value, List<String> controlParameters, TestContext context)
            throws ValidationException {
        if (controlParameters == null || controlParameters.size() != 1) {
            throw new CitrusRuntimeException(
                    "not() requires exactly one sub-matcher expression but got: "
                    + (controlParameters == null ? 0 : controlParameters.size()));
        }

        String subExpr = controlParameters.get(0);
        try {
            ValidationMatcherUtils.resolveValidationMatcher(fieldName, value,
                    CitrusSettings.VALIDATION_MATCHER_PREFIX + subExpr + CitrusSettings.VALIDATION_MATCHER_SUFFIX,
                    context);
        } catch (ValidationException e) {
            // inner matcher failed — not() succeeds
            return;
        }

        // inner matcher passed — not() fails
        throw new ValidationException(
                "not() failed for field '" + fieldName + "': '" + subExpr
                + "' matched value '" + value + "' but was expected not to");
    }
}
