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
package org.citrusframework.graphql.message;

import java.util.List;
import java.util.Map;

import graphql.language.SourceLocation;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.http.message.HttpMessage;
import org.springframework.graphql.GraphQlRequest;
import org.springframework.graphql.GraphQlResponse;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.support.DefaultGraphQlRequest;
import org.springframework.http.HttpMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GraphQlMessagesTest {

    @Test
    public void shouldReadRequestFromPostBody() {
        HttpMessage message = new HttpMessage("""
                {"query":"query Book($id: ID!) { book(id: $id) { title } }","operationName":"Book",
                 "variables":{"id":"42","first":10},"extensions":{"trace":true}}""")
                .method(HttpMethod.POST);

        GraphQlRequest request = GraphQlMessages.toRequest(message);

        assertThat(request.getDocument()).isEqualTo("query Book($id: ID!) { book(id: $id) { title } }");
        assertThat(request.getOperationName()).isEqualTo("Book");
        assertThat(request.getVariables()).containsExactly(Map.entry("id", "42"), Map.entry("first", 10));
        assertThat(request.getExtensions()).containsExactly(Map.entry("trace", true));
    }

    @Test
    public void shouldReadMinimalRequestFromPostBody() {
        GraphQlRequest request = GraphQlMessages.toRequest(new HttpMessage("{\"query\":\"{ books { title } }\"}"));

        assertThat(request.getDocument()).isEqualTo("{ books { title } }");
        assertThat(request.getOperationName()).isNull();
        assertThat(request.getVariables()).isEmpty();
    }

    @Test
    public void shouldReadRequestFromGetQueryParams() {
        HttpMessage message = new HttpMessage()
                .method(HttpMethod.GET)
                .queryParams("query=%7B+books%28first%3A+%24first%29+%7B+title+%7D+%7D,operationName=Books,variables=%7B%22first%22%3A10%7D");

        GraphQlRequest request = GraphQlMessages.toRequest(message);

        assertThat(request.getDocument()).isEqualTo("{ books(first: $first) { title } }");
        assertThat(request.getOperationName()).isEqualTo("Books");
        assertThat(request.getVariables()).containsExactly(Map.entry("first", 10));
    }

    @Test
    public void shouldDecodeCommasInGetQueryParams() {
        HttpMessage message = new HttpMessage()
                .method(HttpMethod.GET)
                .queryParams("query=%7B+a%28x%3A+%22a%2Cb%22%29+%7D");

        assertThat(GraphQlMessages.toRequest(message).getDocument()).isEqualTo("{ a(x: \"a,b\") }");
    }

    @Test
    public void shouldRejectGetRequestWithoutQuery() {
        HttpMessage message = new HttpMessage().method(HttpMethod.GET).queryParams("operationName=Books");

        assertThatThrownBy(() -> GraphQlMessages.toRequest(message))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Invalid GraphQL request: missing 'query'");
    }

    @Test
    public void shouldRejectNonJsonPostBody() {
        assertThatThrownBy(() -> GraphQlMessages.toRequest(new HttpMessage("query { books }")))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Invalid GraphQL request: body is not a JSON object");
    }

    @Test
    public void shouldRejectNonStringQuery() {
        assertThatThrownBy(() -> GraphQlMessages.toRequest(new HttpMessage("{\"query\":42}")))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Invalid GraphQL request: 'query' must be a string");
    }

    @Test
    public void shouldRejectNonObjectVariables() {
        HttpMessage message = new HttpMessage().method(HttpMethod.GET).queryParams("query=%7Ba%7D,variables=%5B1%5D");

        assertThatThrownBy(() -> GraphQlMessages.toRequest(message))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Invalid GraphQL request: 'variables' must be a JSON object");
    }

    @Test
    public void shouldWriteRequestBody() {
        GraphQlRequest request = new DefaultGraphQlRequest("query Book($id: ID!) { book(id: $id) { title } }", "Book",
                Map.of("id", "42"), Map.of());

        assertThat(GraphQlMessages.toRequestBody(request))
                .isEqualTo("{\"query\":\"query Book($id: ID!) { book(id: $id) { title } }\",\"operationName\":\"Book\",\"variables\":{\"id\":\"42\"}}");
    }

    @Test
    public void shouldWriteMinimalRequestBody() {
        assertThat(GraphQlMessages.toRequestBody(new DefaultGraphQlRequest("{ books { title } }")))
                .isEqualTo("{\"query\":\"{ books { title } }\"}");
    }

    @Test
    public void shouldWriteQueryStringThatReadsBackUnchanged() {
        GraphQlRequest request = new DefaultGraphQlRequest("query Books($f: String) {\n  books(filter: $f) { title } # a, b & c\n}",
                "Books", Map.of("f", "a,b & ${c}"), Map.of());

        String queryString = GraphQlMessages.toQueryString(request);
        HttpMessage received = new HttpMessage().method(HttpMethod.GET)
                .queryParams(queryString.replace(",", "%2C").replace("&", ","));

        assertThat(queryString).doesNotContain(" ", "{", "\"", "\n", ",");
        GraphQlRequest decoded = GraphQlMessages.toRequest(received);
        assertThat(decoded.getDocument()).isEqualTo(request.getDocument());
        assertThat(decoded.getOperationName()).isEqualTo("Books");
        assertThat(decoded.getVariables()).isEqualTo(request.getVariables());
    }

    @Test
    public void shouldReadResponseWithDataAndErrors() {
        GraphQlResponse response = GraphQlMessages.toResponse(new HttpMessage("""
                {"data":{"book":{"title":"Dune","author":null}},
                 "errors":[{"message":"Author unavailable","path":["book","author"],
                            "locations":[{"line":1,"column":20}],"extensions":{"code":"PARTIAL","classification":"DataFetchingException"}}],
                 "extensions":{"cost":3}}"""));

        assertThat(response.isValid()).isTrue();
        assertThat(response.<Map<String, Object>>getData()).containsKey("book");
        assertThat(response.getExtensions()).containsEntry("cost", 3);
        ResponseError error = response.getErrors().get(0);
        assertThat(error.getMessage()).isEqualTo("Author unavailable");
        assertThat(error.getParsedPath()).containsExactly("book", "author");
        assertThat(error.getPath()).isEqualTo("book.author");
        assertThat(error.getLocations()).extracting(SourceLocation::getLine, SourceLocation::getColumn)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(1, 20));
        assertThat(error.getExtensions()).containsEntry("code", "PARTIAL");
        assertThat(error.getErrorType()).isNotNull();
        assertThat(error.getErrorType().toString()).isEqualTo("DataFetchingException");
    }

    @Test
    public void shouldFormatIndexedErrorPath() {
        GraphQlResponse response = GraphQlMessages.toResponse(new HttpMessage(
                "{\"data\":null,\"errors\":[{\"message\":\"boom\",\"path\":[\"books\",1,\"title\"]}]}"));

        assertThat(response.isValid()).isFalse();
        assertThat(response.getErrors().get(0).getPath()).isEqualTo("books[1].title");
        assertThat(response.getErrors().get(0).getLocations()).isEmpty();
        assertThat(response.getErrors().get(0).getExtensions()).isEmpty();
    }

    @Test
    public void shouldReadResponseWithoutErrors() {
        GraphQlResponse response = GraphQlMessages.toResponse(new HttpMessage("{\"data\":{\"books\":[]}}"));

        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getExtensions()).isEmpty();
    }

    @Test
    public void shouldRejectNonJsonResponse() {
        assertThatThrownBy(() -> GraphQlMessages.toResponse(new HttpMessage("<html>Bad Gateway</html>")))
                .isInstanceOf(ValidationException.class)
                .hasMessage("GraphQL response body is not a JSON object");
    }

    @Test
    public void shouldRejectEmptyResponse() {
        assertThatThrownBy(() -> GraphQlMessages.toResponse(new HttpMessage("")))
                .isInstanceOf(ValidationException.class)
                .hasMessage("GraphQL response body is not a JSON object");
    }

    @Test
    public void shouldWriteResponseBody() {
        assertThat(GraphQlMessages.toJson(Map.of("data", Map.of("ids", List.of(1, 2)))))
                .isEqualTo("{\"data\":{\"ids\":[1,2]}}");
    }
}
