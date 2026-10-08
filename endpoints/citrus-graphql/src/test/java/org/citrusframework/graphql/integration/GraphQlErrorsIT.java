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
package org.citrusframework.graphql.integration;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.graphql.client.GraphQlClient;
import org.citrusframework.graphql.endpoint.builder.GraphQlEndpoints;
import org.citrusframework.graphql.server.GraphQlServer;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

import static org.citrusframework.api.graphql.GraphQlError.error;
import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

/**
 * GraphQL errors: an HTTP 200 with errors fails unless the test expects them.
 */
@Test
public class GraphQlErrorsIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    private final int port = findAvailableTcpPort(18680);

    @BindToRegistry
    private final GraphQlServer errorServer = GraphQlEndpoints.graphql().server()
            .port(port)
            .autoStart(true)
            .timeout(5000L)
            .build();

    @BindToRegistry
    private final GraphQlClient errorClient = GraphQlEndpoints.graphql().client()
            .requestUrl("http://localhost:%d".formatted(port))
            .timeout(5000L)
            .build();

    @CitrusTest
    public void unexpectedErrorsFailEvenWithHttp200() {
        exchange(graphql().server(errorServer).send()
                .error(error().message("Book not found").path("book").code("NOT_FOUND")));

        then(assertException()
                .exception(ValidationException.class)
                .message("@startsWith('GraphQL response contains 1 unexpected error: [NOT_FOUND] Book not found at book')@")
                .when(graphql().client(errorClient).receive()));
    }

    @CitrusTest
    public void expectedErrorMatches() {
        exchange(graphql().server(errorServer).send()
                .error(error().message("boom").path("books", 1, "title").code("INTERNAL"))
                .error(error().message("Book 7 not found").path("book").code("NOT_FOUND")));

        then(graphql().client(errorClient).receive()
                .expectError(error().code("NOT_FOUND").message("@contains('not found')@").path("book"))
                .expectError(error().path("books", 1, "title")));
    }

    @CitrusTest
    public void noMatchingErrorFails() {
        exchange(graphql().server(errorServer).send()
                .error(error().message("boom").code("INTERNAL")));

        then(assertException()
                .exception(ValidationException.class)
                .message("No GraphQL error matched [NOT_FOUND]; received: [INTERNAL] boom")
                .when(graphql().client(errorClient).receive()
                        .expectError(error().code("NOT_FOUND"))));
    }

    @CitrusTest
    public void expectedErrorsMissingFails() {
        exchange(graphql().server(errorServer).send().data("{\"books\":[]}"));

        then(assertException()
                .exception(ValidationException.class)
                .message("GraphQL errors expected but none were returned")
                .when(graphql().client(errorClient).receive().expectErrors()));
    }

    @CitrusTest
    public void partialResult() {
        exchange(graphql().server(errorServer).send()
                .data("{\"book\":{\"title\":\"Dune\",\"author\":null}}")
                .error(error().message("Author unavailable").path("book", "author").code("PARTIAL")));

        then(graphql().client(errorClient).receive()
                .expectError(error().path("book", "author"))
                .data("$.book.title", "Dune"));
    }

    @CitrusTest
    public void errorsWithClientErrorStatus() {
        var reply = graphql().server(errorServer).send()
                .error(error().message("Validation error (FieldUndefined)").code("GRAPHQL_VALIDATION_FAILED"));
        reply.message().status(400);
        exchange(reply);

        then(graphql().client(errorClient).receive()
                .expectError(error().code("GRAPHQL_VALIDATION_FAILED"))
                .message()
                .status(400));
    }

    private void exchange(org.citrusframework.TestActionBuilder<?> reply) {
        when(graphql().client(errorClient).send()
                .query("{ book(id: 7) { title } }")
                .fork(true));

        then(graphql().server(errorServer).receive());

        then(reply);
    }
}
