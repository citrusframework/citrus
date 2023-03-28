/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.ws.client;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpointComponent;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.message.ErrorHandlingStrategy;

/**
 * Component creates proper web service client from endpoint uri resource and parameters.
 *
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class WebServiceEndpointComponent extends AbstractEndpointComponent {

    /**
     * Default constructor using the name for this component.
     */
    public WebServiceEndpointComponent() {
        super("soap");
    }

    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context) {
        WebServiceClient client = new WebServiceClient();

        client.getEndpointConfiguration().setDefaultUri("http://" + resourcePath);

        if (parameters.containsKey("errorHandlingStrategy")) {
            String strategy = parameters.remove("errorHandlingStrategy");
            client.getEndpointConfiguration().setErrorHandlingStrategy(ErrorHandlingStrategy.fromName(strategy));
        }

        enrichEndpointConfiguration(client.getEndpointConfiguration(), parameters, context);
        return client;
    }
}
