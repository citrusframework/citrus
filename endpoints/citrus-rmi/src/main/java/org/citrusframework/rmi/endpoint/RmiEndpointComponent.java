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

package org.citrusframework.rmi.endpoint;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpointComponent;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.rmi.client.RmiClient;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class RmiEndpointComponent extends AbstractEndpointComponent {

    /**
     * Default constructor using the name for this component.
     */
    public RmiEndpointComponent() {
        super("rmi");
    }

    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context) {
        RmiClient client = new RmiClient();

        client.getEndpointConfiguration().setHost(RmiEndpointUtils.getHost(resourcePath));
        client.getEndpointConfiguration().setPort(RmiEndpointUtils.getPort(resourcePath, client.getEndpointConfiguration()));
        client.getEndpointConfiguration().setBinding(RmiEndpointUtils.getBinding(resourcePath));

        enrichEndpointConfiguration(client.getEndpointConfiguration(),
                getEndpointConfigurationParameters(parameters, RmiEndpointConfiguration.class), context);
        return client;
    }
}
