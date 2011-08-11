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

package com.consol.citrus.validation.matcher;

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.functions.FunctionUtils;
import com.consol.citrus.variable.VariableUtils;

/**
 * Utility class for validation matchers.
 * 
 * @author Christian Wied
 */
public final class ValidationMatcherUtils {

    /**
     * Prevent class instantiation.
     */
    private ValidationMatcherUtils() {}
    
    /**
     * This method resolves a custom falidationMatcher to its respective result.
     * @param validationMatcherExpression to evaluate.
     * @param fieldName the name of the field
     * @throws ValidationException
     * @return evaluated control value
     */
    public static void resolveValidationMatcher(String fieldName, String fieldValue, 
            String validationMatcherExpression, TestContext context) {
        String expression = VariableUtils.cutOffVariablesPrefix(cutOffValidationMatchersPrefix(validationMatcherExpression));
        
        String prefix = expression.substring(0, expression.indexOf(':') + 1);
        String controlString = expression.substring(expression.indexOf('(') + 1, expression.length() - 1);
        String validationMatcher = expression.substring(expression.indexOf(':') + 1, expression.indexOf('('));

        ValidationMatcherLibrary library = context.getValidationMatcherRegistry().getLibraryForPrefix(prefix);

        controlString = VariableUtils.replaceVariablesInString(controlString, context, false);
        controlString = FunctionUtils.replaceFunctionsInString(controlString, context);
        if (controlString.contains("\'")) {
            controlString = controlString.substring(controlString.indexOf('\'') + 1, controlString.lastIndexOf('\''));
        }

        library.getValidationMatcher(validationMatcher).validate(fieldName, fieldValue, controlString);
    }
    
    
    /**
     * Cut off validation matchers prefix and suffix.
     * @param expression
     * @return
     */
    private static String cutOffValidationMatchersPrefix(String expression) {
        if (expression.startsWith(CitrusConstants.VALIDATION_MATCHER_PREFIX) && expression.endsWith(CitrusConstants.VALIDATION_MATCHER_SUFFIX)) {
            return expression.substring(CitrusConstants.VALIDATION_MATCHER_PREFIX.length(), expression.length() - CitrusConstants.VALIDATION_MATCHER_SUFFIX.length());
        }

        return expression;
    }
}
