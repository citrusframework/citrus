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
package org.citrusframework.graphql.validation;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.graphql.client.GraphQlClient;
import org.citrusframework.graphql.document.GraphQlSchemaLoader;
import org.citrusframework.graphql.server.GraphQlServer;
import org.citrusframework.util.StringUtils;

/**
 * GraphQL settings of the endpoint a test action uses: whether checks are strict and which schema
 * validates documents. Plain HTTP endpoints get the defaults: strict, no schema.
 * @param strict whether GraphQL checks are strict
 * @param schemaLoader schema of the endpoint, {@code null} when none is configured
 */
public record GraphQlEndpointSettings(boolean strict, GraphQlSchemaLoader schemaLoader) {

    public static final GraphQlEndpointSettings DEFAULTS = new GraphQlEndpointSettings(true, null);

    /**
     * Reads the settings of an endpoint.
     */
    public static GraphQlEndpointSettings of(Endpoint endpoint) {
        if (endpoint instanceof GraphQlClient client) {
            return new GraphQlEndpointSettings(client.getEndpointConfiguration().isStrict(),
                    client.getEndpointConfiguration().getSchemaLoader());
        }

        if (endpoint instanceof GraphQlServer server) {
            return new GraphQlEndpointSettings(server.isStrict(), server.getSchemaLoader());
        }

        return DEFAULTS;
    }

    /**
     * Reads the settings of the endpoint given as instance or, if none, resolved from its name or URI.
     */
    public static GraphQlEndpointSettings resolve(Endpoint endpoint, String endpointUri, TestContext context) {
        if (endpoint != null) {
            return of(endpoint);
        }

        if (StringUtils.hasText(endpointUri)) {
            return of(context.getEndpointFactory().create(endpointUri, context));
        }

        return DEFAULTS;
    }

    /**
     * Applies an action's {@code strict} override, if any.
     */
    public GraphQlEndpointSettings withStrict(Boolean strictOverride) {
        return strictOverride == null ? this : new GraphQlEndpointSettings(strictOverride, schemaLoader);
    }
}
