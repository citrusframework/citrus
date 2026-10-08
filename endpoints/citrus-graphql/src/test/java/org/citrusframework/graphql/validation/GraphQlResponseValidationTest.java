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
import org.citrusframework.graphql.client.GraphQlClient;
import org.citrusframework.graphql.message.GraphQlMessageHeaders;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.validation.context.ValidationStatus;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.api.graphql.GraphQlError.error;

public class GraphQlResponseValidationTest extends AbstractTestNGUnitTest {

    private final GraphQlMessageValidator validator = new GraphQlMessageValidator();

    private static final String NOT_FOUND = """
            {"data":{"book":null},"errors":[{"message":"Book not found","path":["book"],"extensions":{"code":"NOT_FOUND"}}]}""";

    private static final String TWO_ERRORS = """
            {"data":null,"errors":[
              {"message":"boom","path":["books",1,"title"],"extensions":{"code":"INTERNAL"}},
              {"message":"Book 7 not found","path":["book"],"extensions":{"code":"NOT_FOUND"}}]}""";

    private static final String PARTIAL = """
            {"data":{"book":{"title":"Dune","author":null}},
             "errors":[{"message":"Author unavailable","path":["book","author"],"extensions":{"code":"PARTIAL"}}]}""";

    @Test
    public void shouldPassResponseWithoutErrors() {
        GraphQlMessageValidationContext validationContext = response().build();

        validate(json("{\"data\":{\"book\":{\"title\":\"Dune\"}}}"), validationContext);

        assertThat(validationContext.getStatus()).isEqualTo(ValidationStatus.PASSED);
    }

    @Test
    public void shouldFailOnUnexpectedErrorWithHttp200() {
        GraphQlMessageValidationContext validationContext = response().build();

        assertThatThrownBy(() -> validate(json(NOT_FOUND), validationContext))
                .isInstanceOf(ValidationException.class)
                .hasMessage("GraphQL response contains 1 unexpected error: [NOT_FOUND] Book not found at book" +
                        " - use expectError(...) or expectErrors() if errors are intended");
        assertThat(validationContext.getStatus()).isEqualTo(ValidationStatus.FAILED);
    }

