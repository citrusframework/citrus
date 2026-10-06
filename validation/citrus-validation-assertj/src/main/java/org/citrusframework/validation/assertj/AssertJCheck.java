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

package org.citrusframework.validation.assertj;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ThrowingConsumer;
import org.citrusframework.context.TestContext;

/**
 * Expected value that checks a received value with AssertJ. Wraps either an assertion lambda
 * (optionally preceded by a type conversion) or an AssertJ {@link Condition}. A failed check
 * throws the {@link AssertionError} raised by the assertion, so the failure description is kept.
 */
public final class AssertJCheck {

    private final Class<?> type;
    private final ThrowingConsumer<Object> assertion;
    private final String description;

    private AssertJCheck(Class<?> type, ThrowingConsumer<Object> assertion, String description) {
        this.type = type;
        this.assertion = assertion;
        this.description = description;
    }

    /**
     * Whether the given expected value type is checked with AssertJ: an {@link AssertJCheck} or an AssertJ {@link Condition}.
     */
    public static boolean isSupported(Class<?> type) {
        return type != null && (AssertJCheck.class.isAssignableFrom(type) || Condition.class.isAssignableFrom(type));
    }

    /**
     * Turns a supported expected value into a check.
     * @see #isSupported(Class)
     */
    public static AssertJCheck from(Object expected) {
        if (expected instanceof AssertJCheck check) {
            return check;
        }

        if (expected instanceof Condition<?> condition) {
            return of(condition);
        }

        throw new IllegalArgumentException("Unsupported AssertJ expected value: " + expected);
    }

    /**
     * Check that verifies the received value with an AssertJ condition.
     */
    @SuppressWarnings("unchecked")
    public static AssertJCheck of(Condition<?> condition) {
        Condition<Object> objectCondition = (Condition<Object>) condition;
        return new AssertJCheck(null, value -> Assertions.assertThat(value).is(objectCondition),
                "condition(" + condition.description().value() + ")");
    }

    /**
     * Check that converts the received value to the given type, when a type is set, and hands it to the assertion.
     */
    @SuppressWarnings("unchecked")
    public static <T> AssertJCheck of(Class<T> type, ThrowingConsumer<T> assertion) {
        return new AssertJCheck(type, (ThrowingConsumer<Object>) assertion,
                "satisfies(" + (type != null ? type.getName() : "") + ")");
    }

    /**
     * Runs the check on the received value.
     * @throws AssertionError when the assertion fails or the value cannot be converted to the target type
     */
    public void check(Object received, TestContext context) {
        Object value = convert(received, context);
        try {
            assertion.accept(value);
        } catch (ClassCastException e) {
            throw new AssertionError(String.format("Expecting value '%s' of type %s to be accepted by %s",
                    value, value.getClass().getName(), description), e);
        }
    }

    private Object convert(Object received, TestContext context) {
        if (type == null || received == null) {
            return received;
        }

        try {
            return context.getTypeConverter().convertIfNecessary(received, type);
        } catch (RuntimeException e) {
            throw new AssertionError(String.format("Expecting value '%s' to be convertible to %s", received, type.getName()), e);
        }
    }

    @Override
    public String toString() {
        return description;
    }
}
