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

package org.citrusframework.validation.yaml;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.ValidationException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class YamlNodeValidatorTest extends UnitTestSupport {

    public static final boolean NOT_STRICT = false;
    public static final boolean STRICT = true;

    YamlNodeValidator fixture;

    @Test(dataProvider = "validYamlPairs")
    public void shouldBeValidIfNotStrict(YamlAssertion jsonAssertion) {
        var validationItem = toValidationItem(jsonAssertion);
        fixture = new YamlNodeValidator(NOT_STRICT, context, Set.of());
        assertThatNoException().isThrownBy(() -> fixture.validate(validationItem));
    }

    @Test(dataProvider = "validYamlPairs")
    public void shouldBeInvalidIfStrict(YamlAssertion jsonAssertion) {
        var validationItem = toValidationItem(jsonAssertion);
        fixture = new YamlNodeValidator(STRICT, context, Set.of());
        assertThatThrownBy(() -> fixture.validate(validationItem)).isInstanceOf(ValidationException.class);
    }

    @DataProvider
    public static YamlAssertion[] validYamlPairs() {
        return List.of(
                new YamlAssertion(
                        """
                        text: "Hello World!"
                        index: 5
                        id: "x123456789x"
                        """,
                        """
                        id: "x123456789x"
                        """
                ),
                new YamlAssertion(
                        """
                        - text: "Hello World!"
                          index: 1
                        - text: "Hallo Welt!"
                          index: 2
                        - text: "Hola del mundo!"
                          index: 3
                        """,
                        """
                        - text: "Hello World!"
                          index: 1
                        """
                ),
                new YamlAssertion(
                        """
                        - text: "Hello World!"
                          index: 1
                        - text: "Hallo Welt!"
                          index: 2
                        - text: "Hola del mundo!"
                          index: 3
                        """,
                        """
                        - text: "Hallo Welt!"
                          index: 2
                        """
                ),
                new YamlAssertion(
                        """
                        - text: "Hello World!"
                          index: 1
                        - text: "Hallo Welt!"
                          index: 2
                        - text: "Hola del mundo!"
                          index: 3
                        """,
                        """
                        - index: 1
                        """
                ),
                new YamlAssertion(
                        """
                        - 1
                        - 2
                        - 3
                        """,
                        """
                        - 2
                        """
                ),
                new YamlAssertion(
                        """
                        - 1
                        - 2
                        - 1
                        """,
                        """
                        - 2
                        - 1
                        """
                ),
                new YamlAssertion(
                        """
                        - 1
                        - 2
                        - 1
                        """,
                        """
                        - 1
                        - 2
                        """
                ),
                new YamlAssertion(
                        """
                        books:
                          - book-a
                          - book-b
                          - book-c
                        """,
                        """
                        books:
                          - book-a
                          - book-b
                        """
                )
        ).toArray(new YamlAssertion[0]);
    }

    @Test(dataProvider = "validIfStrict")
    public void shouldBeValidIfStrict(YamlAssertion jsonAssertion) {
        var validationItem = toValidationItem(jsonAssertion);
        fixture = new YamlNodeValidator(STRICT, context, Set.of());
        assertThatNoException().isThrownBy(() -> fixture.validate(validationItem));
    }

    @DataProvider
    public static YamlAssertion[] validIfStrict() {
        return List.of(
                new YamlAssertion(
                        """
                        text: "Hello World!"
                        index: 5
                        id: "x123456789x"
                        """,
                        """
                        text: "Hello World!"
                        index: 5
                        id: "x123456789x"
                        """
                ),
                new YamlAssertion(
                        """
                        text: "Hello World!"
                        person: 
                          name: "John"
                          surname: "Doe"
                        index: 5
                        id: "x123456789x"
                        """,
                        """
                        text: "Hello World!"
                        person:
                          name: "John"
                          surname: "Doe"
                        index: 5
                        id: "x123456789x"
                        """
                ),
                new YamlAssertion(
                        """
                        - text: "Hello World!"
                          index: 1
                        - text: "Hallo Welt!"
                          index: 2
                        - text: "Hola del mundo!"
                          index: 3
                        """,
                        """
                        - text: "Hello World!"
                          index: 1
                        - text: "Hallo Welt!"
                          index: 2
                        - text: "Hola del mundo!"
                          index: 3
                        """
                ),
                new YamlAssertion(
                        """
                        greetings:
                          - text: "Hello World!"
                            index: 1
                          - text: "Hallo Welt!"
                            index: 2
                          - text: "Hola del mundo!"
                            index: 3
                        id: "x123456789x"
                        """,
                        """
                        greetings:
                          - text: "Hello World!"
                            index: 1
                          - text: "Hallo Welt!"
                            index: 2
                          - text: "Hola del mundo!"
                            index: 3
                        id: "x123456789x"
                        """
                ),
                new YamlAssertion(
                        """
                        text: "Hello World!"
                        index: 5
                        object:
                          id: "x123456789x"
                        greetings:
                          - text: "Hello World!"
                            index: 1
                          - text: "Hallo Welt!"
                            index: 2
                          - text: "Hola del mundo!"
                            index: 3
                        """,
                        """
                        text: "Hello World!"
                        index: "@ignore@"
                        object:
                          id: "@ignore@"
                        greetings: "@ignore@"
                        """
                ),
                new YamlAssertion(
                        """
                        text: "Hello World!"
                        index: 5
                        id: null
                        """,
                        """
                        text: "Hello World!"
                        index: 5
                        id: null
                        """
                ),
                new YamlAssertion(
                        "",
                        ""
                ),
                new YamlAssertion(
                        """
                        id: 42
                        """,
                        """
                        id: 42
                        """
                ),
                new YamlAssertion(
                        """
                        test: "Lorem"
                        """,
                        """
                        test: "@equalsIgnoreCase('lorem')@"
                        """
                ),
                new YamlAssertion(
                        """
                        - 1
                        - 2
                        - 3
                        """,
                        """
                        - 1
                        - 2
                        - 3
                        """
                ),
                new YamlAssertion(
                        """
                        - 1
                        - 3
                        - 2
                        """,
                        """
                        - 1
                        - 2
                        - 3
                        """
                ),
                new YamlAssertion(
                        """
                        - 3
                        - 2
                        - 1
                        """,
                        """
                        - 1
                        - 2
                        - 3
                        """
                ),
                new YamlAssertion(
                        """
                        books: ["book-a", "book-b", "book-c"]
                        """,
                        """
                        books: ["book-a", "book-b", "book-c"]
                        """
                ),
                new YamlAssertion(
                        """
                        books: ["book-a", "book-b", "book-c"]
                        """,
                        """
                        books: ["book-b", "book-a", "book-c"]
                        """
                )
        ).toArray(new YamlAssertion[0]);
    }

    @Test(dataProvider = "invalidYamlPairs")
    public void shouldBeInvalid(YamlAssertion jsonAssertion) {
        var validationItem = toValidationItem(jsonAssertion);
        fixture = new YamlNodeValidator(STRICT, context, Set.of());
        assertThatThrownBy(() -> fixture.validate(validationItem)).isInstanceOf(ValidationException.class);
    }

    @Test(dataProvider = "invalidYamlPairs")
    public void shouldBeInvalidIfNotStrict(YamlAssertion jsonAssertion) {
        var validationItem = toValidationItem(jsonAssertion);
        fixture = new YamlNodeValidator(NOT_STRICT, context, Set.of());
        assertThatThrownBy(() -> fixture.validate(validationItem)).isInstanceOf(ValidationException.class);
    }

    @DataProvider
    public static YamlAssertion[] invalidYamlPairs() {
        return List.of(
                new YamlAssertion(
                        """
                        myNumbers:
                          - 11
                          - 22
                          - 44
                        """,
                        """
                        myNumbers:
                          - 11
                          - 22
                          - 33
                        """,
                        "An item in '$['myNumbers']' is missing, expected '33' to be in '[11,22,44]'"
                ),
                new YamlAssertion(
                        """
                        text: "Hello World!"
                        index: 5
                        id: "x123456789x"
                        """,
                        """
                        text: "Hello World!"
                        index: 5
                        id: "x123456789x"
                        missing: "this is missing"
                        """,
                        "Number of entries is not equal in element: '$'",
                        "expected '[missing, index, text, id]' but was '[index, text, id]'"
                ),
                new YamlAssertion(
                        """
                        greetings:
                          - text: "Hello World!"
                            index: 1
                          - text: "Hallo Welt!"
                            index: 0
                          - text: "Hola del mundo!"
                            index: 3
                        id": "x123456789x"
                        """,
                        """
                        greetings:
                          - text: "Hello World!"
                            index: 1
                          - text: "Hallo Welt!"
                            index: 2
                          - text: "Hola del mundo!"
                            index: 3
                        id": "x123456789x"
                        """,
                        "An item in '$['greetings']' is missing, expected '{\"index\":2,\"text\":\"Hallo Welt!\"}' to be in '[{\"index\":1,\"text\":\"Hello World!\"},{\"index\":0,\"text\":\"Hallo Welt!\"},{\"index\":3,\"text\":\"Hola del mundo!\"}]'"
                ),
                new YamlAssertion(
                        """
                        numbers:
                          - 101
                          - 42
                        """,
                        """
                        numbers:
                          - 101
                          - 42
                          - 9000
                        """,
                        "Number of entries is not equal in element: '$['numbers']'",
                        "expected '[101,42,9000]' but was '[101,42]'"
                ),
                new YamlAssertion(
                        """
                        test: "Lorem"
                        """,
                        """
                        test: "@equalsIgnoreCase('lorem ipsum')@"
                        """,
                        "EqualsIgnoreCaseValidationMatcher failed for field 'test'",
                        "Received value is 'Lorem', control value is 'lorem ipsum'"
                ),
                new YamlAssertion(
                        """
                        not-test: "lorem"
                        """,
                        """
                        test: "lorem"
                        """,
                        "Missing JSON entry, expected 'test' to be in '[not-test]'"
                ),
                new YamlAssertion(
                        """
                        greetings:
                          - text: "Hello World!"
                            index: 1
                          - text: "Hallo Welt!"
                            index: 2
                          - text: "Hola del mundo!"
                            index: 3
                        id: "x123456789x"
                        """,
                        """
                        greetings:
                          text: "Hello World!"
                          index: 1
                        id: "x123456789x"
                        """,
                        "expected 'JSONObject'",
                        "but was 'JSONArray'"
                ),
                new YamlAssertion(
                        """
                        text: "Hello World!"
                        index: 5
                        id: "x123456789x"
                        """,
                        """
                        text: "Hello World!"
                        index: 5
                        id: null
                        """,
                        "expected 'null' but was 'x123456789x'"
                ),
                new YamlAssertion(
                        """
                        text: "Hello World!"
                        index: 5
                        id: "wrong"
                        """,
                        """
                        text: "Hello World!"
                        index: 5
                        id: "x123456789x"
                        """,
                        "expected 'x123456789x'",
                        "but was 'wrong'"
                ),
                new YamlAssertion(
                        """
                        text: "Hello World!"
                        person:
                          name: "John"
                          surname: "wrong"
                        index: 5
                        id: "x123456789x"
                        """,
                        """
                        text: "Hello World!"
                        person:
                          name: "John"
                          surname: "Doe"
                        index: 5
                        id: "x123456789x"
                        """,
                        "expected 'Doe'",
                        "but was 'wrong'"
                ),
                new YamlAssertion(
                        """
                        greetings: 
                          text: "Hello World!"
                          index: 1
                        id: "x123456789x"
                        """,
                        """
                        greetings:
                          - text: "Hello World!"
                            index: 1
                          - text: "Hallo Welt!"
                            index: 2
                          - text: "Hola del mundo!"
                            index: 3
                        id: "x123456789x"
                        """,
                        "expected 'JSONArray'",
                        "but was 'JSONObject'"
                ),
                new YamlAssertion(
                        """
                        - 1
                        - 2
                        - 2
                        - 1
                        """,
                        """
                        - 1
                        - 1
                        - 2
                        - 1
                        """
                ),
                new YamlAssertion(
                        """
                        - 1
                        """,
                        """
                        - 1
                        - 1
                        """
                )
        ).toArray(new YamlAssertion[0]);
    }

    @Test(dataProvider = "validOnlyWithIgnoreExpressions")
    public void shouldBeValidOnlyWithIgnoreExpressions(YamlAssertion jsonAssertion) {
        var validationItem = toValidationItem(jsonAssertion);
        fixture = new YamlNodeValidator(NOT_STRICT, context, jsonAssertion.ignoreExpressions);
        assertThatNoException().isThrownBy(() -> fixture.validate(validationItem));
    }

    @Test(dataProvider = "validOnlyWithIgnoreExpressions")
    public void shouldBeInvalidWithoutIgnoreExpressions(YamlAssertion jsonAssertion) {
        var validationItem = toValidationItem(jsonAssertion);
        fixture = new YamlNodeValidator(NOT_STRICT, context, Set.of());
        assertThatThrownBy(() -> fixture.validate(validationItem)).isInstanceOf(ValidationException.class);
    }

    @DataProvider
    public static YamlAssertion[] validOnlyWithIgnoreExpressions() {
        return List.of(
                new YamlAssertion(
                        """
                        text: "Hello World!"
                        index: 5
                        object:
                          id: "x123456789x"
                        greetings:
                          - text: "Hello World!"
                            index: 1
                          - text: "Hallo Welt!"
                            index: 2
                          - text: "Hola del mundo!"
                            index: 3
                        """,
                        """
                        text: "Hello World!"
                        index: "?"
                        object:
                          id: "?"
                        greetings: "?"
                        """,
                        Set.of("$.index", "$.object.id", "$.greetings")
                ),
                new YamlAssertion(
                        """
                        index: "bliblablu"
                        """,
                        """
                        index: "tataa"
                        """,
                        Set.of("$.index")
                ),
                new YamlAssertion(
                        """
                        index:
                          anything:
                            - 0
                        """,
                        """
                        index:
                          anything:
                            - 55
                            - 66
                            - 77
                        """,
                        Set.of("$.index.anything")
                )
        ).toArray(new YamlAssertion[0]);
    }

    private static YamlNodeValidatorItem<Object> toValidationItem(YamlAssertion jsonAssertion) {
        YamlNodeValidatorItem<Iterable<?>> documents = YamlSupport.parseYaml(jsonAssertion.actual, jsonAssertion.expected);
        if (!documents.expected.iterator().hasNext() && !documents.actual.iterator().hasNext()) {
            return new YamlNodeValidatorItem<>("$", Collections.emptyMap(), Collections.emptyMap());
        }

        return new YamlNodeValidatorItem<>("$", documents.actual.iterator().next(), documents.expected.iterator().next());
    }

    public record YamlAssertion (
            String actual,
            String expected,
            Set<String> ignoreExpressions,
            String... messageContains
    ) {
        public YamlAssertion(String actual, String expected, String... messageContains) {
            this(actual, expected, Set.of(), messageContains);
        }
    }
}
