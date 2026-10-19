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
 * Action builder for gRPC simulator operations. Receives calls made by a client under test and
 * sends responses, stream messages or the final status.
 */
public class GrpcServerActionBuilder extends AbstractReferenceResolverAwareTestActionBuilder<TestAction>
        implements org.citrusframework.api.actions.grpc.GrpcServerActionBuilder<TestAction, GrpcServerActionBuilder> {

    private Endpoint server;
    private String serverUri;

    public GrpcServerActionBuilder() {
    }

    public GrpcServerActionBuilder(Endpoint server) {
        this.server = server;
    }

    public GrpcServerActionBuilder(String serverUri) {
        this.serverUri = serverUri;
    }

    @Override
    public GrpcServerRequestActionBuilder receive() {
        GrpcServerRequestActionBuilder builder = new GrpcServerRequestActionBuilder();
        if (server != null) {
            builder.endpoint(server);
        } else {
            builder.endpoint(serverUri);
        }

        builder.name("grpc:receive-call");
        builder.withReferenceResolver(referenceResolver);
        this.delegate = builder;
        return builder;
    }

    @Override
    public GrpcServerResponseActionBuilder send() {
        GrpcServerResponseActionBuilder builder = new GrpcServerResponseActionBuilder();
        if (server != null) {
            builder.endpoint(server);
        } else {
            builder.endpoint(serverUri);
        }

        builder.name("grpc:send-response");
        builder.withReferenceResolver(referenceResolver);
        this.delegate = builder;
        return builder;
    }

    @Override
    public GrpcServerActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
        return this;
    }

    @Override
    public TestAction build() {
        ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");
        return delegate.build();
    }
}
