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
package org.citrusframework.graphql.actions;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.graphql.message.GraphQlMessageHeaders;
import org.citrusframework.graphql.server.GraphQlServer;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.messaging.Producer;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.api.graphql.GraphQlError.error;
import static org.citrusframework.graphql.actions.GraphQlActionBuilder.graphql;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GraphQlServerActionBuilderTest extends AbstractTestNGUnitTest {

    private GraphQlServer server;
    private Producer producer;
    private Consumer consumer;

    @BeforeMethod
    public void setupServer() {
        producer = mock(Producer.class);
        consumer = mock(Consumer.class);
        server = mock(GraphQlServer.class);
        when(server.createProducer()).thenReturn(producer);
        when(server.createConsumer()).thenReturn(consumer);
        when(server.getName()).thenReturn("graphQlServer");
        when(server.isStrict()).thenReturn(true);
        when(server.getPath()).thenReturn("/graphql");
        when(server.getEndpointConfiguration()).thenReturn(new HttpEndpointConfiguration());
    }

    @Test
    public void shouldValidateIncomingOperation() {
        Message received = request("{\"query\":\"query Book($id: ID!) { book(id: $id) { title } }\",\"operationName\":\"Book\",\"variables\":{\"id\":42}}");
        receive(received);

        graphql().server(server).receive()
                .operationName("Book")
                .query("query Book($id:ID!){book(id:$id){title}}")
                .variable("id", "@isNumber()@")
                .build()
                .execute(context);

        assertThat(received.getHeader(GraphQlMessageHeaders.KIND)).isEqualTo("request");
        assertThat(received.getHeader(GraphQlMessageHeaders.OPERATION_NAME)).isEqualTo("Book");
        assertThat(received.getHeader(GraphQlMessageHeaders.OPERATION_TYPE)).isEqualTo("query");
    }

    @Test
    public void shouldFailOnUnexpectedOperation() {
        receive(request("{\"query\":\"query Books { books { title } }\",\"operationName\":\"Books\"}"));

        assertThatThrownBy(() -> graphql().server(server).receive().operationName("Book").build().execute(context))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void shouldReplyWithDataAndErrors() {
        HttpMessage reply = send(graphql().server(server).send()
                .data("{\"book\":{\"title\":\"Dune\",\"author\":null}}")
                .error(error().message("Author unavailable").path("book", "author").code("PARTIAL")));

        assertThat(reply.getPayload(String.class)).isEqualTo("{\"data\":{\"book\":{\"title\":\"Dune\",\"author\":null}}," +
                "\"errors\":[{\"message\":\"Author unavailable\",\"path\":[\"book\",\"author\"],\"extensions\":{\"code\":\"PARTIAL\"}}]}");
        assertThat(reply.getHeader("Content-Type")).isNull();
    }

    @Test
    public void shouldReplyWithErrorsOnly() {
        context.setVariable("bookId", "7");

        HttpMessage reply = send(graphql().server(server).send()
                .error(error().message("Book ${bookId} not found").path("book").code("NOT_FOUND")));

        assertThat(reply.getPayload(String.class))
                .isEqualTo("{\"errors\":[{\"message\":\"Book 7 not found\",\"path\":[\"book\"],\"extensions\":{\"code\":\"NOT_FOUND\"}}]}");
    }

    @Test
    public void shouldKeepStatusAndContentTypeSetOnMessage() {
        var builder = graphql().server(server).send().data("{\"ok\":true}");
        builder.message().status(HttpStatus.BAD_REQUEST).contentType("application/json");

        HttpMessage reply = send(builder);

        assertThat(reply.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(reply.getContentType()).isEqualTo("application/json");
    }

    @Test
    public void shouldRejectMalformedReplyWhenStrict() {
        var builder = graphql().server(server).send();
        builder.message().body("{\"book\":{\"title\":\"Dune\"}}");

        assertThatThrownBy(() -> builder.build().execute(context))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Invalid GraphQL reply: body has neither 'data' nor 'errors'");
        verify(producer, never()).send(any(Message.class), any(TestContext.class));
    }

    @Test
    public void shouldRejectErrorWithoutMessageWhenStrict() {
        assertThatThrownBy(() -> graphql().server(server).send().error(error().code("X")).build().execute(context))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Invalid GraphQL reply: error 0 has no 'message'");
    }

    @Test
    public void shouldSendMalformedReplyWhenNotStrict() {
        var builder = graphql().server(server).send().strict(false);
        builder.message().body("{\"unexpected\":true}");

        assertThat(send(builder).getPayload(String.class)).isEqualTo("{\"unexpected\":true}");
    }

    @Test
    public void shouldNameActions() {
        assertThat(graphql().server(server).receive().build().getName()).isEqualTo("graphql:receive-request");
        assertThat(graphql().server(server).send().data("{}").build().getName()).isEqualTo("graphql:send-response");
    }

    private HttpMessage send(org.citrusframework.TestActionBuilder<?> builder) {
        builder.build().execute(context);
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(producer).send(captor.capture(), eq(context));
        return (HttpMessage) captor.getValue();
    }

    private void receive(Message message) {
        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
    }

    private static HttpMessage request(String body) {
        return new HttpMessage(body).method(HttpMethod.POST).path("/graphql");
    }
}
