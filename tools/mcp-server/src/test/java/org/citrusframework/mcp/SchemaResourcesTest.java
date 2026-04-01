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

package org.citrusframework.mcp;

import io.quarkiverse.mcp.server.TextResourceContents;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaResourcesTest {

    private SchemaResources createResources() {
        CatalogService catalogService = new CatalogService();

        SchemaResources resources = new SchemaResources();
        resources.catalogService = catalogService;
        return resources;
    }

    @Test
    void shouldGetYamlDslSchema() {
        SchemaResources resources = createResources();

        TextResourceContents result = resources.citrus_yaml_dsl_schema();

        assertThat(result).isNotNull();
        assertThat(result.uri()).isEqualTo("citrus://schema/dsl/yaml");
        assertThat(result.text()).startsWith("{\"$schema\" : \"http://json-schema.org/draft-07/schema#\"");
    }

    @Test
    void shouldGetXmlIoDslSchema() {
        SchemaResources resources = createResources();

        TextResourceContents result = resources.citrus_xml_dsl_schema();

        assertThat(result).isNotNull();
        assertThat(result.uri()).isEqualTo("citrus://schema/dsl/xml");
        assertThat(result.text()).startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
    }

    @Test
    void shouldGetActionSchema() {
        SchemaResources resources = createResources();

        TextResourceContents result = resources.citrus_action_schema("send");

        assertThat(result).isNotNull();
        assertThat(result.uri()).isEqualTo("citrus://schema/action/send");
        assertThat(result.text()).startsWith("{\"$schema\":\"http://json-schema.org/draft-07/schema#\"");
    }

    @Test
    void shouldGetEndpointSchema() {
        SchemaResources resources = createResources();

        TextResourceContents result = resources.citrus_endpoint_schema("kubernetes-client");

        assertThat(result).isNotNull();
        assertThat(result.uri()).isEqualTo("citrus://schema/endpoint/kubernetes-client");
        assertThat(result.text()).startsWith("{\"$schema\":\"http://json-schema.org/draft-07/schema#\"");
    }
}
