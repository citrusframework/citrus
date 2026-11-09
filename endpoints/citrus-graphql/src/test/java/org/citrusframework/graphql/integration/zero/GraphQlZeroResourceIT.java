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
 * Resource variant of {@link GraphQlZeroJavaIT}: same 6 resources x get, list, create, update,
 * delete = 30 operations with identical variables and assertions, but every GraphQL document is
 * loaded from a {@code *.graphql} resource via {@code queryResource(...)} instead of an inline
 * {@code query(...)} string.
 */
@Isolated("GraphQL ITs share Citrus Spring context state and bind dynamic/fixed ports under parallel classes")
@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GraphQlZeroResourceIT implements TestActionSupport {

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

    @Test
    @CitrusTest
    public void posts(@CitrusResource GherkinTestActionRunner runner) {
        // query GetPost
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/get-post.graphql")
                .variables("""
                    {"id": "1"}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.post.id", "1")
                .data("$.post.title", "sunt aut facere repellat provident occaecati excepturi optio reprehenderit")
                .data("$.post.user.name", "Leanne Graham")
                .message()
                .status(200));

        // query ListPosts
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/list-posts.graphql")
                .variables("""
                    {"options": {"paginate": {"page": 1, "limit": 2}}}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.posts.meta.totalCount", 100)
                .data("$.posts.data.length()", 2)
                .data("$.posts.data[0].id", "1")
                .data("$.posts.data[1].title", "qui est esse")
                .message()
                .status(200));

        // mutation CreatePost
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/create-post.graphql")
                .variables("""
                    {"input": {"title": "Citrus", "body": "GraphQL rocks"}}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.createPost.id", "101")
                .data("$.createPost.title", "Citrus")
                .data("$.createPost.body", "GraphQL rocks")
                .message()
                .status(200));

        // mutation UpdatePost
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/update-post.graphql")
                .variables("""
                    {"id": "1", "input": {"body": "Updated by Citrus"}}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.updatePost.id", "1")
                .data("$.updatePost.body", "Updated by Citrus")
                .message()
                .status(200));

        // mutation DeletePost
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/delete-post.graphql")
                .variables("""
                    {"id": "1"}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.deletePost", true)
                .message()
                .status(200));
    }

    @Test
    @CitrusTest
    public void users(@CitrusResource GherkinTestActionRunner runner) {
        // query GetUser
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/get-user.graphql")
                .variables("""
                    {"id": "1"}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.user.name", "Leanne Graham")
                .data("$.user.username", "Bret")
                .data("$.user.email", "Sincere@april.biz")
                .data("$.user.address.city", "Gwenborough")
                .message()
                .status(200));

        // query ListUsers
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/list-users.graphql")
                .variables("""
                    {"options": {"paginate": {"page": 1, "limit": 2}}}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.users.meta.totalCount", 10)
                .data("$.users.data.length()", 2)
                .data("$.users.data[0].id", "1")
                .data("$.users.data[1].name", "Ervin Howell")
                .message()
                .status(200));

        // mutation CreateUser
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/create-user.graphql")
                .variables("""
                    {"input": {"name": "Citrus Tester", "username": "citrus", "email": "citrus@example.org"}}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.createUser.id", "11")
                .data("$.createUser.username", "citrus")
                .data("$.createUser.email", "citrus@example.org")
                .message()
                .status(200));

        // mutation UpdateUser
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/update-user.graphql")
                .variables("""
                    {"id": "1", "input": {"name": "Leanne Citrus"}}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.updateUser.id", "1")
                .data("$.updateUser.name", "Leanne Citrus")
                .message()
                .status(200));

        // mutation DeleteUser
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/delete-user.graphql")
                .variables("""
                    {"id": "1"}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.deleteUser", true)
                .message()
                .status(200));
    }

    @Test
    @CitrusTest
    public void comments(@CitrusResource GherkinTestActionRunner runner) {
        // query GetComment
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/get-comment.graphql")
                .variables("""
                    {"id": "1"}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.comment.name", "id labore ex et quam laborum")
                .data("$.comment.email", "Eliseo@gardner.biz")
                .data("$.comment.post.id", "1")
                .message()
                .status(200));

        // query ListComments
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/list-comments.graphql")
                .variables("""
                    {"options": {"paginate": {"page": 1, "limit": 2}}}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.comments.meta.totalCount", 500)
                .data("$.comments.data.length()", 2)
                .data("$.comments.data[0].id", "1")
                .message()
                .status(200));

        // mutation CreateComment
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/create-comment.graphql")
                .variables("""
                    {"input": {"name": "Citrus", "email": "citrus@example.org", "body": "Nice post"}}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.createComment.id", "501")
                .data("$.createComment.body", "Nice post")
                .message()
                .status(200));

        // mutation UpdateComment
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/update-comment.graphql")
                .variables("""
                    {"id": "1", "input": {"body": "Edited by Citrus"}}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.updateComment.id", "1")
                .data("$.updateComment.body", "Edited by Citrus")
                .message()
                .status(200));

        // mutation DeleteComment
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/delete-comment.graphql")
                .variables("""
                    {"id": "1"}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.deleteComment", true)
                .message()
                .status(200));
    }

    @Test
    @CitrusTest
    public void todos(@CitrusResource GherkinTestActionRunner runner) {
        // query GetTodo
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/get-todo.graphql")
                .variables("""
                    {"id": "1"}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.todo.title", "delectus aut autem")
                .data("$.todo.completed", false)
                .data("$.todo.user.id", "1")
                .message()
                .status(200));

        // query ListTodos
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/list-todos.graphql")
                .variables("""
                    {"options": {"paginate": {"page": 1, "limit": 2}}}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.todos.meta.totalCount", 200)
                .data("$.todos.data.length()", 2)
                .data("$.todos.data[0].id", "1")
                .message()
                .status(200));

        // mutation CreateTodo
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/create-todo.graphql")
                .variables("""
                    {"input": {"title": "Write GraphQL tests", "completed": false}}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.createTodo.id", "201")
                .data("$.createTodo.title", "Write GraphQL tests")
                .data("$.createTodo.completed", false)
                .message()
                .status(200));

        // mutation UpdateTodo
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/update-todo.graphql")
                .variables("""
                    {"id": "1", "input": {"completed": true}}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.updateTodo.id", "1")
                .data("$.updateTodo.completed", true)
                .message()
                .status(200));

        // mutation DeleteTodo
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/delete-todo.graphql")
                .variables("""
                    {"id": "1"}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.deleteTodo", true)
                .message()
                .status(200));
    }

    @Test
    @CitrusTest
    public void albums(@CitrusResource GherkinTestActionRunner runner) {
        // query GetAlbum
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/get-album.graphql")
                .variables("""
                    {"id": "1"}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.album.title", "quidem molestiae enim")
                .data("$.album.user.id", "1")
                .message()
                .status(200));

        // query ListAlbums
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/list-albums.graphql")
                .variables("""
                    {"options": {"paginate": {"page": 1, "limit": 2}}}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.albums.meta.totalCount", 100)
                .data("$.albums.data.length()", 2)
                .data("$.albums.data[0].id", "1")
                .message()
                .status(200));

        // mutation CreateAlbum
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/create-album.graphql")
                .variables("""
                    {"input": {"title": "Citrus album", "userId": "1"}}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.createAlbum.id", "101")
                .data("$.createAlbum.title", "Citrus album")
                .message()
                .status(200));

        // mutation UpdateAlbum
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/update-album.graphql")
                .variables("""
                    {"id": "1", "input": {"title": "Renamed by Citrus"}}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.updateAlbum.id", "1")
                .data("$.updateAlbum.title", "Renamed by Citrus")
                .message()
                .status(200));

        // mutation DeleteAlbum
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/delete-album.graphql")
                .variables("""
                    {"id": "1"}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.deleteAlbum", true)
                .message()
                .status(200));
    }

    @Test
    @CitrusTest
    public void photos(@CitrusResource GherkinTestActionRunner runner) {
        // query GetPhoto
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/get-photo.graphql")
                .variables("""
                    {"id": "1"}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.photo.title", "accusamus beatae ad facilis cum similique qui sunt")
                .data("$.photo.url", "https://picsum.photos/seed/1/600")
                .data("$.photo.album.id", "1")
                .message()
                .status(200));

        // query ListPhotos
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/list-photos.graphql")
                .variables("""
                    {"options": {"paginate": {"page": 1, "limit": 2}}}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.photos.meta.totalCount", 5000)
                .data("$.photos.data.length()", 2)
                .data("$.photos.data[0].id", "1")
                .message()
                .status(200));

        // mutation CreatePhoto
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/create-photo.graphql")
                .variables("""
                    {"input": {"title": "Citrus", "url": "https://example.org/citrus.png", "thumbnailUrl": "https://example.org/citrus-t.png"}}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.createPhoto.id", "5001")
                .data("$.createPhoto.url", "https://example.org/citrus.png")
                .message()
                .status(200));

        // mutation UpdatePhoto
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/update-photo.graphql")
                .variables("""
                    {"id": "1", "input": {"title": "Retitled by Citrus"}}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.updatePhoto.id", "1")
                .data("$.updatePhoto.title", "Retitled by Citrus")
                .message()
                .status(200));

        // mutation DeletePhoto
        runner.run(graphql().client(graphqlZero)
                .send()
                .queryResource("classpath:org/citrusframework/graphql/integration/zero/delete-photo.graphql")
                .variables("""
                    {"id": "1"}"""));

        runner.run(graphql().client(graphqlZero)
                .receive()
                .data("$.deletePhoto", true)
                .message()
                .status(200));
    }
}
