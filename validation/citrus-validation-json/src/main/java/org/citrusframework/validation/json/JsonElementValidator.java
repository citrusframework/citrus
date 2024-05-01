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

package org.citrusframework.validation.json;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;

import java.util.Collection;
import java.util.Objects;

import static java.util.Objects.requireNonNullElse;
import static org.citrusframework.CitrusSettings.IGNORE_PLACEHOLDER;
import static org.citrusframework.validation.ValidationUtils.buildValueMismatchErrorMessage;
import static org.citrusframework.validation.ValidationUtils.buildValueToBeInCollectionErrorMessage;
import static org.citrusframework.validation.matcher.ValidationMatcherUtils.isValidationMatcherExpression;
import static org.citrusframework.validation.matcher.ValidationMatcherUtils.resolveValidationMatcher;

public class JsonElementValidator {
    private final boolean strict;
    private final TestContext context;
    private final Collection<String> ignoreExpressions;

    public JsonElementValidator(
            boolean strict,
            TestContext context,
            Collection<String> ignoreExpressions
    ) {
        this.strict = strict;
        this.context = context;
        this.ignoreExpressions = ignoreExpressions;
    }

    public void validate(JsonElementValidatorItem<?> control) {
        if (isIgnoredByPlaceholderOrExpressionList(ignoreExpressions, control))
            return;
        if (isValidationMatcherExpression(requireNonNullElse(control.expectedAsStringOrNull(), ""))) {
            resolveValidationMatcher(control.getJsonPath(), control.actualAsStringOrNull(), control.expectedAsStringOrNull(), context);
        } else if (control.expected instanceof JSONObject) {
            validateJSONObject(this, control);
        } else if (control.expected instanceof JSONArray) {
            validateJSONArray(this, control);
        } else {
            validateNativeType(control);
        }
    }

    private void validateJSONObject(JsonElementValidator validator, JsonElementValidatorItem<?> control) {
        var objectControl = control.ensureType(JSONObject.class);

        if (strict) {
            validateSameSize(objectControl.getJsonPath(), objectControl.expected.keySet(), objectControl.actual.keySet());
        }

        var controlEntries = objectControl.expected
                .entrySet()
                .stream()
                .map(entry -> new JsonElementValidatorItem<>(
                        entry.getKey(), objectControl.actual.get(entry.getKey()), entry.getValue()
                ).parent(objectControl))
                .toList();

        for (var entryControl : controlEntries) {
            if (!objectControl.actual.containsKey(entryControl.getName())) {
                throw new ValidationException(buildValueToBeInCollectionErrorMessage(
                        "Missing JSON entry", entryControl.getName(), objectControl.actual.keySet()
                ));
            }

            validator.validate(entryControl);
        }
    }

    /**
     * Checks if given element node is either on ignore list or
     * contains @ignore@ tag inside control message
     */
    static boolean isIgnoredByPlaceholderOrExpressionList(Collection<String> ignoreExpressions, JsonElementValidatorItem<?> controlEntry) {
        String trimmedControlValue = requireNonNullElse(controlEntry.expectedAsStringOrNull(), "").trim();
        if (trimmedControlValue.equals(IGNORE_PLACEHOLDER)) {
            return true;
        }

        return ignoreExpressions.stream().anyMatch(controlEntry::isPathIgnoredBy);
    }

    private void validateJSONArray(JsonElementValidator validator, JsonElementValidatorItem<?> control) {
        var arrayControl = control.ensureType(JSONArray.class);
        if (strict) {
            validateSameSize(control.getJsonPath(), arrayControl.expected, arrayControl.actual);
        }
        int actualIndex = 0;
        for (int i = 0; i < arrayControl.expected.size(); i++) {
            if (isIgnoredByPlaceholderOrExpressionList(ignoreExpressions, arrayControl.child(i, i))) {
                continue;
            }
            boolean isValid = false;

            while (!isValid && actualIndex < arrayControl.actual.size()) {
                JsonElementValidatorItem<Object> item = arrayControl.child(i, arrayControl.actual.get(actualIndex));
                isValid = isValidItem(item, validator);
                actualIndex++;
            }

            if (!isValid) {
                throw new ValidationException(buildValueToBeInCollectionErrorMessage(
                        "An item in '%s' is missing".formatted(arrayControl.getJsonPath()),
                        arrayControl.expected.get(i),
                        arrayControl.actual
                ));
            }
        }
    }

    private static boolean isValidItem(JsonElementValidatorItem<Object> validatorItem, JsonElementValidator validator) {
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

    private static void validateNativeType(JsonElementValidatorItem<?> control) {
        if (!Objects.equals(control.expected, control.actual)) {
            throwValueMismatch("Values not equal for entry: '" + control.getJsonPath() + "'", control.expected, control.actual);
        }
    }

    private static void throwValueMismatch(String baseMessage, Object expectedValue, Object actualValue) {
        throw new ValidationException(buildValueMismatchErrorMessage(baseMessage, expectedValue, actualValue));
    }

    @FunctionalInterface
    public interface Provider {
        JsonElementValidator getValidator(boolean isStrict, TestContext context, JsonMessageValidationContext validationContext);

        Provider DEFAULT = (
                boolean isStrict,
                TestContext context,
                JsonMessageValidationContext validationContext
        ) -> new JsonElementValidator(isStrict, context, validationContext.getIgnoreExpressions());
    }
}
