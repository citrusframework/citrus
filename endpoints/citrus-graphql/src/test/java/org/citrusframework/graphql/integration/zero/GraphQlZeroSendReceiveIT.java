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

import java.util.Map;

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.graphql.actions.GraphQlClientRequestActionBuilder;
import org.citrusframework.graphql.actions.GraphQlClientResponseActionBuilder;
import org.citrusframework.graphql.client.GraphQlClient;
import org.citrusframework.graphql.endpoint.builder.GraphQlEndpoints;
import org.citrusframework.graphql.server.GraphQlServer;
import org.citrusframework.json.dsl.JsonSupport;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.validation.GenericValidationProcessor;
import org.citrusframework.validation.ValidationProcessor;
import org.testng.annotations.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

/**
 * Raw-action variant of {@link GraphQlZeroJavaIT}: same 6 resources x get, list, create, update,
 * delete = 30 operations with identical documents and assertions, but every step explicitly uses
 * the {@link SendMessageAction} builder ({@link GraphQlClientRequestActionBuilder}) and the
 * {@link ReceiveMessageAction} builder ({@link GraphQlClientResponseActionBuilder}) and executes
 * the built action with {@code run(...)} instead of the {@code $(graphql()...)} shorthand.
 *
 * <p>It also demonstrates the two object-mapping conveniences next to the raw JSON strings:
 * variables as objects ({@code variable(name, Map/record)}) on the writing operations and
 * entity callbacks ({@code JsonSupport.validate(...).mapper(...).validator(...)}) on the six
 * {@code Get*} reads.
 */
@Test
public class GraphQlZeroSendReceiveIT extends TestNGCitrusSpringSupport {

    private final int port = findAvailableTcpPort(19600);

    private static final JsonMapper JSON = JsonMapper.shared();

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

    // --- Response entities (Get* operations bind the data section to these) ---

    record UserRef(String id, String name) {}
    record IdRef(String id) {}

    record Post(String id, String title, UserRef user) {}
    record PostData(Post post) {}
    record PostResponse(PostData data) {}

    record Address(String city) {}
    record User(String id, String name, String username, String email, Address address) {}
    record UserData(User user) {}
    record UserResponse(UserData data) {}

    record CommentPost(String id) {}
    record Comment(String id, String name, String email, CommentPost post) {}
    record CommentData(Comment comment) {}
    record CommentResponse(CommentData data) {}

    record Todo(String id, String title, boolean completed, IdRef user) {}
    record TodoData(Todo todo) {}
    record TodoResponse(TodoData data) {}

    record Album(String id, String title, IdRef user) {}
    record AlbumData(Album album) {}
    record AlbumResponse(AlbumData data) {}

    record Photo(String id, String title, String url, IdRef album) {}
    record PhotoData(Photo photo) {}
    record PhotoResponse(PhotoData data) {}

    // --- Input objects (Create/Update operations send these as variables) ---

    record CreatePostInput(String title, String body) {}
    record CreateUserInput(String name, String username, String email) {}

    private SendMessageAction send(String query, String variablesJson) {
        GraphQlClientRequestActionBuilder builder = new GraphQlClientRequestActionBuilder();
        builder.endpoint(graphqlZero);
        builder.name("graphql:send-request");
        builder.query(query);
        builder.variables(variablesJson);
        return builder.build();
    }

    private SendMessageAction send(String query, Map<String, ?> variables) {
        GraphQlClientRequestActionBuilder builder = new GraphQlClientRequestActionBuilder();
        builder.endpoint(graphqlZero);
        builder.name("graphql:send-request");
        builder.query(query);
        for (Map.Entry<String, ?> variable : variables.entrySet()) {
            builder.variable(variable.getKey(), variable.getValue());
        }
        return builder.build();
    }

