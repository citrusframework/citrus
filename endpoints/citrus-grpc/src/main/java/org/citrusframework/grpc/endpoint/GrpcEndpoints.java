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

import org.citrusframework.endpoint.builder.ClientServerEndpointBuilder;
import org.citrusframework.grpc.client.GrpcClientBuilder;
import org.citrusframework.grpc.server.GrpcServerBuilder;

/**
 * Entry point for gRPC client and server endpoint builders.
 */
public final class GrpcEndpoints extends ClientServerEndpointBuilder<GrpcClientBuilder, GrpcServerBuilder> {

    private GrpcEndpoints() {
        super(new GrpcClientBuilder(), new GrpcServerBuilder());
    }

    public static GrpcEndpoints grpc() {
        return new GrpcEndpoints();
    }
}
