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

import org.citrusframework.graphql.message.GraphQlMessageHeaders;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.springframework.http.HttpMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphQlMessageProcessorTest extends AbstractTestNGUnitTest {

    @Test
    public void shouldMarkResponses() {
        Message message = new HttpMessage("{\"data\":{}}");

        new GraphQlMessageProcessor(GraphQlMessageValidationContext.Builder.response().build()).process(message, context);

        assertThat(message.getHeader(GraphQlMessageHeaders.KIND)).isEqualTo("response");
        assertThat(message.getHeader(GraphQlMessageHeaders.OPERATION_NAME)).isNull();
    }

    @Test
    public void shouldSetOperationHeadersOnRequests() {
        Message message = new HttpMessage("{\"query\":\"mutation AddBook { addBook(title: \\\"Dune\\\") { id } }\"}").method(HttpMethod.POST);

        new GraphQlMessageProcessor(GraphQlMessageValidationContext.Builder.request().build()).process(message, context);

        assertThat(message.getHeader(GraphQlMessageHeaders.KIND)).isEqualTo("request");
        assertThat(message.getHeader(GraphQlMessageHeaders.OPERATION_NAME)).isEqualTo("AddBook");
        assertThat(message.getHeader(GraphQlMessageHeaders.OPERATION_TYPE)).isEqualTo("mutation");
    }

    @Test
    public void shouldPreferGivenOperationName() {
        Message message = new HttpMessage("{\"query\":\"query A { a } query B { b }\",\"operationName\":\"B\"}").method(HttpMethod.POST);

        new GraphQlMessageProcessor(GraphQlMessageValidationContext.Builder.request().build()).process(message, context);

        assertThat(message.getHeader(GraphQlMessageHeaders.OPERATION_NAME)).isEqualTo("B");
        assertThat(message.getHeader(GraphQlMessageHeaders.OPERATION_TYPE)).isEqualTo("query");
    }

    @Test
    public void shouldNotParseDocumentWhenNotStrict() {
        Message message = new HttpMessage("{\"query\":\"mutation AddBook { addBook { id } }\"}").method(HttpMethod.POST);

        new GraphQlMessageProcessor(GraphQlMessageValidationContext.Builder.request().strict(false).build()).process(message, context);

        assertThat(message.getHeader(GraphQlMessageHeaders.OPERATION_NAME)).isNull();
        assertThat(message.getHeader(GraphQlMessageHeaders.OPERATION_TYPE)).isNull();
    }

    @Test
    public void shouldLeaveInvalidRequestsToTheValidator() {
        Message message = new HttpMessage("{\"query\":\"{ broken\"}").method(HttpMethod.POST);

        new GraphQlMessageProcessor(GraphQlMessageValidationContext.Builder.request().build()).process(message, context);

        assertThat(message.getHeader(GraphQlMessageHeaders.KIND)).isEqualTo("request");
        assertThat(message.getHeader(GraphQlMessageHeaders.OPERATION_TYPE)).isNull();
    }
}
