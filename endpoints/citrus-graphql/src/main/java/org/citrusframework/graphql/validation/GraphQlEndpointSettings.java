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
import org.citrusframework.graphql.client.GraphQlEndpointConfiguration;
import org.citrusframework.graphql.server.GraphQlServer;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * GraphQL settings of the endpoint a test action uses. Plain HTTP endpoints get the defaults: strict,
 * no schema, POST to {@code /graphql} (or to the request URL as is when it has a path).
 * @param strict whether GraphQL checks are strict
 * @param schemaLoader schema of the endpoint, {@code null} when none is configured
 * @param requestUrl request URL of a client endpoint, {@code null} otherwise
 * @param path GraphQL path, empty when the request URL is used as is
 * @param requestMethod default request method of a client endpoint
 */
public record GraphQlEndpointSettings(boolean strict, GraphQlSchemaLoader schemaLoader, String requestUrl, String path,
                                      RequestMethod requestMethod) {

    public static final GraphQlEndpointSettings DEFAULTS = new GraphQlEndpointSettings(true, null, null,
            GraphQlEndpointConfiguration.DEFAULT_PATH, RequestMethod.POST);

    /**
     * Reads the settings of an endpoint.
     */
    public static GraphQlEndpointSettings of(Endpoint endpoint) {
        if (endpoint instanceof GraphQlClient client) {
            GraphQlEndpointConfiguration configuration = client.getEndpointConfiguration();
            return new GraphQlEndpointSettings(configuration.isStrict(), configuration.getSchemaLoader(),
                    configuration.getRequestUrl(), configuration.getPath(), configuration.getRequestMethod());
        }

        if (endpoint instanceof GraphQlServer server) {
            return new GraphQlEndpointSettings(server.isStrict(), server.getSchemaLoader(), null, server.getPath(),
                    RequestMethod.POST);
        }

        if (endpoint instanceof HttpClient client) {
            String requestUrl = client.getEndpointConfiguration().getRequestUrl();
            return new GraphQlEndpointSettings(true, null, requestUrl, GraphQlEndpointConfiguration.defaultPath(requestUrl),
                    RequestMethod.POST);
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
        return strictOverride == null ? this : new GraphQlEndpointSettings(strictOverride, schemaLoader, requestUrl, path, requestMethod);
    }
}
