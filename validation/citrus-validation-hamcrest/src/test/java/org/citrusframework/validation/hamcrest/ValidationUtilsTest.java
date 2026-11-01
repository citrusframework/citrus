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

package org.citrusframework.validation.hamcrest;

import org.citrusframework.context.TestContextFactory;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.validation.ValidationUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @since 2.5
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ValidationUtilsTest extends AbstractUnitTest {

    @Override
    protected TestContextFactory createTestContextFactory() {
        return TestContextFactory.newInstance();
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testValidateValues(String actualValue, Object expectedValue, String path) {
        ValidationUtils.validateValues(actualValue, expectedValue, path, context);
    }

    @ParameterizedTest
    @MethodSource("testDataFailed")
    public void testValidateValuesFailure(String actualValue, Object expectedValue, String path) {
        assertThrows(ValidationException.class, () ->
            ValidationUtils.validateValues(actualValue, expectedValue, path, context));
    }

    @SuppressWarnings("deprecation")
    public Object[][] testData() {
        return new Object[][] {
            new Object[] {null, "@assertThat(nullValue())@", "nullValidationMatcherCompare"},
            new Object[] {null, Matchers.nullValue(), "nullHamcrestMatcherCompare"},
            new Object[] {"foo", Matchers.allOf(Matchers.not(Matchers.isEmptyString()), Matchers.equalTo("foo")), "hamcrestMatcherCompare"}
        };
    }

    @SuppressWarnings("deprecation")
    public Object[][] testDataFailed() {
        return new Object[][] {
                new Object[] {null, "@assertThat(notNullValue())@", "nullValidationMatcherCompare"},
                new Object[] {"foo", Matchers.allOf(Matchers.isEmptyString(), Matchers.equalTo("bar")), "hamcrestMatcherCompare"}
        };
    }
}
