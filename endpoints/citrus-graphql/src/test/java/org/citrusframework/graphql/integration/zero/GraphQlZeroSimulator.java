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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import graphql.language.Document;
import org.citrusframework.base.endpoint.adapter.StaticEndpointAdapter;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.graphql.document.GraphQlDocuments;
import org.citrusframework.graphql.document.GraphQlOperation;
import org.citrusframework.graphql.message.GraphQlMessages;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.springframework.graphql.GraphQlRequest;
import org.springframework.http.HttpStatus;

/**
 * Simulates the public GraphQLZero API (JSONPlaceholder data set) on a Citrus GraphQL server: the
 * same 6 resources x get, list, create, update, delete = 30 operations with the same fixed data.
 * Reads return the well-known records, lists return the fixed totals with the first rows, creates
 * and updates echo the sent input with the fixed generated ids, deletes return {@code true} —
 * mirroring the live service, but offline and deterministic.
 */
public class GraphQlZeroSimulator extends StaticEndpointAdapter {

    @Override
    protected Message handleMessageInternal(Message request) {
        GraphQlRequest graphQlRequest = GraphQlMessages.toRequest(request);
        Document document = GraphQlDocuments.parse(graphQlRequest.getDocument());
        GraphQlOperation operation = GraphQlDocuments.selectOperation(document, graphQlRequest.getOperationName());

        if (operation.name() == null) {
            throw new CitrusRuntimeException("GraphQLZero simulator requires a named operation");
        }

        Map<String, Object> data = respond(operation.name(), graphQlRequest.getVariables());
        return new HttpMessage(GraphQlMessages.toJson(Map.of("data", data))).status(HttpStatus.OK);
    }

    private Map<String, Object> respond(String operation, Map<String, Object> variables) {
        return switch (operation) {
            case "GetPost" -> Map.of("post", post(variables));
            case "ListPosts" -> Map.of("posts", Map.of(
                    "data", List.of(
                            Map.of("id", "1", "title", "sunt aut facere repellat provident occaecati excepturi optio reprehenderit"),
                            Map.of("id", "2", "title", "qui est esse")),
                    "meta", Map.of("totalCount", 100)));
            case "CreatePost" -> Map.of("createPost", merge(Map.of("id", "101"), input(variables)));
            case "UpdatePost" -> Map.of("updatePost", Map.of("id", id(variables), "body", input(variables).get("body")));
            case "DeletePost" -> Map.of("deletePost", true);

            case "GetUser" -> Map.of("user", user(variables));
            case "ListUsers" -> Map.of("users", Map.of(
                    "data", List.of(
                            Map.of("id", "1", "name", "Leanne Graham"),
                            Map.of("id", "2", "name", "Ervin Howell")),
                    "meta", Map.of("totalCount", 10)));
            case "CreateUser" -> Map.of("createUser", merge(Map.of("id", "11"), input(variables)));
            case "UpdateUser" -> Map.of("updateUser", Map.of("id", id(variables), "name", input(variables).get("name")));
            case "DeleteUser" -> Map.of("deleteUser", true);

            case "GetComment" -> Map.of("comment", comment(variables));
            case "ListComments" -> Map.of("comments", Map.of(
                    "data", List.of(Map.of("id", "1"), Map.of("id", "2")),
                    "meta", Map.of("totalCount", 500)));
            case "CreateComment" -> Map.of("createComment", merge(Map.of("id", "501"), input(variables)));
            case "UpdateComment" -> Map.of("updateComment", Map.of("id", id(variables), "body", input(variables).get("body")));
            case "DeleteComment" -> Map.of("deleteComment", true);

            case "GetTodo" -> Map.of("todo", todo(variables));
            case "ListTodos" -> Map.of("todos", Map.of(
                    "data", List.of(Map.of("id", "1"), Map.of("id", "2")),
                    "meta", Map.of("totalCount", 200)));
            case "CreateTodo" -> Map.of("createTodo", merge(Map.of("id", "201"), input(variables)));
            case "UpdateTodo" -> Map.of("updateTodo", Map.of("id", id(variables), "completed", input(variables).get("completed")));
            case "DeleteTodo" -> Map.of("deleteTodo", true);

            case "GetAlbum" -> Map.of("album", album(variables));
            case "ListAlbums" -> Map.of("albums", Map.of(
                    "data", List.of(Map.of("id", "1"), Map.of("id", "2")),
                    "meta", Map.of("totalCount", 100)));
            case "CreateAlbum" -> Map.of("createAlbum", Map.of("id", "101", "title", input(variables).get("title")));
            case "UpdateAlbum" -> Map.of("updateAlbum", Map.of("id", id(variables), "title", input(variables).get("title")));
            case "DeleteAlbum" -> Map.of("deleteAlbum", true);

            case "GetPhoto" -> Map.of("photo", photo(variables));
            case "ListPhotos" -> Map.of("photos", Map.of(
                    "data", List.of(Map.of("id", "1"), Map.of("id", "2")),
                    "meta", Map.of("totalCount", 5000)));
            case "CreatePhoto" -> Map.of("createPhoto",
                    Map.of("id", "5001", "title", input(variables).get("title"), "url", input(variables).get("url")));
            case "UpdatePhoto" -> Map.of("updatePhoto", Map.of("id", id(variables), "title", input(variables).get("title")));
            case "DeletePhoto" -> Map.of("deletePhoto", true);

            default -> throw new CitrusRuntimeException("GraphQLZero simulator has no data for operation '%s'".formatted(operation));
        };
    }

