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

package org.citrusframework.endpoint.direct;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpointComponent;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.util.PropertyUtils;

/**
 * Direct endpoint component creates synchronous or asynchronous channel endpoint and sets configuration properties
 * accordingly.
 *
 * @since 3.0
 */
public class DirectEndpointComponent extends AbstractEndpointComponent {

    /**
     * Default constructor using the name for this component.
     */
    public DirectEndpointComponent() {
        super("direct");
    }

    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context) {
        DirectEndpoint endpoint;
        final String queueName;
        if (resourcePath.startsWith("sync:")) {
            DirectSyncEndpointConfiguration endpointConfiguration = new DirectSyncEndpointConfiguration();
            endpoint = new DirectSyncEndpoint(endpointConfiguration);
            queueName = parameters.getOrDefault("queueName", resourcePath.substring("sync:".length()));
        } else {
            endpoint = new DirectEndpoint();
            queueName = parameters.getOrDefault("queueName", resourcePath);
        }

        endpoint.getEndpointConfiguration().setQueueName(queueName);
        if (!context.getReferenceResolver().isResolvable(queueName)) {
            DefaultMessageQueue messageQueue = new DefaultMessageQueue(queueName);
            PropertyUtils.configure(queueName, messageQueue, context.getReferenceResolver());
            context.getReferenceResolver().bind(queueName, messageQueue);
        }

        enrichEndpointConfiguration(endpoint.getEndpointConfiguration(), parameters, context);

        return endpoint;
    }
}
