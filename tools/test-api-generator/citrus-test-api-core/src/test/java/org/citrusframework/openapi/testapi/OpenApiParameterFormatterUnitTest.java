package org.citrusframework.openapi.testapi;

import java.util.List;
import java.util.stream.Stream;

import org.citrusframework.openapi.testapi.RestApiSendMessageActionBuilder.ParameterData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.citrusframework.openapi.testapi.OpenApiParameterFormatter.formatAccordingToStyle;
import static org.citrusframework.openapi.testapi.ParameterStyle.DEEPOBJECT;
import static org.citrusframework.openapi.testapi.ParameterStyle.FORM;
import static org.citrusframework.openapi.testapi.ParameterStyle.LABEL;
import static org.citrusframework.openapi.testapi.ParameterStyle.MATRIX;
import static org.citrusframework.openapi.testapi.ParameterStyle.PIPEDELIMITED;
import static org.citrusframework.openapi.testapi.ParameterStyle.SIMPLE;
import static org.citrusframework.openapi.testapi.ParameterStyle.SPACEDELIMITED;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class OpenApiParameterFormatterUnitTest {

    private static final User USER = new User("admin", "Alex");
    private static final List<Integer> LIST = List.of(3, 4, 5);
    private static final List<String> COLOR_NAMES = List.of("blue", "black", "brown");
    private static final Color COLOR = new Color(100,200,150);
    private static final Integer SINGLE = 5;

    static Stream<Arguments> formatSimple() {
        return Stream.of(
            arguments("single/non exploded/non object/single", new ParameterData("id", SINGLE, SIMPLE, false, false), "id=5"),
            arguments("list/non exploded/non object/array", new ParameterData("id", LIST, SIMPLE, false, false), "id=3,4,5"),
            arguments("user/non exploded/Object/single", new ParameterData("id", USER, SIMPLE, false, true), "id=firstName,Alex,role,admin"),
            arguments("single/exploded/non object/single", new ParameterData("id", SINGLE, SIMPLE, true, false), "id=5"),
            arguments("list/exploded/non object/array", new ParameterData("id", LIST, SIMPLE, true, false), "id=3,4,5"),
            arguments("user/exploded/Object/single", new ParameterData("id", USER, SIMPLE, true, true), "id=firstName=Alex,role=admin"),
            arguments("color/non exploded/non object/array", new ParameterData("color", COLOR_NAMES, SIMPLE, false, false), "color=blue,black,brown"),
            arguments("color/exploded/non object/array", new ParameterData("color", COLOR_NAMES, SIMPLE, true, false), "color=blue,black,brown"),
            arguments("color/non exploded/object/array", new ParameterData("color", COLOR, SIMPLE, false, true), "color=b,150,g,200,r,100"),
            arguments("color/exploded/object/array", new ParameterData("color", COLOR, SIMPLE, true, true), "color=b=150,g=200,r=100")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void formatSimple(String name, ParameterData parameterData, String expected) {
        format(parameterData, expected);
    }

    static Stream<Arguments> formatMatrix() {
        return Stream.of(
            arguments("single/non exploded/non object/single", new ParameterData("id", SINGLE, MATRIX, false, false), "id=;id=5"),
            arguments("list/non exploded/non object/array", new ParameterData("id", LIST, MATRIX, false, false), "id=;id=3,4,5"),
            arguments("user/non exploded/object/single", new ParameterData("id", USER, MATRIX, false, true), "id=;id=firstName,Alex,role,admin"),
            arguments("single/exploded/non object/single", new ParameterData("id", SINGLE, MATRIX, true, false), "id=;id=5"),
            arguments("list/exploded/non object/array", new ParameterData("id", LIST, MATRIX, true, false), "id=;id=3;id=4;id=5"),
            arguments("user/exploded/object/single", new ParameterData("id", USER, MATRIX, true, true), "id=;firstName=Alex;role=admin"),
            arguments("color/non exploded/non object/array", new ParameterData("color", COLOR_NAMES, MATRIX, false, false), "color=;color=blue,black,brown"),
            arguments("color/exploded/non object/array", new ParameterData("color", COLOR_NAMES, MATRIX, true, false), "color=;color=blue;color=black;color=brown"),
            arguments("color/non exploded/object/single", new ParameterData("color", COLOR, MATRIX, false, true), "color=;color=b,150,g,200,r,100"),
            arguments("color/exploded/object/single", new ParameterData("color", COLOR, MATRIX, true, true), "color=;b=150;g=200;r=100")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void formatMatrix(String name, ParameterData parameterData, String expected) {
        format(parameterData, expected);
    }

    static Stream<Arguments> formatLabel() {
        return Stream.of(
            arguments("single/non exploded/non object/single", new ParameterData("id", SINGLE, LABEL, false, false), "id=.5"),
            arguments("list/non exploded/non object/array", new ParameterData("id", LIST, LABEL, false, false), "id=.3,4,5"),
            arguments("user/non exploded/Object/single", new ParameterData("id", USER, LABEL, false, true), "id=.firstName,Alex,role,admin"),
            arguments("single/exploded/non object/single", new ParameterData("id", SINGLE, LABEL, true, false), "id=.5"),
            arguments("list/exploded/non object/array", new ParameterData("id", LIST, LABEL, true, false), "id=.3.4.5"),
            arguments("user/exploded/Object/single", new ParameterData("id", USER, LABEL, true, true), "id=.firstName=Alex.role=admin"),
            arguments("color/non exploded/non object/array", new ParameterData("color", COLOR_NAMES, LABEL, false, false), "color=.blue,black,brown"),
            arguments("color/exploded/non object/array", new ParameterData("color", COLOR_NAMES, LABEL, true, false), "color=.blue.black.brown"),
            arguments("color/non exploded/object/array", new ParameterData("color", COLOR_NAMES, LABEL, false, true), "color=.blue,black,brown"),
            arguments("color/exploded/object/array", new ParameterData("color", COLOR, LABEL, true, true), "color=.b=150.g=200.r=100")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void formatLabel(String name, ParameterData parameterData, String expected) {
        format(parameterData, expected);
    }

    static Stream<Arguments> formatForm() {
        return Stream.of(
            arguments("single/non exploded/non object/single", new ParameterData("id", SINGLE, FORM, false, false), "id=5"),
            arguments("list/non exploded/non object/array", new ParameterData("id", LIST, FORM, false, false), "id=3,4,5"),
            arguments("user/non exploded/object/single", new ParameterData("id", USER, FORM, false, true), "id=firstName,Alex,role,admin"),
            arguments("single/exploded/non object/single", new ParameterData("id", SINGLE, FORM, true, false), "id=5"),
            arguments("list/exploded/non object/array", new ParameterData("id", LIST, FORM, true, false), "id=3&id=4&id=5"),
            arguments("user/exploded/object/single", new ParameterData("id", USER, FORM, true, true), "firstName=Alex&role=admin"),
            arguments("color/non exploded/non object/array", new ParameterData("color", COLOR_NAMES, FORM, false, false), "color=blue,black,brown"),
            arguments("color/exploded/non object/array", new ParameterData("color", COLOR_NAMES, FORM, true, false), "color=blue&color=black&color=brown"),
            arguments("color/non exploded/object/array", new ParameterData("color", COLOR_NAMES, FORM, false, true), "color=blue,black,brown"),
            arguments("color/exploded/object/single", new ParameterData("color", COLOR, FORM, true, true), "b=150&g=200&r=100")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void formatForm(String name, ParameterData parameterData, String expected) {
        format(parameterData, expected);
    }

    static Stream<Arguments> formatSpaceDelimited() {
        return Stream.of(
            arguments("SpaceDelimited/non exploded/non object/array", new ParameterData("color", COLOR_NAMES, SPACEDELIMITED, false, false), "color=blue%20black%20brown"),
            arguments("SpaceDelimited/exploded/non object/array", new ParameterData("color", COLOR_NAMES, SPACEDELIMITED, false, true), "color=blue%20black%20brown")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void formatSpaceDelimited(String name, ParameterData parameterData, String expected) {
        format(parameterData, expected);
    }

    static Stream<Arguments> formatPipeDelimited() {
        return Stream.of(
            arguments("PipeDelimited/non exploded/non object/array", new ParameterData("color", COLOR_NAMES, PIPEDELIMITED, false, false), "color=blue%7Cblack%7Cbrown"),
            arguments("PipeDelimited/exploded/non object/array", new ParameterData("color", COLOR_NAMES, PIPEDELIMITED, false, true), "color=blue%7Cblack%7Cbrown")
        );
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource
    void formatPipeDelimited(String name, ParameterData parameterData, String expected) {
        format(parameterData, expected);
    }

    static Stream<Arguments> formatDeepObject() {
        return Stream.of(
            arguments("DeepObject/exploded/object/single", new ParameterData("id", USER,
                DEEPOBJECT, true, true), "id[firstName]=Alex&id[role]=admin")
        );
    }
    @ParameterizedTest(name = "{0}")
    @MethodSource
    void formatDeepObject(String name, ParameterData parameterData, String expected) {
        format(parameterData, expected);
    }

    void format(ParameterData parameterData, String expected) {
        assertThat(
            formatAccordingToStyle(
                parameterData.name(),
                parameterData.value(),
                parameterData.parameterStyle(),
                parameterData.explode(),
                parameterData.isObject()))
            .isEqualTo(expected);
    }

    private record User(String role, String firstName) {

        // Used for formatting
        @SuppressWarnings({"unused"})
        public String getRole() {
            return role;
        }

        // Used for formatting
        @SuppressWarnings({"unused"})
        public String getFirstName() {
            return firstName;
        }
    }

    private record Color(int r, int g, int b) {

        @SuppressWarnings({"unused"})
        public int getR() {
            return r;
        }

        @SuppressWarnings({"unused"})
        public int getG() {
            return g;
        }

        @SuppressWarnings({"unused"})
        public int getB() {
            return b;
        }
    }
}
