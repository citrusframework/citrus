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
package org.citrusframework.graphql.actions;

import org.citrusframework.TestAction;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.spi.AbstractReferenceResolverAwareTestActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.ObjectHelper;

public class GraphQlServerActionBuilder extends AbstractReferenceResolverAwareTestActionBuilder<TestAction>
        implements org.citrusframework.api.actions.graphql.GraphQlServerActionBuilder<TestAction, GraphQlServerActionBuilder> {

    private Endpoint server;
    private String serverUri;

    public GraphQlServerActionBuilder() {
    }

    public GraphQlServerActionBuilder(Endpoint server) {
        this.server = server;
    }

    public GraphQlServerActionBuilder(String serverUri) {
        this.serverUri = serverUri;
    }

    @Override
    @SuppressWarnings("unchecked")
    public GraphQlServerRequestActionBuilder receive() {
        GraphQlServerRequestActionBuilder builder = new GraphQlServerRequestActionBuilder();
        if (server != null) {
            builder.endpoint(server);
        } else {
            builder.endpoint(serverUri);
        }

        builder.name("graphql:receive-request");
        builder.withReferenceResolver(referenceResolver);
        this.delegate = builder;
        return builder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public GraphQlServerResponseActionBuilder send() {
        GraphQlServerResponseActionBuilder builder = new GraphQlServerResponseActionBuilder();
        if (server != null) {
            builder.endpoint(server);
        } else {
            builder.endpoint(serverUri);
        }

        builder.name("graphql:send-response");
        builder.withReferenceResolver(referenceResolver);
        this.delegate = builder;
        return builder;
    }

    @Override
    public GraphQlServerActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
        return this;
    }

    @Override
    public TestAction build() {
        ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");
        return delegate.build();
    }
}
