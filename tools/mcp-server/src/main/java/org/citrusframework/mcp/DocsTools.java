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

import java.util.List;
import java.util.stream.Collectors;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolCallException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.citrusframework.CitrusVersion;

/**
 * MCP Tools for querying the Citrus documentation using Quarkus MCP Server.
 */
@ApplicationScoped
public class DocsTools {

    @Inject
    DocsData docsData;

    /**
     * Tool to list available Citrus test actions. This includes test actions and containers.
     */
    @Tool(description = "List available Citrus test actions from the catalog. " +
                        "Returns test action name, description, and group information. " +
                        "Use filter to search by name, group to filter by group.")
    public List<String> citrus_docs_index(
            @ToolArg(required = false, description = "Filter documentation by given file name filter (case-insensitive substring match)") String filter,
            @ToolArg(required = false, description = "Citrus version to query. If not specified, uses the default catalog version.") String version) {

        String citrusVersion;
        if (version != null && !version.isBlank()) {
            citrusVersion = version;
        } else {
            citrusVersion = CitrusVersion.version();
        }

        try {
            return docsData.getDocsIndex(citrusVersion).stream()
                    .filter(fileName -> matchesFilter(fileName, filter))
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw new ToolCallException("Failed to get documentation index: " + e.getMessage(), e);
        }
    }

    /**
     * Tool to get the documentation AsciiDoc for a given component kind and a search key.
     */
    @Tool(description = "Get documentation AsciiDoc for a given component kind and search key. " +
            "The kind is the Citrus component type such as action, container, endpoint, function, validationMatcher. " +
            "The search key may be the name of a Citrus test component.")
    public DocsData.DocInfo citrus_docs_page(
            @ToolArg(description = "Test component type (e.g. action, endpoint, function)") String kind,
            @ToolArg(description = "Search key (e.g., name of a test action or endpoint)") String searchKey,
            @ToolArg(required = false, description = "Citrus version to query. If not specified, uses the default catalog version.") String version) {

        if (kind == null || kind.isBlank()) {
            throw new ToolCallException("Component kind is required", null);
        }

        if (searchKey == null || searchKey.isBlank()) {
            throw new ToolCallException("Documentation search key is required", null);
        }

        String citrusVersion;
        if (version != null && !version.isBlank()) {
            citrusVersion = version;
        } else {
            citrusVersion = CitrusVersion.version();
        }

        try {
            DocsData.DocInfo info = docsData.getDocsPage(kind, searchKey, citrusVersion);
            if (info == null) {
                throw new ToolCallException("No matching documentation found for kind: %s and search key: %s".formatted(kind, searchKey));
            }

            return info;
        } catch (ToolCallException e) {
            throw e;
        } catch (Throwable ex) {
            throw new ToolCallException("No matching documentation found for kind: %s and search key: %s".formatted(kind, searchKey));
        }
    }

    // Helper methods

    private boolean matchesFilter(String fileName, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        String lowerFilter = filter.toLowerCase();
        return fileName != null && fileName.toLowerCase().contains(lowerFilter);
    }

}
