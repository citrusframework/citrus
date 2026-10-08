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
package org.citrusframework.graphql.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.web.bind.annotation.RequestMethod;
import org.testng.annotations.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class GraphQlClientTest extends AbstractTestNGUnitTest {

    @Test
    public void shouldUseGraphQlDefaults() {
        GraphQlEndpointConfiguration configuration = new GraphQlEndpointConfiguration();
        configuration.setRequestUrl("http://localhost:8080");

        assertThat(configuration.getPath()).isEqualTo("/graphql");
        assertThat(configuration.getRequestMethod()).isEqualTo(RequestMethod.POST);
        assertThat(configuration.isStrict()).isTrue();
        assertThat(configuration.getSchemaResources()).isEmpty();
        assertThat(configuration.getSchemaLoader()).isNull();
    }

    @Test
    public void shouldUseRequestUrlAsIsWhenItHasAPath() {
        GraphQlEndpointConfiguration configuration = new GraphQlEndpointConfiguration();
        configuration.setRequestUrl("http://localhost:8080/api/graphql");

        assertThat(configuration.getPath()).isEmpty();
    }

    @Test
    public void shouldPreferExplicitPath() {
        GraphQlEndpointConfiguration configuration = new GraphQlEndpointConfiguration();
        configuration.setRequestUrl("http://localhost:8080/api");
        configuration.setPath("/v2/graphql");

        assertThat(configuration.getPath()).isEqualTo("/v2/graphql");
    }

    @Test
    public void shouldCreateSchemaLoaderOnceAndResetItWhenResourcesChange() {
        GraphQlEndpointConfiguration configuration = new GraphQlEndpointConfiguration();
        configuration.setSchemaResources(List.of("classpath:org/citrusframework/graphql/schema/library.graphqls"));

        assertThat(configuration.getSchemaLoader()).isNotNull();
        assertThat(configuration.getSchemaLoader()).isSameAs(configuration.getSchemaLoader());
        assertThat(configuration.getSchemaLoader().getSchema().getQueryType().getFieldDefinition("book")).isNotNull();

        var first = configuration.getSchemaLoader();
        configuration.setSchemaResources(List.of("classpath:org/citrusframework/graphql/schema/query.graphqls",
                "classpath:org/citrusframework/graphql/schema/book.graphqls"));

        assertThat(configuration.getSchemaLoader()).isNotSameAs(first);
    }

    @Test
    public void shouldSendThroughConfiguredRequestFactory() {
        List<URI> requested = new ArrayList<>();
        ClientHttpRequestFactory requestFactory = (uri, method) -> {
            requested.add(uri);
            MockClientHttpRequest request = new MockClientHttpRequest(method, uri);
            MockClientHttpResponse response = new MockClientHttpResponse("{\"data\":{\"ok\":true}}".getBytes(UTF_8), HttpStatus.OK);
            response.getHeaders().add("Content-Type", "application/json");
            request.setResponse(response);
            return request;
        };

        GraphQlEndpointConfiguration configuration = new GraphQlEndpointConfiguration();
        configuration.setRequestUrl("http://localhost:1234");
        configuration.setRequestFactory(requestFactory);
        GraphQlClient client = new GraphQlClient(configuration);
        client.setName("graphQlClient");

        client.send(new HttpMessage("{\"query\":\"{ ok }\"}").method(HttpMethod.POST).path("/graphql"), context);
        Message reply = client.receive(context);

        assertThat(requested).containsExactly(URI.create("http://localhost:1234/graphql"));
        assertThat(reply.getPayload(String.class)).isEqualTo("{\"data\":{\"ok\":true}}");
        assertThat(client.getEndpointConfiguration()).isSameAs(configuration);
    }
}