    private Map<String, Object> post(Map<String, Object> variables) {
        if (!"1".equals(String.valueOf(variables.get("id")))) {
            return nulls("id", "title", "user");
        }

        return Map.of("id", "1",
                "title", "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
                "user", Map.of("id", "1", "name", "Leanne Graham"));
    }

    private Map<String, Object> user(Map<String, Object> variables) {
        if (!"1".equals(String.valueOf(variables.get("id")))) {
            return nulls("id", "name", "username", "email", "address");
        }

        return Map.of("id", "1",
                "name", "Leanne Graham",
                "username", "Bret",
                "email", "Sincere@april.biz",
                "address", Map.of("city", "Gwenborough"));
    }

    private Map<String, Object> comment(Map<String, Object> variables) {
        if (!"1".equals(String.valueOf(variables.get("id")))) {
            return nulls("id", "name", "email", "post");
        }

        return Map.of("id", "1",
                "name", "id labore ex et quam laborum",
                "email", "Eliseo@gardner.biz",
                "post", Map.of("id", "1"));
    }

    private Map<String, Object> todo(Map<String, Object> variables) {
        if (!"1".equals(String.valueOf(variables.get("id")))) {
            return nulls("id", "title", "completed", "user");
        }

        Map<String, Object> todo = new LinkedHashMap<>();
        todo.put("id", "1");
        todo.put("title", "delectus aut autem");
        todo.put("completed", false);
        todo.put("user", Map.of("id", "1"));
        return todo;
    }

    private Map<String, Object> album(Map<String, Object> variables) {
        if (!"1".equals(String.valueOf(variables.get("id")))) {
            return nulls("id", "title", "user");
        }

        return Map.of("id", "1",
                "title", "quidem molestiae enim",
                "user", Map.of("id", "1"));
    }

    private Map<String, Object> photo(Map<String, Object> variables) {
        if (!"1".equals(String.valueOf(variables.get("id")))) {
            return nulls("id", "title", "url", "album");
        }

        return Map.of("id", "1",
                "title", "accusamus beatae ad facilis cum similique qui sunt",
                "url", "https://picsum.photos/seed/1/600",
                "album", Map.of("id", "1"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> input(Map<String, Object> variables) {
        Object input = variables.get("input");
        if (input instanceof Map<?, ?> inputMap) {
            return (Map<String, Object>) inputMap;
        }

        throw new CitrusRuntimeException("GraphQLZero simulator expected an 'input' object variable");
    }

    private String id(Map<String, Object> variables) {
        return String.valueOf(variables.get("id"));
    }

    private Map<String, Object> nulls(String... fields) {
        Map<String, Object> nulled = new LinkedHashMap<>();
        for (String field : fields) {
            nulled.put(field, null);
        }
        return nulled;
    }

    private Map<String, Object> merge(Map<String, Object> generated, Map<String, Object> echoed) {
        Map<String, Object> merged = new LinkedHashMap<>(echoed);
        merged.putAll(generated);
        return merged;
    }
}
