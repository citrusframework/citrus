/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.websocket.endpoint;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpointComponent;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.websocket.client.WebSocketClientEndpointConfiguration;

/**
 * Web Socket endpoint component is able to create Web Socket client endpoint from endpoint uri with parameters.
 *
 * @author Martin Maher
 * @since 2.3
 */
public class WebSocketEndpointComponent extends AbstractEndpointComponent {

    /**
     * Default constructor using the name for this component.
     */
    public WebSocketEndpointComponent() {
        super("websocket");
    }

    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context) {
        WebSocketClientEndpointConfiguration endpointConfiguration = new WebSocketClientEndpointConfiguration();
        endpointConfiguration.setEndpointUri(String.format("ws://%s%s", resourcePath, getParameterString(parameters, WebSocketClientEndpointConfiguration.class)));
        WebSocketEndpoint endpoint = new WebSocketEndpoint(endpointConfiguration);

        enrichEndpointConfiguration(endpoint.getEndpointConfiguration(), getEndpointConfigurationParameters(parameters, WebSocketClientEndpointConfiguration.class), context);

        return endpoint;
    }
}
