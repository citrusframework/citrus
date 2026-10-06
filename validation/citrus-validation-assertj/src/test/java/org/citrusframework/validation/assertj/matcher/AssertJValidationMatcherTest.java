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

package org.citrusframework.validation.assertj.matcher;

import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.validation.assertj.UnitTestSupport;
import org.citrusframework.validation.matcher.ValidationMatcher;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.validation.matcher.ValidationMatcherUtils.resolveValidationMatcher;

public class AssertJValidationMatcherTest extends UnitTestSupport {

    @Test
    public void shouldBeRegisteredAsAssertj() {
        assertThat(ValidationMatcher.lookup().get("assertj")).isInstanceOf(AssertJValidationMatcher.class);
        assertThat(ValidationMatcher.lookup().get("assertThat")).isNotInstanceOf(AssertJValidationMatcher.class);
    }

    @Test(dataProvider = "passing")
    public void shouldPass(String value, String expression) {
        context.setVariable("prefix", "OK");
        context.setVariable("i", "2");

        assertThatCode(() -> resolveValidationMatcher("field", value, expression, context)).doesNotThrowAnyException();
    }

    @DataProvider
    public Object[][] passing() {
        return new Object[][] {
            { "OK-123", "@assertj(isNotBlank().startsWith('OK'))@" },
            { "7", "@assertj(asInt().isBetween(1, 10))@" },
            { "OK-1", "@assertj(startsWith('${prefix}'))@" },
            { "ignored", "@assertj('${i}', 'asInt().isLessThan(3)')@" },
            { "a, b (c)", "@assertj(contains('a', 'b (c)'))@" },
            { "it's", "@assertj(isEqualTo('it\\'s'))@" },
            { "OK-1", "@assertj('${prefix}-1', 'startsWith('OK').endsWith('1')')@" },
            { "ignored", "@assertj('it\\'s', 'isEqualTo('it\\'s')')@" },
            { "[a, b, c]", "@assertj(asList().hasSize(3).contains('b'))@" }
        };
    }

    @Test
    public void shouldReportFailedAssertion() {
        assertThatThrownBy(() -> resolveValidationMatcher("status", "FAILED", "@assertj(startsWith('OK'))@", context))
                .isInstanceOf(ValidationException.class)
                .hasMessageStartingWith("AssertJValidationMatcher failed for field 'status'. Received value is 'FAILED' and did not satisfy 'startsWith('OK')'.")
                .hasCauseInstanceOf(AssertionError.class)
                .cause().hasMessageContaining("to start with");
    }

    @Test
    public void shouldReportFailedAssertionOnExplicitValue() {
        context.setVariable("i", "3");

        assertThatThrownBy(() -> resolveValidationMatcher("iteratingCondition", "3", "@assertj('${i}', 'asInt().isLessThan(3)')@", context))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Received value is '3' and did not satisfy 'asInt().isLessThan(3)'");
    }

    @Test
    public void shouldSplitExplicitValueFromChain() {
        AssertJValidationMatcher matcher = new AssertJValidationMatcher();

        assertThat(matcher.extractControlValues("isNotBlank()", null)).containsExactly("isNotBlank()");
        assertThat(matcher.extractControlValues("'v', 'startsWith('OK')'", null)).containsExactly("v", "startsWith('OK')");
        assertThat(matcher.extractControlValues("'it\\'s',isEqualTo('x')", null)).containsExactly("it's", "isEqualTo('x')");
        assertThat(matcher.extractControlValues("'just a value'", null)).containsExactly("'just a value'");
    }

    @Test
    public void shouldReportUnresolvableMethod() {
        assertThatThrownBy(() -> resolveValidationMatcher("status", "OK", "@assertj(isAwesome())@", context))
                .isInstanceOf(ValidationException.class)
                .hasMessage("No AssertJ assertion 'isAwesome()' on StringAssert");
    }

    @Test
    public void shouldReportSyntaxError() {
        assertThatThrownBy(() -> resolveValidationMatcher("status", "OK", "@assertj(startsWith(OK))@", context))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("unquoted argument 'OK'");
    }
}
