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

import java.util.Arrays;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.api.yaml.SchemaType;
import org.citrusframework.http.server.AbstractHttpServerBuilder;

/**
 * Builds a {@link GraphQlServer}. Every HTTP server setting applies; the GraphQL settings are the
 * path, the schema resources and strict document checks.
 */
@SchemaType(module = "citrus-graphql")
@XmlType(name = "", propOrder = {})
public class GraphQlServerBuilder extends AbstractHttpServerBuilder<GraphQlServer, GraphQlServerBuilder> {

    public GraphQlServerBuilder() {
        super(new GraphQlServer());
    }

    /**
     * Sets the GraphQL path (default {@code /graphql}).
     */
    public GraphQlServerBuilder path(String path) {
        getEndpoint().setPath(path);
        return this;
    }

    @SchemaProperty(description = "The GraphQL path.", defaultValue = "/graphql")
    @XmlAttribute
    public void setPath(String path) {
        path(path);
    }

    /**
     * Sets the SDL schema resources used to validate incoming GraphQL documents.
     */
    public GraphQlServerBuilder schema(String... resources) {
        getEndpoint().setSchemaResources(Arrays.asList(resources));
        return this;
    }

    @SchemaProperty(description = "The SDL schema resources used to validate incoming GraphQL documents.")
    @XmlAttribute
    public void setSchema(List<String> resources) {
        getEndpoint().setSchemaResources(resources);
    }

    /**
     * Enables or disables GraphQL document and reply checks (default enabled).
     */
    public GraphQlServerBuilder strict(boolean strict) {
        getEndpoint().setStrict(strict);
        return this;
    }

    @SchemaProperty(description = "Enables or disables GraphQL document and reply checks.", defaultValue = "true")
    @XmlAttribute
    public void setStrict(boolean strict) {
        strict(strict);
    }
}
