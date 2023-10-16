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

package org.citrusframework.validation.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.functions.FunctionUtils;
import org.citrusframework.variable.VariableUtils;

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
     * This method resolves a custom validationMatcher to its respective result.
     * @param fieldName the name of the field
     * @param fieldValue the value of the field
     * @param validationMatcherExpression to evaluate.
     * @param context the test context
     */
    public static void resolveValidationMatcher(String fieldName, String fieldValue,
            String validationMatcherExpression, TestContext context) {
        String expression = VariableUtils.cutOffVariablesPrefix(cutOffValidationMatchersPrefix(validationMatcherExpression));

        if (expression.equals("ignore")) {
            expression += "()";
        }

        int bodyStart = expression.indexOf('(');
        if (bodyStart < 0) {
            throw new CitrusRuntimeException("Illegal syntax for validation matcher expression - missing validation value in '()' function body");
        }

        String prefix = "";
        if (expression.indexOf(':') > 0 && expression.indexOf(':') < bodyStart) {
            prefix = expression.substring(0, expression.indexOf(':') + 1);
        }

        String matcherValue = expression.substring(bodyStart + 1, expression.length() - 1);
        String matcherName = expression.substring(prefix.length(), bodyStart);

        ValidationMatcherLibrary library = context.getValidationMatcherRegistry().getLibraryForPrefix(prefix);
        ValidationMatcher validationMatcher = library.getValidationMatcher(matcherName);

        ControlExpressionParser controlExpressionParser = lookupControlExpressionParser(validationMatcher);
        List<String> params = controlExpressionParser.extractControlValues(matcherValue, null);
        List<String> replacedParams = replaceVariablesAndFunctionsInParameters(params, context);
        validationMatcher.validate(fieldName, fieldValue, replacedParams, context);
    }

    private static List<String> replaceVariablesAndFunctionsInParameters(List<String> params, TestContext context) {
        List<String> replacedParams = new ArrayList<>(params.size());
        for (String param : params) {
            String parsedVariablesParam = VariableUtils.replaceVariablesInString(param, context, false);
            String parsedFunctionsParam = FunctionUtils.replaceFunctionsInString(parsedVariablesParam, context);
            replacedParams.add(parsedFunctionsParam);
        }
        return replacedParams;
    }

    /**
     * Checks if expression is a validation matcher expression.
     * @param expression the expression to check
     * @return
     */
    public static boolean isValidationMatcherExpression(String expression) {
        return expression.startsWith(CitrusSettings.VALIDATION_MATCHER_PREFIX) &&
                expression.endsWith(CitrusSettings.VALIDATION_MATCHER_SUFFIX);
    }

    /**
     * Cut off validation matchers prefix and suffix.
     * @param expression
     * @return
     */
    private static String cutOffValidationMatchersPrefix(String expression) {
        if (expression.startsWith(CitrusSettings.VALIDATION_MATCHER_PREFIX) && expression.endsWith(CitrusSettings.VALIDATION_MATCHER_SUFFIX)) {
            return expression.substring(CitrusSettings.VALIDATION_MATCHER_PREFIX.length(), expression.length() - CitrusSettings.VALIDATION_MATCHER_SUFFIX.length());
        }

        return expression;
    }

    private static ControlExpressionParser lookupControlExpressionParser(ValidationMatcher validationMatcher) {
        if (validationMatcher instanceof ControlExpressionParser) {
            return (ControlExpressionParser) validationMatcher;
        }
        return new DefaultControlExpressionParser();
    }

    public static String getParameterListAsString(List<String> parameters) {
        return parameters.stream().collect(Collectors.joining(",", "'", "'"));
    }
}
