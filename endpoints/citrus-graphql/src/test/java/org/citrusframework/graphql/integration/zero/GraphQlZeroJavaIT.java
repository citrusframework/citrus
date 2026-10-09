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

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.graphql.client.GraphQlClient;
import org.citrusframework.graphql.endpoint.builder.GraphQlEndpoints;
import org.citrusframework.graphql.server.GraphQlServer;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

/**
 * Java DSL against the {@link GraphQlZeroSimulator}: every query and mutation of the GraphQLZero
 * API (6 resources x get, list, create, update, delete = 30 operations). Documents are validated
 * against the schema before sending; responses must contain no GraphQL errors.
 * <p>
 * Generated from the same operation table as the XML and YAML tests.
 */
@Test
public class GraphQlZeroJavaIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    private final int port = findAvailableTcpPort(19580);

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

    @CitrusTest
    public void posts() {
        // query GetPost
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    query GetPost($id: ID!) {
                      post(id: $id) {
                        id
                        title
                        user { id name }
                      }
                    }""")
                .variables("""
                    {"id": "1"}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.post.id", "1")
                .data("$.post.title", "sunt aut facere repellat provident occaecati excepturi optio reprehenderit")
                .data("$.post.user.name", "Leanne Graham")
                .message()
                .status(200));

        // query ListPosts
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    query ListPosts($options: PageQueryOptions) {
                      posts(options: $options) {
                        data { id title }
                        meta { totalCount }
                      }
                    }""")
                .variables("""
                    {"options": {"paginate": {"page": 1, "limit": 2}}}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.posts.meta.totalCount", 100)
                .data("$.posts.data.length()", 2)
                .data("$.posts.data[0].id", "1")
                .data("$.posts.data[1].title", "qui est esse")
                .message()
                .status(200));

        // mutation CreatePost
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    mutation CreatePost($input: CreatePostInput!) {
                      createPost(input: $input) { id title body }
                    }""")
                .variables("""
                    {"input": {"title": "Citrus", "body": "GraphQL rocks"}}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.createPost.id", "101")
                .data("$.createPost.title", "Citrus")
                .data("$.createPost.body", "GraphQL rocks")
                .message()
                .status(200));

        // mutation UpdatePost
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    mutation UpdatePost($id: ID!, $input: UpdatePostInput!) {
                      updatePost(id: $id, input: $input) { id body }
                    }""")
                .variables("""
                    {"id": "1", "input": {"body": "Updated by Citrus"}}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.updatePost.id", "1")
                .data("$.updatePost.body", "Updated by Citrus")
                .message()
                .status(200));

        // mutation DeletePost
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    mutation DeletePost($id: ID!) {
                      deletePost(id: $id)
                    }""")
                .variables("""
                    {"id": "1"}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.deletePost", true)
                .message()
                .status(200));
    }

    @CitrusTest
    public void users() {
        // query GetUser
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    query GetUser($id: ID!) {
                      user(id: $id) {
                        id
                        name
                        username
                        email
                        address { city }
                      }
                    }""")
                .variables("""
                    {"id": "1"}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.user.name", "Leanne Graham")
                .data("$.user.username", "Bret")
                .data("$.user.email", "Sincere@april.biz")
                .data("$.user.address.city", "Gwenborough")
                .message()
                .status(200));

        // query ListUsers
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    query ListUsers($options: PageQueryOptions) {
                      users(options: $options) {
                        data { id name }
                        meta { totalCount }
                      }
                    }""")
                .variables("""
                    {"options": {"paginate": {"page": 1, "limit": 2}}}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.users.meta.totalCount", 10)
                .data("$.users.data.length()", 2)
                .data("$.users.data[0].id", "1")
                .data("$.users.data[1].name", "Ervin Howell")
                .message()
                .status(200));

        // mutation CreateUser
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    mutation CreateUser($input: CreateUserInput!) {
                      createUser(input: $input) { id name username email }
                    }""")
                .variables("""
                    {"input": {"name": "Citrus Tester", "username": "citrus", "email": "citrus@example.org"}}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.createUser.id", "11")
                .data("$.createUser.username", "citrus")
                .data("$.createUser.email", "citrus@example.org")
                .message()
                .status(200));

        // mutation UpdateUser
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    mutation UpdateUser($id: ID!, $input: UpdateUserInput!) {
                      updateUser(id: $id, input: $input) { id name }
                    }""")
                .variables("""
                    {"id": "1", "input": {"name": "Leanne Citrus"}}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.updateUser.id", "1")
                .data("$.updateUser.name", "Leanne Citrus")
                .message()
                .status(200));

        // mutation DeleteUser
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    mutation DeleteUser($id: ID!) {
                      deleteUser(id: $id)
                    }""")
                .variables("""
                    {"id": "1"}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.deleteUser", true)
                .message()
                .status(200));
    }

    @CitrusTest
    public void comments() {
        // query GetComment
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    query GetComment($id: ID!) {
                      comment(id: $id) {
                        id
                        name
                        email
                        post { id }
                      }
                    }""")
                .variables("""
                    {"id": "1"}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.comment.name", "id labore ex et quam laborum")
                .data("$.comment.email", "Eliseo@gardner.biz")
                .data("$.comment.post.id", "1")
                .message()
                .status(200));

        // query ListComments
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    query ListComments($options: PageQueryOptions) {
                      comments(options: $options) {
                        data { id }
                        meta { totalCount }
                      }
                    }""")
                .variables("""
                    {"options": {"paginate": {"page": 1, "limit": 2}}}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.comments.meta.totalCount", 500)
                .data("$.comments.data.length()", 2)
                .data("$.comments.data[0].id", "1")
                .message()
                .status(200));

        // mutation CreateComment
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    mutation CreateComment($input: CreateCommentInput!) {
                      createComment(input: $input) { id name email body }
                    }""")
                .variables("""
                    {"input": {"name": "Citrus", "email": "citrus@example.org", "body": "Nice post"}}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.createComment.id", "501")
                .data("$.createComment.body", "Nice post")
                .message()
                .status(200));

        // mutation UpdateComment
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    mutation UpdateComment($id: ID!, $input: UpdateCommentInput!) {
                      updateComment(id: $id, input: $input) { id body }
                    }""")
                .variables("""
                    {"id": "1", "input": {"body": "Edited by Citrus"}}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.updateComment.id", "1")
                .data("$.updateComment.body", "Edited by Citrus")
                .message()
                .status(200));

        // mutation DeleteComment
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    mutation DeleteComment($id: ID!) {
                      deleteComment(id: $id)
                    }""")
                .variables("""
                    {"id": "1"}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.deleteComment", true)
                .message()
                .status(200));
    }

    @CitrusTest
    public void todos() {
        // query GetTodo
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    query GetTodo($id: ID!) {
                      todo(id: $id) {
                        id
                        title
                        completed
                        user { id }
                      }
                    }""")
                .variables("""
                    {"id": "1"}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.todo.title", "delectus aut autem")
                .data("$.todo.completed", false)
                .data("$.todo.user.id", "1")
                .message()
                .status(200));

        // query ListTodos
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    query ListTodos($options: PageQueryOptions) {
                      todos(options: $options) {
                        data { id }
                        meta { totalCount }
                      }
                    }""")
                .variables("""
                    {"options": {"paginate": {"page": 1, "limit": 2}}}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.todos.meta.totalCount", 200)
                .data("$.todos.data.length()", 2)
                .data("$.todos.data[0].id", "1")
                .message()
                .status(200));

        // mutation CreateTodo
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    mutation CreateTodo($input: CreateTodoInput!) {
                      createTodo(input: $input) { id title completed }
                    }""")
                .variables("""
                    {"input": {"title": "Write GraphQL tests", "completed": false}}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.createTodo.id", "201")
                .data("$.createTodo.title", "Write GraphQL tests")
                .data("$.createTodo.completed", false)
                .message()
                .status(200));

        // mutation UpdateTodo
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    mutation UpdateTodo($id: ID!, $input: UpdateTodoInput!) {
                      updateTodo(id: $id, input: $input) { id completed }
                    }""")
                .variables("""
                    {"id": "1", "input": {"completed": true}}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.updateTodo.id", "1")
                .data("$.updateTodo.completed", true)
                .message()
                .status(200));

        // mutation DeleteTodo
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    mutation DeleteTodo($id: ID!) {
                      deleteTodo(id: $id)
                    }""")
                .variables("""
                    {"id": "1"}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.deleteTodo", true)
                .message()
                .status(200));
    }

    @CitrusTest
    public void albums() {
        // query GetAlbum
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    query GetAlbum($id: ID!) {
                      album(id: $id) {
                        id
                        title
                        user { id }
                      }
                    }""")
                .variables("""
                    {"id": "1"}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.album.title", "quidem molestiae enim")
                .data("$.album.user.id", "1")
                .message()
                .status(200));

        // query ListAlbums
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    query ListAlbums($options: PageQueryOptions) {
                      albums(options: $options) {
                        data { id }
                        meta { totalCount }
                      }
                    }""")
                .variables("""
                    {"options": {"paginate": {"page": 1, "limit": 2}}}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.albums.meta.totalCount", 100)
                .data("$.albums.data.length()", 2)
                .data("$.albums.data[0].id", "1")
                .message()
                .status(200));

        // mutation CreateAlbum
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    mutation CreateAlbum($input: CreateAlbumInput!) {
                      createAlbum(input: $input) { id title }
                    }""")
                .variables("""
                    {"input": {"title": "Citrus album", "userId": "1"}}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.createAlbum.id", "101")
                .data("$.createAlbum.title", "Citrus album")
                .message()
                .status(200));

        // mutation UpdateAlbum
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    mutation UpdateAlbum($id: ID!, $input: UpdateAlbumInput!) {
                      updateAlbum(id: $id, input: $input) { id title }
                    }""")
                .variables("""
                    {"id": "1", "input": {"title": "Renamed by Citrus"}}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.updateAlbum.id", "1")
                .data("$.updateAlbum.title", "Renamed by Citrus")
                .message()
                .status(200));

        // mutation DeleteAlbum
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    mutation DeleteAlbum($id: ID!) {
                      deleteAlbum(id: $id)
                    }""")
                .variables("""
                    {"id": "1"}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.deleteAlbum", true)
                .message()
                .status(200));
    }

    @CitrusTest
    public void photos() {
        // query GetPhoto
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    query GetPhoto($id: ID!) {
                      photo(id: $id) {
                        id
                        title
                        url
                        album { id }
                      }
                    }""")
                .variables("""
                    {"id": "1"}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.photo.title", "accusamus beatae ad facilis cum similique qui sunt")
                .data("$.photo.url", "https://picsum.photos/seed/1/600")
                .data("$.photo.album.id", "1")
                .message()
                .status(200));

        // query ListPhotos
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    query ListPhotos($options: PageQueryOptions) {
                      photos(options: $options) {
                        data { id }
                        meta { totalCount }
                      }
                    }""")
                .variables("""
                    {"options": {"paginate": {"page": 1, "limit": 2}}}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.photos.meta.totalCount", 5000)
                .data("$.photos.data.length()", 2)
                .data("$.photos.data[0].id", "1")
                .message()
                .status(200));

        // mutation CreatePhoto
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    mutation CreatePhoto($input: CreatePhotoInput!) {
                      createPhoto(input: $input) { id title url }
                    }""")
                .variables("""
                    {"input": {"title": "Citrus", "url": "https://example.org/citrus.png", "thumbnailUrl": "https://example.org/citrus-t.png"}}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.createPhoto.id", "5001")
                .data("$.createPhoto.url", "https://example.org/citrus.png")
                .message()
                .status(200));

        // mutation UpdatePhoto
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    mutation UpdatePhoto($id: ID!, $input: UpdatePhotoInput!) {
                      updatePhoto(id: $id, input: $input) { id title }
                    }""")
                .variables("""
                    {"id": "1", "input": {"title": "Retitled by Citrus"}}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.updatePhoto.id", "1")
                .data("$.updatePhoto.title", "Retitled by Citrus")
                .message()
                .status(200));

        // mutation DeletePhoto
        $(graphql().client(graphqlZero)
                .send()
                .query("""
                    mutation DeletePhoto($id: ID!) {
                      deletePhoto(id: $id)
                    }""")
                .variables("""
                    {"id": "1"}"""));

        $(graphql().client(graphqlZero)
                .receive()
                .data("$.deletePhoto", true)
                .message()
                .status(200));
    }

}
