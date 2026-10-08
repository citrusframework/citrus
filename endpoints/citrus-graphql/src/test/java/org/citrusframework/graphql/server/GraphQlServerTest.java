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
package org.citrusframework.graphql.server;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.ReplyProducer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

public class GraphQlServerTest extends AbstractTestNGUnitTest {

    @Test
    public void shouldUseGraphQlDefaults() {
        GraphQlServer server = new GraphQlServer();

        assertThat(server.getPath()).isEqualTo("/graphql");
        assertThat(server.isStrict()).isTrue();
        assertThat(server.getSchemaLoader()).isNull();

        server.setSchemaResources(List.of("classpath:org/citrusframework/graphql/schema/library.graphqls"));
        assertThat(server.getSchemaLoader()).isSameAs(server.getSchemaLoader());
    }

    @Test
    public void shouldWrapEndpointAdapterOnce() {
        GraphQlServer server = new GraphQlServer();
        server.setName("wrappedServer");

        server.initialize();
        server.initialize();

        assertThat(server.getEndpointAdapter()).isInstanceOf(GraphQlEndpointAdapter.class);
        assertThat(((GraphQlEndpointAdapter) server.getEndpointAdapter()).getDelegate())
                .isNotInstanceOf(GraphQlEndpointAdapter.class);
    }

    @Test
    public void shouldServeGetRequestsAsJsonThroughStartedServer() throws Exception {
        int port = findAvailableTcpPort(18280);
        GraphQlServer server = new GraphQlServer();
        server.setName("graphQlServer");
        server.setPort(port);
        server.setAutoStart(true);
        server.setDefaultTimeout(5000L);
        server.initialize();

        try {
            CompletableFuture<HttpResponse<String>> response = HttpClient.newHttpClient().sendAsync(
                    HttpRequest.newBuilder(URI.create("http://localhost:%d/graphql?query=%%7B+books+%%7B+title+%%7D+%%7D".formatted(port)))
                            .header("Accept", "application/graphql-response+json")
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofString());

            SelectiveConsumer consumer = (SelectiveConsumer) server.createConsumer();
            Message request = consumer.receive(context, 5000L);
            assertThat(request.getPayload(String.class)).isEqualTo("{\"query\":\"{ books { title } }\"}");

            ((ReplyProducer) consumer).send(new HttpMessage("{\"data\":{\"books\":[]}}"), context);

            HttpResponse<String> reply = response.get(10, TimeUnit.SECONDS);
            assertThat(reply.statusCode()).isEqualTo(200);
            assertThat(reply.headers().firstValue("Content-Type")).hasValueSatisfying(
                    contentType -> assertThat(contentType).startsWith("application/graphql-response+json"));
            assertThat(reply.body()).isEqualTo("{\"data\":{\"books\":[]}}");
        } finally {
            server.stop();
        }
    }
}
