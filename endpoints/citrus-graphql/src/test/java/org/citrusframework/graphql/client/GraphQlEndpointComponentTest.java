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

import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.springframework.web.bind.annotation.RequestMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphQlEndpointComponentTest extends AbstractTestNGUnitTest {

    @Test
    public void shouldCreateHttpClientFromUri() {
        Endpoint endpoint = new GraphQlEndpointComponent().createEndpoint("graphql://localhost:8080/graphql", context);

        assertThat(endpoint).isInstanceOf(GraphQlClient.class);
        GraphQlEndpointConfiguration configuration = ((GraphQlClient) endpoint).getEndpointConfiguration();
        assertThat(configuration.getRequestUrl()).isEqualTo("http://localhost:8080/graphql");
        assertThat(configuration.getPath()).isEmpty();
        assertThat(configuration.isStrict()).isTrue();
        assertThat(configuration.getRequestMethod()).isEqualTo(RequestMethod.POST);
    }

    @Test
    public void shouldCreateHttpsClientFromUri() {
        Endpoint endpoint = new GraphQlsEndpointComponent().createEndpoint("graphqls://api.example.com/graphql", context);

        assertThat(((GraphQlClient) endpoint).getEndpointConfiguration().getRequestUrl())
                .isEqualTo("https://api.example.com/graphql");
    }

    @Test
    public void shouldApplyUriParameters() {
        GraphQlClient client = (GraphQlClient) new GraphQlEndpointComponent()
                .createEndpoint("graphql://localhost:8080?strict=false&requestMethod=GET&timeout=1500", context);

        GraphQlEndpointConfiguration configuration = client.getEndpointConfiguration();
        assertThat(configuration.getRequestUrl()).isEqualTo("http://localhost:8080");
        assertThat(configuration.getPath()).isEqualTo("/graphql");
        assertThat(configuration.isStrict()).isFalse();
        assertThat(configuration.getRequestMethod()).isEqualTo(RequestMethod.GET);
        assertThat(configuration.getTimeout()).isEqualTo(1500L);
    }

    @Test
    public void shouldResolveThroughEndpointFactory() {
        Endpoint endpoint = context.getEndpointFactory().create("graphql://localhost:8080/graphql", context);

        assertThat(endpoint).isInstanceOf(GraphQlClient.class);
    }
}
