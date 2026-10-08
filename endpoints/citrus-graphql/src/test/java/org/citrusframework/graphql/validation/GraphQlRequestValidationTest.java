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
package org.citrusframework.graphql.validation;

import java.util.List;

import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.graphql.message.GraphQlMessageHeaders;
import org.citrusframework.graphql.server.GraphQlServer;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.springframework.http.HttpMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GraphQlRequestValidationTest extends AbstractTestNGUnitTest {

    private static final List<String> SCHEMA = List.of("classpath:org/citrusframework/graphql/schema/library.graphqls");

    private final GraphQlMessageValidator validator = new GraphQlMessageValidator();

    @Test
    public void shouldAcceptValidRequestWithoutExpectations() {
        validate(request("{\"query\":\"query Book($id: ID!) { book(id: $id) { title } }\",\"variables\":{\"id\":\"42\"}}"),
                GraphQlMessageValidationContext.Builder.request().build());
    }

    @Test
    public void shouldRejectUnparseableDocumentWhenStrict() {
        assertThatThrownBy(() -> validate(request("{\"query\":\"query { book(id: 1) { title }\"}"),
                GraphQlMessageValidationContext.Builder.request().build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageStartingWith("Invalid GraphQL document (line 1, column ");
    }

    @Test
    public void shouldRejectRequestThatIsNoGraphQlRequestWhenStrict() {
        assertThatThrownBy(() -> validate(request("hello"), GraphQlMessageValidationContext.Builder.request().build()))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Invalid GraphQL request: body is not a JSON object");
    }

    @Test
    public void shouldRejectSeveralOperationsWithoutNameWhenStrict() {
        assertThatThrownBy(() -> validate(request("{\"query\":\"query A { a } query B { b }\"}"),
                GraphQlMessageValidationContext.Builder.request().build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("an operation name is required");
    }

    @Test
    public void shouldValidateAgainstServerSchema() {
        GraphQlServer server = new GraphQlServer();
        server.setSchemaResources(SCHEMA);

        assertThatThrownBy(() -> validate(request("{\"query\":\"{ book(id: 1) { isbn } }\"}"),
                GraphQlMessageValidationContext.Builder.request().endpoint(server).build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageStartingWith("GraphQL document does not match the schema: ")
                .hasMessageContaining("isbn");
    }

    @Test
    public void shouldAcceptAnythingWhenNotStrict() {
        GraphQlServer server = new GraphQlServer();
        server.setSchemaResources(SCHEMA);

        validate(request("{\"query\":\"{ book(id: 1) { isbn }\"}"),
                GraphQlMessageValidationContext.Builder.request().endpoint(server).strict(false).build());
        validate(request("not json"), GraphQlMessageValidationContext.Builder.request().strict(false).build());
    }

    @Test
    public void shouldUseStrictSettingOfServer() {
        GraphQlServer server = new GraphQlServer();
        server.setStrict(false);

        validate(request("{\"query\":\"{ broken\"}"), GraphQlMessageValidationContext.Builder.request().endpoint(server).build());
    }

    @Test
    public void shouldValidateOperationName() {
        Message message = request("{\"query\":\"query Book { book(id: 1) { title } }\",\"operationName\":\"Book\"}");

        validate(message, GraphQlMessageValidationContext.Builder.request().operationName("Book").build());
        validate(message, GraphQlMessageValidationContext.Builder.request().operationName("@startsWith('Bo')@").build());
        assertThatThrownBy(() -> validate(message, GraphQlMessageValidationContext.Builder.request().operationName("Books").build()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void shouldValidateEffectiveOperationNameOfSingleOperation() {
        Message message = request("{\"query\":\"mutation AddBook { addBook { id } }\"}");

        validate(message, GraphQlMessageValidationContext.Builder.request().operationName("AddBook").build());
    }

    @Test
    public void shouldCompareQueriesIgnoringFormattingWhenStrict() {
        Message message = request("{\"query\":\"query Book($id:ID!){book(id:$id){title}}\"}");

        validate(message, GraphQlMessageValidationContext.Builder.request()
                .query("""
                        query Book($id: ID!) {
                          book(id: $id) { title } # comment
                        }""")
                .build());
    }

    @Test
    public void shouldReportQueryMismatchWhenStrict() {
        Message message = request("{\"query\":\"{ book(id: 1) { title } }\"}");

        assertThatThrownBy(() -> validate(message, GraphQlMessageValidationContext.Builder.request()
                .query("{ book(id: 1) { id } }")
                .build()))
                .isInstanceOf(ValidationException.class)
                .hasMessage("GraphQL query does not match - expected: {book(id:1){id}} but was: {book(id:1){title}}");
    }

    @Test
    public void shouldCompareRawQueryWithMatcherWhenNotStrict() {
        Message message = request("{\"query\":\"{ books { title } }\"}");

        validate(message, GraphQlMessageValidationContext.Builder.request().strict(false).query("@contains('books')@").build());
        assertThatThrownBy(() -> validate(message, GraphQlMessageValidationContext.Builder.request()
                .strict(false)
                .query("{books{title}}")
                .build()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void shouldUseMatcherForQueryEvenWhenStrict() {
        validate(request("{\"query\":\"{ books { title } }\"}"),
                GraphQlMessageValidationContext.Builder.request().query("@contains('books')@").build());
    }

    @Test
    public void shouldValidateVariables() {
        context.setVariable("id", "42");
        Message message = request("{\"query\":\"query B($id: ID!, $first: Int) { book(id: $id) { title } }\",\"variables\":{\"id\":\"42\",\"first\":10,\"extra\":true}}");

        validate(message, GraphQlMessageValidationContext.Builder.request()
                .variable("id", "${id}")
                .variable("first", 10)
                .variable("first", "@isNumber()@")
                .build());
    }

    @Test
    public void shouldReportVariableMismatch() {
        Message message = request("{\"query\":\"{ a }\",\"variables\":{\"id\":\"42\"}}");

        assertThatThrownBy(() -> validate(message, GraphQlMessageValidationContext.Builder.request().variable("id", "7").build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("variables.id");
    }

    @Test
    public void shouldReportMissingVariable() {
        Message message = request("{\"query\":\"{ a }\",\"variables\":{\"id\":\"42\"}}");

        assertThatThrownBy(() -> validate(message, GraphQlMessageValidationContext.Builder.request().variable("first", 10).build()))
                .isInstanceOf(ValidationException.class)
                .hasMessage("GraphQL variable 'first' is missing; received variables: {id=42}");
    }

    @Test
    public void shouldCompareVariablesObject() {
        Message message = request("{\"query\":\"{ a }\",\"variables\":{\"id\":\"42\",\"first\":10}}");

        validate(message, GraphQlMessageValidationContext.Builder.request().variables("{\"id\":\"42\",\"first\":\"@ignore@\"}").build());
        assertThatThrownBy(() -> validate(message, GraphQlMessageValidationContext.Builder.request()
                .variables("{\"id\":\"7\",\"first\":10}")
                .build()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void shouldValidateGetRequestPresentedAsBody() {
        HttpMessage message = new HttpMessage().method(HttpMethod.GET)
                .queryParams("query=%7B+books+%7B+title+%7D+%7D,variables=%7B%22first%22%3A10%7D");
        message.setHeader(GraphQlMessageHeaders.KIND, GraphQlMessageHeaders.KIND_REQUEST);

        validate(message, GraphQlMessageValidationContext.Builder.request().query("{books{title}}").variable("first", 10).build());
    }

    private void validate(Message received, GraphQlMessageValidationContext validationContext) {
        validator.validateMessage(received, new HttpMessage(), context, List.of(validationContext));
    }

    private static HttpMessage request(String body) {
        HttpMessage message = new HttpMessage(body).method(HttpMethod.POST);
        message.setHeader(GraphQlMessageHeaders.KIND, GraphQlMessageHeaders.KIND_REQUEST);
        return message;
    }
}
