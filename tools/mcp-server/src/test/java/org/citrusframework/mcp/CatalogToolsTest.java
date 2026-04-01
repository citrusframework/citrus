/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citrusframework.mcp;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogToolsTest {

    private CatalogTools createTools() {
        CatalogService catalogService = new CatalogService();

        CatalogTools tools = new CatalogTools();
        tools.catalogService = catalogService;
        return tools;
    }

    @Test
    void shouldGetActionDetails() {
        CatalogTools tools = createTools();

        CatalogTools.ComponentDetailResult result = tools.citrus_catalog_action("print", null);

        assertThat(result).isNotNull();
        assertThat(result.version()).isNotNull();
        assertThat(result.name()).isEqualTo("print");
        assertThat(result.properties().size()).isEqualTo(2);
        assertThat(result.properties().get(0).name()).isEqualTo("description");
        assertThat(result.properties().get(0).required()).isFalse();
        assertThat(result.properties().get(1).name()).isEqualTo("message");
        assertThat(result.properties().get(1).required()).isTrue();
    }

    @Test
    void shouldGetEndpointDetails() {
        CatalogTools tools = createTools();

        CatalogTools.ComponentDetailResult result = tools.citrus_catalog_endpoint("http-client", null);

        assertThat(result).isNotNull();
        assertThat(result.version()).isNotNull();
        assertThat(result.name()).isEqualTo("client");
        assertThat(result.fullName()).isEqualTo("http-client");
        assertThat(result.properties().size()).isEqualTo(23);
    }

    @Test
    void shouldGetActionSchema() {
        CatalogTools tools = createTools();

        String result = tools.citrus_catalog_action_schema("print", null);

        assertThat(result).isNotNull();
        assertThat(result).contains("The message to print.");

        result = tools.citrus_catalog_action_schema("iterate", null);

        assertThat(result).isNotNull();
        assertThat(result).contains("Sequence of test actions to execute.");
    }

    @Test
    void shouldGetEndpointSchema() {
        CatalogTools tools = createTools();

        String result = tools.citrus_catalog_endpoint_schema("kubernetes-client", null);

        assertThat(result).isNotNull();
        assertThat(result).contains("Kubernetes client");

        result = tools.citrus_catalog_endpoint_schema("direct-asynchronous", null);

        assertThat(result).isNotNull();
        assertThat(result).contains("The queue name.");
    }

    @Test
    void shouldGetActionsCatalogWithNullArguments() {
        CatalogTools tools = createTools();

        CatalogTools.ComponentListResult result = tools.citrus_catalog_actions(null, null, null);

        assertThat(result).isNotNull();
        assertThat(result.version()).isNotNull();
        assertThat(result.count()).isNotZero();
        assertThat(result.components().size()).isNotZero();
    }

    @Test
    void shouldGetActionsCatalogWithEmptyArguments() {
        CatalogTools tools = createTools();

        CatalogTools.ComponentListResult result = tools.citrus_catalog_actions("", "", "");

        assertThat(result).isNotNull();
        assertThat(result.version()).isNotNull();
        assertThat(result.count()).isNotZero();
        assertThat(result.components().size()).isNotZero();
    }

    @Test
    void shouldGetActionsCatalogWithFilter() {
        CatalogTools tools = createTools();

        CatalogTools.ComponentListResult result = tools.citrus_catalog_actions("print", null, null);

        assertThat(result).isNotNull();
        assertThat(result.count()).isEqualTo(1);
        assertThat(result.components().size()).isEqualTo(1);
        assertThat(result.components().get(0).properties().size()).isEqualTo(2);
    }

    @Test
    void shouldGetActionsCatalogWithGroup() {
        CatalogTools tools = createTools();

        CatalogTools.ComponentListResult result = tools.citrus_catalog_actions(null, "http", null);

        assertThat(result).isNotNull();
        assertThat(result.count()).isEqualTo(4);
    }

    @Test
    void shouldGetEndpointsCatalogWithNullArguments() {
        CatalogTools tools = createTools();

        CatalogTools.ComponentListResult result = tools.citrus_catalog_endpoints(null, null, null);

        assertThat(result).isNotNull();
        assertThat(result.version()).isNotNull();
        assertThat(result.count()).isNotZero();
    }

    @Test
    void shouldGetEndpointsCatalogWithEmptyArguments() {
        CatalogTools tools = createTools();

        CatalogTools.ComponentListResult result = tools.citrus_catalog_endpoints("", "", "");

        assertThat(result).isNotNull();
        assertThat(result.version()).isNotNull();
        assertThat(result.count()).isNotZero();
    }

    @Test
    void shouldGetEndpointsCatalogWithFilter() {
        CatalogTools tools = createTools();

        CatalogTools.ComponentListResult result = tools.citrus_catalog_endpoints("kubernetes-client", null, null);

        assertThat(result).isNotNull();
        assertThat(result.count()).isEqualTo(1);
        assertThat(result.components().size()).isEqualTo(1);
        assertThat(result.components().get(0).properties().size()).isEqualTo(11);
    }

    @Test
    void shouldGetEndpointsCatalogWithGroup() {
        CatalogTools tools = createTools();

        CatalogTools.ComponentListResult result = tools.citrus_catalog_endpoints(null, "http", null);

        assertThat(result).isNotNull();
        assertThat(result.count()).isEqualTo(2);
    }

}
