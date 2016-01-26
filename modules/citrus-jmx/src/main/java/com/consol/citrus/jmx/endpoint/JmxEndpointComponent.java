/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.jmx.endpoint;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.AbstractEndpointComponent;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.jmx.client.JmxClient;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JmxEndpointComponent extends AbstractEndpointComponent {

    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context) {
        JmxClient client = new JmxClient();

        if (resourcePath.contains("platform")) {
            client.getEndpointConfiguration().setServerUrl("platform");
        } else {
            client.getEndpointConfiguration().setServerUrl("service:jmx:" + resourcePath);
        }

        enrichEndpointConfiguration(client.getEndpointConfiguration(),
                getEndpointConfigurationParameters(parameters, JmxEndpointConfiguration.class), context);
        return client;
    }
}
