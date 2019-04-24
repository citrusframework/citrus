/*
 * Copyright 2006-2019 the original author or authors.
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

import com.consol.citrus.Citrus;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.functions.FunctionUtils;
import com.consol.citrus.variable.VariableUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public static void resolveValidationMatcher(final String fieldName, final String fieldValue,
                                                final String validationMatcherExpression, final TestContext context) {
        String expression = VariableUtils.cutOffVariablesPrefix(cutOffValidationMatchersPrefix(validationMatcherExpression));

        if (expression.equals("ignore")) {
            expression += "()";
        }

        final int bodyStart = expression.indexOf('(');
        if (bodyStart < 0) {
            throw new CitrusRuntimeException("Illegal syntax for validation matcher expression - missing validation value in '()' function body");
        }
        
        String prefix = "";
        if (expression.indexOf(':') > 0 && expression.indexOf(':') < bodyStart) {
            prefix = expression.substring(0, expression.indexOf(':') + 1);
        }

        final String matcherValue = expression.substring(bodyStart + 1, expression.length() - 1);
        final String matcherName = expression.substring(prefix.length(), bodyStart);

        final ValidationMatcherLibrary library = context.getValidationMatcherRegistry().getLibraryForPrefix(prefix);
        final ValidationMatcher validationMatcher = library.getValidationMatcher(matcherName);

        final ControlExpressionParser controlExpressionParser = lookupControlExpressionParser(validationMatcher);
        final List<String> params = controlExpressionParser.extractControlValues(matcherValue, null);
        final List<String> replacedParams = replaceVariablesAndFunctionsInParameters(params, context);
        validationMatcher.validate(fieldName, fieldValue, replacedParams, context);
    }

    private static List<String> replaceVariablesAndFunctionsInParameters(final List<String> params, final TestContext context) {
        final List<String> replacedParams = new ArrayList<>(params.size());
        for (final String param : params) {
            final String parsedVariablesParam = VariableUtils.replaceVariablesInString(param, context, false);
            final String parsedFunctionsParam = FunctionUtils.replaceFunctionsInString(parsedVariablesParam, context);
            replacedParams.add(parsedFunctionsParam);
        }
        return replacedParams;
    }

    /**
     * Checks if expression is a validation matcher expression.
     * @param expression the expression to check
     * @return
     */
    public static boolean isValidationMatcherExpression(final String expression) {
        return expression.startsWith(Citrus.VALIDATION_MATCHER_PREFIX) &&
                expression.endsWith(Citrus.VALIDATION_MATCHER_SUFFIX);
    }
    
    /**
     * Cut off validation matchers prefix and suffix.
     * @param expression
     * @return
     */
    private static String cutOffValidationMatchersPrefix(final String expression) {
        if (expression.startsWith(Citrus.VALIDATION_MATCHER_PREFIX) && expression.endsWith(Citrus.VALIDATION_MATCHER_SUFFIX)) {
            return expression.substring(Citrus.VALIDATION_MATCHER_PREFIX.length(), expression.length() - Citrus.VALIDATION_MATCHER_SUFFIX.length());
        }

        return expression;
    }

    private static ControlExpressionParser lookupControlExpressionParser(final ValidationMatcher validationMatcher) {
        if (validationMatcher instanceof ControlExpressionParser) {
            return (ControlExpressionParser) validationMatcher;
        }
        return new DefaultControlExpressionParser();
    }

    public static String getParameterListAsString(final List<String> parameters) {
        return StringUtils.collectionToDelimitedString(parameters, ",", "'", "'");
    }

    /**
     * Substitutes ignore statements in the control value with the actual value from the received message at the
     * ignore statements position. This way we can ignore words in a plaintext value.
     * @param control The control message that potentially contains ignore statements
     * @param substitute The message to receive the content for the substitution from.
     *                   Mostly the message for validation, received from the endpoint.
     * @return The control message with substituted ignore statements
     */
    public static String substituteIgnoreStatements(final String control, final String substitute) {
        if (control.equals(Citrus.IGNORE_PLACEHOLDER)) {
            return control;
        }

        final Pattern separatorPattern = Pattern.compile("[\\W]");
        final Pattern ignorePattern = Pattern.compile("@ignore\\(?(\\d*)\\)?@");

        String processedMessage = control;
        Matcher ignoreMatcher = ignorePattern.matcher(processedMessage);

        while (ignoreMatcher.find()) {
            String actualValue;

            if (ignoreMatcher.groupCount() > 0 && StringUtils.hasText(ignoreMatcher.group(1))) {
                int end = ignoreMatcher.start() + Integer.valueOf(ignoreMatcher.group(1));
                if (end > substitute.length()) {
                    end = substitute.length();
                }

                if (ignoreMatcher.start() > substitute.length()) {
                    actualValue = "";
                } else {
                    actualValue = substitute.substring(ignoreMatcher.start(), end);
                }
            } else {
                actualValue = substitute.substring(ignoreMatcher.start());
                final Matcher whitespaceMatcher = separatorPattern.matcher(actualValue);
                if (whitespaceMatcher.find()) {
                    actualValue = actualValue.substring(0, whitespaceMatcher.start());
                }
            }

            processedMessage = ignoreMatcher.replaceFirst(actualValue);
            ignoreMatcher = ignorePattern.matcher(processedMessage);
        }

        return processedMessage;
    }
}
