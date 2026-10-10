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
package org.citrusframework.graphql.integration.zero;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.graphql.endpoint.builder.GraphQlEndpoints;
import org.citrusframework.graphql.server.GraphQlServer;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

/**
 * YAML DSL against the {@link GraphQlZeroSimulator}: every query and mutation of the GraphQLZero
 * API, one test per resource. The simulator server lives in the registry; each test declares its
 * GraphQL client.
 */
public class GraphQlZeroYamlIT extends TestNGCitrusSpringSupport {

    @BindToRegistry
    private final GraphQlServer zeroServer = GraphQlEndpoints.graphql().server()
            .port(19381)
            .endpointAdapter(new GraphQlZeroSimulator())
            .autoStart(true)
            .timeout(5000L)
            .build();

    @Test
    @CitrusTestSource(type = TestLoader.YAML, packageName = "org.citrusframework.graphql.integration.zero", name = {
            "graphqlzero-posts.citrus.it",
            "graphqlzero-users.citrus.it",
            "graphqlzero-comments.citrus.it",
            "graphqlzero-todos.citrus.it",
            "graphqlzero-albums.citrus.it",
            "graphqlzero-photos.citrus.it"
    })
    public void graphQlZeroYaml() {
    }
}
