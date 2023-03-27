/*
 * Copyright 2006-2012 the original author or authors.
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

package org.citrusframework.citrus.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.citrusframework.citrus.exceptions.ValidationException;
import org.citrusframework.citrus.validation.matcher.ValidationMatcherUtils;
import org.apache.commons.codec.binary.Base64;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Utility class provides helper methods for validation work in Citrus.
 *
 * @author Christoph Deppisch
 * @since 1.3
 */
public abstract class ValidationUtils {

    /** Set of default value matchers located via resource path lookup */
    private static final Map<String, ValueMatcher> DEFAULT_VALUE_MATCHERS = ValueMatcher.lookup();

    /**
     * Prevent instantiation.
     */
    private ValidationUtils() {
        super();
    }

    /**
     * Validates actual against expected value of element
     * @param actualValue
     * @param expectedValue
     * @param pathExpression
     * @param context
     * @throws org.citrusframework.citrus.exceptions.ValidationException if validation fails
     */
    public static void validateValues(Object actualValue, Object expectedValue, String pathExpression, TestContext context)
            throws ValidationException {
        try {
            if (actualValue != null) {
                Assert.isTrue(expectedValue != null,
                        ValidationUtils.buildValueMismatchErrorMessage(
                                "Values not equal for element '" + pathExpression + "'", null, actualValue));

                Optional<ValueMatcher> matcher = getValueMatcher(expectedValue, context);
                if (matcher.isPresent()) {
                    Assert.isTrue(matcher.get().validate(actualValue, expectedValue, context),
                            ValidationUtils.buildValueMismatchErrorMessage(
                                    "Values not matching for element '" + pathExpression + "'", expectedValue, actualValue));
                    return;
                }

                if (!(expectedValue instanceof String)) {
                    Object converted = context.getTypeConverter().convertIfNecessary(actualValue, expectedValue.getClass());

                    if (converted == null) {
                        throw new CitrusRuntimeException(String.format("Failed to convert value '%s' to required type '%s'", actualValue, expectedValue.getClass()));
                    }

                    if (converted instanceof List) {
                        Assert.isTrue(converted.toString().equals(expectedValue.toString()),
                                ValidationUtils.buildValueMismatchErrorMessage(
                                        "Values not equal for element '" + pathExpression + "'", expectedValue.toString(), converted.toString()));
                    } else if (converted instanceof String[]) {
                        String convertedDelimitedString = StringUtils.arrayToCommaDelimitedString((String[]) converted);
                        String expectedDelimitedString = StringUtils.arrayToCommaDelimitedString((String[]) expectedValue);

                        Assert.isTrue(convertedDelimitedString.equals(expectedDelimitedString),
                                ValidationUtils.buildValueMismatchErrorMessage(
                                        "Values not equal for element '" + pathExpression + "'", expectedDelimitedString, convertedDelimitedString));
                    } else if (converted instanceof byte[]) {
                        String convertedBase64 = Base64.encodeBase64String((byte[]) converted);
                        String expectedBase64 = Base64.encodeBase64String((byte[]) expectedValue);

                        Assert.isTrue(convertedBase64.equals(expectedBase64),
                                ValidationUtils.buildValueMismatchErrorMessage(
                                        "Values not equal for element '" + pathExpression + "'", expectedBase64, convertedBase64));
                    } else {
                        Assert.isTrue(converted.equals(expectedValue),
                                ValidationUtils.buildValueMismatchErrorMessage(
                                        "Values not equal for element '" + pathExpression + "'", expectedValue, converted));
                    }
                } else {
                    String expectedValueString = expectedValue.toString();
                    String actualValueString;
                    if (List.class.isAssignableFrom(actualValue.getClass())) {
                        actualValueString = StringUtils.arrayToCommaDelimitedString(((List)actualValue).toArray(new Object[((List)actualValue).size()]));
                        expectedValueString = expectedValueString.replaceAll("^\\[", "").replaceAll("\\]$", "").replaceAll(",\\s", ",");
                    } else {
                        actualValueString = actualValue.toString();
                    }

                    if (ValidationMatcherUtils.isValidationMatcherExpression(String.valueOf(expectedValueString))) {
                        ValidationMatcherUtils.resolveValidationMatcher(pathExpression,
                                actualValueString,
                                String.valueOf(expectedValueString),
                                context);
                    } else {
                        Assert.isTrue(actualValueString.equals(expectedValueString),
                                ValidationUtils.buildValueMismatchErrorMessage(
                                        "Values not equal for element '" + pathExpression + "'", expectedValueString, actualValueString));
                    }
                }
            } else if (expectedValue != null) {
                Optional<ValueMatcher> matcher = getValueMatcher(expectedValue, context);
                if (matcher.isPresent()) {
                    Assert.isTrue(matcher.get().validate(actualValue, expectedValue, context),
                            ValidationUtils.buildValueMismatchErrorMessage(
                                    "Values not matching for element '" + pathExpression + "'", expectedValue, null));
                } else if (expectedValue instanceof String) {
                    String expectedValueString = expectedValue.toString();

                    if (ValidationMatcherUtils.isValidationMatcherExpression(expectedValueString)) {
                        ValidationMatcherUtils.resolveValidationMatcher(pathExpression,
                                null,
                                expectedValueString,
                                context);
                    } else {
                        Assert.isTrue(!StringUtils.hasText(expectedValueString),
                                ValidationUtils.buildValueMismatchErrorMessage(
                                        "Values not equal for element '" + pathExpression + "'", expectedValueString, null));
                    }
                } else {
                    throw new ValidationException("Validation failed: " + ValidationUtils.buildValueMismatchErrorMessage(
                            "Values not equal for element '" + pathExpression + "'", expectedValue, null));
                }
            }
        } catch (IllegalArgumentException | AssertionError e) {
            throw new ValidationException("Validation failed:", e);
        }
    }

    /**
     * Combines value matchers from multiple sources. Includes validators coming from reference resolver
     * and resource path lookup are added.
     *
     * Then pick matcher that explicitly supports the given expected value type.
     * @param expectedValue
     * @param context
     * @return
     */
    private static Optional<ValueMatcher> getValueMatcher(Object expectedValue, TestContext context) {
        // add validators from resource path lookup
        Map<String, ValueMatcher> allMatchers = new HashMap<>(DEFAULT_VALUE_MATCHERS);

        // add validators in reference resolver
        allMatchers.putAll(context.getReferenceResolver().resolveAll(ValueMatcher.class));

        return allMatchers.values().stream()
                .filter(matcher -> matcher.supports(expectedValue.getClass()))
                .findFirst();
    }

    /**
     * Constructs proper error message with expected value and actual value.
     * @param baseMessage the base error message.
     * @param controlValue the expected value.
     * @param actualValue the actual value.
     * @return
     */
    public static String buildValueMismatchErrorMessage(String baseMessage, Object controlValue, Object actualValue) {
        return baseMessage + ", expected '" + controlValue + "' but was '" + actualValue + "'";
    }
}