    private ReceiveMessageAction receive(Object[][] data, ValidationProcessor... validators) {
        GraphQlClientResponseActionBuilder builder = new GraphQlClientResponseActionBuilder();
        builder.endpoint(graphqlZero);
        builder.name("graphql:receive-response");
        for (Object[] entry : data) {
            builder.data((String) entry[0], entry[1]);
        }
        for (ValidationProcessor validator : validators) {
            builder.validate(validator);
        }
        builder.message().status(200);
        return builder.build();
    }

    private static <T> ValidationProcessor entity(Class<T> type, GenericValidationProcessor<T> assertions) {
        return JsonSupport.validate(type).mapper(JSON).validator(assertions).build();
    }

    @CitrusTest
    public void posts() {
        // query GetPost
        run(send("""
                    query GetPost($id: ID!) {
                      post(id: $id) {
                        id
                        title
                        user { id name }
                      }
                    }""",
                """
                    {"id": "1"}"""));
        run(receive(new Object[][]{
                {"$.post.id", "1"},
                {"$.post.title", "sunt aut facere repellat provident occaecati excepturi optio reprehenderit"},
                {"$.post.user.name", "Leanne Graham"},
        }, entity(PostResponse.class, (response, headers, context) -> {
            assertThat(response.data().post().id()).isEqualTo("1");
            assertThat(response.data().post().title())
                    .isEqualTo("sunt aut facere repellat provident occaecati excepturi optio reprehenderit");
            assertThat(response.data().post().user().name()).isEqualTo("Leanne Graham");
        })));

        // query ListPosts
        run(send("""
                    query ListPosts($options: PageQueryOptions) {
                      posts(options: $options) {
                        data { id title }
                        meta { totalCount }
                      }
                    }""",
                """
                    {"options": {"paginate": {"page": 1, "limit": 2}}}"""));
        run(receive(new Object[][]{
                {"$.posts.meta.totalCount", 100},
                {"$.posts.data.length()", 2},
                {"$.posts.data[0].id", "1"},
                {"$.posts.data[1].title", "qui est esse"},
        }));

        // mutation CreatePost (input sent as a record, not a JSON string)
        run(send("""
                    mutation CreatePost($input: CreatePostInput!) {
                      createPost(input: $input) { id title body }
                    }""",
                Map.of("input", new CreatePostInput("Citrus", "GraphQL rocks"))));
        run(receive(new Object[][]{
                {"$.createPost.id", "101"},
                {"$.createPost.title", "Citrus"},
                {"$.createPost.body", "GraphQL rocks"},
        }));

        // mutation UpdatePost (variables sent as objects)
        run(send("""
                    mutation UpdatePost($id: ID!, $input: UpdatePostInput!) {
                      updatePost(id: $id, input: $input) { id body }
                    }""",
                Map.of("id", "1", "input", Map.of("body", "Updated by Citrus"))));
        run(receive(new Object[][]{
                {"$.updatePost.id", "1"},
                {"$.updatePost.body", "Updated by Citrus"},
        }));

        // mutation DeletePost
        run(send("""
                    mutation DeletePost($id: ID!) {
                      deletePost(id: $id)
                    }""",
                """
                    {"id": "1"}"""));
        run(receive(new Object[][]{
                {"$.deletePost", true},
        }));
    }

