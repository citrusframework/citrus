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

import java.util.List;

import org.citrusframework.http.server.HttpServer;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphQlServerBuilderTest {

    @Test
    public void shouldBuildGraphQlServer() {
        GraphQlServer server = new GraphQlServerBuilder()
                .port(18380)
                .path("/api/graphql")
                .schema("classpath:org/citrusframework/graphql/schema/library.graphqls")
                .strict(false)
                .timeout(2000L)
                .build();

        assertThat(server.getPort()).isEqualTo(18380);
        assertThat(server.getPath()).isEqualTo("/api/graphql");
        assertThat(server.getSchemaResources()).hasSize(1);
        assertThat(server.isStrict()).isFalse();
    }

    @Test
    public void shouldBuildFromSetters() {
        GraphQlServerBuilder builder = new GraphQlServerBuilder();
        builder.setPath("/gql");
        builder.setSchema(List.of("classpath:org/citrusframework/graphql/schema/library.graphqls"));
        builder.setStrict(false);

        GraphQlServer server = builder.build();

        assertThat(server.getPath()).isEqualTo("/gql");
        assertThat(server.getSchemaResources()).hasSize(1);
        assertThat(server.isStrict()).isFalse();
    }

    @Test
    public void shouldSupportOnlyGraphQlServers() {
        GraphQlServerBuilder builder = new GraphQlServerBuilder();

        assertThat(builder.supports(GraphQlServer.class)).isTrue();
        assertThat(builder.supports(HttpServer.class)).isFalse();
    }
}
