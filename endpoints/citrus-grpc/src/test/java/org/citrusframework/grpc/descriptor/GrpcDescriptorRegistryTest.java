/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
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

import org.citrusframework.grpc.test.proto.helloworld.GreeterGrpc;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GrpcDescriptorRegistryTest {

    private static final String DESCRIPTOR_SET = "classpath:descriptors/test-protos.binpb";

    @Test
    public void shouldFindMethodFromGeneratedClass() {
        var registry = new GrpcDescriptorRegistry(List.of(GreeterGrpc.class.getName()));

        var method = registry.findMethod("helloworld.Greeter/SayHello");

        assertThat(method.getInputType().getFullName()).isEqualTo("helloworld.HelloRequest");
        assertThat(method.getOutputType().getFullName()).isEqualTo("helloworld.HelloReply");
        assertThat(registry.findMessage("helloworld.HelloRequest").findFieldByName("name")).isNotNull();
        assertThat(registry.services())
                .extracting(service -> service.getFullName())
                .containsExactly("helloworld.Greeter");
    }

    @Test
    public void shouldFindMethodFromDescriptorSet() {
        var registry = new GrpcDescriptorRegistry(List.of(DESCRIPTOR_SET));

        var method = registry.findMethod("market.Prices/Subscribe");

        assertThat(method.isServerStreaming()).isTrue();
        assertThat(registry.services())
                .extracting(service -> service.getFullName())
                .contains("market.Prices", "helloworld.Greeter");
    }

    @Test
    public void shouldFailForUnknownMethod() {
        var registry = new GrpcDescriptorRegistry(List.of(GreeterGrpc.class.getName()));

        assertThatThrownBy(() -> registry.findMethod("helloworld.Greeter/SayGoodbye"))
                .isInstanceOf(DescriptorRegistryException.class)
                .hasMessageContaining("helloworld.Greeter/SayGoodbye")
                .hasMessageContaining("helloworld.Greeter");
    }

    @Test
    public void shouldFailForUnknownDescriptorValue() {
        var registry = new GrpcDescriptorRegistry(List.of("not.a.Class"));

        assertThatThrownBy(() -> registry.findMethod("helloworld.Greeter/SayHello"))
                .isInstanceOf(DescriptorRegistryException.class)
                .hasMessageContaining("not.a.Class");
    }

    @Test
    public void shouldExposeTypeRegistry() {
        var registry = new GrpcDescriptorRegistry(List.of(DESCRIPTOR_SET));

        assertThat(registry.typeRegistry().find("feat.Feature")).isNotNull();
    }

    @Test
    public void shouldLoadLazilyAndCacheDescriptors() {
        var registry = new GrpcDescriptorRegistry(List.of(GreeterGrpc.class.getName()));

        assertThat(registry.services()).isSameAs(registry.services());
    }
}
