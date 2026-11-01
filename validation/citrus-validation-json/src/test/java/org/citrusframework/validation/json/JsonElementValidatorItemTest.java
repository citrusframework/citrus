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

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonElementValidatorItemTest {

    static Stream<Arguments> getPathPairs() {
        return Stream.of(
                Arguments.of("$['propertyA']", new JsonElementValidatorItem<>("propertyA", "", "")),
                Arguments.of("$['propertyA']", new JsonElementValidatorItem<>("propertyA", "", "").parent(
                        new JsonElementValidatorItem<>(null, "", "")
                )),
                Arguments.of("$['propertyA']['propertyB']", new JsonElementValidatorItem<>("propertyB", "", "").parent(
                        new JsonElementValidatorItem<>("propertyA", "", "").parent(
                                new JsonElementValidatorItem<>(null, "", "")
                        )
                )),
                Arguments.of("$['propertyA'][1]", new JsonElementValidatorItem<>(1, "", "").parent(
                        new JsonElementValidatorItem<>("propertyA", "", "").parent(
                                new JsonElementValidatorItem<>(null, "", "")
                        )
                )),
                Arguments.of("$[1]", new JsonElementValidatorItem<>(1, "", "").parent(
                        new JsonElementValidatorItem<>(null, "", "")
                ))
        );
    }

    @ParameterizedTest
    @MethodSource("getPathPairs")
    void shouldGetJsonPath(String expectedPath, JsonElementValidatorItem<?> fixture) {
        assertThat(fixture.getJsonPath()).isEqualTo(expectedPath);
    }

    static Stream<Arguments> getNamePairs() {
        return Stream.of(
                Arguments.of("$", new JsonElementValidatorItem<>(null, "", "")),
                Arguments.of("propertyA", new JsonElementValidatorItem<>("propertyA", "", "")),
                Arguments.of("[2]", new JsonElementValidatorItem<>(2, "", ""))
        );
    }

    @ParameterizedTest
    @MethodSource("getNamePairs")
    void shouldGetName(String expectedPath, JsonElementValidatorItem<?> fixture) {
        assertThat(fixture.getName()).isEqualTo(expectedPath);
    }
}
