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
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.context.TestContext;
import org.citrusframework.graphql.client.GraphQlClient;
import org.citrusframework.graphql.client.GraphQlEndpointConfiguration;
import org.citrusframework.graphql.message.GraphQlMessageHeaders;
import org.citrusframework.graphql.message.GraphQlMessages;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Producer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.citrusframework.yaml.YamlTestLoader;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GraphQlClientTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadGraphQlClientActions() {
        GraphQlEndpointConfiguration configuration = new GraphQlEndpointConfiguration();
        configuration.setRequestUrl("http://localhost:8080");
        Producer producer = mock(Producer.class);
        SelectiveConsumer consumer = mock(SelectiveConsumer.class);
        GraphQlClient client = mock(GraphQlClient.class);
        when(client.getEndpointConfiguration()).thenReturn(configuration);
        when(client.createProducer()).thenReturn(producer);
        when(client.createConsumer()).thenReturn(consumer);
        when(client.getName()).thenReturn("graphQlClient");
        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new HttpMessage("{\"data\":{\"book\":{\"title\":\"Dune\"}}}").status(HttpStatus.OK),
                new HttpMessage("{\"data\":{\"book\":null},\"errors\":[{\"message\":\"Book 7 not found\",\"path\":[\"book\"],\"extensions\":{\"code\":\"NOT_FOUND\"}}]}")
                        .status(HttpStatus.OK));
        context.getReferenceResolver().bind("graphQlClient", client);

        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/graphql/yaml/graphql-client-test.yaml");
        testLoader.load();

        TestCase result = testLoader.getTestCase();
        assertThat(result.getName()).isEqualTo("GraphQlClientTest");
        assertThat(result.getMetaInfo().getStatus()).isEqualTo(TestCaseMetaInfo.Status.FINAL);
        assertThat(result.getActionCount()).isEqualTo(4L);
        assertThat(result.getTestAction(0).getName()).isEqualTo("graphql:send-request");
        assertThat(result.getTestAction(1).getName()).isEqualTo("graphql:receive-response");

        ArgumentCaptor<Message> sent = ArgumentCaptor.forClass(Message.class);
        verify(producer, times(2)).send(sent.capture(), any(TestContext.class));
        List<Message> messages = sent.getAllValues();

        HttpMessage post = (HttpMessage) messages.get(0);
        assertThat(post.getRequestMethod().name()).isEqualTo("POST");
        assertThat(post.getPayload(String.class)).isEqualTo(
                "{\"query\":\"query Book($id: ID!) { book(id: $id) { title } }\",\"operationName\":\"Book\",\"variables\":{\"id\":\"42\"}}");
        assertThat(post.getHeader(GraphQlMessageHeaders.OPERATION_TYPE)).isEqualTo("query");

        HttpMessage get = (HttpMessage) messages.get(1);
        assertThat(get.getRequestMethod().name()).isEqualTo("GET");
        assertThat(get.getPath()).startsWith("/graphql?query=query+Book");
        assertThat(get.getHeader(GraphQlMessageHeaders.OPERATION_TYPE)).isNull();
        assertThat(GraphQlMessages.toQueryString(GraphQlMessages.toRequest(new HttpMessage().method(org.springframework.http.HttpMethod.GET)
                .queryParams(get.getPath().substring(get.getPath().indexOf('?') + 1).replace(",", "%2C").replace("&", ",")))))
                .contains("variables=%7B%22id%22%3A%227%22%7D");
    }
}
