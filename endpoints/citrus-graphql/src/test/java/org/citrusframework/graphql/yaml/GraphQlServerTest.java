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
package org.citrusframework.graphql.yaml;

import java.util.List;

import org.citrusframework.TestCase;
import org.citrusframework.context.TestContext;
import org.citrusframework.graphql.server.GraphQlServer;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Producer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.citrusframework.yaml.YamlTestLoader;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GraphQlServerTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadGraphQlServerActions() {
        Producer producer = mock(Producer.class);
        SelectiveConsumer consumer = mock(SelectiveConsumer.class);
        GraphQlServer server = mock(GraphQlServer.class);
        when(server.createProducer()).thenReturn(producer);
        when(server.createConsumer()).thenReturn(consumer);
        when(server.getName()).thenReturn("graphQlServer");
        when(server.isStrict()).thenReturn(true);
        when(server.getPath()).thenReturn("/graphql");
        when(server.getEndpointConfiguration()).thenReturn(new HttpEndpointConfiguration());
        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new HttpMessage("{\"query\":\"query Book($id: ID!) { book(id: $id) { title } }\",\"operationName\":\"Book\",\"variables\":{\"id\":42}}")
                        .method(HttpMethod.POST)
                        .path("/graphql"));
        context.getReferenceResolver().bind("graphQlServer", server);

        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/graphql/yaml/graphql-server-test.yaml");
        testLoader.load();

        TestCase result = testLoader.getTestCase();
        assertThat(result.getActionCount()).isEqualTo(3L);
        assertThat(result.getTestAction(0).getName()).isEqualTo("graphql:receive-request");
        assertThat(result.getTestAction(1).getName()).isEqualTo("graphql:send-response");
        assertThat(context.getVariable("operationType")).isEqualTo("query");

        ArgumentCaptor<Message> sent = ArgumentCaptor.forClass(Message.class);
        verify(producer, times(2)).send(sent.capture(), any(TestContext.class));
        List<Message> replies = sent.getAllValues();

        HttpMessage reply = (HttpMessage) replies.get(0);
        assertThat(reply.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(reply.getPayload(String.class)).isEqualTo("{\"data\":{\"book\":{\"title\":\"Dune\",\"author\":null}}," +
                "\"errors\":[{\"message\":\"Author unavailable (query)\",\"path\":[\"book\",\"author\"],\"extensions\":{\"code\":\"PARTIAL\"}}]}");
        assertThat(replies.get(1).getPayload(String.class)).isEqualTo("{\"unexpected\":true}");
    }
}
