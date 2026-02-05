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

package org.citrusframework.endpoint.context;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpointComponent;
import org.citrusframework.endpoint.Endpoint;

/**
 * Endpoint component creates endpoints that are able to receive messages from the message store.
 */
public class MessageStoreEndpointComponent extends AbstractEndpointComponent {

    /**
     * Default constructor using the name for this component.
     */
    public MessageStoreEndpointComponent() {
        super("message-store");
    }

    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context) {
        MessageStoreEndpointConfiguration endpointConfiguration = new MessageStoreEndpointConfiguration();

        if (!resourcePath.equals("message-store")) {
            endpointConfiguration.setMessageName(resourcePath);
        }

        enrichEndpointConfiguration(endpointConfiguration, parameters, context);

        return new MessageStoreEndpoint(endpointConfiguration);
    }

    @Override
    public boolean supportsEndpointCaching() {
        // Do not allow endpoint caching because message store is context scoped and should not be reused/cached between tests.
        return false;
    }
}
