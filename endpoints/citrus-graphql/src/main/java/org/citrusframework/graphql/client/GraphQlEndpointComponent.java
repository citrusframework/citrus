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

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpointComponent;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Creates GraphQL clients from endpoint URIs such as {@code graphql://localhost:8080/graphql?strict=false}.
 * The URI path, if any, is used as GraphQL path; without one the client uses {@code /graphql}.
 */
public class GraphQlEndpointComponent extends AbstractEndpointComponent {

    public GraphQlEndpointComponent() {
        this("graphql");
    }

    public GraphQlEndpointComponent(String name) {
        super(name);
    }

    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context) {
        GraphQlClient client = new GraphQlClient();
        GraphQlEndpointConfiguration configuration = client.getEndpointConfiguration();
        configuration.setRequestUrl(getScheme() + resourcePath + getParameterString(parameters, GraphQlEndpointConfiguration.class));

        if (parameters.containsKey("requestMethod")) {
            configuration.setRequestMethod(RequestMethod.valueOf(parameters.remove("requestMethod")));
        }

        if (parameters.containsKey("errorHandlingStrategy")) {
            configuration.setErrorHandlingStrategy(ErrorHandlingStrategy.fromName(parameters.remove("errorHandlingStrategy")));
        }

        enrichEndpointConfiguration(configuration,
                getEndpointConfigurationParameters(parameters, GraphQlEndpointConfiguration.class), context);
        return client;
    }

    protected String getScheme() {
        return "http://";
    }
}
