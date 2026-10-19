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
package org.citrusframework.grpc.endpoint;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.grpc.client.GrpcClient;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GrpcEndpointComponentTest {

    @Test
    public void shouldCreateClientFromEndpointUri() {
        GrpcEndpointComponent component = new GrpcEndpointComponent();

        Endpoint endpoint = component.createEndpoint(
                "grpc://localhost:50051?descriptors=classpath:descriptors/test-protos.binpb&timeout=1000",
                new TestContext());

        assertThat(endpoint).isInstanceOf(GrpcClient.class);
        GrpcClient client = (GrpcClient) endpoint;
        assertThat(client.getEndpointConfiguration().getTarget()).isEqualTo("localhost:50051");
        assertThat(client.getEndpointConfiguration().getDescriptors())
                .containsExactly("classpath:descriptors/test-protos.binpb");
        assertThat(client.getEndpointConfiguration().getTimeout()).isEqualTo(1000L);
    }

    @Test
    public void shouldCreateTlsClientFromEndpointUri() {
        GrpcEndpointComponent component = new GrpcEndpointComponent();

        Endpoint endpoint = component.createEndpoint(
                "grpc://service.example.com:443?descriptors=helloworld/greeter.proto&tls=true&trustCertificate=classpath:certs/ca.pem",
                new TestContext());

        GrpcClient client = (GrpcClient) endpoint;
        assertThat(client.getEndpointConfiguration().isTls()).isTrue();
        assertThat(client.getEndpointConfiguration().getTrustCertificate()).isEqualTo("classpath:certs/ca.pem");
    }
}
