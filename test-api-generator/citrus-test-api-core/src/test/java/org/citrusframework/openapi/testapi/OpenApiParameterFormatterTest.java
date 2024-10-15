package org.citrusframework.openapi.testapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.citrusframework.openapi.testapi.OpenApiParameterFormatter.formatArray;
import static org.citrusframework.openapi.testapi.ParameterStyle.DEEPOBJECT;
import static org.citrusframework.openapi.testapi.ParameterStyle.FORM;
import static org.citrusframework.openapi.testapi.ParameterStyle.LABEL;
import static org.citrusframework.openapi.testapi.ParameterStyle.MATRIX;
import static org.citrusframework.openapi.testapi.ParameterStyle.SIMPLE;

import java.util.List;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.citrusframework.openapi.testapi.RestApiSendMessageActionBuilder.ParameterData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class OpenApiParameterFormatterTest {

    private static final User USER = User.user().role("admin").firstName("Alex").build();
    private static final List<Integer> LIST = List.of(3,4,5);
    private static final Integer SINGLE = 5;

    static Stream<Arguments> format() {
        return Stream.of(
            Arguments.arguments("Simple/non exploded/non object/single", new ParameterData("id", SINGLE, SIMPLE, false, false), "5"),
            Arguments.arguments("Simple/non exploded/non object/array",  new ParameterData("id", LIST,   SIMPLE, false, false), "3,4,5"),
            Arguments.arguments("Simple/non exploded/Object/single",     new ParameterData("id", USER,   SIMPLE, false, true) , "firstName,Alex,role,admin"),
            Arguments.arguments("Simple/exploded/non object/single",     new ParameterData("id", SINGLE, SIMPLE, true, false), "5"),
            Arguments.arguments("Simple/exploded/non object/array",      new ParameterData("id", LIST,   SIMPLE, true, false), "3,4,5"),
            Arguments.arguments("Simple/exploded/Object/single",         new ParameterData("id", USER,   SIMPLE, true, true) , "firstName=Alex,role=admin"),
            Arguments.arguments("Label/non exploded/non object/single",  new ParameterData("id", SINGLE, LABEL, false, false), ".5"),
            Arguments.arguments("Label/non exploded/non object/array",   new ParameterData("id", LIST,   LABEL, false, false), ".3,4,5"),
            Arguments.arguments("Label/non exploded/Object/single",      new ParameterData("id", USER,   LABEL, false, true) , ".firstName,Alex,role,admin"),
            Arguments.arguments("Label/exploded/non object/single",      new ParameterData("id", SINGLE, LABEL, true, false), ".5"),
            Arguments.arguments("Label/exploded/non object/array",       new ParameterData("id", LIST,   LABEL, true, false), ".3.4.5"),
            Arguments.arguments("Label/exploded/Object/single",          new ParameterData("id", USER,   LABEL, true, true) , ".firstName=Alex.role=admin"),
            Arguments.arguments("Matrix/non exploded/non object/single", new ParameterData("id", SINGLE, MATRIX, false, false), ";id=5"),
            Arguments.arguments("Matrix/non exploded/non object/array",  new ParameterData("id", LIST,   MATRIX, false, false), ";id=3,4,5"),
            Arguments.arguments("Matrix/non exploded/Object/single",     new ParameterData("id", USER,   MATRIX, false, true) , ";id=firstName,Alex,role,admin"),
            Arguments.arguments("Matrix/exploded/non object/single",     new ParameterData("id", SINGLE, MATRIX, true, false), ";id=5"),
            Arguments.arguments("Matrix/exploded/non object/array",      new ParameterData("id", LIST,   MATRIX, true, false), ";id=3;id=4;id=5"),
            Arguments.arguments("Matrix/exploded/Object/single",         new ParameterData("id", USER,   MATRIX, true, true) , ";firstName=Alex;role=admin"),
            Arguments.arguments("Form/non exploded/non object/single",   new ParameterData("id", SINGLE, FORM, false, false) , "id=5"),
            Arguments.arguments("Form/non exploded/non object/array",    new ParameterData("id", LIST, FORM, false, false) , "id=3,4,5"),
            Arguments.arguments("Form/non exploded/object/single",       new ParameterData("id", USER, FORM, false, true) , "id=firstName,Alex,role,admin"),
            Arguments.arguments("Form/exploded/non object/single",       new ParameterData("id", SINGLE, FORM, true, false) , "id=5"),
            Arguments.arguments("Form/exploded/non object/array",        new ParameterData("id", LIST, FORM, true, false) , "id=3&id=4&id=5"),
            Arguments.arguments("Form/exploded/object/single",           new ParameterData("id", USER, FORM, true, true) , "firstName=Alex&role=admin"),
            Arguments.arguments("DeepObject/exploded/object/single",     new ParameterData("id", USER, DEEPOBJECT, true, true) , "id[firstName]=Alex&id[role]=admin")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void format(String name, ParameterData parameterData, String expected) {
        assertThat(formatArray(parameterData.name(), parameterData.value(), parameterData.parameterStyle(), parameterData.explode(),
            parameterData.isObject())).isEqualTo(expected);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder(builderMethodName = "user")
    private static class User {
        String role;
        String firstName;
    }
}
