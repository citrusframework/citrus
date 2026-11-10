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

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.graphql.client.GraphQlClient;
import org.citrusframework.graphql.endpoint.builder.GraphQlEndpoints;
import org.citrusframework.graphql.server.GraphQlServer;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import static org.citrusframework.api.graphql.GraphQlError.error;
import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

/**
 * GraphQL errors: an HTTP 200 with errors fails unless the test expects them.
 */
@Isolated("GraphQL ITs share Citrus Spring context state and bind dynamic/fixed ports under parallel classes")
@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GraphQlErrorsIT implements TestActionSupport {

    private final int port = findAvailableTcpPort(20000, 32767);

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

    @Test
    @CitrusTest
    public void unexpectedErrorsFailEvenWithHttp200(@CitrusResource GherkinTestActionRunner runner) {
        exchange(runner, graphql().server(errorServer).send()
                .error(error().message("Book not found").path("book").code("NOT_FOUND")));

        runner.then(assertException()
                .exception(ValidationException.class)
                .message("@startsWith('GraphQL response contains 1 unexpected error: [NOT_FOUND] Book not found at book')@")
                .when(graphql().client(errorClient).receive()));
    }

    @Test
    @CitrusTest
    public void expectedErrorMatches(@CitrusResource GherkinTestActionRunner runner) {
        exchange(runner, graphql().server(errorServer).send()
                .error(error().message("boom").path("books", 1, "title").code("INTERNAL"))
                .error(error().message("Book 7 not found").path("book").code("NOT_FOUND")));

        runner.then(graphql().client(errorClient).receive()
                .expectError(error().code("NOT_FOUND").message("@contains('not found')@").path("book"))
                .expectError(error().path("books", 1, "title")));
    }

    @Test
    @CitrusTest
    public void noMatchingErrorFails(@CitrusResource GherkinTestActionRunner runner) {
        exchange(runner, graphql().server(errorServer).send()
                .error(error().message("boom").code("INTERNAL")));

        runner.then(assertException()
                .exception(ValidationException.class)
                .message("No GraphQL error matched [NOT_FOUND]; received: [INTERNAL] boom")
                .when(graphql().client(errorClient).receive()
                        .expectError(error().code("NOT_FOUND"))));
    }

    @Test
    @CitrusTest
    public void expectedErrorsMissingFails(@CitrusResource GherkinTestActionRunner runner) {
        exchange(runner, graphql().server(errorServer).send().data("{\"books\":[]}"));

        runner.then(assertException()
                .exception(ValidationException.class)
                .message("GraphQL errors expected but none were returned")
                .when(graphql().client(errorClient).receive().expectErrors()));
    }

    @Test
    @CitrusTest
    public void partialResult(@CitrusResource GherkinTestActionRunner runner) {
        exchange(runner, graphql().server(errorServer).send()
                .data("{\"book\":{\"title\":\"Dune\",\"author\":null}}")
                .error(error().message("Author unavailable").path("book", "author").code("PARTIAL")));

        runner.then(graphql().client(errorClient).receive()
                .expectError(error().path("book", "author"))
                .data("$.book.title", "Dune"));
    }

    @Test
    @CitrusTest
    public void errorsWithClientErrorStatus(@CitrusResource GherkinTestActionRunner runner) {
        var reply = graphql().server(errorServer).send()
                .error(error().message("Validation error (FieldUndefined)").code("GRAPHQL_VALIDATION_FAILED"));
        reply.message().status(400);
        exchange(runner, reply);

        runner.then(graphql().client(errorClient).receive()
                .expectError(error().code("GRAPHQL_VALIDATION_FAILED"))
                .message()
                .status(400));
    }

    private void exchange(GherkinTestActionRunner runner, org.citrusframework.TestActionBuilder<?> reply) {
        runner.when(graphql().client(errorClient).send()
                .query("{ book(id: 7) { title } }")
                .fork(true));

        runner.then(graphql().server(errorServer).receive());

        runner.then(reply);
    }
}
