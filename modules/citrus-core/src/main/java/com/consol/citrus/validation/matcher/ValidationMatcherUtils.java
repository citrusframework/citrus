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
     * @param validationMatcherString to evaluate.
     * @param fieldName the name of the field
     * @throws ValidationException
     * @return evaluated control value
     */
    public static void resolveValidationMatcher(String fieldName, String value, String validationMatcherString, TestContext context) {

        if (validationMatcherString.startsWith(CitrusConstants.VALIDATION_MATCHER_PREFIX)) {
            validationMatcherString = validationMatcherString.substring(CitrusConstants.VALIDATION_MATCHER_PREFIX.length());
        }
        if (validationMatcherString.endsWith(CitrusConstants.VALIDATION_MATCHER_SUFFIX)) {
            validationMatcherString = validationMatcherString.substring(0, validationMatcherString.length() - CitrusConstants.VALIDATION_MATCHER_SUFFIX.length());
        }
        
        String validationMatcherPrefix = validationMatcherString.substring(0, validationMatcherString.indexOf(':') + 1);
        String validationMatcherExpression = cutOffValidationMatchersPrefix(validationMatcherString);
        String controlString = validationMatcherExpression.substring(validationMatcherExpression.indexOf('(') + 1, validationMatcherExpression.length() - 1);
        String validationMatcher = validationMatcherExpression.substring(validationMatcherExpression.indexOf(':') + 1, validationMatcherExpression.indexOf('('));

        ValidationMatcherLibrary library = context.getValidationMatcherRegistry().getLibraryForPrefix(validationMatcherPrefix);

        controlString = VariableUtils.replaceVariablesInString(controlString, context, false);
        controlString = FunctionUtils.replaceFunctionsInString(controlString, context);
        if (controlString.contains("\'")) {
            controlString = controlString.substring(controlString.indexOf("\'") + 1, controlString.lastIndexOf("\'"));
        }

        library.getValidationMatcher(validationMatcher).validate(fieldName, value, controlString);
    }
    
    
    /**
     * Cut off validation matchers prefix
     * @param validationMatcherString
     * @return
     */
    public static String cutOffValidationMatchersPrefix(String validationMatcherString) {
        if (validationMatcherString.indexOf(CitrusConstants.VARIABLE_PREFIX) == 0 && validationMatcherString.charAt(validationMatcherString.length()-1) == CitrusConstants.VARIABLE_SUFFIX) {
            return validationMatcherString.substring(CitrusConstants.VARIABLE_PREFIX.length(), validationMatcherString.length()-1);
        }

        return validationMatcherString;
    }
}
