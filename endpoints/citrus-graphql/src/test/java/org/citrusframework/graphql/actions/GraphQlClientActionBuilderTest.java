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

import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.graphql.client.GraphQlClient;
import org.citrusframework.graphql.client.GraphQlEndpointConfiguration;
import org.citrusframework.graphql.message.GraphQlMessageHeaders;
import org.citrusframework.graphql.message.GraphQlMessages;
import org.citrusframework.graphql.validation.GraphQlMessageProcessor;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.SelectiveConsumer;
import org.citrusframework.messaging.Producer;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
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

public class GraphQlClientActionBuilderTest extends AbstractTestNGUnitTest {

    private GraphQlClient client;
    private GraphQlEndpointConfiguration configuration;
    private Producer producer;
    private SelectiveConsumer consumer;

    @BeforeMethod
    public void setupClient() {
        configuration = new GraphQlEndpointConfiguration();
        configuration.setRequestUrl("http://localhost:8080");
        producer = mock(Producer.class);
        consumer = mock(SelectiveConsumer.class);
        client = mock(GraphQlClient.class);
        when(client.getEndpointConfiguration()).thenReturn(configuration);
        when(client.createProducer()).thenReturn(producer);
        when(client.createConsumer()).thenReturn(consumer);
        when(client.getName()).thenReturn("graphQlClient");
    }

    @Test
    public void shouldPostGraphQlEnvelope() {
        context.setVariable("id", "42");

        HttpMessage sent = send(graphql().client(client).send()
                .query("query Book($id: ID!) { book(id: $id) { title } }")
                .operationName("Book")
                .variable("id", "${id}"));

        assertThat(sent.getPayload(String.class)).isEqualTo(
                "{\"query\":\"query Book($id: ID!) { book(id: $id) { title } }\",\"operationName\":\"Book\",\"variables\":{\"id\":\"42\"}}");
        assertThat(sent.getRequestMethod().name()).isEqualTo("POST");
        assertThat(sent.getPath()).isEqualTo("/graphql");
        assertThat(sent.getContentType()).isEqualTo("application/json");
        assertThat(sent.getAccept()).isEqualTo("application/graphql-response+json, application/json");
        assertThat(sent.getHeader(GraphQlMessageHeaders.OPERATION_NAME)).isEqualTo("Book");
        assertThat(sent.getHeader(GraphQlMessageHeaders.OPERATION_TYPE)).isEqualTo("query");
    }

    @Test
    public void shouldKeepAcceptHeaderSetOnMessage() {
        var builder = graphql().client(client).send().query("{ a }");
        builder.message().accept("application/json");

        assertThat(send(builder).getAccept()).isEqualTo("application/json");
    }

    @Test
    public void shouldReadQueryFromResource() {
        HttpMessage sent = send(graphql().client(client).send()
                .queryResource("classpath:org/citrusframework/graphql/actions/book.graphql")
                .variable("id", "1"));

        assertThat(GraphQlMessages.toRequest(sent).getDocument()).startsWith("query Book($id: ID!) {\n  book(id: $id)");
    }

    @Test
    public void shouldSendQueryOverGet() {
        HttpMessage sent = send(graphql().client(client).send()
                .get()
                .query("query Books($first: Int) { books(first: $first) { title } }")
                .variable("first", 10));

        assertThat(sent.getRequestMethod().name()).isEqualTo("GET");
        assertThat(sent.getPayload(String.class)).isEmpty();
        assertThat(sent.getPath()).startsWith("/graphql?query=");
        Map<String, String> params = decodeQuery(sent.getPath().substring(sent.getPath().indexOf('?') + 1));
        assertThat(params).containsEntry("query", "query Books($first: Int) { books(first: $first) { title } }")
                .containsEntry("variables", "{\"first\":10}");
    }

    @Test
    public void shouldKeepVariableTypes() {
        context.setVariable("n", "5");

        HttpMessage sent = send(graphql().client(client).send()
                .query("{ books { title } }")
                .variables("{\"after\": ${n}}")
                .variable("first", 10)
                .variable("draft", true)
                .variable("tag", "${n}"));

        assertThat(GraphQlMessages.toRequest(sent).getVariables())
                .containsExactly(Map.entry("after", 5), Map.entry("first", 10), Map.entry("draft", true), Map.entry("tag", "5"));
    }

    @Test
    public void shouldRejectInvalidDocumentBeforeSending() {
        assertThatThrownBy(() -> graphql().client(client).send().query("query { book(id: 1) { title }").build().execute(context))
                .isInstanceOf(ValidationException.class)
                .hasMessageStartingWith("Invalid GraphQL document (line 1, column ");
        verify(producer, never()).send(any(Message.class), any(TestContext.class));
    }

    @Test
    public void shouldRejectMutationOverGetBeforeSending() {
        assertThatThrownBy(() -> graphql().client(client).send().get().query("mutation { deleteBook(id: 1) }").build().execute(context))
                .isInstanceOf(ValidationException.class)
                .hasMessage("GraphQL mutation operations cannot be sent over GET - only queries can");
        verify(producer, never()).send(any(Message.class), any(TestContext.class));
    }

