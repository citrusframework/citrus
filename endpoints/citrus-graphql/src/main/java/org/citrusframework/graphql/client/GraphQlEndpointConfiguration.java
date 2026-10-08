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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.citrusframework.graphql.document.GraphQlSchemaLoader;
import org.citrusframework.http.client.HttpEndpointConfiguration;

/**
 * HTTP client configuration with GraphQL settings: the GraphQL path, optional SDL schema resources and
 * whether document checks are strict.
 */
public class GraphQlEndpointConfiguration extends HttpEndpointConfiguration {

    public static final String DEFAULT_PATH = "/graphql";

    private String path;
    private List<String> schemaResources = new ArrayList<>();
    private boolean strict = true;

    private GraphQlSchemaLoader schemaLoader;

    /**
     * Gets the GraphQL path appended to the request URL: the configured path, or {@value DEFAULT_PATH}
     * unless the request URL already has a path of its own.
     */
    public String getPath() {
        if (path != null) {
            return path;
        }

        return hasPath(getRequestUrl()) ? "" : DEFAULT_PATH;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getSchemaResources() {
        return schemaResources;
    }

    public void setSchemaResources(List<String> schemaResources) {
        this.schemaResources = new ArrayList<>(schemaResources);
        this.schemaLoader = schemaResources.isEmpty() ? null : new GraphQlSchemaLoader(schemaResources);
    }

    /**
     * Gets the loader of the configured schema (it builds the schema lazily); {@code null} when no
     * schema is configured.
     */
    public GraphQlSchemaLoader getSchemaLoader() {
        return schemaLoader;
    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    private static boolean hasPath(String requestUrl) {
        if (requestUrl == null || requestUrl.isBlank()) {
            return false;
        }

        try {
            String urlPath = URI.create(requestUrl).getPath();
            return urlPath != null && !urlPath.isEmpty() && !"/".equals(urlPath);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