    @Test
    public void shouldFailOnUnexpectedErrorWithHttp4xx() {
        Message message = json("{\"errors\":[{\"message\":\"Validation error\"}]}")
                .status(HttpStatus.BAD_REQUEST)
                .contentType("application/graphql-response+json");

        assertThatThrownBy(() -> validate(message, response().build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageStartingWith("GraphQL response contains 1 unexpected error: Validation error");
    }

    @Test
    public void shouldListAllUnexpectedErrors() {
        assertThatThrownBy(() -> validate(json(TWO_ERRORS), response().build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageStartingWith("GraphQL response contains 2 unexpected errors: " +
                        "[INTERNAL] boom at books[1].title; [NOT_FOUND] Book 7 not found at book");
    }

    @Test
    public void shouldAcceptExpectedErrors() {
        validate(json(TWO_ERRORS), response().expectErrors().build());
    }

    @Test
    public void shouldFailWhenExpectedErrorsAreMissing() {
        assertThatThrownBy(() -> validate(json("{\"data\":{\"books\":[]}}"), response().expectErrors().build()))
                .isInstanceOf(ValidationException.class)
                .hasMessage("GraphQL errors expected but none were returned");
    }

    @Test
    public void shouldMatchExpectedErrorAmongSeveral() {
        validate(json(TWO_ERRORS), response()
                .expectError(error().code("NOT_FOUND").message("@contains('not found')@"))
                .build());
    }

    @Test
    public void shouldMatchExpectedErrorByPath() {
        validate(json(TWO_ERRORS), response()
                .expectError(error().path("books", 1, "title"))
                .expectError(error().path("book").code("NOT_FOUND"))
                .build());
    }

    @Test
    public void shouldResolveCitrusVariablesInExpectedError() {
        context.setVariable("code", "NOT_FOUND");

        validate(json(NOT_FOUND), response().expectError(error().code("${code}")).build());
    }

    @Test
    public void shouldListReceivedErrorsWhenNoErrorMatches() {
        GraphQlMessageValidationContext validationContext = response()
                .expectError(error().code("FORBIDDEN"))
                .build();

        assertThatThrownBy(() -> validate(json(TWO_ERRORS), validationContext))
                .isInstanceOf(ValidationException.class)
                .hasMessage("No GraphQL error matched [FORBIDDEN]; received: " +
                        "[INTERNAL] boom at books[1].title; [NOT_FOUND] Book 7 not found at book");
    }

    @Test
    public void shouldRequireEveryExpectedErrorToMatch() {
        GraphQlMessageValidationContext validationContext = response()
                .expectError(error().code("NOT_FOUND"))
                .expectError(error().code("FORBIDDEN"))
                .build();

        assertThatThrownBy(() -> validate(json(TWO_ERRORS), validationContext))
                .isInstanceOf(ValidationException.class)
                .hasMessageStartingWith("No GraphQL error matched [FORBIDDEN]");
    }

    @Test
    public void shouldValidateDataByPathRelativeToData() {
        validate(json("{\"data\":{\"book\":{\"title\":\"Dune\",\"pages\":412}}}"), response()
                .data("$.book.title", "Dune")
                .data("book.pages", 412)
                .data("$.book.title", "@startsWith('Du')@")
                .build());
    }

    @Test
    public void shouldResolveCitrusVariablesInExpectedData() {
        context.setVariable("title", "Dune");

        validate(json("{\"data\":{\"book\":{\"title\":\"Dune\"}}}"), response().data("$.book.title", "${title}").build());
    }

    @Test
    public void shouldFailOnDataMismatch() {
        assertThatThrownBy(() -> validate(json("{\"data\":{\"book\":{\"title\":\"Dune\"}}}"),
                response().data("$.book.title", "Emma").build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Emma")
                .hasMessageContaining("Dune");
    }

    @Test
    public void shouldFailOnMissingDataPath() {
        assertThatThrownBy(() -> validate(json("{\"data\":{\"book\":{\"title\":\"Dune\"}}}"),
                response().data("$.book.isbn", "123").build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("$.data.book.isbn");
    }

    @Test
    public void shouldCompareWholeDataObject() {
        validate(json("{\"data\":{\"book\":{\"title\":\"Dune\",\"author\":\"Herbert\"}}}"),
                response().data("{\"book\":{\"title\":\"Dune\",\"author\":\"@ignore@\"}}").build());
    }

    @Test
    public void shouldFailOnWholeDataMismatch() {
        assertThatThrownBy(() -> validate(json("{\"data\":{\"book\":{\"title\":\"Dune\"}}}"),
                response().data("{\"book\":{\"title\":\"Emma\"}}").build()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void shouldIncludeErrorsWhenDataIsNull() {
        GraphQlMessageValidationContext validationContext = response()
                .expectErrors()
                .data("$.book.title", "Dune")
                .build();

        assertThatThrownBy(() -> validate(json("{\"data\":null,\"errors\":[{\"message\":\"boom\"}]}"), validationContext))
                .isInstanceOf(ValidationException.class)
                .hasMessage("GraphQL data is null; errors: boom");
    }

    @Test
    public void shouldReportAbsentData() {
        GraphQlMessageValidationContext validationContext = response()
                .expectErrors()
                .data("{\"book\":null}")
                .build();

        assertThatThrownBy(() -> validate(json("{\"errors\":[{\"message\":\"boom\"}]}"), validationContext))
                .isInstanceOf(ValidationException.class)
                .hasMessage("GraphQL data is absent; errors: boom");
    }

    @Test
    public void shouldValidatePartialResult() {
        validate(json(PARTIAL), response()
                .expectError(error().path("book", "author"))
                .data("$.book.title", "Dune")
                .build());
    }

    @Test
    public void shouldRejectNonJsonBodyWhenStrict() {
        Message message = new HttpMessage("<html>Bad Gateway</html>")
                .status(HttpStatus.BAD_GATEWAY)
                .contentType("text/html")
                .setHeader(GraphQlMessageHeaders.KIND, GraphQlMessageHeaders.KIND_RESPONSE);

        assertThatThrownBy(() -> validate(message, response().build()))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Expected GraphQL response but got content type 'text/html' with status 502: body is not a JSON object");
    }

    @Test
    public void shouldRejectJsonWithoutDataAndErrorsWhenStrict() {
        assertThatThrownBy(() -> validate(json("{\"book\":{\"title\":\"Dune\"}}"), response().build()))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Expected GraphQL response but got content type 'application/json' with status 200: " +
                        "body has neither 'data' nor 'errors'");
    }

    @Test
    public void shouldRejectErrorWithoutMessageWhenStrict() {
        assertThatThrownBy(() -> validate(json("{\"errors\":[{\"path\":[\"book\"]}]}"), response().expectErrors().build()))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Expected GraphQL response but got content type 'application/json' with status 200: " +
                        "error 0 has no 'message'");
    }

    @Test
    public void shouldSkipShapeCheckWhenNotStrict() {
        Message message = new HttpMessage("<html>Bad Gateway</html>")
                .status(HttpStatus.BAD_GATEWAY)
                .contentType("text/html")
                .setHeader(GraphQlMessageHeaders.KIND, GraphQlMessageHeaders.KIND_RESPONSE);

        validate(message, response().strict(false).build());
    }

    @Test
    public void shouldUseStrictSettingOfEndpoint() {
        GraphQlClient client = new GraphQlClient();
        client.getEndpointConfiguration().setStrict(false);

        validate(json("{\"book\":{}}"), response().endpoint(client).build());
    }

    @Test
    public void shouldLetActionOverrideEndpointStrictSetting() {
        GraphQlClient client = new GraphQlClient();
        client.getEndpointConfiguration().setStrict(false);

        assertThatThrownBy(() -> validate(json("{\"book\":{}}"), response().endpoint(client).strict(true).build()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void shouldStillReportUnexpectedErrorsWhenNotStrict() {
        assertThatThrownBy(() -> validate(json("{\"errors\":[{\"message\":\"x\"}]}"), response().strict(false).build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageStartingWith("GraphQL response contains 1 unexpected error: x");
    }

    @Test
    public void shouldRequireGraphQlResponseForExpectationsWhenNotStrict() {
        Message message = new HttpMessage("<html/>").contentType("text/html").status(HttpStatus.OK)
                .setHeader(GraphQlMessageHeaders.KIND, GraphQlMessageHeaders.KIND_RESPONSE);

        assertThatThrownBy(() -> validate(message, response().strict(false).data("$.a", 1).build()))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Expected GraphQL response but got content type 'text/html' with status 200: body is not a JSON object");
    }

    private void validate(Message received, GraphQlMessageValidationContext validationContext) {
        validator.validateMessage(received, new HttpMessage(), context, List.of(validationContext));
    }

    private static GraphQlMessageValidationContext.Builder response() {
        return GraphQlMessageValidationContext.Builder.response();
    }

    private static HttpMessage json(String body) {
        HttpMessage message = new HttpMessage(body).status(HttpStatus.OK).contentType("application/json");
        message.setHeader(GraphQlMessageHeaders.KIND, GraphQlMessageHeaders.KIND_RESPONSE);
        return message;
    }
}