    @CitrusTest
    public void users() {
        // query GetUser
        run(send("""
                    query GetUser($id: ID!) {
                      user(id: $id) {
                        id
                        name
                        username
                        email
                        address { city }
                      }
                    }""",
                """
                    {"id": "1"}"""));
        run(receive(new Object[][]{
                {"$.user.name", "Leanne Graham"},
                {"$.user.username", "Bret"},
                {"$.user.email", "Sincere@april.biz"},
                {"$.user.address.city", "Gwenborough"},
        }, entity(UserResponse.class, (response, headers, context) -> {
            assertThat(response.data().user().name()).isEqualTo("Leanne Graham");
            assertThat(response.data().user().username()).isEqualTo("Bret");
            assertThat(response.data().user().email()).isEqualTo("Sincere@april.biz");
            assertThat(response.data().user().address().city()).isEqualTo("Gwenborough");
        })));

        // query ListUsers
        run(send("""
                    query ListUsers($options: PageQueryOptions) {
                      users(options: $options) {
                        data { id name }
                        meta { totalCount }
                      }
                    }""",
                """
                    {"options": {"paginate": {"page": 1, "limit": 2}}}"""));
        run(receive(new Object[][]{
                {"$.users.meta.totalCount", 10},
                {"$.users.data.length()", 2},
                {"$.users.data[0].id", "1"},
                {"$.users.data[1].name", "Ervin Howell"},
        }));

        // mutation CreateUser (input sent as a record, not a JSON string)
        run(send("""
                    mutation CreateUser($input: CreateUserInput!) {
                      createUser(input: $input) { id name username email }
                    }""",
                Map.of("input", new CreateUserInput("Citrus Tester", "citrus", "citrus@example.org"))));
        run(receive(new Object[][]{
                {"$.createUser.id", "11"},
                {"$.createUser.username", "citrus"},
                {"$.createUser.email", "citrus@example.org"},
        }));

        // mutation UpdateUser (variables sent as objects)
        run(send("""
                    mutation UpdateUser($id: ID!, $input: UpdateUserInput!) {
                      updateUser(id: $id, input: $input) { id name }
                    }""",
                Map.of("id", "1", "input", Map.of("name", "Leanne Citrus"))));
        run(receive(new Object[][]{
                {"$.updateUser.id", "1"},
                {"$.updateUser.name", "Leanne Citrus"},
        }));

        // mutation DeleteUser
        run(send("""
                    mutation DeleteUser($id: ID!) {
                      deleteUser(id: $id)
                    }""",
                """
                    {"id": "1"}"""));
        run(receive(new Object[][]{
                {"$.deleteUser", true},
        }));
    }

    @CitrusTest
    public void comments() {
        // query GetComment
        run(send("""
                    query GetComment($id: ID!) {
                      comment(id: $id) {
                        id
                        name
                        email
                        post { id }
                      }
                    }""",
                """
                    {"id": "1"}"""));
        run(receive(new Object[][]{
                {"$.comment.name", "id labore ex et quam laborum"},
                {"$.comment.email", "Eliseo@gardner.biz"},
                {"$.comment.post.id", "1"},
        }, entity(CommentResponse.class, (response, headers, context) -> {
            assertThat(response.data().comment().name()).isEqualTo("id labore ex et quam laborum");
            assertThat(response.data().comment().email()).isEqualTo("Eliseo@gardner.biz");
            assertThat(response.data().comment().post().id()).isEqualTo("1");
        })));

        // query ListComments
        run(send("""
                    query ListComments($options: PageQueryOptions) {
                      comments(options: $options) {
                        data { id }
                        meta { totalCount }
                      }
                    }""",
                """
                    {"options": {"paginate": {"page": 1, "limit": 2}}}"""));
        run(receive(new Object[][]{
                {"$.comments.meta.totalCount", 500},
                {"$.comments.data.length()", 2},
                {"$.comments.data[0].id", "1"},
        }));

        // mutation CreateComment (variables sent as objects)
        run(send("""
                    mutation CreateComment($input: CreateCommentInput!) {
                      createComment(input: $input) { id name email body }
                    }""",
                Map.of("input", Map.of("name", "Citrus", "email", "citrus@example.org", "body", "Nice post"))));
        run(receive(new Object[][]{
                {"$.createComment.id", "501"},
                {"$.createComment.body", "Nice post"},
        }));

        // mutation UpdateComment (variables sent as objects)
        run(send("""
                    mutation UpdateComment($id: ID!, $input: UpdateCommentInput!) {
                      updateComment(id: $id, input: $input) { id body }
                    }""",
                Map.of("id", "1", "input", Map.of("body", "Edited by Citrus"))));
        run(receive(new Object[][]{
                {"$.updateComment.id", "1"},
                {"$.updateComment.body", "Edited by Citrus"},
        }));

        // mutation DeleteComment
        run(send("""
                    mutation DeleteComment($id: ID!) {
                      deleteComment(id: $id)
                    }""",
                """
                    {"id": "1"}"""));
        run(receive(new Object[][]{
                {"$.deleteComment", true},
        }));
    }

