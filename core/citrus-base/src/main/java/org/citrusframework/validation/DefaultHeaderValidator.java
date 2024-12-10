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

package org.citrusframework.validation;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.matcher.ValidationMatcherUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @since 2.7.6
 */
public class DefaultHeaderValidator implements HeaderValidator {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(DefaultHeaderValidator.class);

    /** Set of default header validators located via resource path lookup */
    private static final Map<String, HeaderValidator> DEFAULT_VALIDATORS = HeaderValidator.lookup();

    @Override
    public void validateHeader(String headerName, Object receivedValue, Object controlValue, TestContext context, HeaderValidationContext validationContext) {
        Optional<HeaderValidator> validator = getHeaderValidator(headerName, controlValue, context);
        if (validator.isPresent()) {
            validator.get().validateHeader(headerName, receivedValue, controlValue, context, validationContext);
            return;
        }

        String expectedValue = Optional.ofNullable(controlValue)
                .map(value -> context.getTypeConverter().convertIfNecessary(value, String.class))
                .map(context::replaceDynamicContentInString)
                .orElse("");

        if (receivedValue != null) {
            String receivedValueString = context.getTypeConverter().convertIfNecessary(receivedValue, String.class);
            if (ValidationMatcherUtils.isValidationMatcherExpression(expectedValue)) {
                ValidationMatcherUtils.resolveValidationMatcher(headerName, receivedValueString,
                        expectedValue, context);
                return;
            }

            if (!receivedValueString.equals(expectedValue)) {
                throw new ValidationException("Values not equal for header element '"
                        + headerName + "', expected '"
                        + expectedValue + "' but was '"
                        + receivedValue + "'");
            }
        } else if (StringUtils.hasText(expectedValue)) {
                throw new ValidationException("Values not equal for header element '"
                        + headerName + "', expected '"
                        + expectedValue + "' but was '"
                        + null + "'");
        }

        logger.debug("Validating header element: {}='{}' : OK", headerName, expectedValue);
    }

    public void validateHeaderArray(String headerName, Object receivedValue, Object controlValue, TestContext context, HeaderValidationContext validationContext) {
        Optional<HeaderValidator> validator = getHeaderValidator(headerName, controlValue, context);
        if (validator.isPresent()) {
            validator.get().validateHeader(headerName, receivedValue, controlValue, context, validationContext);
            return;
        }

        List<String> receivedValues = toList(receivedValue);
        List<String> controlValues = toList(controlValue);

        // Convert and replace dynamic content for controlValue
        List<String> expectedValues = controlValues.stream()
                .map(value -> context.getTypeConverter().convertIfNecessary(value, String.class))
                .map(context::replaceDynamicContentInString)
                .toList();

        // Process received values
        if (receivedValue != null) {
            List<String> receivedValueStrings = receivedValues.stream()
                .map(value -> context.getTypeConverter().convertIfNecessary(value, String.class))
                .toList();

            List<String> expectedValuesCopy = new ArrayList<>(expectedValues);

            // Iterate over received values and try to match with expected values
            for (String receivedValueString : receivedValueStrings) {

                Iterator<String> expectedIterator = expectedValuesCopy.iterator();
                boolean validated = validateExpected(headerName, context, receivedValueString, expectedIterator);

                if (!validated) {
                    throw new ValidationException(String.format("Values not equal for header element '%s', expected '%s' but was '%s'",
                        headerName, String.join(", ", expectedValues), receivedValueString));
                }
            }

            if (!expectedValuesCopy.isEmpty()) {
                throw new ValidationException(String.format("Values not equal for header element '%s', expected '%s' but was '%s'",
                    headerName, String.join(", ", expectedValues), String.join(", ", receivedValues)));
            }
        } else if (!expectedValues.isEmpty()) {
            throw new ValidationException(String.format("Values not equal for header element '%s', expected '%s' but was 'null'",
                headerName, String.join(", ", expectedValues)));
        }

        logger.debug("Validating header element: {}='{}' : OK", headerName, String.join(", ", expectedValues));
    }

    private static boolean validateExpected(String headerName, TestContext context,
        String receivedValueString, Iterator<String> expectedIterator) {
        boolean validated = false;
        while (expectedIterator.hasNext()) {
            String expectedValue = expectedIterator.next();

            if (ValidationMatcherUtils.isValidationMatcherExpression(expectedValue)) {
                try {
                    ValidationMatcherUtils.resolveValidationMatcher(headerName, receivedValueString, expectedValue,
                        context);
                    validated = true;
                    expectedIterator.remove();  // Remove matched value
                    break;
                } catch (ValidationException e) {
                    // Ignore this exception and try other expected values
                }
            } else {
                if (receivedValueString.equals(expectedValue)) {
                    validated = true;
                    expectedIterator.remove();  // Remove matched value
                    break;
                }
            }
        }

        return validated;
    }

    private static List<String> toList(Object value) {
        List<String> receivedValuesList;
        if (value == null) {
            receivedValuesList = Collections.emptyList();
        } else  if (!(value instanceof  List)) {
            receivedValuesList = new ArrayList<>();
            receivedValuesList.add(value.toString());
        } else {
            //noinspection unchecked
            receivedValuesList = (List<String>) value;
        }

        return receivedValuesList;
    }

    @Override
    public boolean supports(String headerName, Class<?> type) {
        return type == null || type.isInstance(String.class) || type.isPrimitive();
    }

    /**
     * Combines header validators from multiple sources. Includes validators coming from reference resolver
     * and resource path lookup are added.
     *
     * <p>Then pick validator that explicitly supports the given header name or control value and return as optional.
     */
    private static Optional<HeaderValidator> getHeaderValidator(String headerName, Object controlValue, TestContext context) {
        // add validators from resource path lookup
        Map<String, HeaderValidator> allValidators = new HashMap<>(DEFAULT_VALIDATORS);

        // add validators in reference resolver
        allValidators.putAll(context.getReferenceResolver().resolveAll(HeaderValidator.class));

        return allValidators.values().stream()
                .filter(validator -> !(validator instanceof DefaultHeaderValidator))
                .filter(validator -> validator.supports(headerName, Optional.ofNullable(controlValue).map(Object::getClass).orElse(null)))
                .findFirst();
    }
}
