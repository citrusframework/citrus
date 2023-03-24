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

package org.citrusframework.jms.endpoint;

import jakarta.jms.ConnectionFactory;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpointComponent;
import org.citrusframework.endpoint.Endpoint;

/**
 * Jms endpoint component is able to create jms endpoint from endpoint uri with parameters. Depending on uri creates a
 * synchronous or asynchronous endpoint on a queue or topic destination.
 *
 * Further endpoint parameters such as connectionFactory get passed to the endpoint configuration.
 *
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class JmsEndpointComponent extends AbstractEndpointComponent {

    /**
     * Default constructor using the name for this component.
     */
    public JmsEndpointComponent() {
        super("jms");
    }

    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context) {
        JmsEndpoint endpoint;

        if (resourcePath.startsWith("sync:")) {
            endpoint = new JmsSyncEndpoint();
        } else {
            endpoint = new JmsEndpoint();
        }

        if (resourcePath.contains("topic:")) {
            endpoint.getEndpointConfiguration().setPubSubDomain(true);
        }

        // set destination name
        if (resourcePath.indexOf(':') > 0) {
            endpoint.getEndpointConfiguration().setDestinationName(resourcePath.substring(resourcePath.lastIndexOf(':') + 1));
        } else {
            endpoint.getEndpointConfiguration().setDestinationName(resourcePath);
        }

        // set default jms connection factory
        if (context.getReferenceResolver() != null && context.getReferenceResolver().isResolvable("connectionFactory")) {
            endpoint.getEndpointConfiguration().setConnectionFactory(context.getReferenceResolver().resolve("connectionFactory", ConnectionFactory.class));
        }

        enrichEndpointConfiguration(endpoint.getEndpointConfiguration(), parameters, context);

        return endpoint;
    }
}
