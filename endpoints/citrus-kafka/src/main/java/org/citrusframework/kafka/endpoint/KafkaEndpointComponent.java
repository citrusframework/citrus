/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.kafka.endpoint;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpointComponent;
import org.citrusframework.endpoint.Endpoint;

/**
 * Kafka endpoint component is able to create kafka endpoint from endpoint uri with parameters. Depending on uri creates a
 * synchronous or asynchronous endpoint on a queue or topic destination.
 *
 * Further endpoint parameters such as connectionFactory get passed to the endpoint configuration.
 *
 * @author Christoph Deppisch
 * @since 2.8
 */
public class KafkaEndpointComponent extends AbstractEndpointComponent {

    /**
     * Default constructor using the name for this component.
     */
    public KafkaEndpointComponent() {
        super("kafka");
    }

    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context) {
        KafkaEndpoint endpoint = new KafkaEndpoint();

        // set topic name
        if (resourcePath.indexOf(':') > 0) {
            endpoint.getEndpointConfiguration().setTopic(resourcePath.substring(resourcePath.lastIndexOf(':') + 1));
        } else {
            endpoint.getEndpointConfiguration().setTopic(resourcePath);
        }

        enrichEndpointConfiguration(endpoint.getEndpointConfiguration(), parameters, context);

        return endpoint;
    }
}