    @CitrusTest
    public void todos() {
        // query GetTodo
        run(send("""
                    query GetTodo($id: ID!) {
                      todo(id: $id) {
                        id
                        title
                        completed
                        user { id }
                      }
                    }""",
                """
                    {"id": "1"}"""));
        run(receive(new Object[][]{
                {"$.todo.title", "delectus aut autem"},
                {"$.todo.completed", false},
                {"$.todo.user.id", "1"},
        }, entity(TodoResponse.class, (response, headers, context) -> {
            assertThat(response.data().todo().title()).isEqualTo("delectus aut autem");
            assertThat(response.data().todo().completed()).isFalse();
            assertThat(response.data().todo().user().id()).isEqualTo("1");
        })));

        // query ListTodos
        run(send("""
                    query ListTodos($options: PageQueryOptions) {
                      todos(options: $options) {
                        data { id }
                        meta { totalCount }
                      }
                    }""",
                """
                    {"options": {"paginate": {"page": 1, "limit": 2}}}"""));
        run(receive(new Object[][]{
                {"$.todos.meta.totalCount", 200},
                {"$.todos.data.length()", 2},
                {"$.todos.data[0].id", "1"},
        }));

        // mutation CreateTodo (variables sent as objects, booleans keep their type)
        run(send("""
                    mutation CreateTodo($input: CreateTodoInput!) {
                      createTodo(input: $input) { id title completed }
                    }""",
                Map.of("input", Map.of("title", "Write GraphQL tests", "completed", false))));
        run(receive(new Object[][]{
                {"$.createTodo.id", "201"},
                {"$.createTodo.title", "Write GraphQL tests"},
                {"$.createTodo.completed", false},
        }));

        // mutation UpdateTodo (variables sent as objects)
        run(send("""
                    mutation UpdateTodo($id: ID!, $input: UpdateTodoInput!) {
                      updateTodo(id: $id, input: $input) { id completed }
                    }""",
                Map.of("id", "1", "input", Map.of("completed", true))));
        run(receive(new Object[][]{
                {"$.updateTodo.id", "1"},
                {"$.updateTodo.completed", true},
        }));

        // mutation DeleteTodo
        run(send("""
                    mutation DeleteTodo($id: ID!) {
                      deleteTodo(id: $id)
                    }""",
                """
                    {"id": "1"}"""));
        run(receive(new Object[][]{
                {"$.deleteTodo", true},
        }));
    }

