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
package org.citrusframework.graphql.config.annotation;

import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.graphql.client.GraphQlClient;
import org.citrusframework.graphql.client.GraphQlEndpointConfiguration;
import org.citrusframework.graphql.server.GraphQlServer;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RequestMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphQlConfigParserTest extends AbstractTestNGUnitTest {

    private final ClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

    @CitrusEndpoint
    @GraphQlClientConfig(requestUrl = "http://localhost:8080")
    private GraphQlClient defaultClient;

    @CitrusEndpoint
    @GraphQlClientConfig(requestUrl = "http://localhost:8080",
            path = "/api/graphql",
            schema = "classpath:org/citrusframework/graphql/schema/library.graphqls",
            requestMethod = RequestMethod.GET,
            strict = false,
            requestFactory = "myRequestFactory",
            timeout = 1500L)
    private GraphQlClient customClient;

    @CitrusEndpoint
    @GraphQlServerConfig(port = 18480,
            path = "/gql",
            schema = "classpath:org/citrusframework/graphql/schema/library.graphqls",
            strict = false,
            timeout = 2500L)
    private GraphQlServer server;

    @BeforeMethod
    public void bindReferences() {
        context.getReferenceResolver().bind("myRequestFactory", requestFactory);
    }

    @Test
    public void shouldParseClientDefaults() {
        CitrusAnnotations.injectEndpoints(this, context);

        GraphQlEndpointConfiguration configuration = defaultClient.getEndpointConfiguration();
        assertThat(configuration.getRequestUrl()).isEqualTo("http://localhost:8080");
        assertThat(configuration.getPath()).isEqualTo("/graphql");
        assertThat(configuration.getSchemaResources()).isEmpty();
        assertThat(configuration.getRequestMethod()).isEqualTo(RequestMethod.POST);
        assertThat(configuration.isStrict()).isTrue();
    }

    @Test
    public void shouldParseClientSettings() {
        CitrusAnnotations.injectEndpoints(this, context);

        GraphQlEndpointConfiguration configuration = customClient.getEndpointConfiguration();
        assertThat(configuration.getPath()).isEqualTo("/api/graphql");
        assertThat(configuration.getSchemaResources()).containsExactly("classpath:org/citrusframework/graphql/schema/library.graphqls");
        assertThat(configuration.getRequestMethod()).isEqualTo(RequestMethod.GET);
        assertThat(configuration.isStrict()).isFalse();
        assertThat(configuration.getRequestFactory()).isSameAs(requestFactory);
        assertThat(configuration.getTimeout()).isEqualTo(1500L);
    }

    @Test
    public void shouldParseServerSettings() {
        CitrusAnnotations.injectEndpoints(this, context);

        assertThat(server.getPort()).isEqualTo(18480);
        assertThat(server.getPath()).isEqualTo("/gql");
        assertThat(server.getSchemaResources()).hasSize(1);
        assertThat(server.isStrict()).isFalse();
        assertThat(server.getDefaultTimeout()).isEqualTo(2500L);
        assertThat(server.isAutoStart()).isFalse();
    }
}
