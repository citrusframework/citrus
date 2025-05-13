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

package org.citrusframework.validation.yaml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.validation.context.MessageValidationContext;
import org.yaml.snakeyaml.scanner.ScannerException;

import static java.util.Objects.requireNonNullElse;
import static org.citrusframework.CitrusSettings.IGNORE_PLACEHOLDER;
import static org.citrusframework.validation.ValidationUtils.buildValueMismatchErrorMessage;
import static org.citrusframework.validation.ValidationUtils.buildValueToBeInCollectionErrorMessage;
import static org.citrusframework.validation.matcher.ValidationMatcherUtils.isValidationMatcherExpression;
import static org.citrusframework.validation.matcher.ValidationMatcherUtils.resolveValidationMatcher;

public class YamlNodeValidator {
    private final boolean strict;
    private final TestContext context;
    private final Collection<String> ignoreExpressions;

    public YamlNodeValidator(boolean strict, TestContext context, Collection<String> ignoreExpressions) {
        this.strict = strict;
        this.context = context;
        this.ignoreExpressions = ignoreExpressions;
    }

    public void validate(YamlNodeValidatorItem<?> control) {
        if (isIgnoredByPlaceholderOrExpressionList(ignoreExpressions, control))
            return;
        if (isValidationMatcherExpression(requireNonNullElse(control.expectedAsStringOrNull(), ""))) {
            resolveValidationMatcher(control.getNodePath(), control.actualAsStringOrNull(), control.expectedAsStringOrNull(), context);
        } else if (control.expected instanceof Map<?, ?>) {
            validateObject(this, control);
        } else if (control.expected instanceof Iterable<?>) {
            validateArray(this, control);
        } else {
            validateNativeType(control);
        }
    }

    private void validateObject(YamlNodeValidator validator, YamlNodeValidatorItem<?> control) {
        var objectControl = control.ensureType(Map.class);

        if (strict) {
            validateSameSize(objectControl.getNodePath(), objectControl.expected.keySet(), objectControl.actual.keySet());
        }

        Map<String, Object> expected = objectControl.expected;
        Map<String, Object> actual = objectControl.actual;
        for (var mapEntry : expected.entrySet()) {
            if (!actual.containsKey(mapEntry.getKey())) {
                throw new ValidationException(buildValueToBeInCollectionErrorMessage(
                        "Missing property in YAML entry '%s'".formatted(objectControl.getNodePath()), mapEntry.getKey(), actual.keySet()));
            }

            validator.validate(new YamlNodeValidatorItem<>(mapEntry.getKey(),
                    actual.get(mapEntry.getKey()), mapEntry.getValue()).parent(objectControl));
        }
    }

    /**
     * Checks if given element node is either on ignore list or
     * contains @ignore@ tag inside control message
     */
    static boolean isIgnoredByPlaceholderOrExpressionList(Collection<String> ignoreExpressions, YamlNodeValidatorItem<?> controlEntry) {
        String trimmedControlValue = requireNonNullElse(controlEntry.expectedAsStringOrNull(), "").trim();
        if (trimmedControlValue.equals(IGNORE_PLACEHOLDER)) {
            return true;
        }

        return ignoreExpressions.stream().anyMatch(controlEntry::isPathIgnoredBy);
    }

    private void validateArray(YamlNodeValidator validator, YamlNodeValidatorItem<?> control) {
        var arrayControl = control.ensureType(Iterable.class);
        List<Object> controlItems = new ArrayList<>();
        List<Object> actualItems = new ArrayList<>();
        try {
            arrayControl.expected.iterator().forEachRemaining(controlItems::add);
            arrayControl.actual.iterator().forEachRemaining(actualItems::add);
        } catch (ScannerException e) {
            throw new ValidationException("Failed to read YAML source", e);
        }

        if (strict) {
            validateSameSize(control.getNodePath(), controlItems, actualItems);
        }

        List<Object> remaining = new ArrayList<>();
        arrayControl.actual.iterator().forEachRemaining(remaining::add);
        for (int i = 0; i < controlItems.size(); i++) {
            if (isIgnoredByPlaceholderOrExpressionList(ignoreExpressions,
                    arrayControl.child(i, i, controlItems.get(i)).parent(arrayControl))) {
                continue;
            }

            if (remaining.isEmpty()) {
                throwValueMismatch("Missing entries in array element: '" + control.getName() + "'",
                        controlItems.size(), actualItems.size());
            }

            boolean isValid = false;

            int actualIndex = 0;
            while (!isValid && actualIndex < remaining.size()) {
                YamlNodeValidatorItem<Object> item = arrayControl.child(i,
                        remaining.get(actualIndex), controlItems.get(i))
                        .parent(arrayControl);
                if (isValidItem(item, validator)) {
                    isValid = true;
                    remaining.remove(actualIndex);
                }
                actualIndex++;
            }

            if (!isValid) {
                throw new ValidationException(buildValueToBeInCollectionErrorMessage(
                        "An item in '%s' is missing".formatted(arrayControl.getNodePath()),
                        controlItems.get(i),
                        actualItems
                ));
            }
        }
    }

    private static boolean isValidItem(YamlNodeValidatorItem<Object> validatorItem, YamlNodeValidator validator) {
        try {
            validator.validate(validatorItem);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }

    private void validateSameSize(String path, Collection<?> expected, Collection<?> actual) {
        if (expected.size() != actual.size()) {
            throwValueMismatch("Number of entries is not equal in element: '" + path + "'", expected, actual);
        }
    }

    private static void validateNativeType(YamlNodeValidatorItem<?> control) {
        if (!Objects.equals(control.expected, control.actual)) {
            throwValueMismatch("Values not equal for entry: '" + control.getNodePath() + "'", control.expected, control.actual);
        }
    }

    private static void throwValueMismatch(String baseMessage, Object expectedValue, Object actualValue) {
        throw new ValidationException(buildValueMismatchErrorMessage(baseMessage, expectedValue, actualValue));
    }

    @FunctionalInterface
    public interface Provider {
        YamlNodeValidator getValidator(boolean strict, TestContext context, MessageValidationContext validationContext);

        Provider DEFAULT = (
                boolean strict,
                TestContext context,
                MessageValidationContext validationContext
        ) -> new YamlNodeValidator(strict, context, validationContext.getIgnoreExpressions());
    }
}
