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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolCallException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.citrusframework.CitrusVersion;

/**
 * MCP Tools for querying the Citrus catalog and its component definitions using Quarkus MCP Server.
 */
@ApplicationScoped
public class CatalogTools {

    @Inject
    CatalogService catalogService;

    /**
     * Tool to list available Citrus test actions. This includes test actions and containers.
     */
    @Tool(description = "List available Citrus test actions from the catalog. " +
                        "Returns test action name, description, and group information. " +
                        "Use filter to search by name, group to filter by group.")
    public ComponentListResult citrus_catalog_actions(
            @ToolArg(required = false, description = "Filter actions by name (case-insensitive substring match)") String filter,
            @ToolArg(required = false, description = "Filter by group (e.g., camel, kubernetes, selenium)") String group,
            @ToolArg(required = false, description = "Citrus version to query. If not specified, uses the default catalog version.") String version) {

        try {
            String citrusVersion;
            if (version != null && !version.isBlank()) {
                citrusVersion = version;
            } else {
                citrusVersion = CitrusVersion.version();
            }

            catalogService.loadCatalog(citrusVersion);

            List<ComponentDetailResult> results = new ArrayList<>();
             results.addAll(catalogService.getTestActions().values()
                    .stream()
                    .filter(def -> def.kind().equals("testAction"))
                    .filter(def -> matchesFilter(def.name(), def.title(), def.description(), filter))
                    .filter(def -> matchesGroup(def.group(), group))
                    .map(this::toComponentDetailResult)
                    .toList());

            results.addAll(catalogService.getTestContainers().values()
                    .stream()
                    .filter(def -> matchesFilter(def.name(), def.title(), def.description(), filter))
                    .filter(def -> matchesGroup(def.group(), group))
                    .map(this::toComponentDetailResult)
                    .toList());

            return new ComponentListResult(citrusVersion, results.size(), results);
        } catch (ToolCallException e) {
            throw e;
        } catch (Throwable e) {
            throw new ToolCallException("Failed to list actions: " + e.getMessage(), e);
        }
    }

    /**
     * Tool to get detailed information for a specific test action.
     */
    @Tool(description = "Get detailed information for a Citrus test action including all properties and some usage examples.")
    public ComponentDetailResult citrus_catalog_action(
            @ToolArg(description = "Test action name (e.g., send, receive, print)") String name,
            @ToolArg(required = false, description = "Citrus version to query. If not specified, uses the default catalog version.") String version) {

        if (name == null || name.isBlank()) {
            throw new ToolCallException("Test action name is required", null);
        }

        try {
            catalogService.loadCatalog(version);
            CatalogService.ComponentDefinition definition = catalogService.getTestActions().get(name);
            if (definition == null) {
                definition = catalogService.getTestContainers().get(name);
            }

            if (definition == null) {
                throw new ToolCallException("Test action not found: " + name);
            }

            return toComponentDetailResult(definition);
        } catch (ToolCallException e) {
            throw e;
        } catch (Throwable ex) {
            throw new ToolCallException(
                    "Test action not found: " + name + " (" + ex.getClass().getName() + "): " + ex.getMessage(), null);
        }
    }

    /**
     * Tool to get the Json schema for a specific test action with all defined properties.
     */
    @Tool(description = "Get the detailed property Json schema for a Citrus test action.")
    public String citrus_catalog_action_schema(
            @ToolArg(description = "Test action name (e.g., kafka, http, file, timer)") String name,
            @ToolArg(description = "Citrus version to query. If not specified, uses the default catalog version.") String version) {

        if (name == null || name.isBlank()) {
            throw new ToolCallException("Test action name is required", null);
        }

        try {
            catalogService.loadCatalog(version);
            CatalogService.ComponentDefinition definition = catalogService.getTestActions().get(name);
            if (definition == null) {
                definition = catalogService.getTestContainers().get(name);
            }

            if (definition == null) {
                throw new ToolCallException("Test action not found: " + name);
            }

            return JsonSupport.json().writeValueAsString(definition.propertiesSchema());
        } catch (ToolCallException e) {
            throw e;
        } catch (Throwable ex) {
            throw new ToolCallException(
                    "Test action not found: " + name + " (" + ex.getClass().getName() + "): " + ex.getMessage(), null);
        }
    }

    /**
     * Tool to list available Citrus endpoints.
     */
    @Tool(description = "List available Citrus endpoints from the catalog. " +
            "Returns endpoint name, description and the endpoint properties specification. " +
            "Use filter to search by name, group to filter by group.")
    public ComponentListResult citrus_catalog_endpoints(
            @ToolArg(required = false, description = "Filter endpoints by name (case-insensitive substring match)") String filter,
            @ToolArg(required = false, description = "Filter by group (e.g., camel, kubernetes, selenium)") String group,
            @ToolArg(required = false, description = "Citrus version to query. If not specified, uses the default catalog version.") String version) {

        try {
            String citrusVersion;
            if (version != null && !version.isBlank()) {
                citrusVersion = version;
            } else {
                citrusVersion = CitrusVersion.version();
            }

            catalogService.loadCatalog(citrusVersion);

            List<ComponentDetailResult> results = catalogService.getEndpoints().values()
                    .stream()
                    .filter(def -> matchesFilter(def.name(), def.title(), def.description(), filter))
                    .filter(def -> matchesGroup(def.group(), group))
                    .map(this::toComponentDetailResult)
                    .toList();

            return new ComponentListResult(citrusVersion, results.size(), results);
        } catch (ToolCallException e) {
            throw e;
        } catch (Throwable e) {
            throw new ToolCallException("Failed to list endpoints: " + e.getMessage(), e);
        }
    }

