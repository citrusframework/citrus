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
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.validation.assertj.AssertJ.satisfies;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssertJCheckTest extends UnitTestSupport {

    @Test
    public void shouldPassWhenLambdaReturnsNormally() {
        AssertJCheck check = satisfies(v -> assertThat(v).asString().startsWith("OK"));

        assertThatCode(() -> check.check("OK-123", context)).doesNotThrowAnyException();
    }

    @Test
    public void shouldFailWithAssertJMessage() {
        AssertJCheck check = satisfies(v -> assertThat(v).asString().startsWith("OK"));

        assertThatThrownBy(() -> check.check("FAILED", context))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("FAILED")
                .hasMessageContaining("OK");
    }

    @Test
    public void shouldFailWithJUnitAssertionMessage() {
        AssertJCheck check = satisfies(v -> assertEquals("us", v));

        assertThatThrownBy(() -> check.check("eu", context))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("expected: <us> but was: <eu>");
    }

    @Test
    public void shouldConvertToTargetType() {
        AssertJCheck check = satisfies(Integer.class, c -> assertThat(c).isBetween(40, 50));

        assertThatCode(() -> check.check("42", context)).doesNotThrowAnyException();
    }

    @Test
    public void shouldNameTargetTypeWhenConversionFails() {
        AssertJCheck check = satisfies(Integer.class, c -> assertThat(c).isPositive());

        assertThatThrownBy(() -> check.check("many", context))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("many")
                .hasMessageContaining("java.lang.Integer");
    }

    @Test
    public void shouldHandNullToLambda() {
        AssertJCheck untyped = satisfies(v -> assertThat(v).isNull());
        AssertJCheck typed = satisfies(Integer.class, v -> assertThat(v).isNull());

        assertThatCode(() -> untyped.check(null, context)).doesNotThrowAnyException();
        assertThatCode(() -> typed.check(null, context)).doesNotThrowAnyException();
    }

    @Test
    public void shouldCheckCondition() {
        AssertJCheck check = AssertJCheck.of(new Condition<>(v -> v.equals("OK"), "ok status"));

        assertThatCode(() -> check.check("OK", context)).doesNotThrowAnyException();
        assertThatThrownBy(() -> check.check("FAILED", context))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("FAILED")
                .hasMessageContaining("ok status");
    }

    @Test
    public void shouldDescribeItself() {
        assertThat(AssertJCheck.of(new Condition<>(v -> true, "ok status")))
                .hasToString("condition(ok status)");
        assertThat(satisfies(Integer.class, v -> {}))
                .hasToString("satisfies(java.lang.Integer)");
        assertThat(satisfies(v -> {}))
                .hasToString("satisfies()");
    }
}
