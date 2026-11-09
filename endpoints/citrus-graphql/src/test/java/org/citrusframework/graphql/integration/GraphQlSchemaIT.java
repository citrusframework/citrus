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

import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

/**
 * Schema validation of documents on both sides; the schema is split across several SDL files.
 */
@Isolated("GraphQL ITs share Citrus Spring context state and bind dynamic/fixed ports under parallel classes")
@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GraphQlSchemaIT implements TestActionSupport {

    private static final String[] SCHEMA = {
            "classpath:org/citrusframework/graphql/schema/query.graphqls",
            "classpath:org/citrusframework/graphql/schema/book.graphqls",
            "classpath:org/citrusframework/graphql/schema/scalars.graphqls"
    };

    private final int port = findAvailableTcpPort(20000, 32767);

    @BindToRegistry
    private final GraphQlServer schemaServer = GraphQlEndpoints.graphql().server()
            .port(port)
            .autoStart(true)
            .timeout(3000L)
            .schema(SCHEMA)
            .build();

    @BindToRegistry
    private final GraphQlClient schemaClient = GraphQlEndpoints.graphql().client()
            .requestUrl("http://localhost:%d".formatted(port))
            .timeout(3000L)
            .schema(SCHEMA)
            .build();

    @Test
    @CitrusTest
    public void validDocumentPassesOnBothSides(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(graphql().client(schemaClient).send()
                .query("{ booksSince(date: \"2026-01-01\") { title published } }")
                .fork(true));

        runner.then(graphql().server(schemaServer).receive());

        runner.then(graphql().server(schemaServer).send()
                .data("{\"booksSince\":[]}"));

        runner.then(graphql().client(schemaClient).receive()
                .data("$.booksSince.length()", 0));
    }

    @Test
    @CitrusTest
    public void clientRejectsInvalidDocumentBeforeSending(@CitrusResource GherkinTestActionRunner runner) {
        runner.then(assertException()
                .exception(ValidationException.class)
                .message("@contains('isbn')@")
                .when(graphql().client(schemaClient).send()
                        .query("{ book(id: 1) { isbn } }")));
    }

    @Test
    @CitrusTest
    public void simulatorRejectsInvalidIncomingDocument(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(graphql().client(schemaClient).send()
                .strict(false)
                .query("{ book(id: 1) { isbn } }")
                .fork(true));

        runner.then(assertException()
                .exception(ValidationException.class)
                .message("@startsWith('GraphQL document does not match the schema')@")
                .when(graphql().server(schemaServer).receive()));

        runner.then(graphql().server(schemaServer).send()
                .error(org.citrusframework.api.graphql.GraphQlError.error().message("Field 'isbn' is undefined")));

        runner.then(graphql().client(schemaClient).receive()
                .expectErrors());
    }
}
