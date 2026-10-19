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
package org.citrusframework.dsl.endpoint.grpc;

import org.citrusframework.grpc.client.GrpcClientBuilder;
import org.citrusframework.grpc.endpoint.GrpcEndpoints;
import org.citrusframework.grpc.server.GrpcServerBuilder;

public class GrpcEndpointCatalog {

    /**
     * Private constructor setting the client and server builder implementation.
     */
    private GrpcEndpointCatalog() {
        // prevent direct instantiation
    }

    public static GrpcEndpointCatalog grpc() {
        return new GrpcEndpointCatalog();
    }

    /**
     * Gets the gRPC client builder.
     * @return
     */
    public GrpcClientBuilder client() {
        return GrpcEndpoints.grpc().client();
    }

    /**
     * Gets the gRPC server builder.
     * @return
     */
    public GrpcServerBuilder server() {
        return GrpcEndpoints.grpc().server();
    }
}
