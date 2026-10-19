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
package org.citrusframework.grpc.actions;

import org.citrusframework.TestAction;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.spi.AbstractReferenceResolverAwareTestActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.ObjectHelper;

/**
 * Entry point of the gRPC test actions: {@code grpc().client(...)} calls a gRPC service,
 * {@code grpc().server(...)} simulates one.
 */
public class GrpcActionBuilder extends AbstractReferenceResolverAwareTestActionBuilder<TestAction>
        implements org.citrusframework.api.actions.grpc.GrpcActionBuilder<TestAction, GrpcActionBuilder> {

    /**
     * Static entry method for gRPC test actions.
     */
    public static GrpcActionBuilder grpc() {
        return new GrpcActionBuilder();
    }

    @Override
    public GrpcClientActionBuilder client() {
        return delegateTo(new GrpcClientActionBuilder());
    }

    @Override
    public GrpcClientActionBuilder client(Endpoint client) {
        return delegateTo(new GrpcClientActionBuilder(client));
    }

    @Override
    public GrpcClientActionBuilder client(String client) {
        return delegateTo(new GrpcClientActionBuilder(client));
    }

    @Override
    public GrpcServerActionBuilder server() {
        return delegateTo(new GrpcServerActionBuilder());
    }

    @Override
    public GrpcServerActionBuilder server(Endpoint server) {
        return delegateTo(new GrpcServerActionBuilder(server));
    }

    @Override
    public GrpcServerActionBuilder server(String server) {
        return delegateTo(new GrpcServerActionBuilder(server));
    }

    @Override
    public GrpcActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
        return this;
    }

    @Override
    public TestAction build() {
        ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");
        return delegate.build();
    }

    private GrpcClientActionBuilder delegateTo(GrpcClientActionBuilder builder) {
        builder.withReferenceResolver(referenceResolver);
        this.delegate = builder;
        return builder;
    }

    private GrpcServerActionBuilder delegateTo(GrpcServerActionBuilder builder) {
        builder.withReferenceResolver(referenceResolver);
        this.delegate = builder;
        return builder;
    }
}
