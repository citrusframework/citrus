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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AssertionChainInvokerTest {

    @Test(dataProvider = "passing")
    public void shouldPass(String value, String expression) {
        assertThatCode(() -> invoke(value, expression)).doesNotThrowAnyException();
    }

    @DataProvider
    public Object[][] passing() {
        return new Object[][] {
            // strings
            { "OK-123", "isNotBlank().startsWith('OK').endsWith('123')" },
            { "OK", "isEqualTo('OK')" },
            { "ok", "isEqualToIgnoringCase('OK')" },
            { "a, b (c)", "contains('a', 'b (c)')" },
            { "a, b (c)", "contains('b')" },
            { "abc-123", "matches('[a-z]+-\\\\d+')" },
            { "", "isEmpty()" },
            { "abc", "hasSize(3)" },
            { "abc", "isIn('x', 'abc')" },
            { "abc", "hasSizeBetween(1, 5).doesNotContain('z')" },
            { null, "isNull()" },
            { "OK", "as('status').isEqualTo('OK')" },
            // numbers and booleans
            { "7", "asInt().isBetween(1, 10)" },
            { "7", "asInt().isEqualTo(7).isGreaterThan(5).isPositive()" },
            { "9999999999", "asLong().isGreaterThan(5)" },
            { "7", "asLong().isEqualTo(7)" },
            { "7", "asShort().isEqualTo(7)" },
            { "7", "asByte().isLessThan(8)" },
            { "3.5", "asDouble().isGreaterThan(3).isLessThan(3.6)" },
            { "3.5", "asFloat().isEqualTo(3.5)" },
            { "true", "asBoolean().isTrue()" },
            // collections
            { "[a, b, c]", "asList().hasSize(3).contains('b')" },
            { "[\"a\", \"b\"]", "asList().containsExactly('a', 'b')" },
            { "[]", "asList().isEmpty()" },
            { "{k=v, x=\"y\"}", "asMap().containsKey('k').containsEntry('x', 'y')" }
        };
    }

    @Test(dataProvider = "failing")
    public void shouldFailAssertion(String value, String expression) {
        assertThatThrownBy(() -> invoke(value, expression)).isInstanceOf(AssertionError.class);
    }

    @DataProvider
    public Object[][] failing() {
        return new Object[][] {
            { "FAILED", "startsWith('OK')" },
            { "OK-1", "startsWith('OK').endsWith('2')" },
            { "11", "asInt().isBetween(1, 10)" },
            { "abc", "asInt().isPositive()" },
            { "[a, b]", "asList().hasSize(3)" },
            { "{k=v}", "asMap().containsKey('x')" },
            { null, "isNotNull()" }
        };
    }

    @Test
    public void shouldNameUnknownMethod() {
        assertThatThrownBy(() -> invoke("x", "isAwesome()"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("No AssertJ assertion 'isAwesome()' on StringAssert");
    }

    @Test
    public void shouldNameArgumentTypes() {
        assertThatThrownBy(() -> invoke("7", "asInt().isBetween('a', 'b')"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("No AssertJ assertion 'isBetween(String, String)' on ")
                .hasMessageContaining("IntegerAssert");
    }

    @Test
    public void shouldNameWrongArity() {
        assertThatThrownBy(() -> invoke("x", "startsWith()"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("No AssertJ assertion 'startsWith()' on StringAssert");
    }

    @Test
    public void shouldRejectNonAssertionMethod() {
        assertThatThrownBy(() -> invoke("x", "actual()"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("'actual' is not an AssertJ assertion method on StringAssert");
        assertThatThrownBy(() -> invoke("x", "hashCode()"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("'hashCode' is not an AssertJ assertion method on StringAssert");
    }

    @Test
    public void shouldEndChainAfterVoidAssertion() {
        assertThatThrownBy(() -> invoke("", "isEmpty().isNotNull()"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("'isEmpty' ends the assertion chain and cannot be followed by 'isNotNull'");
    }

    @Test
    public void shouldRejectArgumentsThatNeedObjects() {
        assertThatThrownBy(() -> invoke("3.5", "asDouble().isCloseTo(3.4, 0.2)"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("No AssertJ assertion 'isCloseTo(Double, Double)'");
    }

    @Test
    public void shouldOnlyAllowCollectionConversionFirst() {
        assertThatThrownBy(() -> invoke("[a]", "isNotEmpty().asList()"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("'asList' is only supported as the first call of an @assertj()@ expression");
    }

    @Test
    public void shouldRejectBareCollectionConversion() {
        assertThatThrownBy(() -> invoke("[a]", "asList()"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("'asList' must be followed by an AssertJ assertion, e.g. asList().hasSize(3)");
        assertThatThrownBy(() -> invoke("{k=v}", "asMap()"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("'asMap' must be followed by an AssertJ assertion, e.g. asMap().hasSize(3)");
    }

    private static void invoke(String value, String expression) {
        AssertionChainInvoker.invoke(value, AssertionChainParser.parse(expression));
    }
}
