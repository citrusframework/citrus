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

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.graphql.client.GraphQlEndpointConfiguration;
import org.citrusframework.graphql.document.GraphQlSchemaLoader;
import org.citrusframework.http.server.HttpServer;

/**
 * Server for simulating a GraphQL API. Runs on the HTTP server; requests on the GraphQL path are
 * presented to tests as JSON request bodies whatever the HTTP method, and replies get a GraphQL
 * content type unless the test sets one.
 */
public class GraphQlServer extends HttpServer {

    private String path = GraphQlEndpointConfiguration.DEFAULT_PATH;
    private List<String> schemaResources = new ArrayList<>();
    private boolean strict = true;

    private GraphQlSchemaLoader schemaLoader;

    /**
     * Wraps the endpoint adapter before the server starts, so the HTTP message controller uses the
     * GraphQL adapter from the first request on.
     */
    @Override
    public void initialize() {
        boolean autoStart = isAutoStart();
        setAutoStart(false);
        try {
            super.initialize();
        } finally {
            setAutoStart(autoStart);
        }

        if (!(getEndpointAdapter() instanceof GraphQlEndpointAdapter)) {
            setEndpointAdapter(new GraphQlEndpointAdapter(getEndpointAdapter(), path));
        }

        if (autoStart && !isRunning()) {
            start();
        }
    }

    public String getPath() {
        return path;
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
}
