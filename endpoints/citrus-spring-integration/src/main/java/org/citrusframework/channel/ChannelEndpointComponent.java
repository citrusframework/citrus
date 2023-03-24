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

package org.citrusframework.channel;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpointComponent;
import org.citrusframework.endpoint.Endpoint;
import org.springframework.messaging.MessageChannel;

/**
 * Channel endpoint component creates synchronous or asynchronous channel endpoint and sets configuration properties
 * accordingly.
 *
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class ChannelEndpointComponent extends AbstractEndpointComponent {

    /**
     * Default constructor using the name for this component.
     */
    public ChannelEndpointComponent() {
        super("channel");
    }

    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context) {
        ChannelEndpoint endpoint;
        if (resourcePath.startsWith("sync:")) {
            ChannelSyncEndpointConfiguration endpointConfiguration = new ChannelSyncEndpointConfiguration();
            endpoint = new ChannelSyncEndpoint(endpointConfiguration);
            endpoint.getEndpointConfiguration().setChannelName(resourcePath.substring("sync:".length()));
        } else {
            endpoint = new ChannelEndpoint();
            endpoint.getEndpointConfiguration().setChannelName(resourcePath);
        }

        if (context.getReferenceResolver() != null) {
            endpoint.getEndpointConfiguration().setChannelResolver(channelName -> context.getReferenceResolver().resolve(channelName, MessageChannel.class));
        }

        enrichEndpointConfiguration(endpoint.getEndpointConfiguration(), parameters, context);

        return endpoint;
    }
}
