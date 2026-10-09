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

import java.util.Arrays;
import java.util.List;

import org.citrusframework.exceptions.ValidationException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AssertionChainParserTest {

    @Test(dataProvider = "chains")
    public void shouldParse(String expression, List<AssertionCall> expected) {
        assertThat(AssertionChainParser.parse(expression)).isEqualTo(expected);
    }

    @DataProvider
    public Object[][] chains() {
        return new Object[][] {
            { "isNotBlank()", List.of(call("isNotBlank")) },
            { "startsWith('OK').endsWith('1')", List.of(call("startsWith", "OK"), call("endsWith", "1")) },
            { "contains('a', 'b (c)')", List.of(call("contains", "a", "b (c)")) },
            { "isEqualTo('it\\'s')", List.of(call("isEqualTo", "it's")) },
            { "isEqualTo('a\\\\b')", List.of(call("isEqualTo", "a\\b")) },
            { "isEqualTo('x.y').isNotEmpty()", List.of(call("isEqualTo", "x.y"), call("isNotEmpty")) },
            { "isEqualTo('')", List.of(call("isEqualTo", "")) },
            { "asInt().isBetween(-5, 10)", List.of(call("asInt"), call("isBetween", -5, 10)) },
            { "isEqualTo(3.14)", List.of(call("isEqualTo", 3.14d)) },
            { "isEqualTo(9999999999)", List.of(call("isEqualTo", 9999999999L)) },
            { "isIn(true, false, null)", List.of(call("isIn", true, false, null)) },
            { "  asInt ( ) . isBetween ( 1 ,10 )  ", List.of(call("asInt"), call("isBetween", 1, 10)) }
        };
    }

    @Test(dataProvider = "invalidChains")
    public void shouldRejectInvalidSyntax(String expression, String reason) {
        assertThatThrownBy(() -> AssertionChainParser.parse(expression))
                .isInstanceOf(ValidationException.class)
                .hasMessageStartingWith("Invalid @assertj()@ expression '" + expression + "': ")
                .hasMessageContaining(reason)
                .hasMessageContaining("at position");
    }

    @DataProvider
    public Object[][] invalidChains() {
        return new Object[][] {
            { "", "expected assertion method name" },
            { "isNotBlank(", "expected argument or ')'" },
            { "isNotBlank", "expected '('" },
            { "startsWith('OK)", "unterminated quoted argument" },
            { "(1)", "expected assertion method name" },
            { "isNotBlank().", "expected assertion method name" },
            { "startsWith(OK)", "unquoted argument 'OK'" },
            { "startsWith('a' 'b')", "expected ',' or ')'" },
            { "isNotBlank() isEmpty()", "expected '.'" },
            { "isEqualTo(99999999999999999999)", "number out of range" }
        };
    }

    private static AssertionCall call(String name, Object... arguments) {
        return new AssertionCall(name, Arrays.asList(arguments));
    }
}