    @CitrusTest
    public void albums() {
        // query GetAlbum
        run(send("""
                    query GetAlbum($id: ID!) {
                      album(id: $id) {
                        id
                        title
                        user { id }
                      }
                    }""",
                """
                    {"id": "1"}"""));
        run(receive(new Object[][]{
                {"$.album.title", "quidem molestiae enim"},
                {"$.album.user.id", "1"},
        }, entity(AlbumResponse.class, (response, headers, context) -> {
            assertThat(response.data().album().title()).isEqualTo("quidem molestiae enim");
            assertThat(response.data().album().user().id()).isEqualTo("1");
        })));

        // query ListAlbums
        run(send("""
                    query ListAlbums($options: PageQueryOptions) {
                      albums(options: $options) {
                        data { id }
                        meta { totalCount }
                      }
                    }""",
                """
                    {"options": {"paginate": {"page": 1, "limit": 2}}}"""));
        run(receive(new Object[][]{
                {"$.albums.meta.totalCount", 100},
                {"$.albums.data.length()", 2},
                {"$.albums.data[0].id", "1"},
        }));

        // mutation CreateAlbum (variables sent as objects)
        run(send("""
                    mutation CreateAlbum($input: CreateAlbumInput!) {
                      createAlbum(input: $input) { id title }
                    }""",
                Map.of("input", Map.of("title", "Citrus album", "userId", "1"))));
        run(receive(new Object[][]{
                {"$.createAlbum.id", "101"},
                {"$.createAlbum.title", "Citrus album"},
        }));

        // mutation UpdateAlbum (variables sent as objects)
        run(send("""
                    mutation UpdateAlbum($id: ID!, $input: UpdateAlbumInput!) {
                      updateAlbum(id: $id, input: $input) { id title }
                    }""",
                Map.of("id", "1", "input", Map.of("title", "Renamed by Citrus"))));
        run(receive(new Object[][]{
                {"$.updateAlbum.id", "1"},
                {"$.updateAlbum.title", "Renamed by Citrus"},
        }));

        // mutation DeleteAlbum
        run(send("""
                    mutation DeleteAlbum($id: ID!) {
                      deleteAlbum(id: $id)
                    }""",
                """
                    {"id": "1"}"""));
        run(receive(new Object[][]{
                {"$.deleteAlbum", true},
        }));
    }

    @CitrusTest
    public void photos() {
        // query GetPhoto
        run(send("""
                    query GetPhoto($id: ID!) {
                      photo(id: $id) {
                        id
                        title
                        url
                        album { id }
                      }
                    }""",
                """
                    {"id": "1"}"""));
        run(receive(new Object[][]{
                {"$.photo.title", "accusamus beatae ad facilis cum similique qui sunt"},
                {"$.photo.url", "https://picsum.photos/seed/1/600"},
                {"$.photo.album.id", "1"},
        }, entity(PhotoResponse.class, (response, headers, context) -> {
            assertThat(response.data().photo().title())
                    .isEqualTo("accusamus beatae ad facilis cum similique qui sunt");
            assertThat(response.data().photo().url()).isEqualTo("https://picsum.photos/seed/1/600");
            assertThat(response.data().photo().album().id()).isEqualTo("1");
        })));

        // query ListPhotos
        run(send("""
                    query ListPhotos($options: PageQueryOptions) {
                      photos(options: $options) {
                        data { id }
                        meta { totalCount }
                      }
                    }""",
                """
                    {"options": {"paginate": {"page": 1, "limit": 2}}}"""));
        run(receive(new Object[][]{
                {"$.photos.meta.totalCount", 5000},
                {"$.photos.data.length()", 2},
                {"$.photos.data[0].id", "1"},
        }));

        // mutation CreatePhoto (variables sent as objects)
        run(send("""
                    mutation CreatePhoto($input: CreatePhotoInput!) {
                      createPhoto(input: $input) { id title url }
                    }""",
                Map.of("input", Map.of("title", "Citrus", "url", "https://example.org/citrus.png",
                        "thumbnailUrl", "https://example.org/citrus-t.png"))));
        run(receive(new Object[][]{
                {"$.createPhoto.id", "5001"},
                {"$.createPhoto.url", "https://example.org/citrus.png"},
        }));

        // mutation UpdatePhoto (variables sent as objects)
        run(send("""
                    mutation UpdatePhoto($id: ID!, $input: UpdatePhotoInput!) {
                      updatePhoto(id: $id, input: $input) { id title }
                    }""",
                Map.of("id", "1", "input", Map.of("title", "Retitled by Citrus"))));
        run(receive(new Object[][]{
                {"$.updatePhoto.id", "1"},
                {"$.updatePhoto.title", "Retitled by Citrus"},
        }));

        // mutation DeletePhoto
        run(send("""
                    mutation DeletePhoto($id: ID!) {
                      deletePhoto(id: $id)
                    }""",
                """
                    {"id": "1"}"""));
        run(receive(new Object[][]{
                {"$.deletePhoto", true},
        }));
    }
}
