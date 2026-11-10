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
import static org.citrusframework.dsl.MessageSupport.MessageBodySupport.fromBody;
import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

/**
 * Edge cases against the {@link GraphQlZeroSimulator}: GET transport, schema validation before
 * sending, server-side errors, unexpected errors, null data, multi-operation documents, aliases
 * and fragments, resource documents and chaining values between requests.
 *
 * <p>Unlike the public Apollo server, the simulator needs no CSRF preflight header for GET and
 * answers GraphQL errors with status 200.
 */
@Isolated("GraphQL ITs share Citrus Spring context state and bind dynamic/fixed ports under parallel classes")
@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GraphQlZeroEdgeCasesIT implements TestActionSupport {

    private final int port = findAvailableTcpPort(20000, 32767);

    @BindToRegistry
    private final GraphQlServer zeroServer = GraphQlEndpoints.graphql().server()
            .port(port)
            .endpointAdapter(new GraphQlZeroSimulator())
            .autoStart(true)
            .timeout(5000L)
            .build();

    @BindToRegistry
    private final GraphQlClient graphqlZero = GraphQlEndpoints.graphql().client()
            .requestUrl("http://localhost:%d".formatted(port))
            .schema("classpath:org/citrusframework/graphql/integration/zero/graphqlzero.graphqls")
            .timeout(5000L)
            .build();

    /** The simulator serves GET without any preflight header. */
    @Test
    @CitrusTest
    public void queryOverGet(@CitrusResource GherkinTestActionRunner runner) {
        runner.run(graphql().client(graphqlZero)
                .send()
                .get()
                .query("""
                    query ListPosts($options: PageQueryOptions) {
                      posts(options: $options) {
                        data { id title }
                        meta { totalCount }
                      }
                    }""")
                .variables("""
                    {"options": {"paginate": {"page": 1, "limit": 2}}}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.posts.meta.totalCount", 100)
                .data("$.posts.data.length()", 2)
                .data("$.posts.data[0].id", "1")
                .message()
                .status(200));
    }

    /** The downloaded schema rejects the document before anything is sent. */
    @Test
    @CitrusTest
    public void schemaRejectsUnknownFieldBeforeSending(@CitrusResource GherkinTestActionRunner runner) {
        runner.run(assertException()
                .exception(ValidationException.class)
                .message("@contains('isbn')@")
                .when(graphql().client(graphqlZero)
                        .send()
                        .query("{ post(id: 1) { isbn } }")));
    }

    /** Deleting an unknown user is a GraphQL error, matched by code and message. */
    @Test
    @CitrusTest
    public void deleteUnknownUserReturnsNotFound(@CitrusResource GherkinTestActionRunner runner) {
        runner.run(graphql().client(graphqlZero)
                .send()
                .query("""
                    mutation DeleteUser($id: ID!) {
                      deleteUser(id: $id)
                    }""")
                .variable("id", "7"));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .expectError(error().code("NOT_FOUND").message("User 7 not found").path("deleteUser"))
                .message()
                .status(200));
    }

    @Test
    @CitrusTest
    public void unexpectedErrorsFailTheTest(@CitrusResource GherkinTestActionRunner runner) {
        runner.run(graphql().client(graphqlZero)
                .send()
                .query("""
                    mutation DeleteUser($id: ID!) {
                      deleteUser(id: $id)
                    }""")
                .variable("id", "7"));

        runner.run(assertException()
                .exception(ValidationException.class)
                .message("@startsWith('GraphQL response contains 1 unexpected error: [NOT_FOUND]')@")
                .when(graphql().client(graphqlZero).receive()));
    }

    /** An unknown id is not an error: the fields come back null. */
    @Test
    @CitrusTest
    public void unknownIdReturnsNullFields(@CitrusResource GherkinTestActionRunner runner) {
        runner.run(graphql().client(graphqlZero)
                .send()
                .query("query GetPost($id: ID!) { post(id: $id) { id title } }")
                .variable("id", "9999"));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("{\"post\": {\"id\": null, \"title\": null}}"));
    }

    @Test
    @CitrusTest
    public void multiOperationDocumentWithOperationName(@CitrusResource GherkinTestActionRunner runner) {
        runner.run(graphql().client(graphqlZero)
                .send()
                .query("""
                        query GetUser { user(id: 2) { name } }
                        query GetTodo { todo(id: 2) { title completed } }""")
                .operationName("GetTodo"));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("{\"todo\": {\"title\": \"@ignore@\", \"completed\": \"@ignore@\"}}"));
    }

    @Test
    @CitrusTest
    public void aliasesAndFragments(@CitrusResource GherkinTestActionRunner runner) {
        runner.run(graphql().client(graphqlZero)
                .send()
                .query("""
                        query Users {
                          first: user(id: 1) { ...UserFields }
                          second: user(id: 2) { ...UserFields }
                        }
                        fragment UserFields on User { name username }"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.first.username", "Bret")
                .data("$.second.name", "Ervin Howell"));
    }

    @Test
    @CitrusTest
    public void nestedQueryFromResourceWithTypedOptions(@CitrusResource GherkinTestActionRunner runner) {
        runner.given(createVariable("limit", "3"));

        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/post-with-comments.graphql")
                .variables("{\"id\": \"1\", \"options\": {\"paginate\": {\"page\": 1, \"limit\": ${limit}}}}"));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.post.comments.data.length()", 3)
                .data("$.post.comments.meta.totalCount", 5)
                .data("$.post.comments.data[0].email", "Eliseo@gardner.biz"));
    }

    /** A value extracted from one response drives the next request. */
    @Test
    @CitrusTest
    public void chainValuesBetweenRequests(@CitrusResource GherkinTestActionRunner runner) {
        runner.run(graphql().client(graphqlZero)
                .send()
                .query("query GetPost($id: ID!) { post(id: $id) { user { id } } }")
                .variable("id", "1"));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .message()
                .extract(fromBody().expression("$.data.post.user.id", "authorId")));

        runner.run(graphql().client(graphqlZero)
                .send()
                .query("query GetUser($id: ID!) { user(id: $id) { id username } }")
                .variable("id", "${authorId}"));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.user.id", "${authorId}")
                .data("$.user.username", "Bret"));
    }
}
