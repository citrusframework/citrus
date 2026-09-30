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

package org.citrusframework.validation.matcher;

import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CombinatorExpressionParserTest {

    @Test(dataProvider = "expressions")
    public void shouldSplitSubExpressions(String body, List<String> expected) {
        List<String> result = CombinatorExpressionParser.splitSubExpressions(body);
        Assert.assertEquals(result, expected);
    }

    @DataProvider
    public Object[][] expressions() {
        return new Object[][]{
                // null / blank → empty list
                {null, Collections.emptyList()},
                {"", Collections.emptyList()},
                {"   ", Collections.emptyList()},

                // single expression — no split
                {"startsWith('hello')", List.of("startsWith('hello')")},
                {"isNumber()", List.of("isNumber()")},

                // simple two-argument split
                {"startsWith('a'), endsWith('b')", List.of("startsWith('a')", "endsWith('b')")},

                // whitespace around comma is trimmed
                {"startsWith('a'),endsWith('b')", List.of("startsWith('a')", "endsWith('b')")},
                {"startsWith('a') , endsWith('b')", List.of("startsWith('a')", "endsWith('b')")},

                // three arguments
                {"startsWith('a'), contains('b'), endsWith('c')",
                        List.of("startsWith('a')", "contains('b')", "endsWith('c')")},

                // nested combinator — comma inside parens must not split
                {"anyOf(endsWith('world'), contains('!')), startsWith('hello')",
                        List.of("anyOf(endsWith('world'), contains('!'))", "startsWith('hello')")},

                // deeply nested
                {"allOf(anyOf(startsWith('a'), startsWith('b')), not(contains('x')))",
                        List.of("allOf(anyOf(startsWith('a'), startsWith('b')), not(contains('x')))")},

                // comma inside single-quoted string must not split
                {"startsWith('hello, world'), endsWith('!')",
                        List.of("startsWith('hello, world')", "endsWith('!')")},

                // quoted string with paren chars inside — must not affect depth tracking
                {"startsWith('a(b)'), endsWith('c')", List.of("startsWith('a(b)')", "endsWith('c')")},

                // multiple nesting levels as two sibling expressions
                {"not(contains('error')), anyOf(startsWith('foo'), startsWith('bar'))",
                        List.of("not(contains('error'))", "anyOf(startsWith('foo'), startsWith('bar'))")},
        };
    }
}
