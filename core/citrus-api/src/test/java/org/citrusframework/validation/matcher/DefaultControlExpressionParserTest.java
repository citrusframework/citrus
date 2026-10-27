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

import org.citrusframework.exceptions.CitrusRuntimeException;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @since 2.5
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DefaultControlExpressionParserTest {

    @ParameterizedTest
    @MethodSource("validControlExpressions")
    public void shouldExtractControlParametersSuccessfully(String controlExpression, List<String> expectedParameters) {
        ControlExpressionParser expressionParser = new DefaultControlExpressionParser();
        List<String> extractedParameters = expressionParser.extractControlValues(controlExpression, null);

        Assertions.assertEquals(expectedParameters.size(), extractedParameters.size());

        for (int i = 0; i < expectedParameters.size(); i++) {
            Assertions.assertTrue(extractedParameters.size() > i);
            Assertions.assertEquals(expectedParameters.get(i), extractedParameters.get(i));
        }
    }

    public Object[][] validControlExpressions() {
        return new Object[][]{
                // {control-expression, expected-parameter-1, expected-parameter-2, ..}
                {"'a'", singletonList("a")},
                {"'a',", singletonList("a")},
                {"'a','b'", List.of("a","b")},
                {"'a','b',", List.of("a","b")},
                {"'a,s','b',", List.of("a,s","b")},
                {"'a)s','b',", List.of("a)s","b")},
                {"'a's','b',", List.of("a's","b")},
                {"''", singletonList("")},
                {"'',", singletonList("")},
                {"", List.<String>of()},
                {null, List.<String>of()},
        };
    }

    @ParameterizedTest
    @MethodSource("invalidControlExpressions")
    public void shouldNotExtractControlParametersSuccessfully(String controlExpression) {
        ControlExpressionParser expressionParser = new DefaultControlExpressionParser();
        assertThrows(CitrusRuntimeException.class, () -> {
            expressionParser.extractControlValues(controlExpression, null);
        });
    }

    public Object[][] invalidControlExpressions() {
        return new Object[][]{
                {"'"},
                {"',"},
                {"'a"},
                {"'a,"},
                {"'a's,"},
                {"'a',s'"},
                {"'a','b"},
                {"'a','b,"},
        };
    }
}
