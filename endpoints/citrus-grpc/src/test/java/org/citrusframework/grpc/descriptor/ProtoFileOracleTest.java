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

package org.citrusframework.grpc.descriptor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Compares the descriptors derived from {@code .proto} sources through Wire with the descriptor set
 * that {@code protoc} produces during the build. Any divergence in message, field, enum, service or
 * method descriptors fails this test.
 */
public class ProtoFileOracleTest {

    private static final String DESCRIPTOR_SET = "classpath:descriptors/test-protos.binpb";

    private static final List<String> TEST_PROTOS = List.of(
            "helloworld/greeter.proto",
            "market/prices.proto",
            "feat/features.proto",
            "feat/legacy.proto",
            "common/money.proto");

    @Test
    public void shouldMatchProtocDescriptors() {
        Map<String, FileDescriptor> protoc = new LinkedHashMap<>();
        for (FileDescriptor file : new DescriptorSetSource(DESCRIPTOR_SET).getDescriptors()) {
            protoc.put(file.getName(), file);
        }

        for (String proto : TEST_PROTOS) {
            FileDescriptor expected = protoc.get(proto);
            assertThat(expected).as("descriptor set contains %s", proto).isNotNull();

            FileDescriptor actual = new ProtoFileSource("classpath:" + proto).getDescriptors().stream()
                    .filter(file -> file.getName().equals(proto))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Wire did not produce " + proto));

            assertSameFile(expected, actual);
        }
    }

    private static void assertSameFile(FileDescriptor expected, FileDescriptor actual) {
        assertThat(actual.getName()).as("file name").isEqualTo(expected.getName());
        assertThat(actual.getPackage()).as("package of %s", expected.getName()).isEqualTo(expected.getPackage());

        assertSameServices(expected, actual);
        assertSameMessages(expected.getName(), expected.getMessageTypes(), actual.getMessageTypes());
        assertSameEnums(expected.getName(), expected.getEnumTypes(), actual.getEnumTypes());
    }

    private static void assertSameServices(FileDescriptor expected, FileDescriptor actual) {
        Map<String, ServiceDescriptor> actualServices = actual.getServices().stream()
                .collect(Collectors.toMap(ServiceDescriptor::getFullName, Function.identity()));

        assertThat(actualServices.keySet()).as("services of %s", expected.getName())
                .containsExactlyInAnyOrderElementsOf(expected.getServices().stream()
                        .map(ServiceDescriptor::getFullName)
                        .toList());

        for (ServiceDescriptor expectedService : expected.getServices()) {
            ServiceDescriptor actualService = actualServices.get(expectedService.getFullName());
            Map<String, MethodDescriptor> actualMethods = actualService.getMethods().stream()
                    .collect(Collectors.toMap(MethodDescriptor::getName, Function.identity()));

            assertThat(actualMethods.keySet()).as("methods of %s", expectedService.getFullName())
                    .containsExactlyInAnyOrderElementsOf(expectedService.getMethods().stream()
                            .map(MethodDescriptor::getName)
                            .toList());

            for (MethodDescriptor expectedMethod : expectedService.getMethods()) {
                MethodDescriptor actualMethod = actualMethods.get(expectedMethod.getName());
                assertThat(actualMethod.getInputType().getFullName())
                        .as("input type of %s/%s", expectedService.getFullName(), expectedMethod.getName())
                        .isEqualTo(expectedMethod.getInputType().getFullName());
                assertThat(actualMethod.getOutputType().getFullName())
                        .as("output type of %s/%s", expectedService.getFullName(), expectedMethod.getName())
                        .isEqualTo(expectedMethod.getOutputType().getFullName());
                assertThat(actualMethod.isClientStreaming())
                        .as("client streaming of %s/%s", expectedService.getFullName(), expectedMethod.getName())
                        .isEqualTo(expectedMethod.isClientStreaming());
                assertThat(actualMethod.isServerStreaming())
                        .as("server streaming of %s/%s", expectedService.getFullName(), expectedMethod.getName())
                        .isEqualTo(expectedMethod.isServerStreaming());
            }
        }
    }

