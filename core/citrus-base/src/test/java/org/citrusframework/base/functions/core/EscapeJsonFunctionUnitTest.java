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

package org.citrusframework.base.functions.core;

import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.functions.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class EscapeJsonFunctionUnitTest {

    @Mock
    private TestContext context;

    private EscapeJsonFunction fixture;

    @BeforeEach
    public void beforeEach() {
        fixture = new EscapeJsonFunction();
    }

    @Test
    public void isCitrusFunction() {
        assertThat(fixture)
                .isInstanceOf(Function.class);
    }

    public static Object[][] testChangeParameter() {
        return new Object[][]{
                {"{\"mySuperJson\": \"[{\"pippin\":\"nooooo\"}, {\"gandalf\":\"fly you fools\"}]\"}", "{\\\"mySuperJson\\\": \\\"[{\\\"pippin\\\":\\\"nooooo\\\"}, {\\\"gandalf\\\":\\\"fly you fools\\\"}]\\\"}"},
                {"{\"mySuperJson\": \"{\"pippin\":\"nooooo\"}\"}", "{\\\"mySuperJson\\\": \\\"{\\\"pippin\\\":\\\"nooooo\\\"}\\\"}"},
                {"{\"mySuperJson\": \"nooooo\"}", "{\\\"mySuperJson\\\": \\\"nooooo\\\"}"},
                {"[{\"mySuperJson\": \"nooooo\"},{\"mySuperJson2\": \"nooooo\"}]", "[{\\\"mySuperJson\\\": \\\"nooooo\\\"},{\\\"mySuperJson2\\\": \\\"nooooo\\\"}]"},
                {"{}", "{}"}
        };
    }

    @ParameterizedTest
    @MethodSource("testChangeParameter")
    public void testChangeParameter(String string, String expectedResult) {
        String newValue = fixture.execute(List.of(string), context);
        assertEquals(expectedResult, newValue);
    }

    public static Object[][] testMalformedParameterListProvider() {
        return new Object[][]{
                {emptyList()},
                {singletonList("")},
                {List.of("rip_bozo", "")}
        };
    }

    @ParameterizedTest
    @MethodSource("testMalformedParameterListProvider")
    public void testMalformedParameterList(List<String> parameters) {
        assertThrows(Exception.class, () -> fixture.execute(parameters, context));
    }
}
