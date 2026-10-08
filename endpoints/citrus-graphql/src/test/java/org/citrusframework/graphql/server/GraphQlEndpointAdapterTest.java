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

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.base.endpoint.adapter.EmptyResponseEndpointAdapter;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.message.Message;
import org.citrusframework.endpoint.EndpointAdapter;
import org.springframework.http.HttpMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphQlEndpointAdapterTest {

    private final List<Message> handled = new ArrayList<>();

    @BeforeMethod
    public void clearHandled() {
        handled.clear();
    }

    @Test
    public void shouldPresentGetRequestAsJsonBody() {
        HttpMessage request = getRequest("/graphql", "query=%7B+books+%7B+title+%7D+%7D,operationName=Books,variables=%7B%22first%22%3A10%7D");

        adapter(new HttpMessage()).handleMessage(request);

        Message received = handled.get(0);
        assertThat(received.getPayload(String.class))
                .isEqualTo("{\"query\":\"{ books { title } }\",\"operationName\":\"Books\",\"variables\":{\"first\":10}}");
        assertThat(received.getHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD)).isEqualTo("GET");
        assertThat(received.getHeader(HttpMessageHeaders.HTTP_QUERY_PARAMS))
                .isEqualTo("query=%7B+books+%7B+title+%7D+%7D,operationName=Books,variables=%7B%22first%22%3A10%7D");
    }

    @Test
    public void shouldPassPostRequestThroughUnchanged() {
        HttpMessage request = new HttpMessage("{\"query\":\"{ a }\"}").method(HttpMethod.POST).path("/graphql");

        adapter(new HttpMessage()).handleMessage(request);

        assertThat(handled.get(0).getPayload(String.class)).isEqualTo("{\"query\":\"{ a }\"}");
    }

    @Test
    public void shouldPassRequestsOnOtherPathsThroughUnchanged() {
        HttpMessage request = getRequest("/health", "query=%7B+a+%7D");

        adapter(new HttpMessage()).handleMessage(request);

        assertThat(handled.get(0).getPayload(String.class)).isEmpty();
    }

    @Test
    public void shouldPassGetRequestWithoutQueryThroughUnchanged() {
        HttpMessage request = getRequest("/graphql", "operationName=Books");

        adapter(new HttpMessage()).handleMessage(request);

        assertThat(handled.get(0).getPayload(String.class)).isEmpty();
    }

    @Test
    public void shouldNegotiateGraphQlResponseMediaType() {
        HttpMessage request = postRequest().accept("application/graphql-response+json, application/json");

        Message reply = adapter(new HttpMessage("{\"data\":{}}")).handleMessage(request);

        assertThat(reply.getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE)).isEqualTo("application/graphql-response+json");
    }

    @Test
    public void shouldFallBackToJsonMediaType() {
        Message reply = adapter(new HttpMessage("{\"data\":{}}")).handleMessage(postRequest().accept("application/json"));

        assertThat(reply.getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE)).isEqualTo("application/json");
    }

    @Test
    public void shouldUseJsonMediaTypeWithoutAcceptHeader() {
        Message reply = adapter(new HttpMessage("{\"data\":{}}")).handleMessage(postRequest());

        assertThat(reply.getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE)).isEqualTo("application/json");
    }

    @Test
    public void shouldReadAcceptHeaderCaseInsensitively() {
        HttpMessage request = postRequest();
        request.setHeader("accept", "application/graphql-response+json");

        Message reply = adapter(new HttpMessage("{\"data\":{}}")).handleMessage(request);

        assertThat(reply.getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE)).isEqualTo("application/graphql-response+json");
    }

    @Test
    public void shouldKeepExplicitReplyContentType() {
        HttpMessage request = postRequest().accept("application/graphql-response+json");

        Message reply = adapter(new HttpMessage("oops").contentType("text/plain")).handleMessage(request);

        assertThat(reply.getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE)).isEqualTo("text/plain");
    }

    @Test
    public void shouldNotNegotiateOnOtherPaths() {
        HttpMessage request = new HttpMessage().method(HttpMethod.GET).path("/health").accept("application/json");

        Message reply = adapter(new HttpMessage("UP")).handleMessage(request);

        assertThat(reply.getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE)).isNull();
    }

    @Test
    public void shouldPassMissingReplyThrough() {
        assertThat(adapter(null).handleMessage(postRequest())).isNull();
    }

    @Test
    public void shouldDelegateEndpoint() {
        EmptyResponseEndpointAdapter delegate = new EmptyResponseEndpointAdapter();
        GraphQlEndpointAdapter adapter = new GraphQlEndpointAdapter(delegate, "/graphql");

        assertThat(adapter.getDelegate()).isSameAs(delegate);
        assertThat(adapter.getEndpoint()).isSameAs(delegate.getEndpoint());
        assertThat(adapter.getEndpointConfiguration()).isSameAs(delegate.getEndpointConfiguration());
    }

    private GraphQlEndpointAdapter adapter(Message reply) {
        return new GraphQlEndpointAdapter(new EndpointAdapter() {
            @Override
            public Message handleMessage(Message message) {
                handled.add(message);
                return reply;
            }

            @Override
            public Endpoint getEndpoint() {
                return null;
            }

            @Override
            public EndpointConfiguration getEndpointConfiguration() {
                return null;
            }
        }, "/graphql");
    }

    private static HttpMessage getRequest(String path, String queryParams) {
        return new HttpMessage("").method(HttpMethod.GET).path(path).queryParams(queryParams);
    }

    private static HttpMessage postRequest() {
        return new HttpMessage("{\"query\":\"{ a }\"}").method(HttpMethod.POST).path("/graphql");
    }
}
