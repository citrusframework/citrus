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
package org.citrusframework.graphql.integration.interop;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.graphql.endpoint.builder.GraphQlEndpoints;
import org.citrusframework.graphql.server.GraphQlServer;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.graphql.client.HttpSyncGraphQlClient;
import org.springframework.web.client.RestClient;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.citrusframework.api.graphql.GraphQlError.error;
import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

/**
 * A real Spring GraphQL client against the GraphQL simulator.
 */
@Test
public class GraphQlServerInteropIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    private final int port = findAvailableTcpPort(19080);

    @BindToRegistry
    private final GraphQlServer simulator = GraphQlEndpoints.graphql().server()
            .port(port)
            .autoStart(true)
            .timeout(5000L)
            .build();

    @CitrusTest
    public void springClientReadsSimulatedData() throws Exception {
        CompletableFuture<ClientGraphQlResponse> response = CompletableFuture.supplyAsync(() -> springClient()
                .document("query Book($id: ID!) { book(id: $id) { title } }")
                .variable("id", "1")
                .executeSync());

        then(graphql().server(simulator).receive()
                .operationName("Book")
                .variable("id", "1")
                .message()
                .header("Accept", "@contains('application/graphql-response+json')@"));

        then(graphql().server(simulator).send()
                .data("{\"book\":{\"title\":\"Dune\"}}"));

        ClientGraphQlResponse result = response.get(10, TimeUnit.SECONDS);
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.field("book.title").<String>getValue()).isEqualTo("Dune");
    }

    @CitrusTest
    public void springClientReadsSimulatedErrors() throws Exception {
        CompletableFuture<ClientGraphQlResponse> response = CompletableFuture.supplyAsync(() -> springClient()
                .document("{ book(id: \"7\") { title } }")
                .executeSync());

        then(graphql().server(simulator).receive());

        then(graphql().server(simulator).send()
                .data("{\"book\":null}")
                .error(error().message("Book 7 not found").path("book").code("NOT_FOUND")));

        ClientGraphQlResponse result = response.get(10, TimeUnit.SECONDS);
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        ResponseError error = result.getErrors().get(0);
        assertThat(error.getMessage()).isEqualTo("Book 7 not found");
        assertThat(error.getPath()).isEqualTo("book");
        assertThat(error.getExtensions()).containsEntry("code", "NOT_FOUND");
        assertThat(result.field("book").<Object>getValue()).isNull();
    }

    private HttpSyncGraphQlClient springClient() {
        return HttpSyncGraphQlClient.create(RestClient.create("http://localhost:%d/graphql".formatted(port)));
    }
}
