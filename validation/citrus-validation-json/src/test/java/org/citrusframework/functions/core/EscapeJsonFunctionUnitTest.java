package org.citrusframework.functions.core;

import org.citrusframework.context.TestContext;
import org.citrusframework.functions.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class EscapeJsonFunctionUnitTest {
    @Mock
    private TestContext context;
    private static final EscapeJsonFunction function = new EscapeJsonFunction();
    @Test
    void isCitrusFunction() {
        assertThat(function)
                .isInstanceOf(Function.class);
    }
    private static Stream<Arguments> testChangeParameter() {
        return Stream.of(
                arguments("{\"mySuperJson\": \"[{\"pippin\":\"nooooo\"}, {\"gandalf\":\"fly you fools\"}]\"}",
                        "{\\\"mySuperJson\\\": \\\"[{\\\"pippin\\\":\\\"nooooo\\\"}, {\\\"gandalf\\\":\\\"fly you fools\\\"}]\\\"}"),
                arguments("{\"mySuperJson\": \"{\"pippin\":\"nooooo\"}\"}", "{\\\"mySuperJson\\\": \\\"{\\\"pippin\\\":\\\"nooooo\\\"}\\\"}"),
                arguments("{\"mySuperJson\": \"nooooo\"}", "{\\\"mySuperJson\\\": \\\"nooooo\\\"}"),
                arguments("[{\"mySuperJson\": \"nooooo\"},{\"mySuperJson2\": \"nooooo\"}]", "[{\\\"mySuperJson\\\": \\\"nooooo\\\"},{\\\"mySuperJson2\\\": \\\"nooooo\\\"}]"),
                arguments("{}", "{}"));
    }
    @ParameterizedTest
    @MethodSource
    void testChangeParameter(String string, String expectedResult) {
        String newValue = function.execute(List.of(string), context);
        assertEquals(expectedResult, newValue);
    }
    private static Stream<Arguments> testMalformedParameterList() {
        return Stream.of(
                arguments(emptyList()),
                arguments(singletonList("")),
                arguments(List.of("rip_bozo", ""))
        );
    }
    @ParameterizedTest
    @MethodSource
    void testMalformedParameterList(List<String> parameters) {
        assertThrows(Exception.class, () -> function.execute(parameters, context));
    }
}