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

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DescriptorSetSourceTest {

    private static final String DESCRIPTOR_SET = "classpath:descriptors/test-protos.binpb";

    @Test
    public void shouldLoadAllFilesFromDescriptorSet() {
        var source = new DescriptorSetSource(DESCRIPTOR_SET);

        var files = source.getDescriptors();

        assertThat(files)
                .extracting(file -> file.getName())
                .contains("helloworld/greeter.proto", "market/prices.proto", "feat/features.proto",
                        "feat/legacy.proto", "common/money.proto");
    }

    @Test
    public void shouldResolveDependenciesInOrder() {
        var source = new DescriptorSetSource(DESCRIPTOR_SET);

        var files = source.getDescriptors();

        var prices = files.stream()
                .filter(file -> file.getName().equals("market/prices.proto"))
                .findFirst()
                .orElseThrow();

        var price = prices.findMessageTypeByName("Price");
        assertThat(price.findFieldByName("as_of").getMessageType().getFullName())
                .isEqualTo("google.protobuf.Timestamp");
    }

    @Test
    public void shouldFailForMissingResource() {
        var source = new DescriptorSetSource("classpath:descriptors/missing.binpb");

        assertThatThrownBy(source::getDescriptors)
                .isInstanceOf(DescriptorRegistryException.class)
                .hasMessageContaining("missing.binpb");
    }
}
