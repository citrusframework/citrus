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

import java.util.List;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProtoFileSourceTest {

    @Test
    public void shouldLoadProtoFileFromClasspath() {
        List<FileDescriptor> files = new ProtoFileSource("classpath:helloworld/greeter.proto").getDescriptors();

        assertThat(files).extracting(FileDescriptor::getName).contains("helloworld/greeter.proto");

        FileDescriptor greeter = file(files, "helloworld/greeter.proto");
        assertThat(greeter.findServiceByName("Greeter")).isNotNull();
        assertThat(greeter.findServiceByName("Greeter").findMethodByName("SayHello").getInputType().getFullName())
                .isEqualTo("helloworld.HelloRequest");
    }

    @Test
    public void shouldResolveImportedProtoFiles() {
        List<FileDescriptor> files = new ProtoFileSource("classpath:market/prices.proto").getDescriptors();

        assertThat(files).extracting(FileDescriptor::getName)
                .contains("market/prices.proto", "common/money.proto");

        FileDescriptor prices = file(files, "market/prices.proto");
        FieldDescriptor amount = prices.findMessageTypeByName("Price").findFieldByName("amount");
        assertThat(amount.getMessageType().getFullName()).isEqualTo("common.Money");

        FieldDescriptor asOf = prices.findMessageTypeByName("Price").findFieldByName("as_of");
        assertThat(asOf.getMessageType().getFullName()).isEqualTo("google.protobuf.Timestamp");
    }

    @Test
    public void shouldHonorDeclaredJsonName() {
        List<FileDescriptor> files = new ProtoFileSource("classpath:feat/features.proto").getDescriptors();

        FileDescriptor features = file(files, "feat/features.proto");
        assertThat(features.findMessageTypeByName("Feature").findFieldByName("renamed").getJsonName())
                .isEqualTo("otherName");
        assertThat(features.findMessageTypeByName("Feature").findFieldByName("labels").isMapField()).isTrue();
    }

    @Test
    public void shouldLoadProto2Source() {
        List<FileDescriptor> files = new ProtoFileSource("classpath:feat/legacy.proto").getDescriptors();

        FileDescriptor legacy = file(files, "feat/legacy.proto");
        FieldDescriptor name = legacy.findMessageTypeByName("LegacyRequest").findFieldByName("name");
        assertThat(name.isRequired()).isTrue();
        assertThat(legacy.findMessageTypeByName("LegacyRequest").findFieldByName("id").getDefaultValue())
                .isEqualTo(42);
    }

    @Test
    public void shouldFailForMissingResource() {
        assertThatThrownBy(() -> new ProtoFileSource("classpath:missing.proto").getDescriptors())
                .isInstanceOf(DescriptorRegistryException.class)
                .hasMessageContaining("missing.proto");
    }

    @Test
    public void shouldFailForSyntaxError() {
        assertThatThrownBy(() -> new ProtoFileSource("classpath:bad/syntax.proto").getDescriptors())
                .isInstanceOf(DescriptorRegistryException.class)
                .hasMessageContaining("bad/syntax.proto");
    }

    @Test
    public void shouldFailForUnresolvableImport() {
        assertThatThrownBy(() -> new ProtoFileSource("classpath:bad/missing-import.proto").getDescriptors())
                .isInstanceOf(DescriptorRegistryException.class)
                .hasMessageContaining("bad/missing-import.proto")
                .hasMessageContaining("not/there/common.proto");
    }

    @Test
    public void shouldFailForEditionsFile() {
        assertThatThrownBy(() -> new ProtoFileSource("classpath:bad/editions.proto").getDescriptors())
                .isInstanceOf(DescriptorRegistryException.class)
                .hasMessageContaining("bad/editions.proto")
                .hasMessageContaining("descriptor set");
    }

    private static FileDescriptor file(List<FileDescriptor> files, String name) {
        return files.stream()
                .filter(file -> file.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