    /**
     * Tool to get detailed information for a specific Citrus endpoint.
     */
    @Tool(description = "Get detailed information for a Citrus endpoint including all endpoint properties.")
    public ComponentDetailResult citrus_catalog_endpoint(
            @ToolArg(description = "Test endpoint name (e.g., kafka, http, file, timer)") String name,
            @ToolArg(required = false, description = "Citrus version to query. If not specified, uses the default catalog version.") String version) {

        if (name == null || name.isBlank()) {
            throw new ToolCallException("Test endpoint name is required", null);
        }

        try {
            catalogService.loadCatalog(version);
            CatalogService.ComponentDefinition definition = catalogService.getEndpoints().get(name);

            if (definition == null && !name.contains("-")) {
                definition = catalogService.getEndpoints().get(name + "-asynchronous");
            }

            if (definition == null) {
                throw new ToolCallException("Test endpoint not found: " + name);
            }

            return toComponentDetailResult(definition);
        } catch (ToolCallException e) {
            throw e;
        } catch (Throwable ex) {
            throw new ToolCallException(
                    "Test endpoint not found: " + name + " (" + ex.getClass().getName() + "): " + ex.getMessage(), null);
        }
    }

    /**
     * Tool to get the Json schema for a specific Citrus endpoint with all defined properties.
     */
    @Tool(description = "Get the detailed property Json schema for a Citrus endpoint.")
    public String citrus_catalog_endpoint_schema(
            @ToolArg(description = "Endpoint name (e.g., kafka, http, direct)") String name,
            @ToolArg(description = "Citrus version to query. If not specified, uses the default catalog version.") String version) {

        if (name == null || name.isBlank()) {
            throw new ToolCallException("Test endpoint name is required", null);
        }

        try {
            catalogService.loadCatalog(version);
            CatalogService.ComponentDefinition definition = catalogService.getEndpoints().get(name);

            if (definition == null && !name.contains("-")) {
                definition = catalogService.getEndpoints().get(name + "-asynchronous");
            }

            if (definition == null) {
                throw new ToolCallException("Test endpoint not found: " + name);
            }

            return JsonSupport.json().writeValueAsString(definition.propertiesSchema());
        } catch (ToolCallException e) {
            throw e;
        } catch (Throwable ex) {
            throw new ToolCallException(
                    "Test endpoint not found: " + name + " (" + ex.getClass().getName() + "): " + ex.getMessage(), null);
        }
    }

    private ComponentDetailResult toComponentDetailResult(CatalogService.ComponentDefinition definition) {
        List<PropertyInfo> properties = new ArrayList<>();
        if (definition.propertiesSchema() != null) {
            Optional.ofNullable(definition.propertiesSchema().get("properties"))
                    .map(JsonNode::properties)
                    .orElseGet(Collections::emptySet)
                    .forEach(property -> properties.add(new PropertyInfo(
                        property.getKey(),
                        Optional.ofNullable(property.getValue().get("description"))
                                .map(JsonNode::textValue)
                                .orElse(null),
                        Optional.ofNullable(property.getValue().get("type")).map(JsonNode::textValue).orElse(null),
                        Optional.ofNullable(definition.propertiesSchema().get("required"))
                                .map(o -> (ArrayNode) o)
                                .map(array -> array.valueStream()
                                        .anyMatch(node -> property.getKey().equals(node.textValue())))
                                .orElse(false),
                        Optional.ofNullable(property.getValue().get("defaultValue"))
                                .map(JsonNode::textValue)
                                .orElse(null))));
        }

        return new ComponentDetailResult(
                catalogService.resolveName(definition.name(), definition.group()),
                definition.name(),
                definition.version(),
                definition.group(),
                definition.module(),
                definition.title(),
                definition.description(),
                properties);
    }

    public record ComponentListResult(String version, int count, List<ComponentDetailResult> components) {
    }

    public record ComponentDetailResult(String name, String fullName, String version, String group, String module,
                                        String title, String description, List<PropertyInfo> properties) {
    }

    public record PropertyInfo(String name, String description, String type, boolean required,
                             String defaultValue) {
    }

    // Helper methods

    private boolean matchesFilter(String name, String title, String description, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        String lowerFilter = filter.toLowerCase();
        return (name != null && name.toLowerCase().contains(lowerFilter))
                || (title != null && title.toLowerCase().contains(lowerFilter))
                || (description != null && description.toLowerCase().contains(lowerFilter));
    }

    private boolean matchesGroup(String group, String groupFilter) {
        if (groupFilter == null || groupFilter.isBlank()) {
            return true;
        }
        if (group == null) {
            return false;
        }
        return group.toLowerCase().contains(groupFilter.toLowerCase());
    }

}
