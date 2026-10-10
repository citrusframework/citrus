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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.spi.SimpleReferenceResolver;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RequestMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphQlClientBuilderTest {

    @Test
    public void shouldBuildGraphQlClient() {
        GraphQlClient client = new GraphQlClientBuilder()
                .requestUrl("http://localhost:8080")
                .path("/api/graphql")
                .schema("classpath:org/citrusframework/graphql/schema/library.graphqls")
                .strict(false)
                .requestMethod(RequestMethod.GET)
                .timeout(2000L)
                .build();

        GraphQlEndpointConfiguration configuration = client.getEndpointConfiguration();
        assertThat(configuration.getRequestUrl()).isEqualTo("http://localhost:8080");
        assertThat(configuration.getPath()).isEqualTo("/api/graphql");
        assertThat(configuration.getSchemaResources()).containsExactly("classpath:org/citrusframework/graphql/schema/library.graphqls");
        assertThat(configuration.isStrict()).isFalse();
        assertThat(configuration.getRequestMethod()).isEqualTo(RequestMethod.GET);
        assertThat(configuration.getTimeout()).isEqualTo(2000L);
    }

    @Test
    public void shouldSupportOnlyGraphQlClients() {
        GraphQlClientBuilder builder = new GraphQlClientBuilder();

        assertThat(builder.supports(GraphQlClient.class)).isTrue();
        assertThat(builder.supports(HttpClient.class)).isFalse();
    }

    @Test
    public void shouldResolveHttpReferencesLikeTheHttpClientBuilder() {
        ClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        SimpleReferenceResolver referenceResolver = new SimpleReferenceResolver();
        referenceResolver.bind("myRequestFactory", requestFactory);

        GraphQlClientBuilder builder = new GraphQlClientBuilder();
        builder.setRequestUrl("http://localhost:8080");
        builder.setRequestFactory("myRequestFactory");
        builder.setSchema(List.of("classpath:org/citrusframework/graphql/schema/library.graphqls"));
        builder.setPath("/gql");
        builder.setStrict(false);
        builder.setReferenceResolver(referenceResolver);

        GraphQlClient client = builder.build();

        assertThat(client.getEndpointConfiguration().getRequestFactory()).isSameAs(requestFactory);
        assertThat(client.getEndpointConfiguration().getPath()).isEqualTo("/gql");
        assertThat(client.getEndpointConfiguration().getSchemaResources()).hasSize(1);
        assertThat(client.getEndpointConfiguration().isStrict()).isFalse();
    }

    /**
     * Every fluent HTTP client setting is re-declared with the GraphQL builder as return type, so
     * chains keep their type, e.g. {@code requestUrl(...).path(...)}.
     */
    @Test
    public void shouldOverrideEveryFluentHttpClientSetting() {
        List<Method> fluentHttpMethods = Arrays.stream(HttpClientBuilder.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> method.getReturnType().equals(HttpClientBuilder.class))
                .toList();

        assertThat(fluentHttpMethods).isNotEmpty();
        for (Method httpMethod : fluentHttpMethods) {
            assertThat(declaredOverride(httpMethod))
                    .as("GraphQlClientBuilder must override %s returning GraphQlClientBuilder", httpMethod)
                    .isTrue();
        }
    }

    private static boolean declaredOverride(Method httpMethod) {
        try {
            Method override = GraphQlClientBuilder.class.getDeclaredMethod(httpMethod.getName(), httpMethod.getParameterTypes());
            return override.getReturnType().equals(GraphQlClientBuilder.class);
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
