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

import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

/**
 * Schema validation of documents on both sides; the schema is split across several SDL files.
 */
@Test
public class GraphQlSchemaIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    private static final String[] SCHEMA = {
            "classpath:org/citrusframework/graphql/schema/query.graphqls",
            "classpath:org/citrusframework/graphql/schema/book.graphqls",
            "classpath:org/citrusframework/graphql/schema/scalars.graphqls"
    };

    private final int port = findAvailableTcpPort(18780);

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

    @CitrusTest
    public void validDocumentPassesOnBothSides() {
        when(graphql().client(schemaClient).send()
                .query("{ booksSince(date: \"2026-01-01\") { title published } }")
                .fork(true));

        then(graphql().server(schemaServer).receive());

        then(graphql().server(schemaServer).send()
                .data("{\"booksSince\":[]}"));

        then(graphql().client(schemaClient).receive()
                .data("$.booksSince.length()", 0));
    }

    @CitrusTest
    public void clientRejectsInvalidDocumentBeforeSending() {
        then(assertException()
                .exception(ValidationException.class)
                .message("@contains('isbn')@")
                .when(graphql().client(schemaClient).send()
                        .query("{ book(id: 1) { isbn } }")));
    }

    @CitrusTest
    public void simulatorRejectsInvalidIncomingDocument() {
        when(graphql().client(schemaClient).send()
                .strict(false)
                .query("{ book(id: 1) { isbn } }")
                .fork(true));

        then(assertException()
                .exception(ValidationException.class)
                .message("@startsWith('GraphQL document does not match the schema')@")
                .when(graphql().server(schemaServer).receive()));

        then(graphql().server(schemaServer).send()
                .error(org.citrusframework.api.graphql.GraphQlError.error().message("Field 'isbn' is undefined")));

        then(graphql().client(schemaClient).receive()
                .expectErrors());
    }
}
