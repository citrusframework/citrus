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
import org.citrusframework.validation.ValueMatcher;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.validation.assertj.AssertJ.satisfies;

public class AssertJValueMatcherTest extends UnitTestSupport {

    private final AssertJValueMatcher matcher = new AssertJValueMatcher();

    @Test
    public void shouldBeResolvableByLookup() {
        assertThat(ValueMatcher.lookup()).containsKey("assertj");
        assertThat(ValueMatcher.lookup().get("assertj")).isInstanceOf(AssertJValueMatcher.class);
        assertThat(ValueMatcher.lookup("assertj")).isPresent();
    }

    @Test
    public void shouldSupportAssertJTypesOnly() {
        assertThat(matcher.supports(Condition.class)).isTrue();
        assertThat(matcher.supports(new Condition<>(v -> true, "any").getClass())).isTrue();
        assertThat(matcher.supports(satisfies(v -> {}).getClass())).isTrue();
        assertThat(matcher.supports(String.class)).isFalse();
        assertThat(matcher.supports(Matchers.equalTo("x").getClass())).isFalse();
    }

    @Test
    public void shouldValidateCondition() {
        assertThat(matcher.validate(7, new Condition<Integer>(q -> q > 5, "greater than 5"), context)).isTrue();
    }

    @Test
    public void shouldValidateAssertion() {
        assertThat(matcher.validate("7", satisfies(Integer.class, q -> assertThat(q).isGreaterThan(5)), context)).isTrue();
    }

    @Test
    public void shouldThrowWithAssertJDescription() {
        assertThatThrownBy(() -> matcher.validate("3", satisfies(Integer.class, q -> assertThat(q).isGreaterThan(5)), context))
                .isInstanceOf(ValidationException.class)
                .hasMessageStartingWith("AssertJ validation failed: ")
                .hasMessageContaining("to be greater than")
                .hasCauseInstanceOf(AssertionError.class);
    }
}
