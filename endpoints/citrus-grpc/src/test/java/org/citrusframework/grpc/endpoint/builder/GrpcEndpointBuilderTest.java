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
package org.citrusframework.grpc.endpoint.builder;

import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.grpc.client.GrpcClient;
import org.citrusframework.grpc.client.GrpcClientBuilder;
import org.citrusframework.grpc.server.GrpcServer;
import org.citrusframework.grpc.server.GrpcServerBuilder;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.spi.ReferenceResolver;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GrpcEndpointBuilderTest {

    @Test
    public void shouldBuildClientEndpoint() {
        GrpcEndpointBuilder builder = new GrpcEndpointBuilder();
        GrpcClientBuilder clientBuilder = new GrpcClientBuilder();
        clientBuilder.target("localhost:8080");
        builder.setClient(clientBuilder);

        Endpoint endpoint = builder.build();

        assertThat(endpoint).isInstanceOf(GrpcClient.class);
        assertThat(((GrpcClient) endpoint).getEndpointConfiguration().getTarget()).isEqualTo("localhost:8080");
        assertThat(builder.supports(GrpcClient.class)).isTrue();
    }

    @Test
    public void shouldBuildServerEndpoint() {
        GrpcEndpointBuilder builder = new GrpcEndpointBuilder();
        GrpcServerBuilder serverBuilder = new GrpcServerBuilder();
        serverBuilder.port(8080);
        builder.setServer(serverBuilder);

        Endpoint endpoint = builder.build();

        assertThat(endpoint).isInstanceOf(GrpcServer.class);
        assertThat(((GrpcServer) endpoint).getEndpointConfiguration().getPort()).isEqualTo(8080);
    }

    @Test
    public void shouldFailWithoutClientOrServer() {
        GrpcEndpointBuilder builder = new GrpcEndpointBuilder();

        assertThatThrownBy(builder::build)
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("has not been initialized");
    }

    @Test
    public void shouldFailWithoutReferenceResolverForCorrelator() {
        GrpcEndpointBuilder builder = new GrpcEndpointBuilder();
        GrpcClientBuilder clientBuilder = new GrpcClientBuilder();
        clientBuilder.correlator("myCorrelator");
        builder.setClient(clientBuilder);

        assertThatThrownBy(builder::build)
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("Missing reference resolver");
    }

    @Test
    public void shouldResolveCorrelatorWithReferenceResolver() {
        MessageCorrelator correlator = Mockito.mock(MessageCorrelator.class);
        ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
        Mockito.when(referenceResolver.resolve("myCorrelator", MessageCorrelator.class)).thenReturn(correlator);

        GrpcEndpointBuilder builder = new GrpcEndpointBuilder();
        GrpcClientBuilder clientBuilder = new GrpcClientBuilder();
        clientBuilder.correlator("myCorrelator");
        builder.setClient(clientBuilder);
        builder.setReferenceResolver(referenceResolver);

        Endpoint endpoint = builder.build();

        assertThat(endpoint).isInstanceOf(GrpcClient.class);
        assertThat(((GrpcClient) endpoint).getEndpointConfiguration().getCorrelator()).isSameAs(correlator);
    }
}
