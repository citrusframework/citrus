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

import org.citrusframework.TestActionBuilder;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.grpc.actions.GrpcActionBuilder;
import org.citrusframework.grpc.client.GrpcClientBuilder;
import org.citrusframework.grpc.config.annotation.GrpcClientConfigParser;
import org.citrusframework.grpc.config.annotation.GrpcServerConfigParser;
import org.citrusframework.grpc.endpoint.builder.GrpcEndpointBuilder;
import org.citrusframework.grpc.endpoint.builder.GrpcEndpoints;
import org.citrusframework.grpc.server.GrpcServerBuilder;
import org.citrusframework.grpc.validation.GrpcStatusValidationContext;
import org.citrusframework.grpc.validation.GrpcStatusValidator;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.ValidationContext;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that all gRPC resource path registrations are resolvable. The META-INF resource files are
 * plain text, so a typo in a file name would silently disable the registration without any build signal.
 */
public class GrpcEndpointSpiTest {

    @Test
    public void shouldLookupEndpointBuilders() {
        assertThat(EndpointBuilder.lookup("grpc")).containsInstanceOf(GrpcEndpointBuilder.class);
        assertThat(EndpointBuilder.lookup("grpc.client")).containsInstanceOf(GrpcClientBuilder.class);
        assertThat(EndpointBuilder.lookup("grpc.server")).containsInstanceOf(GrpcServerBuilder.class);
    }

    @Test
    public void shouldLookupEndpointComponents() {
        assertThat(EndpointComponent.lookup("grpc")).containsInstanceOf(GrpcEndpointComponent.class);
    }

    @Test
    public void shouldLookupAnnotationConfigParsers() {
        assertThat(AnnotationConfigParser.lookup("grpc.client")).containsInstanceOf(GrpcClientConfigParser.class);
        assertThat(AnnotationConfigParser.lookup("grpc.server")).containsInstanceOf(GrpcServerConfigParser.class);
    }

    @Test
    public void shouldLookupTestActionBuilder() {
        assertThat(TestActionBuilder.lookup("grpc")).containsInstanceOf(GrpcActionBuilder.class);
    }

    @Test
    public void shouldLookupStatusValidator() {
        assertThat(MessageValidator.lookup("grpcStatus")).containsInstanceOf(GrpcStatusValidator.class);
        assertThat(MessageValidator.lookup()).containsKey("defaultGrpcStatusMessageValidator");
    }

    @Test
    public void shouldLookupStatusValidationContextBuilder() {
        assertThat(ValidationContext.lookup("grpcStatus")).containsInstanceOf(GrpcStatusValidationContext.Builder.class);
    }

    @Test
    public void shouldCreateBuildersFromEntryPoint() {
        assertThat(GrpcEndpoints.grpc().client()).isInstanceOf(GrpcClientBuilder.class);
        assertThat(GrpcEndpoints.grpc().server()).isInstanceOf(GrpcServerBuilder.class);
    }
}
