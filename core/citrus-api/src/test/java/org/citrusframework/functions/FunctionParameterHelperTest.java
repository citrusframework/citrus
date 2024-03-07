/*
 * Copyright 2006-2024 the original author or authors.
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

package org.citrusframework.functions;

import org.citrusframework.functions.FunctionParameterHelper.ParameterParser;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.citrusframework.functions.FunctionParameterHelper.getParameterList;

class FunctionParameterHelperTest {

    @Test
    void shouldOneParam() {
        var result = getParameterList("lorem");
        assertThat(result).containsExactly("lorem");
    }

    @Test
    void shouldTwoParam() {
        var result = getParameterList("lorem, ipsum");
        assertThat(result).containsExactly("lorem", "ipsum");
    }

    @Test
    void shouldTwoParam_oneQuoted() {
        var result = getParameterList("lorem, 'ipsum'");
        assertThat(result).containsExactly("lorem", "ipsum");
    }

    @Test
    void shouldTwoParam_withCommaInParam() {
        var result = getParameterList("'lorem, dolor', 'ipsum'");
        assertThat(result).containsExactly("lorem, dolor", "ipsum");
    }

    @Test
    void shouldTwoParam_withLinebreak() {
        var result = getParameterList("'lorem, dolor', 'ipsum\n sit'");
        assertThat(result).containsExactly("lorem, dolor", "ipsum\n sit");
    }

    @Test
    void shouldTwoParam_withLinebreakAfterComma() {
        var result = getParameterList("'lorem,\n dolor', 'ipsum sit'");
        assertThat(result).containsExactly("lorem,\n dolor", "ipsum sit");
    }

    @Test
    void shouldTwoParam_withWhitespacesAfterComma() {
        var result = getParameterList("'lorem,    dolor', 'ipsum sit'");
        assertThat(result).containsExactly("lorem,    dolor", "ipsum sit");
    }

    @Test
    void shouldConvertSingleLineJson() {
        String json = """
                {"myValues": ["O15o3a8","PhDjdSruZgG"]}""";
        var result = getParameterList(wrappedInSingleQuotes(json));
        assertThat(result).containsExactly(json);
    }

    @Test
    void shouldConvertMultiLineJson() {
        // language=JSON
        String json = """
                {
                    "id": 133,
                    "myValues": [
                        "O15o3a8",
                        "PhDjdSruZgG",
                        "I2qrC1Mu, PmSsd8LPLe"
                    ]
                }""";
        var result = getParameterList(wrappedInSingleQuotes(json));
        assertThat(result).containsExactly(json);
    }

    @Test
    void shouldConvertNestedSingleQuotedStrings() {
        // language=JSON
        String json = """
                ["part of first param", "also  'part' of first param"]""";
        var result = getParameterList(wrappedInSingleQuotes(json));
        assertThat(result).hasSize(1).containsExactly(json);
    }

    @Test
    void shouldConvertIdempotent() {
        // language=JSON
        String json = """
                ["part of first param", "also  'part' of first param"]""";

        var parser = new ParameterParser(wrappedInSingleQuotes(json));
        var result1 = parser.parse();
        var result2 = parser.parse();

        assertThat(result1).isEqualTo(result2).hasSize(1).containsExactly(json);
    }

    @Test
    void cannotConvertSpecialNestedSingleQuotedStrings() {
        String threeParams = """
                '["part of first param", "following comma will be missing ',' should also be first param"]', 'lorem', ipsum""";
        var parser = new ParameterParser(threeParams);
        var result = parser.parse();
        assertThat(result).containsExactly(
                "[\"part of first param\", \"following comma will be missing ",
                " should also be first param\"]",
                "lorem",
                "ipsum"
        );
    }

    private static String wrappedInSingleQuotes(String parameterString) {
        return "'%s'".formatted(parameterString);
    }
}
