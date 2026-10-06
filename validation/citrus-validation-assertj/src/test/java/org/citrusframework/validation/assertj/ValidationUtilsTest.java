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

import org.assertj.core.api.Condition;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.validation.ValidationUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.validation.assertj.AssertJ.satisfies;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValidationUtilsTest extends UnitTestSupport {

    @Test(dataProvider = "testData")
    public void shouldValidateValues(Object actualValue, Object expectedValue, String path) {
        ValidationUtils.validateValues(actualValue, expectedValue, path, context);
    }

    @Test(dataProvider = "testDataFailed", expectedExceptions = ValidationException.class)
    public void shouldFailValidation(Object actualValue, Object expectedValue, String path) {
        ValidationUtils.validateValues(actualValue, expectedValue, path, context);
    }

    @Test
    public void shouldReportAssertJDescription() {
        assertThatThrownBy(() -> ValidationUtils.validateValues(3, satisfies(Integer.class, q -> assertThat(q).isGreaterThan(5)), "$.qty", context))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Expecting actual:")
                .hasMessageContaining("to be greater than:");
    }

    @DataProvider
    public Object[][] testData() {
        return new Object[][] {
            new Object[] {7, new Condition<Integer>(q -> q > 5, "greater than 5"), "$.qty"},
            new Object[] {"OK-123", satisfies(v -> assertThat(v).asString().startsWith("OK")), "/Order/Status"},
            new Object[] {"7", satisfies(Integer.class, q -> assertThat(q).isBetween(1, 10)), "$.qty"},
            new Object[] {"us", satisfies(v -> assertEquals("us", v)), "region"},
            new Object[] {null, satisfies(v -> assertThat(v).isNull()), "$.missing"},
            new Object[] {null, new Condition<>(v -> v == null, "null"), "$.missing"}
        };
    }

    @DataProvider
    public Object[][] testDataFailed() {
        return new Object[][] {
            new Object[] {3, new Condition<Integer>(q -> q > 5, "greater than 5"), "$.qty"},
            new Object[] {"FAILED", satisfies(v -> assertThat(v).asString().startsWith("OK")), "/Order/Status"},
            new Object[] {"many", satisfies(Integer.class, q -> assertThat(q).isPositive()), "$.qty"},
            new Object[] {"eu", satisfies(v -> assertEquals("us", v)), "region"},
            new Object[] {null, satisfies(v -> assertThat(v).isNotNull()), "$.missing"}
        };
    }
}
