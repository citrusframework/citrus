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

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.graphql.client.GraphQlClient;
import org.citrusframework.graphql.endpoint.builder.GraphQlEndpoints;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.citrusframework.api.graphql.GraphQlError.error;
import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

/**
 * The GraphQL client against a real Spring GraphQL server.
 */
@Test
public class GraphQlClientInteropIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    private final int port = findAvailableTcpPort(18980);

    private SpringGraphQlServer springServer;

    @BindToRegistry
    private final GraphQlClient springGraphQl = GraphQlEndpoints.graphql().client()
            .requestUrl("http://localhost:%d".formatted(port))
            .timeout(5000L)
            .build();

    @BeforeClass(alwaysRun = true)
    public void startSpringGraphQl() throws Exception {
        springServer = SpringGraphQlServer.start(port);
    }

    @AfterClass(alwaysRun = true)
    public void stopSpringGraphQl() throws Exception {
        if (springServer != null) {
            springServer.close();
        }
    }

    @CitrusTest
    public void readsData() {
        when(graphql().client(springGraphQl).send()
                .query("query Book($id: ID!) { book(id: $id) { id title } }")
                .variable("id", "1"));

        then(graphql().client(springGraphQl).receive()
                .data("{\"book\":{\"id\":\"1\",\"title\":\"Dune\"}}")
                .message()
                .status(200)
                .header("Content-Type", "@startsWith('application/graphql-response+json')@"));
    }

    @CitrusTest
    public void readsFieldErrors() {
        when(graphql().client(springGraphQl).send()
                .query("{ book(id: \"7\") { title } }"));

        then(graphql().client(springGraphQl).receive()
                .expectError(error().message("Book 7 not found").path("book").code("NOT_FOUND"))
                .message()
                .status(200));
    }

    @CitrusTest
    public void failsOnUnexpectedFieldErrors() {
        when(graphql().client(springGraphQl).send()
                .query("{ book(id: \"7\") { title } }"));

        then(assertException()
                .exception(ValidationException.class)
                .message("@startsWith('GraphQL response contains 1 unexpected error: [NOT_FOUND] Book 7 not found at book')@")
                .when(graphql().client(springGraphQl).receive()));
    }

    @CitrusTest
    public void serverRejectsInvalidDocumentWithClientError() {
        when(graphql().client(springGraphQl).send()
                .strict(false)
                .query("{ book(id: \"1\") { isbn } }"));

        then(graphql().client(springGraphQl).receive()
                .expectError(error().message("@contains('isbn')@"))
                .message()
                .status(400));
    }

    @CitrusTest
    public void serverAnswersLegacyJsonWithOk() {
        var request = graphql().client(springGraphQl).send()
                .strict(false)
                .query("{ book(id: \"1\") { isbn } }");
        request.message().accept("application/json");
        when(request);

        then(graphql().client(springGraphQl).receive()
                .expectErrors()
                .message()
                .status(200)
                .header("Content-Type", "@startsWith('application/json')@"));
    }
}
