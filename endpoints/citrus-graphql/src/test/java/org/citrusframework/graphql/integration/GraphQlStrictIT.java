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
 * The strict switch in all four directions: client send, client receive, simulator receive and
 * simulator send, each with checks on and off.
 */
@Isolated("GraphQL ITs share Citrus Spring context state and bind dynamic/fixed ports under parallel classes")
@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GraphQlStrictIT implements TestActionSupport {

    private final int port = findAvailableTcpPort(20000, 32767);

    @BindToRegistry
    private final GraphQlServer strictServer = GraphQlEndpoints.graphql().server()
            .port(port)
            .autoStart(true)
            .timeout(3000L)
            .build();

    @BindToRegistry
    private final GraphQlClient strictClient = GraphQlEndpoints.graphql().client()
            .requestUrl("http://localhost:%d".formatted(port))
            .timeout(3000L)
            .build();

    @Test
    @CitrusTest
    public void clientSendStrictRejectsMalformedDocument(@CitrusResource GherkinTestActionRunner runner) {
        runner.then(assertException()
                .exception(ValidationException.class)
                .message("@startsWith('Invalid GraphQL document (line 1')@")
                .when(graphql().client(strictClient).send().query("query { book(id: 1) { title }")));
    }

    @Test
    @CitrusTest
    public void clientSendNotStrictDeliversMalformedDocumentAndSimulatorAcceptsIt(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(graphql().client(strictClient).send()
                .strict(false)
                .query("query { book(id: 1) { title }")
                .fork(true));

        runner.then(graphql().server(strictServer).receive()
                .strict(false)
                .query("@contains('book(id: 1)')@"));

        runner.then(graphql().server(strictServer).send()
                .error(error().message("Syntax error")));

        runner.then(graphql().client(strictClient).receive()
                .expectError(error().message("Syntax error")));
    }

    @Test
    @CitrusTest
    public void clientSendStrictRejectsMutationOverGet(@CitrusResource GherkinTestActionRunner runner) {
        runner.then(assertException()
                .exception(ValidationException.class)
                .message("GraphQL mutation operations cannot be sent over GET - only queries can")
                .when(graphql().client(strictClient).send().get().query("mutation { deleteBook(id: 1) }")));
    }

    @Test
    @CitrusTest
    public void clientSendNotStrictDeliversMutationOverGet(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(graphql().client(strictClient).send()
                .strict(false)
                .get()
                .query("mutation { deleteBook(id: 1) }")
                .fork(true));

        runner.then(graphql().server(strictServer).receive()
                .query("mutation { deleteBook(id: 1) }")
                .method("GET"));

        runner.then(graphql().server(strictServer).send()
                .error(error().message("Mutations are not allowed over GET")));

        runner.then(graphql().client(strictClient).receive()
                .expectErrors());
    }

    @Test
    @CitrusTest
    public void simulatorReceiveStrictRejectsMalformedDocument(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(graphql().client(strictClient).send()
                .strict(false)
                .query("{ broken")
                .fork(true));

        runner.then(assertException()
                .exception(ValidationException.class)
                .message("@startsWith('Invalid GraphQL document')@")
                .when(graphql().server(strictServer).receive()));

        runner.then(graphql().server(strictServer).send()
                .error(error().message("Syntax error")));

        runner.then(graphql().client(strictClient).receive()
                .expectErrors());
    }

    @Test
    @CitrusTest
    public void simulatorSendStrictRejectsMalformedReply(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(graphql().client(strictClient).send()
                .query("{ book(id: 1) { title } }")
                .fork(true));

        runner.then(graphql().server(strictServer).receive());

        var malformed = graphql().server(strictServer).send();
        malformed.message().body("{\"book\":{\"title\":\"Dune\"}}");
        runner.then(assertException()
                .exception(ValidationException.class)
                .message("Invalid GraphQL reply: body has neither 'data' nor 'errors'")
                .when(malformed));

        runner.then(graphql().server(strictServer).send()
                .data("{\"book\":{\"title\":\"Dune\"}}"));

        runner.then(graphql().client(strictClient).receive()
                .data("$.book.title", "Dune"));
    }

    @Test
    @CitrusTest
    public void clientReceiveStrictRejectsMalformedReply(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(graphql().client(strictClient).send()
                .query("{ book(id: 1) { title } }")
                .fork(true));

        runner.then(graphql().server(strictServer).receive());

        var malformed = graphql().server(strictServer).send().strict(false);
        malformed.message().body("{\"unexpected\":true}");
        runner.then(malformed);

        runner.then(assertException()
                .exception(ValidationException.class)
                .message("@startsWith('Expected GraphQL response but got content type')@")
                .when(graphql().client(strictClient).receive()));
    }

    @Test
    @CitrusTest
    public void clientReceiveNotStrictAcceptsMalformedReply(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(graphql().client(strictClient).send()
                .query("{ book(id: 1) { title } }")
                .fork(true));

        runner.then(graphql().server(strictServer).receive());

        var malformed = graphql().server(strictServer).send().strict(false);
        malformed.message().body("{\"unexpected\":true}");
        runner.then(malformed);

        runner.then(graphql().client(strictClient).receive()
                .strict(false)
                .message()
                .body("{\"unexpected\":true}"));
    }
}
