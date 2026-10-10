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
package org.citrusframework.api.actions.graphql;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.api.actions.ReferenceResolverAwareBuilder;
import org.citrusframework.endpoint.Endpoint;

public interface GraphQlActionBuilder<T extends TestAction, B extends TestActionBuilder.DelegatingTestActionBuilder<T>>
        extends ReferenceResolverAwareBuilder<T, B> {

    /**
     * Uses a GraphQL client to call a GraphQL API.
     */
    GraphQlClientActionBuilder<?, ?> client();

    /**
     * Uses the given GraphQL client to call a GraphQL API.
     */
    GraphQlClientActionBuilder<?, ?> client(Endpoint endpoint);

    /**
     * Uses the GraphQL client with the given name or endpoint URI to call a GraphQL API.
     */
    GraphQlClientActionBuilder<?, ?> client(String endpoint);

    /**
     * Uses a GraphQL server to simulate a GraphQL API.
     */
    GraphQlServerActionBuilder<?, ?> server();

    /**
     * Uses the given GraphQL server to simulate a GraphQL API.
     */
    GraphQlServerActionBuilder<?, ?> server(Endpoint endpoint);

    /**
     * Uses the GraphQL server with the given name to simulate a GraphQL API.
     */
    GraphQlServerActionBuilder<?, ?> server(String endpoint);

    interface BuilderFactory {

        GraphQlActionBuilder<?, ?> graphql();
    }
}
