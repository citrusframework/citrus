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

/**
 * Entry point of the GraphQL test actions: {@code graphql().client(...)} calls a GraphQL API,
 * {@code graphql().server(...)} simulates one.
 */
public class GraphQlActionBuilder extends AbstractReferenceResolverAwareTestActionBuilder<TestAction>
        implements org.citrusframework.api.actions.graphql.GraphQlActionBuilder<TestAction, GraphQlActionBuilder> {

    /**
     * Static entry method for GraphQL test actions.
     */
    public static GraphQlActionBuilder graphql() {
        return new GraphQlActionBuilder();
    }

    @Override
    public GraphQlClientActionBuilder client() {
        return delegateTo(new GraphQlClientActionBuilder());
    }

    @Override
    public GraphQlClientActionBuilder client(Endpoint client) {
        return delegateTo(new GraphQlClientActionBuilder(client));
    }

    @Override
    public GraphQlClientActionBuilder client(String client) {
        return delegateTo(new GraphQlClientActionBuilder(client));
    }

    @Override
    public GraphQlServerActionBuilder server() {
        return delegateTo(new GraphQlServerActionBuilder());
    }

    @Override
    public GraphQlServerActionBuilder server(Endpoint server) {
        return delegateTo(new GraphQlServerActionBuilder(server));
    }

    @Override
    public GraphQlServerActionBuilder server(String server) {
        return delegateTo(new GraphQlServerActionBuilder(server));
    }

    @Override
    public GraphQlActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
        return this;
    }

    @Override
    public TestAction build() {
        ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");
        return delegate.build();
    }

    private GraphQlClientActionBuilder delegateTo(GraphQlClientActionBuilder builder) {
        builder.withReferenceResolver(referenceResolver);
        this.delegate = builder;
        return builder;
    }

    private GraphQlServerActionBuilder delegateTo(GraphQlServerActionBuilder builder) {
        builder.withReferenceResolver(referenceResolver);
        this.delegate = builder;
        return builder;
    }
}
