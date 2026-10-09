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
import org.citrusframework.spi.SimpleReferenceResolver;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Verifies that {@link ValidationUtils#validateValues(Object, Object, String, TestContext)}
 * names the path expression when a {@link ValueMatcher} fails.
 *
 * <p>Covers <a href="https://github.com/citrusframework/citrus/issues/1784">#1784</a>:
 * throwing matchers (e.g. AssertJ) and generic {@link AssertionError} /
 * {@link IllegalArgumentException} failures must name the path, while matchers
 * returning {@code false} (e.g. Hamcrest) keep their message unchanged.
 */
public class ValidationUtilsTest {

    @Test
    public void throwingMatcherNamesPath() {
        TestContext context = contextWith(new ThrowingMatcher());
        String path = "$.qty";

        try {
            ValidationUtils.validateValues(3, new ThrowingControl("Expecting actual: 3 to be greater than: 5"), path, context);
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("'" + path + "'"),
                    "Message should name the path, was: " + e.getMessage());
            assertTrue(e.getMessage().contains("Expecting actual: 3"),
                    "Message should keep the matcher description, was: " + e.getMessage());
            assertTrue(e.getMessage().startsWith("Values not matching for element '" + path + "'"),
                    "Message should be prepended with path, was: " + e.getMessage());
        }
    }

    @Test
    public void throwingMatcherNamesPathForNullActual() {
        TestContext context = contextWith(new ThrowingMatcher());
        String path = "$.qty";

        try {
            ValidationUtils.validateValues(null, new ThrowingControl("Expecting actual: null to be greater than: 5"), path, context);
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("'" + path + "'"),
                    "Message should name the path for null actual, was: " + e.getMessage());
            assertTrue(e.getMessage().contains("Expecting actual"),
                    "Message should keep the matcher description, was: " + e.getMessage());
        }
    }

    @Test
    public void throwingMatcherPreservesOriginalAsCause() {
        ValidationException original = new ValidationException("Expecting actual: 3 to be greater than: 5");
        TestContext context = contextWith(new FixedThrowingMatcher(original));
        String path = "$.qty";

        try {
            ValidationUtils.validateValues(3, new ThrowingControl("ignored"), path, context);
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertEquals(e.getCause(), original, "Original matcher exception should be the cause");
        }
    }

    @Test
    public void assertionErrorNamesPath() {
        TestContext context = contextWith(new AssertionErrorMatcher());
        String path = "$.qty";

        try {
            ValidationUtils.validateValues(3, new AssertionErrorControl(), path, context);
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("'" + path + "'"),
                    "Generic AssertionError wrapping should name the path, was: " + e.getMessage());
            assertTrue(e.getCause() instanceof AssertionError,
                    "Cause should be the original AssertionError, was: " + e.getCause());
        }
    }

    @Test
    public void assertionErrorNamesPathForNullActual() {
        TestContext context = contextWith(new AssertionErrorMatcher());
        String path = "$.qty";

        try {
            ValidationUtils.validateValues(null, new AssertionErrorControl(), path, context);
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("'" + path + "'"),
                    "Generic AssertionError wrapping should name the path for null actual, was: " + e.getMessage());
        }
    }

    @Test
    public void illegalArgumentExceptionNamesPath() {
        TestContext context = contextWith(new IllegalArgumentMatcher());
        String path = "$.qty";

        try {
            ValidationUtils.validateValues(3, new IllegalArgumentControl(), path, context);
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("'" + path + "'"),
                    "Generic IllegalArgumentException wrapping should name the path, was: " + e.getMessage());
            assertTrue(e.getCause() instanceof IllegalArgumentException,
                    "Cause should be the original IllegalArgumentException, was: " + e.getCause());
        }
    }

    @Test
    public void illegalArgumentExceptionNamesPathForNullActual() {
        TestContext context = contextWith(new IllegalArgumentMatcher());
        String path = "$.qty";

        try {
            ValidationUtils.validateValues(null, new IllegalArgumentControl(), path, context);
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("'" + path + "'"),
                    "Generic IllegalArgumentException wrapping should name the path for null actual, was: " + e.getMessage());
        }
    }

    @Test
    public void falseMatcherKeepsMessageUnchanged() {
        TestContext context = contextWith(new FalseMatcher());
        String path = "$.qty";
        FalseControl expected = new FalseControl("expected-value");

        try {
            ValidationUtils.validateValues("actual-value", expected, path, context);
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            String expectedMessage = ValidationUtils.buildValueMismatchErrorMessage(
                    "Values not matching for element '" + path + "'", expected, "actual-value");
            assertEquals(e.getMessage(), expectedMessage,
                    "False-returning matcher must keep today's message unchanged");
        }
    }

    @Test
    public void falseMatcherKeepsMessageUnchangedForNullActual() {
        TestContext context = contextWith(new FalseMatcher());
        String path = "$.qty";
        FalseControl expected = new FalseControl("expected-value");

        try {
            ValidationUtils.validateValues(null, expected, path, context);
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            String expectedMessage = ValidationUtils.buildValueMismatchErrorMessage(
                    "Values not matching for element '" + path + "'", expected, null);
            assertEquals(e.getMessage(), expectedMessage,
                    "False-returning matcher must keep today's message unchanged for null actual");
        }
    }

    private static TestContext contextWith(ValueMatcher matcher) {
        TestContext context = new TestContext();
        SimpleReferenceResolver resolver = new SimpleReferenceResolver();
        resolver.bind(matcher.getClass().getName(), matcher);
        context.setReferenceResolver(resolver);
        return context;
    }

    static class ThrowingControl {
        final String detail;

        ThrowingControl(String detail) {
            this.detail = detail;
        }
    }

    static class FalseControl {
        final String value;

        FalseControl(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    static class AssertionErrorControl {
    }

    static class IllegalArgumentControl {
    }

    /** Simulates the AssertJ value matcher: throws to keep the failure description. */
    static class ThrowingMatcher implements ValueMatcher {
        @Override
        public boolean supports(Class<?> controlType) {
            return ThrowingControl.class.isAssignableFrom(controlType);
        }

        @Override
        public boolean validate(Object received, Object control, TestContext context) {
            throw new ValidationException(((ThrowingControl) control).detail);
        }
    }

    static class FixedThrowingMatcher implements ValueMatcher {
        private final ValidationException failure;

        FixedThrowingMatcher(ValidationException failure) {
            this.failure = failure;
        }

        @Override
        public boolean supports(Class<?> controlType) {
            return ThrowingControl.class.isAssignableFrom(controlType);
        }

        @Override
        public boolean validate(Object received, Object control, TestContext context) {
            throw failure;
        }
    }

    /** Simulates the Hamcrest value matcher: returns false on mismatch. */
    static class FalseMatcher implements ValueMatcher {
        @Override
        public boolean supports(Class<?> controlType) {
            return FalseControl.class.isAssignableFrom(controlType);
        }

        @Override
        public boolean validate(Object received, Object control, TestContext context) {
            return false;
        }
    }

    static class AssertionErrorMatcher implements ValueMatcher {
        @Override
        public boolean supports(Class<?> controlType) {
            return AssertionErrorControl.class.isAssignableFrom(controlType);
        }

        @Override
        public boolean validate(Object received, Object control, TestContext context) {
            throw new AssertionError("Expecting actual: " + received + " to be greater than: 5");
        }
    }

    static class IllegalArgumentMatcher implements ValueMatcher {
        @Override
        public boolean supports(Class<?> controlType) {
            return IllegalArgumentControl.class.isAssignableFrom(controlType);
        }

        @Override
        public boolean validate(Object received, Object control, TestContext context) {
            throw new IllegalArgumentException("Invalid value: " + received);
        }
    }
}
