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
package org.citrusframework.graphql.endpoint;

import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.graphql.client.GraphQlClientBuilder;
import org.citrusframework.graphql.client.GraphQlEndpointComponent;
import org.citrusframework.graphql.client.GraphQlsEndpointComponent;
import org.citrusframework.graphql.config.annotation.GraphQlClientConfigParser;
import org.citrusframework.graphql.config.annotation.GraphQlServerConfigParser;
import org.citrusframework.graphql.endpoint.builder.GraphQlEndpointBuilder;
import org.citrusframework.graphql.endpoint.builder.GraphQlEndpoints;
import org.citrusframework.graphql.server.GraphQlServerBuilder;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphQlEndpointSpiTest {

    @Test
    public void shouldLookupEndpointBuilders() {
        assertThat(EndpointBuilder.lookup("graphql")).containsInstanceOf(GraphQlEndpointBuilder.class);
        assertThat(EndpointBuilder.lookup("graphql.client")).containsInstanceOf(GraphQlClientBuilder.class);
        assertThat(EndpointBuilder.lookup("graphql.server")).containsInstanceOf(GraphQlServerBuilder.class);
    }

    @Test
    public void shouldLookupEndpointComponents() {
        assertThat(EndpointComponent.lookup("graphql")).containsInstanceOf(GraphQlEndpointComponent.class);
        assertThat(EndpointComponent.lookup("graphqls")).containsInstanceOf(GraphQlsEndpointComponent.class);
    }

    @Test
    public void shouldLookupAnnotationConfigParsers() {
        assertThat(AnnotationConfigParser.lookup("graphql.client")).containsInstanceOf(GraphQlClientConfigParser.class);
        assertThat(AnnotationConfigParser.lookup("graphql.server")).containsInstanceOf(GraphQlServerConfigParser.class);
    }

    @Test
    public void shouldCreateBuildersFromEntryPoint() {
        assertThat(GraphQlEndpoints.graphql().client()).isInstanceOf(GraphQlClientBuilder.class);
        assertThat(GraphQlEndpoints.graphql().server()).isInstanceOf(GraphQlServerBuilder.class);
    }
}
