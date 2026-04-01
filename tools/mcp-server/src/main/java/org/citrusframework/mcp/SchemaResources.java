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

import io.quarkiverse.mcp.server.Resource;
import io.quarkiverse.mcp.server.ResourceTemplate;
import io.quarkiverse.mcp.server.ResourceTemplateArg;
import io.quarkiverse.mcp.server.TextResourceContents;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * MCP Resources exposing Citrus schemas for instance the YAML and XML DSL schema and endpoint property schemas.
 */
@ApplicationScoped
public class SchemaResources {

    @Inject
    CatalogService catalogService;

    /**
     * Resource provides access to Citrus YAML DSL schema.
     */
    @Resource(uri = "citrus://schema/dsl/yaml",
            name = "citrus_dsl_schema_yaml",
            title = "Citrus YAML DSL Schema",
            description = "Retrieves the schema for the Citrus YAML domain specific language.",
            mimeType = "application/json")
    public TextResourceContents citrus_yaml_dsl_schema() {
        String uri = "citrus://schema/dsl/yaml";

        try {
            String schema = catalogService.getSchema("citrus-yaml");
            if (schema == null) {
                return error(uri, "Schema not found: citrus-yaml");
            }

            return new TextResourceContents(uri, schema, "application/json");
        } catch (Throwable ex) {
            return error(uri, ex.getMessage());
        }
    }

    /**
     * Resource provides access to Citrus XML DSL schema.
     */
    @Resource(uri = "citrus://schema/dsl/xml",
            name = "citrus_dsl_schema_xml",
            title = "Citrus XML DSL Schema",
            description = "Retrieves the schema for the Citrus XML domain specific language.",
            mimeType = "application/xml")
    public TextResourceContents citrus_xml_dsl_schema() {
        String uri = "citrus://schema/dsl/xml";

        try {
            String schema = catalogService.getSchema("citrus-xml");
            if (schema == null) {
                return error(uri, "Schema not found: citrus-xml");
            }

            return new TextResourceContents(uri, schema, "application/xml");
        } catch (Throwable ex) {
            return error(uri, ex.getMessage());
        }
    }

    /**
     * Resource provides access to Citrus test action schemas.
     */
    @ResourceTemplate(uriTemplate = "citrus://schema/action/{name}",
            name = "citrus_action_schema",
            title = "Citrus Test Action Schema",
            description = "Retrieves the Json schema for a specific Citrus test action (e.g., print, send, receive). " +
                    "The schema includes all available test action properties",
            mimeType = "application/json")
    public TextResourceContents citrus_action_schema(@ResourceTemplateArg(name = "name") String name) {
        String uri = "citrus://schema/action/" + name;

        if (name == null || name.isBlank()) {
            return error(uri, "Test action name is required");
        }

        try {
            CatalogService.ComponentDefinition definition = catalogService.getTestActions().get(name);
            if (definition == null) {
                definition = catalogService.getTestContainers().get(name);
            }
            if (definition == null) {
                return error(uri, "Test action not found:" + name);
            }

            return new TextResourceContents(uri, JsonSupport.json().writeValueAsString(definition.propertiesSchema()), "application/json");
        } catch (Throwable ex) {
            return error(uri, ex.getMessage());
        }
    }

    /**
     * Resource provides access to Citrus endpoint schemas.
     */
    @ResourceTemplate(uriTemplate = "citrus://schema/endpoint/{name}",
            name = "citrus_endpoint_schema",
            title = "Citrus Endpoint Schema",
            description = "Retrieves the Json schema for a specific Citrus endpoint (e.g., kafka, http, direct). " +
                    "The schema includes all available endpoint properties",
            mimeType = "application/json")
    public TextResourceContents citrus_endpoint_schema(@ResourceTemplateArg(name = "name") String name) {
        String uri = "citrus://schema/endpoint/" + name;

        if (name == null || name.isBlank()) {
            return error(uri, "Test endpoint name is required");
        }

        try {
            CatalogService.ComponentDefinition definition = catalogService.getEndpoints().get(name);
            if (definition == null) {
                return error(uri, "Test endpoint not found:" + name);
            }

            return new TextResourceContents(uri, JsonSupport.json().writeValueAsString(definition.propertiesSchema()), "application/json");
        } catch (Throwable ex) {
            return error(uri, ex.getMessage());
        }
    }

    /**
     * Create a simple Json response that represents an error information.
     */
    private TextResourceContents error(String uri, String message) {
        return new TextResourceContents(uri, "{ \"error\": \"%s\" }".formatted(message), "application/json");
    }
}
