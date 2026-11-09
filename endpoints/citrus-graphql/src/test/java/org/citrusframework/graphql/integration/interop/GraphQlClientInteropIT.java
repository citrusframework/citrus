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

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.graphql.client.GraphQlClient;
import org.citrusframework.graphql.endpoint.builder.GraphQlEndpoints;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import static org.citrusframework.api.graphql.GraphQlError.error;
import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

/**
 * The GraphQL client against a real Spring GraphQL server.
 */
@Isolated("GraphQL ITs share Citrus Spring context state and bind dynamic/fixed ports under parallel classes")
@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GraphQlClientInteropIT implements TestActionSupport {

    private static final int port = findAvailableTcpPort(20000, 32767);

    private static SpringGraphQlServer springServer;

    @BindToRegistry
    private final GraphQlClient springGraphQl = GraphQlEndpoints.graphql().client()
            .requestUrl("http://localhost:%d".formatted(port))
            .timeout(5000L)
            .build();

    @BeforeAll
    static void startSpringGraphQl() throws Exception {
        springServer = SpringGraphQlServer.start(port);
    }

    @AfterAll
    static void stopSpringGraphQl() throws Exception {
        if (springServer != null) {
            springServer.close();
        }
    }

    @Test
    @CitrusTest
    public void readsData(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(graphql().client(springGraphQl).send()
                .query("query Book($id: ID!) { book(id: $id) { id title } }")
                .variable("id", "1"));

        runner.then(graphql().client(springGraphQl).receive()
                .data("{\"book\":{\"id\":\"1\",\"title\":\"Dune\"}}")
                .message()
                .status(200)
                .header("Content-Type", "@startsWith('application/graphql-response+json')@"));
    }

    @Test
    @CitrusTest
    public void readsFieldErrors(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(graphql().client(springGraphQl).send()
                .query("{ book(id: \"7\") { title } }"));

        runner.then(graphql().client(springGraphQl).receive()
                .expectError(error().message("Book 7 not found").path("book").code("NOT_FOUND"))
                .message()
                .status(200));
    }

    @Test
    @CitrusTest
    public void failsOnUnexpectedFieldErrors(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(graphql().client(springGraphQl).send()
                .query("{ book(id: \"7\") { title } }"));

        runner.then(assertException()
                .exception(ValidationException.class)
                .message("@startsWith('GraphQL response contains 1 unexpected error: [NOT_FOUND] Book 7 not found at book')@")
                .when(graphql().client(springGraphQl).receive()));
    }

    @Test
    @CitrusTest
    public void serverRejectsInvalidDocumentWithClientError(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(graphql().client(springGraphQl).send()
                .strict(false)
                .query("{ book(id: \"1\") { isbn } }"));

        runner.then(graphql().client(springGraphQl).receive()
                .expectError(error().message("@contains('isbn')@"))
                .message()
                .status(400));
    }

    @Test
    @CitrusTest
    public void serverAnswersLegacyJsonWithOk(@CitrusResource GherkinTestActionRunner runner) {
        var request = graphql().client(springGraphQl).send()
                .strict(false)
                .query("{ book(id: \"1\") { isbn } }");
        request.message().accept("application/json");
        runner.when(request);

        runner.then(graphql().client(springGraphQl).receive()
                .expectErrors()
                .message()
                .status(200)
                .header("Content-Type", "@startsWith('application/json')@"));
    }
}
