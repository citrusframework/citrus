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

package org.citrusframework.validation.json;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.ValidationException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

import static net.minidev.json.parser.JSONParser.DEFAULT_PERMISSIVE_MODE;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JsonElementValidatorTest extends UnitTestSupport {

    public static final boolean NOT_STRICT = false;
    public static final boolean STRICT = true;

    public static final boolean CHECK_ARRAY_ORDER = true;
    public static final boolean DO_NOT_CHECK_ARRAY_ORDER = false;

    JsonElementValidator fixture;

    @Test(dataProvider = "validJsonPairsIfNotStrict")
    public void shouldBeValidIfNotStrict(JsonAssertion jsonAssertion) {
        var validationItem = toValidationItem(jsonAssertion);
        fixture = new JsonElementValidator(NOT_STRICT, context, Set.of());
        assertThatNoException().isThrownBy(() -> fixture.validate(validationItem));
    }

    @Test(dataProvider = "validJsonPairsIfNotStrict")
    public void shouldBeInvalidIfStrict(JsonAssertion jsonAssertion) {
        var validationItem = toValidationItem(jsonAssertion);
        fixture = new JsonElementValidator(STRICT, context, Set.of());
        assertThatThrownBy(() -> fixture.validate(validationItem)).isInstanceOf(ValidationException.class);
    }

    @DataProvider
    public static JsonAssertion[] validJsonPairsIfNotStrict() {
        return List.of(
                new JsonAssertion(
                        "{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}",
                        "{\"id\":\"x123456789x\"}"
                ),
                new JsonAssertion(
                        "[{\"text\":\"Hello World!\", \"index\":1}, {\"text\":\"Hallo Welt!\", \"index\":2}, {\"text\":\"Hola del mundo!\", \"index\":3}]",
                        "[{\"text\":\"Hello World!\", \"index\":1}]"
                ),
                new JsonAssertion(
                        "[{\"text\":\"Hello World!\", \"index\":1}, {\"text\":\"Hallo Welt!\", \"index\":2}, {\"text\":\"Hola del mundo!\", \"index\":3}]",
                        "[{\"text\":\"Hallo Welt!\", \"index\":2}]"
                ),
                new JsonAssertion(
                        "[{\"text\":\"Hello World!\", \"index\":1}, {\"text\":\"Hallo Welt!\", \"index\":2}, {\"text\":\"Hola del mundo!\", \"index\":3}]",
                        "[{\"index\": 1}]"
                ),
                new JsonAssertion(
                        "[1, 2, 3]",
                        "[2] "
                )
        ).toArray(new JsonAssertion[0]);
    }

    @Test(dataProvider = "validIfStrict")
    public void shouldBeValidIfStrict(JsonAssertion jsonAssertion) {
        var validationItem = toValidationItem(jsonAssertion);
        fixture = new JsonElementValidator(STRICT, context, Set.of());
        assertThatNoException().isThrownBy(() -> fixture.validate(validationItem));
    }

    @DataProvider
    public static JsonAssertion[] validIfStrict() {
        return List.of(
                new JsonAssertion(
                        "{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}",
                        "{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}"
                ),
                new JsonAssertion(
                        "{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}",
                        "{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}"
                ),
                new JsonAssertion(
                        "[{\"text\":\"Hello World!\", \"index\":1}, {\"text\":\"Hallo Welt!\", \"index\":2}, {\"text\":\"Hola del mundo!\", \"index\":3}]",
                        "[{\"text\":\"Hello World!\", \"index\":1}, {\"text\":\"Hallo Welt!\", \"index\":2}, {\"text\":\"Hola del mundo!\", \"index\":3}]"
                ),
                new JsonAssertion(
                        "[1, {\"text\":\"Hallo Welt!\", \"index\":2}, \"pizza\"]",
                        "[1,{\"text\":\"Hallo Welt!\", \"index\":2},\"pizza\"]"
                ),
                new JsonAssertion(
                        "{\"greetings\":[{\"text\":\"Hello World!\", \"index\":1}, {\"text\":\"Hallo Welt!\", \"index\":2}, {\"text\":\"Hola del mundo!\", \"index\":3}], \"id\":\"x123456789x\"}",
                        "{\"greetings\":[{\"text\":\"Hello World!\", \"index\":1}, {\"text\":\"Hallo Welt!\", \"index\":2}, {\"text\":\"Hola del mundo!\", \"index\":3}], \"id\":\"x123456789x\"}"
                ),
                new JsonAssertion(
                        "{\"text\":\"Hello World!\", \"index\":5, \"object\":{\"id\":\"x123456789x\"}, \"greetings\":[{\"text\":\"Hello World!\", \"index\":1}, {\"text\":\"Hallo Welt!\", \"index\":2}, {\"text\":\"Hola del mundo!\", \"index\":3}],}",
                        "{\"text\":\"Hello World!\", \"index\":\"@ignore@\", \"object\":{\"id\":\"@ignore@\"}, \"greetings\":\"@ignore@\"}"
                ),
                new JsonAssertion(
                        "{\"text\":\"Hello World!\", \"index\":5, \"id\":null}",
                        "{\"text\":\"Hello World!\", \"index\":5, \"id\":null}"
                ),
                new JsonAssertion(
                        "",
                        ""
                ),
                new JsonAssertion(
                        "{\"id\":42}",
                        "{\"id\":42}"
                ),
                new JsonAssertion(
                        "{\"test\": \"Lorem\"}",
                        "{\"test\": \"@equalsIgnoreCase('lorem')@\"}"
                ),
                new JsonAssertion(
                        "[1, 2, 3]",
                        "[1, 2, 3]"
                ),
                new JsonAssertion(
                        "{ \"books\": [\"book-a\", \"book-b\", \"book-c\"] }",
                        "{ \"books\": [\"book-a\", \"book-b\", \"book-c\"] }"
                )
        ).toArray(new JsonAssertion[0]);
    }


    @Test(dataProvider = "invalidJsonPairs")
    public void shouldBeInvalid(JsonAssertion jsonAssertion) {
        var validationItem = toValidationItem(jsonAssertion);
        fixture = new JsonElementValidator(STRICT, context, Set.of());
        assertThatThrownBy(() -> fixture.validate(validationItem))
                .isInstanceOf(ValidationException.class)
                .hasMessageContainingAll(jsonAssertion.messageContains());
    }

    @DataProvider
    public static JsonAssertion[] invalidJsonPairs() {
        return List.of(
                new JsonAssertion(
                        "{\"myNumbers\": [11, 22, 44]}",
                        "{\"myNumbers\": [11, 22, 33]}",
                        "Elements not equal for array '$['myNumbers']' at position 2, expected '33' but was '44'"),
                new JsonAssertion(
                        "{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}",
                        "{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\", \"missing\":\"this is missing\"}",
                        "Number of entries is not equal in element: '$'",
                        "expected '[missing, index, text, id]' but was '[index, text, id]'"),
                new JsonAssertion(
                        "{\"greetings\":[{\"text\":\"Hello World!\", \"index\":1}, {\"text\":\"Hallo Welt!\", \"index\":0}, {\"text\":\"Hola del mundo!\", \"index\":3}], \"id\":\"x123456789x\"}",
                        "{\"greetings\":[{\"text\":\"Hello World!\", \"index\":1}, {\"text\":\"Hallo Welt!\", \"index\":2}, {\"text\":\"Hola del mundo!\", \"index\":3}], \"id\":\"x123456789x\"}",
                        "Elements not equal for array '$['greetings']' at position 1, expected '{\"index\":2,\"text\":\"Hallo Welt!\"}' but was '{\"index\":0,\"text\":\"Hallo Welt!\"}'"),
                new JsonAssertion(
                        "{\"numbers\":[101, 42]}",
                        "{\"numbers\":[101, 42, 9000]}",
                        "Number of entries is not equal in element: '$['numbers']'",
                        "expected '[101,42,9000]' but was '[101,42]'"),
                new JsonAssertion(
                        "{\"test\": \"Lorem\"}",
                        "{\"test\": \"@equalsIgnoreCase('lorem ipsum')@\"}",
                        "EqualsIgnoreCaseValidationMatcher failed for field '$['test']'",
                        "Received value is 'Lorem'", "control value is 'lorem ipsum'."),
                new JsonAssertion(
                        "{\"not-test\": \"lorem\"}",
                        "{\"test\": \"lorem\"}",
                        "Missing JSON entry, expected 'test' to be in '[not-test]'"),
                new JsonAssertion(
                        "{\"greetings\":[{\"text\":\"Hello World!\", \"index\":1}, {\"text\":\"Hallo Welt!\", \"index\":2}, {\"text\":\"Hola del mundo!\", \"index\":3}], \"id\":\"x123456789x\"}",
                        "{\"greetings\":{\"text\":\"Hello World!\", \"index\":1}, \"id\":\"x123456789x\"}",
                        "expected 'JSONObject'",
                        "but was 'JSONArray'"),
                new JsonAssertion(
                        "{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}",
                        "{\"text\":\"Hello World!\", \"index\":5, \"id\":null}",
                        "expected 'null' but was 'x123456789x'"),
                new JsonAssertion(
                        "{\"text\":\"Hello World!\", \"index\":5, \"id\":\"wrong\"}",
                        "{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}",
                        "expected 'x123456789x'",
                        "but was 'wrong'"),
                new JsonAssertion(
                        "{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"wrong\"}, \"index\":5, \"id\":\"x123456789x\"}",
                        "{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}",
                        "expected 'Doe'",
                        "but was 'wrong'"),
                new JsonAssertion(
                        "{\"greetings\":{\"text\":\"Hello World!\", \"index\":1}, \"id\":\"x123456789x\"}",
                        "{\"greetings\":[{\"text\":\"Hello World!\", \"index\":1}, {\"text\":\"Hallo Welt!\", \"index\":2}, {\"text\":\"Hola del mundo!\", \"index\":3}], \"id\":\"x123456789x\"}",
                        "expected 'JSONArray'",
                        "but was 'JSONObject'"),
                new JsonAssertion(
                        "[\"a\", \"c\", \"b\"]",
                        "[\"a\", \"b\", \"c\"]",
                        "Elements not equal for array '$' at position 1, expected 'b' but was 'c'")
        ).toArray(new JsonAssertion[0]);
    }

    @Test(dataProvider = "validOnlyWithIgnoreExpressions")
    public void shouldBeValidOnlyWithIgnoreExpressions(JsonAssertion jsonAssertion) {
        var validationItem = toValidationItem(jsonAssertion);
        fixture = new JsonElementValidator(NOT_STRICT, context, jsonAssertion.ignoreExpressions);
        assertThatNoException().isThrownBy(() -> fixture.validate(validationItem));
    }

    @Test(dataProvider = "validOnlyWithIgnoreExpressions")
    public void shouldBeInvalidWithoutIgnoreExpressions(JsonAssertion jsonAssertion) {
        var validationItem = toValidationItem(jsonAssertion);
        fixture = new JsonElementValidator(NOT_STRICT, context, Set.of());
        assertThatThrownBy(() -> fixture.validate(validationItem)).isInstanceOf(ValidationException.class);
    }

    @DataProvider
    public static JsonAssertion[] validOnlyWithIgnoreExpressions() {
        return List.of(
                new JsonAssertion(
                        "{\"text\":\"Hello World!\", \"index\":5, \"object\":{\"id\":\"x123456789x\"}, \"greetings\":[{\"text\":\"Hello World!\", \"index\":1}, {\"text\":\"Hallo Welt!\", \"index\":2}, {\"text\":\"Hola del mundo!\", \"index\":3}],}",
                        "{\"text\":\"Hello World!\", \"index\":\"?\", \"object\":{\"id\":\"?\"}, \"greetings\":\"?\"}",
                        Set.of("$..index", "$.object.id", "$.greetings")
                ),
                new JsonAssertion(
                        "{\"index\":\"bliblablu\"}",
                        "{\"index\":\"tataa\"}",
                        Set.of("$..index")
                ),
                new JsonAssertion(
                        "{\"index\": {\"anything\": [0]} }",
                        "{\"index\": {\"anything\": [55, 66, 77]} }",
                        Set.of("$.index['anything'][*]")
                ),
                new JsonAssertion(
                        "{\"index\": {\"anything\": [0], \"something\": null} }",
                        "{\"index\": {\"anything\": [55, 66, 77]} }",
                        Set.of("$.index['anything'][*]")
                )
        ).toArray(new JsonAssertion[0]);
    }

    @Test
    void validate_shouldCheckArrayOrder_whenPropertyTrue() {
        var validItem = toValidationItem(new JsonAssertion(
                "[\"a\", \"b\", \"c\"]",
                "[\"a\", \"b\", \"c\"]"));

        fixture = new JsonElementValidator(STRICT, context, Set.of(), true);
        assertThatNoException().isThrownBy(() -> fixture.validate(validItem));

        var invalidItem = toValidationItem(new JsonAssertion(
                "[\"a\", \"b\", \"c\"]",
                "[\"c\", \"a\", \"b\"]"));

        fixture = new JsonElementValidator(STRICT, context, Set.of(), true);
        assertThatThrownBy(() -> fixture.validate(invalidItem))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Elements not equal for array");
    }

    @Test
    void validate_shouldNotCheckArrayOrder_whenPropertyFalse() {
        var invalidItem = toValidationItem(new JsonAssertion(
                "[\"a\", \"b\", \"c\"]",
                "[\"c\", \"a\", \"b\"]"));

        fixture = new JsonElementValidator(STRICT, context, Set.of(), false);
        assertThatNoException().isThrownBy(() -> fixture.validate(invalidItem));
    }

    @Test
    void validate_shouldCheckArrayOrder_whenPropertyNotSet_andStrict() {
        var validItem = toValidationItem(new JsonAssertion(
                "[\"a\", \"b\", \"c\"]",
                "[\"a\", \"b\", \"c\"]"));

        fixture = new JsonElementValidator(STRICT, context, Set.of());
        assertThatNoException().isThrownBy(() -> fixture.validate(validItem));

        var invalidItem = toValidationItem(new JsonAssertion(
                "[\"a\", \"b\", \"c\"]",
                "[\"c\", \"a\", \"b\"]"));

        fixture = new JsonElementValidator(STRICT, context, Set.of());
        assertThatThrownBy(() -> fixture.validate(invalidItem))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Elements not equal for array");
    }

    @Test
    void validate_shouldNotCheckArrayOrder_whenPropertyNotSet_andNotStrict() {
        var validationItem = toValidationItem(new JsonAssertion(
                "[\"a\", \"b\", \"c\"]",
                "[\"c\", \"a\", \"b\"]"));

        fixture = new JsonElementValidator(NOT_STRICT, context, Set.of());
        assertThatNoException().isThrownBy(() -> fixture.validate(validationItem));
    }

    private static JsonElementValidatorItem<Object> toValidationItem(JsonAssertion jsonAssertion) {
        return JsonElementValidatorItem.parseJson(DEFAULT_PERMISSIVE_MODE, jsonAssertion.actual, jsonAssertion.expected);
    }

    private record JsonAssertion(
            String actual,
            String expected,
            Set<String> ignoreExpressions,
            String... messageContains
    ) {
        public JsonAssertion(String actual, String expected) {
            this(actual, expected, (String[]) null);
        }

        public JsonAssertion(String actual, String expected, String... messageContains) {
            this(actual, expected, Set.of(), messageContains);
        }
    }
}
