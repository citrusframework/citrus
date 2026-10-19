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
package org.citrusframework.api.actions.grpc;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.api.actions.ReferenceResolverAwareBuilder;
import org.citrusframework.endpoint.Endpoint;

/**
 * Action builder interface for gRPC client and server actions.
 */
public interface GrpcActionBuilder<T extends TestAction, B extends TestActionBuilder.DelegatingTestActionBuilder<T>>
        extends ReferenceResolverAwareBuilder<T, B> {

    /**
     * Uses a gRPC client to call a gRPC API.
     * @return
     */
    GrpcClientActionBuilder<?, ?> client();

    /**
     * Uses the given gRPC client to call a gRPC API.
     * @param endpoint
     * @return
     */
    GrpcClientActionBuilder<?, ?> client(Endpoint endpoint);

    /**
     * Uses the gRPC client with the given name or endpoint URI to call a gRPC API.
     * @param endpoint
     * @return
     */
    GrpcClientActionBuilder<?, ?> client(String endpoint);

    /**
     * Uses a gRPC server to expose a gRPC API.
     * @return
     */
    GrpcServerActionBuilder<?, ?> server();

    /**
     * Uses the given gRPC server to expose a gRPC API.
     * @param endpoint
     * @return
     */
    GrpcServerActionBuilder<?, ?> server(Endpoint endpoint);

    /**
     * Uses the gRPC server with the given name or endpoint URI to expose a gRPC API.
     * @param endpoint
     * @return
     */
    GrpcServerActionBuilder<?, ?> server(String endpoint);

    interface BuilderFactory {

        GrpcActionBuilder<?, ?> grpc();
    }
}