    private static void assertSameMessages(String fileName, List<Descriptor> expected, List<Descriptor> actual) {
        Map<String, Descriptor> actualMessages = actual.stream()
                .collect(Collectors.toMap(Descriptor::getName, Function.identity()));

        assertThat(actualMessages.keySet()).as("messages of %s", fileName)
                .containsExactlyInAnyOrderElementsOf(expected.stream().map(Descriptor::getName).toList());

        for (Descriptor expectedMessage : expected) {
            Descriptor actualMessage = actualMessages.get(expectedMessage.getName());
            assertSameFields(expectedMessage, actualMessage);
            assertSameEnums(expectedMessage.getFullName(), expectedMessage.getEnumTypes(), actualMessage.getEnumTypes());
            assertSameMessages(expectedMessage.getFullName(),
                    expectedMessage.getNestedTypes(), actualMessage.getNestedTypes());
        }
    }

    private static void assertSameFields(Descriptor expected, Descriptor actual) {
        Map<String, FieldDescriptor> actualFields = actual.getFields().stream()
                .collect(Collectors.toMap(FieldDescriptor::getName, Function.identity()));

        assertThat(actualFields.keySet()).as("fields of %s", expected.getFullName())
                .containsExactlyInAnyOrderElementsOf(expected.getFields().stream()
                        .map(FieldDescriptor::getName)
                        .toList());

        for (FieldDescriptor expectedField : expected.getFields()) {
            FieldDescriptor actualField = actualFields.get(expectedField.getName());
            String description = expected.getFullName() + "." + expectedField.getName();

            assertThat(actualField.getNumber()).as("number of %s", description)
                    .isEqualTo(expectedField.getNumber());
            assertThat(actualField.getType()).as("type of %s", description)
                    .isEqualTo(expectedField.getType());
            assertThat(actualField.isRepeated()).as("repeated of %s", description)
                    .isEqualTo(expectedField.isRepeated());
            assertThat(actualField.isRequired()).as("required of %s", description)
                    .isEqualTo(expectedField.isRequired());
            assertThat(actualField.getJsonName()).as("json name of %s", description)
                    .isEqualTo(expectedField.getJsonName());
            assertThat(actualField.isMapField()).as("map field %s", description)
                    .isEqualTo(expectedField.isMapField());

            if (expectedField.getJavaType() == FieldDescriptor.JavaType.MESSAGE) {
                assertThat(actualField.getMessageType().getFullName()).as("message type of %s", description)
                        .isEqualTo(expectedField.getMessageType().getFullName());
            }
            if (expectedField.getJavaType() == FieldDescriptor.JavaType.ENUM) {
                assertThat(actualField.getEnumType().getFullName()).as("enum type of %s", description)
                        .isEqualTo(expectedField.getEnumType().getFullName());
            }
        }
    }

    private static void assertSameEnums(String owner, List<EnumDescriptor> expected, List<EnumDescriptor> actual) {
        Map<String, EnumDescriptor> actualEnums = actual.stream()
                .collect(Collectors.toMap(EnumDescriptor::getName, Function.identity()));

        assertThat(actualEnums.keySet()).as("enums of %s", owner)
                .containsExactlyInAnyOrderElementsOf(expected.stream().map(EnumDescriptor::getName).toList());

        for (EnumDescriptor expectedEnum : expected) {
            EnumDescriptor actualEnum = actualEnums.get(expectedEnum.getName());
            assertThat(actualEnum.getValues().stream().map(value -> value.getName() + "=" + value.getNumber()).toList())
                    .as("constants of %s", expectedEnum.getFullName())
                    .isEqualTo(expectedEnum.getValues().stream()
                            .map(value -> value.getName() + "=" + value.getNumber()).toList());
        }
    }
}
