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

package com.consol.citrus.http.client;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.AbstractEndpointComponent;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.message.ErrorHandlingStrategy;
import org.springframework.http.HttpMethod;

import java.util.Map;

/**
 * Component creates proper HTTP client endpoint from endpoint uri resource and parameters.
 *
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class HttpEndpointComponent extends AbstractEndpointComponent {

    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context) {
        HttpClient client = new HttpClient();

        client.getEndpointConfiguration().setRequestUrl("http://" + resourcePath + getParameterString(parameters, HttpEndpointConfiguration.class));

        if (parameters.containsKey("requestMethod")) {
            String method = parameters.remove("requestMethod");
            client.getEndpointConfiguration().setRequestMethod(HttpMethod.valueOf(method));
        }

        if (parameters.containsKey("errorHandlingStrategy")) {
            String strategy = parameters.remove("errorHandlingStrategy");
            client.getEndpointConfiguration().setErrorHandlingStrategy(ErrorHandlingStrategy.fromName(strategy));
        }

        enrichEndpointConfiguration(client.getEndpointConfiguration(),
                getEndpointConfigurationParameters(parameters, HttpEndpointConfiguration.class), context);
        return client;
    }
}
