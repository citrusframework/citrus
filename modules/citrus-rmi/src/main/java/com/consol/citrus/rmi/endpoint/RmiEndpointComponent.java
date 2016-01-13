/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.rmi.endpoint;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.AbstractEndpointComponent;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.rmi.client.RmiClient;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class RmiEndpointComponent extends AbstractEndpointComponent {

    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context) {
        RmiClient client = new RmiClient();

        client.getEndpointConfiguration().setHost(getHost(resourcePath));
        client.getEndpointConfiguration().setPort(getPort(resourcePath, client.getEndpointConfiguration()));
        client.getEndpointConfiguration().setBinding(getBinding(resourcePath));

        enrichEndpointConfiguration(client.getEndpointConfiguration(),
                getEndpointConfigurationParameters(parameters, RmiEndpointConfiguration.class), context);
        return client;
    }

    /**
     * Extract service binding information from endpoint resource path. This is usualle the path after the port specification.
     * @param resourcePath
     * @return
     */
    private String getBinding(String resourcePath) {
        if (resourcePath.contains("/")) {
            return resourcePath.substring(resourcePath.indexOf('/') + 1);
        }

        return null;
    }

    /**
     * Extract port number from resource path. If not present use default port from endpoint configuration.
     * @param resourcePath
     * @param endpointConfiguration
     * @return
     */
    private Integer getPort(String resourcePath, RmiEndpointConfiguration endpointConfiguration) {
        if (resourcePath.contains(":")) {
            String portSpec = resourcePath.split(":")[1];

            if (portSpec.contains("/")) {
                portSpec = portSpec.substring(0, portSpec.indexOf('/'));
            }

            return Integer.valueOf(portSpec);
        }

        return endpointConfiguration.getPort();
    }

    /**
     * Extract host name from resource path.
     * @param resourcePath
     * @return
     */
    private String getHost(String resourcePath) {
        String hostSpec;
        if (resourcePath.contains(":")) {
            hostSpec = resourcePath.split(":")[0];
        } else {
            hostSpec = resourcePath;
        }

        if (hostSpec.contains("/")) {
            hostSpec = hostSpec.substring(0, hostSpec.indexOf('/'));
        }

        return hostSpec;
    }
}
