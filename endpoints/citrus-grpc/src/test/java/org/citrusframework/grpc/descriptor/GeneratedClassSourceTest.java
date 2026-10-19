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

import org.citrusframework.grpc.test.proto.helloworld.GreeterGrpc;
import org.citrusframework.grpc.test.proto.helloworld.GreeterOuterClass;
import org.citrusframework.grpc.test.proto.helloworld.HelloRequest;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GeneratedClassSourceTest {

    @Test
    public void shouldLoadDescriptorFromMessageClass() {
        var source = new GeneratedClassSource(HelloRequest.class);

        var files = source.getDescriptors();

        assertThat(files).hasSize(1);
        assertThat(files.get(0).getName()).isEqualTo("helloworld/greeter.proto");
        assertThat(files.get(0).findServiceByName("Greeter")).isNotNull();
    }

    @Test
    public void shouldLoadDescriptorFromOuterClass() {
        var source = new GeneratedClassSource(GreeterOuterClass.class);

        var files = source.getDescriptors();

        assertThat(files).hasSize(1);
        assertThat(files.get(0).getName()).isEqualTo("helloworld/greeter.proto");
    }

    @Test
    public void shouldLoadDescriptorFromServiceClass() {
        var source = new GeneratedClassSource(GreeterGrpc.class);

        var files = source.getDescriptors();

        assertThat(files).hasSize(1);
        assertThat(files.get(0).getName()).isEqualTo("helloworld/greeter.proto");
        assertThat(files.get(0).getServices())
                .extracting(service -> service.getFullName())
                .contains("helloworld.Greeter");
    }

    @Test
    public void shouldFailOnClassWithoutDescriptors() {
        var source = new GeneratedClassSource(String.class);

        assertThatThrownBy(source::getDescriptors)
                .isInstanceOf(DescriptorRegistryException.class)
                .hasMessageContaining("String");
    }
}
