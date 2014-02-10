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

package com.consol.citrus.channel;

import com.consol.citrus.endpoint.AbstractEndpointAdapter;
import com.consol.citrus.exceptions.ActionTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;

/**
 * Endpoint adapter forwards incoming requests to message channel and waits synchronously for response
 * on reply channel. Provides channel endpoint for clients to connect to message channel in order to provide proper
 * response message.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class ChannelEndpointAdapter extends AbstractEndpointAdapter {

    /** Endpoint handling incoming requests */
    private ChannelSyncEndpoint endpoint;
    private ChannelSyncProducer producer;

    /** Endpoint configuration */
    private final ChannelSyncEndpointConfiguration endpointConfiguration;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(ChannelEndpointAdapter.class);

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public ChannelEndpointAdapter(ChannelSyncEndpointConfiguration endpointConfiguration) {
        this.endpointConfiguration = endpointConfiguration;

        endpoint = new ChannelSyncEndpoint(endpointConfiguration);
        producer = new ChannelSyncProducer(endpointConfiguration);
    }

    @Override
    public Message<?> handleMessageInternal(Message<?> request) {
        log.info("Forwarding request to message channel ...");

        Message<?> replyMessage = null;
        try {
            producer.send(request);
            replyMessage = producer.receive(endpointConfiguration.getTimeout());
        } catch (ActionTimeoutException e) {
            log.warn(e.getMessage());
        }

        return replyMessage;
    }

    @Override
    public ChannelEndpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public ChannelSyncEndpointConfiguration getEndpointConfiguration() {
        return endpointConfiguration;
    }

}
