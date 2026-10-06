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
import org.citrusframework.validation.HeaderValidator;
import org.citrusframework.validation.context.ValidationStatus;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.validation.assertj.AssertJ.satisfies;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssertJHeaderValidatorTest extends UnitTestSupport {

    private final AssertJHeaderValidator validator = new AssertJHeaderValidator();

    @Test
    public void shouldBeResolvableByLookup() {
        assertThat(HeaderValidator.lookup()).containsKey("assertjHeaderValidator");
        assertThat(HeaderValidator.lookup().get("assertjHeaderValidator")).isInstanceOf(AssertJHeaderValidator.class);
    }

    @Test
    public void shouldSupportAssertJTypesOnly() {
        assertThat(validator.supports("status", Condition.class)).isTrue();
        assertThat(validator.supports("status", satisfies(v -> {}).getClass())).isTrue();
        assertThat(validator.supports("status", String.class)).isFalse();
        assertThat(validator.supports("status", Matchers.equalTo("x").getClass())).isFalse();
        assertThat(validator.supports("status", null)).isFalse();
    }

    @Test
    public void shouldPassCondition() {
        HeaderValidationContext validationContext = new HeaderValidationContext();

        validator.validateHeader("status", "OK-1", new Condition<String>(s -> s.startsWith("OK"), "starts with OK"), context, validationContext);

        assertThat(validationContext.getStatus()).isEqualTo(ValidationStatus.PASSED);
    }

    @Test
    public void shouldConvertBeforeAssertion() {
        HeaderValidationContext validationContext = new HeaderValidationContext();

        validator.validateHeader("count", "42", satisfies(Integer.class, c -> assertThat(c).isBetween(40, 50)), context, validationContext);

        assertThat(validationContext.getStatus()).isEqualTo(ValidationStatus.PASSED);
    }

    @Test
    public void shouldFailNamingHeaderAndCondition() {
        HeaderValidationContext validationContext = new HeaderValidationContext();

        assertThatThrownBy(() -> validator.validateHeader("status", "FAILED", new Condition<String>(s -> s.startsWith("OK"), "starts with OK"), context, validationContext))
                .isInstanceOf(ValidationException.class)
                .hasMessageStartingWith("Header validation failed for header 'status': ")
                .hasMessageContaining("starts with OK");
        assertThat(validationContext.getStatus()).isNotEqualTo(ValidationStatus.PASSED);
    }

    @Test
    public void shouldFailNamingTargetType() {
        assertThatThrownBy(() -> validator.validateHeader("count", "many", satisfies(Integer.class, c -> assertThat(c).isPositive()), context, new HeaderValidationContext()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("java.lang.Integer");
    }

    @Test
    public void shouldKeepJUnitMessage() {
        assertThatThrownBy(() -> validator.validateHeader("region", "eu", satisfies(v -> assertEquals("us", v)), context, new HeaderValidationContext()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("expected: <us> but was: <eu>");
    }
}
