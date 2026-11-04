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

package org.citrusframework.kubernetes.config;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class CredentialValidatorTest {

    private static final String IS_VALID_DATA_PROVIDER = "isValidData";

    static Stream<Arguments> isValidData() {
        return Stream.of(
                Arguments.of("user1", "pass1", null, true),
                Arguments.of(null, null, "token1", true),
                Arguments.of("user1", null, "token1", false),
                Arguments.of(null, "pass1", "token1", false),
                Arguments.of("user1", "pass1", "token1", false),
                Arguments.of(null, "pass1", null, false),
                Arguments.of(null, null, null, true)
        );
    }

    @ParameterizedTest(name = "{0},{1},{2}")
    @MethodSource(IS_VALID_DATA_PROVIDER)
    public void isValid(String username, String password, String oauthToken, boolean expectedResult) {
        Assertions.assertEquals(expectedResult, CredentialValidator.isValid(username, password, oauthToken));
    }}
