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

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpointAdapter;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Endpoint adapter forwards incoming requests to message queue and waits synchronously for response
 * on reply queue. Provides simple endpoint for clients to connect to message queue in order to provide proper
 * response message.
 *
 * @since 3.0
 */
public class DirectEndpointAdapter extends AbstractEndpointAdapter {

    /** Endpoint handling incoming requests */
    private final DirectSyncEndpoint endpoint;
    private final DirectSyncProducer producer;

    /** Endpoint configuration */
    private final DirectSyncEndpointConfiguration endpointConfiguration;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(DirectEndpointAdapter.class);

    /**
     * Default constructor using endpoint.
     * @param endpoint
     */
    public DirectEndpointAdapter(DirectSyncEndpoint endpoint) {
        this.endpointConfiguration = endpoint.getEndpointConfiguration();

        endpoint.setName(getName());
        producer = new DirectSyncProducer(endpoint.getProducerName(), endpointConfiguration);
        this.endpoint = endpoint;
    }

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public DirectEndpointAdapter(DirectSyncEndpointConfiguration endpointConfiguration) {
        this.endpointConfiguration = endpointConfiguration;

        endpoint = new DirectSyncEndpoint(endpointConfiguration);
        endpoint.setName(getName());
        producer = new DirectSyncProducer(endpoint.getProducerName(), endpointConfiguration);
    }

    @Override
    public Message handleMessageInternal(Message request) {
        logger.debug("Forwarding request to message queue ...");

        TestContext context = getTestContext();
        Message replyMessage = null;
        try {
            producer.send(request, context);
            if (endpointConfiguration.getCorrelator() != null) {
                replyMessage = producer.receive(endpointConfiguration.getCorrelator().getCorrelationKey(request), context, endpointConfiguration.getTimeout());
            } else {
                replyMessage = producer.receive(context, endpointConfiguration.getTimeout());
            }
        } catch (ActionTimeoutException e) {
            logger.warn(e.getMessage());
        }

        return replyMessage;
    }

    @Override
    public DirectEndpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public DirectSyncEndpointConfiguration getEndpointConfiguration() {
        return endpointConfiguration;
    }
}