    @Test
    public void shouldValidateAgainstEndpointSchemaBeforeSending() {
        configuration.setSchemaResources(List.of("classpath:org/citrusframework/graphql/schema/library.graphqls"));

        assertThatThrownBy(() -> graphql().client(client).send().query("{ book(id: 1) { isbn } }").build().execute(context))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("isbn");
        verify(producer, never()).send(any(Message.class), any(TestContext.class));
    }

    @Test
    public void shouldSendDocumentAsWrittenWhenNotStrict() {
        HttpMessage sent = send(graphql().client(client).send()
                .strict(false)
                .get()
                .query("mutation { broken"));

        assertThat(decodeQuery(sent.getPath().substring(sent.getPath().indexOf('?') + 1)))
                .containsEntry("query", "mutation { broken");
        assertThat(sent.getHeader(GraphQlMessageHeaders.OPERATION_TYPE)).isNull();
    }

    @Test
    public void shouldUseEndpointDefaultMethodUnlessOverridden() {
        configuration.setRequestMethod(RequestMethod.GET);

        assertThat(send(graphql().client(client).send().query("{ a }")).getRequestMethod().name()).isEqualTo("GET");
        setupClient();
        configuration.setRequestMethod(RequestMethod.GET);
        assertThat(send(graphql().client(client).send().post().query("{ a }")).getRequestMethod().name()).isEqualTo("POST");
    }

    @Test
    public void shouldUseRequestUrlAsIsWhenItHasAPath() {
        configuration.setRequestUrl("http://localhost:8080/api/graphql");

        HttpMessage post = send(graphql().client(client).send().query("{ a }"));
        assertThat(post.getPath()).isNull();

        setupClient();
        configuration.setRequestUrl("http://localhost:8080/api/graphql");
        HttpMessage get = send(graphql().client(client).send().get().query("{ a }"));
        assertThat(get.getHeader(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME).toString())
                .startsWith("http://localhost:8080/api/graphql?query=");
    }

    @Test
    public void shouldRequireQuery() {
        assertThatThrownBy(() -> graphql().client(client).send().build().execute(context))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessage("Missing GraphQL query - set query(...) or queryResource(...)");
    }

    @Test
    public void shouldNameActions() {
        assertThat(graphql().client(client).send().query("{ a }").build().getName()).isEqualTo("graphql:send-request");
        assertThat(graphql().client(client).receive().build().getName()).isEqualTo("graphql:receive-response");
    }

    @Test
    public void shouldBeFoundAsTestActionBuilder() {
        assertThat(TestActionBuilder.lookup("graphql")).containsInstanceOf(GraphQlActionBuilder.class);
    }

    @Test
    public void shouldValidateReceivedData() {
        receive(response("{\"data\":{\"book\":{\"title\":\"Dune\"}}}"));

        graphql().client(client).receive().data("$.book.title", "Dune").build().execute(context);
    }

    @Test
    public void shouldFailReceiveOnUnexpectedErrors() {
        receive(response("{\"data\":null,\"errors\":[{\"message\":\"Book not found\",\"path\":[\"book\"],\"extensions\":{\"code\":\"NOT_FOUND\"}}]}"));

        assertThatThrownBy(() -> graphql().client(client).receive().build().execute(context))
                .isInstanceOf(ValidationException.class)
                .hasMessageStartingWith("GraphQL response contains 1 unexpected error: [NOT_FOUND] Book not found at book");
    }

    @Test
    public void shouldAcceptExpectedErrors() {
        Message received = response("{\"errors\":[{\"message\":\"Book not found\",\"extensions\":{\"code\":\"NOT_FOUND\"}}]}");
        receive(received);

        graphql().client(client).receive().expectError(error().code("NOT_FOUND")).build().execute(context);

        assertThat(received.getHeader(GraphQlMessageHeaders.KIND)).isEqualTo("response");
    }

    @Test
    public void shouldUseEndpointStrictSettingOnReceive() {
        configuration.setStrict(false);
        receive(new HttpMessage("<html>Bad Gateway</html>").status(HttpStatus.BAD_GATEWAY).contentType("text/html"));

        graphql().client(client).receive().build().execute(context);
    }

    @Test
    public void shouldAddProcessorOnce() {
        var builder = graphql().client(client).receive();
        builder.build();

        assertThat(builder.getMessageProcessors()).filteredOn(GraphQlMessageProcessor.class::isInstance).hasSize(1);
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

    private static HttpMessage response(String body) {
        return new HttpMessage(body).status(HttpStatus.OK).contentType("application/json");
    }

    private static Map<String, String> decodeQuery(String queryString) {
        Map<String, String> params = new LinkedHashMap<>();
        for (String pair : queryString.split("&")) {
            int separator = pair.indexOf('=');
            params.put(pair.substring(0, separator), URLDecoder.decode(pair.substring(separator + 1), UTF_8));
        }
        return params;
    }
}
