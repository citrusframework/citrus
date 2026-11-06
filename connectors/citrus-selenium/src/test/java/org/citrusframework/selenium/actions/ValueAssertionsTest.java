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

package org.citrusframework.selenium.actions;

import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * @since 5.1
 */
public class ValueAssertionsTest extends AbstractUnitTest {

    @Test
    public void shouldAssertEqualValue() {
        ValueAssertions.assertValue("text", "Hello Citrus", "Hello Citrus", context);
    }

    @Test
    public void shouldAssertValueWithDynamicContent() {
        context.setVariable("expectedText", "Hello Citrus");

        ValueAssertions.assertValue("text", "${expectedText}", "Hello Citrus", context);
    }

    @Test
    public void shouldAssertValueWithValidationMatcher() {
        ValueAssertions.assertValue("text", "@contains('Citrus')@", "Hello Citrus", context);
    }

    @Test
    public void shouldFailOnValueMismatch() {
        var e = Assertions.assertThrows(ValidationException.class, () -> {
        ValueAssertions.assertValue("text", "Hello", "Citrus", context);
    });
        Assertions.assertTrue(e.getMessage().matches("Selenium validation failed, text expected 'Hello', but was 'Citrus'"), e.getMessage());
    }

    @Test
    public void shouldFailOnValueMismatchWithLabel() {
        var e = Assertions.assertThrows(ValidationException.class, () -> {
        ValueAssertions.assertValue("attribute 'type'", "submit", "button", context);
    });
        Assertions.assertTrue(e.getMessage().matches("Selenium validation failed, attribute 'type' expected 'submit', but was 'button'"), e.getMessage());
    }

    @Test
    public void shouldSkipEmptyExpectedValue() {
        ValueAssertions.assertValue("text", "", "Citrus", context);
        ValueAssertions.assertValue("text", null, "Citrus", context);
    }
}
