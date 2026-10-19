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
 * Entry point of the gRPC client test actions.
 */
public class GrpcClientActionBuilder extends AbstractReferenceResolverAwareTestActionBuilder<TestAction>
        implements org.citrusframework.api.actions.grpc.GrpcClientActionBuilder<TestAction, GrpcClientActionBuilder> {

    private Endpoint client;
    private String clientUri;

    public GrpcClientActionBuilder() {
    }

    public GrpcClientActionBuilder(Endpoint client) {
        this.client = client;
    }

    public GrpcClientActionBuilder(String clientUri) {
        this.clientUri = clientUri;
    }

    @Override
    public GrpcClientRequestActionBuilder send() {
        GrpcClientRequestActionBuilder builder = new GrpcClientRequestActionBuilder();
        if (client != null) {
            builder.endpoint(client);
        } else {
            builder.endpoint(clientUri);
        }

        builder.name("grpc:send-request");
        builder.withReferenceResolver(referenceResolver);
        this.delegate = builder;
        return builder;
    }

    @Override
    public GrpcClientResponseActionBuilder receive() {
        GrpcClientResponseActionBuilder builder = new GrpcClientResponseActionBuilder();
        if (client != null) {
            builder.endpoint(client);
        } else {
            builder.endpoint(clientUri);
        }

        builder.name("grpc:receive-response");
        builder.withReferenceResolver(referenceResolver);
        this.delegate = builder;
        return builder;
    }

    @Override
    public GrpcCancelAction.Builder cancel() {
        GrpcCancelAction.Builder builder = new GrpcCancelAction.Builder();
        if (client != null) {
            builder.endpoint(client);
        } else {
            builder.endpoint(clientUri);
        }

        builder.name("grpc:cancel");
        builder.withReferenceResolver(referenceResolver);
        this.delegate = builder;
        return builder;
    }

    @Override
    public GrpcClientActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
        return this;
    }

    @Override
    public TestAction build() {
        ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");
        return delegate.build();
    }
}
