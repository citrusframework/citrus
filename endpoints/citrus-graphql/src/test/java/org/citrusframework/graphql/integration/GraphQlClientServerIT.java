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
import org.citrusframework.graphql.client.GraphQlClient;
import org.citrusframework.graphql.endpoint.builder.GraphQlEndpoints;
import org.citrusframework.graphql.server.GraphQlServer;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

import static org.citrusframework.dsl.MessageSupport.MessageHeaderSupport.fromHeaders;
import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

/**
 * A GraphQL client calling the GraphQL simulator: queries and mutations over POST and GET.
 */
@Test
public class GraphQlClientServerIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    private final int port = findAvailableTcpPort(18580);

    @BindToRegistry
    private final GraphQlServer bookServer = GraphQlEndpoints.graphql().server()
            .port(port)
            .autoStart(true)
            .timeout(5000L)
            .build();

    @BindToRegistry
    private final GraphQlClient bookClient = GraphQlEndpoints.graphql().client()
            .requestUrl("http://localhost:%d".formatted(port))
            .timeout(5000L)
            .build();

    @CitrusTest
    public void queryOverPost() {
        variable("id", "42");

        when(graphql().client(bookClient).send()
                .query("query Book($id: ID!) { book(id: $id) { title } }")
                .operationName("Book")
                .variable("id", "${id}")
                .fork(true));

        then(graphql().server(bookServer).receive()
                .operationName("Book")
                .query("query Book($id: ID!) {\n  book(id: $id) { title }\n}")
                .variable("id", "${id}")
                .method("POST"));

        then(graphql().server(bookServer).send()
                .data("{\"book\":{\"title\":\"Dune\"}}"));

        then(graphql().client(bookClient).receive()
                .data("$.book.title", "Dune")
                .message()
                .status(200)
                .header("Content-Type", "@startsWith('application/graphql-response+json')@"));
    }

    @CitrusTest
    public void mutationOverPost() {
        when(graphql().client(bookClient).send()
                .query("mutation AddBook($title: String!) { addBook(title: $title) { id } }")
                .variable("title", "Dune")
                .fork(true));

        then(graphql().server(bookServer).receive()
                .operationName("AddBook")
                .variable("title", "Dune")
                .message()
                .header("citrus_graphql_operation_type", "mutation"));

        then(graphql().server(bookServer).send()
                .data("{\"addBook\":{\"id\":\"7\"}}"));

        then(graphql().client(bookClient).receive()
                .data("$.addBook.id", "7"));
    }

    @CitrusTest
    public void queryOverGet() {
        when(graphql().client(bookClient).send()
                .get()
                .query("query Books($first: Int, $filter: String) { books(first: $first, filter: $filter) { title } }")
                .variable("first", 10)
                .variable("filter", "a, b & c")
                .fork(true));

        then(graphql().server(bookServer).receive()
                .operationName("Books")
                .variable("first", 10)
                .variable("filter", "a, b & c")
                .query("query Books($first: Int, $filter: String) { books(first: $first, filter: $filter) { title } }")
                .method("GET"));

        then(graphql().server(bookServer).send()
                .data("{\"books\":[{\"title\":\"Dune\"},{\"title\":\"Emma\"}]}"));

        then(graphql().client(bookClient).receive()
                .data("$.books[1].title", "Emma")
                .data("$.books.length()", 2));
    }

    @CitrusTest
    public void queryFromResourceWithTypedVariables() {
        variable("id", "42");

        when(graphql().client(bookClient).send()
                .queryResource("classpath:org/citrusframework/graphql/integration/book.graphql")
                .variables("{\"id\": \"${id}\", \"limit\": ${limit}}".replace("${limit}", "3"))
                .fork(true));

        then(graphql().server(bookServer).receive()
                .operationName("Book")
                .variables("{\"id\":\"42\",\"limit\":3}")
                .variable("limit", "@isNumber()@"));

        then(graphql().server(bookServer).send()
                .data("{\"book\":{\"title\":\"Dune\"}}"));

        then(graphql().client(bookClient).receive()
                .data("{\"book\":{\"title\":\"@ignore@\"}}"));
    }

    @CitrusTest
    public void extractOperationHeaders() {
        when(graphql().client(bookClient).send()
                .query("query Book { book(id: 1) { title } }")
                .fork(true));

        then(graphql().server(bookServer).receive()
                .message()
                .extract(fromHeaders()
                        .header("citrus_graphql_operation_name", "operationName")
                        .header("citrus_graphql_operation_type", "operationType")));

        then(graphql().server(bookServer).send()
                .data("{\"book\":{\"title\":\"${operationName}-${operationType}\"}}"));

        then(graphql().client(bookClient).receive()
                .data("$.book.title", "Book-query"));
    }
}
