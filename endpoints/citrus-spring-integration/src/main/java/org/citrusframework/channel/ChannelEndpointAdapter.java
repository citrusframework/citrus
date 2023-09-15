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

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpointAdapter;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;

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
    private final ChannelSyncEndpoint endpoint;
    private final ChannelSyncProducer producer;

    /** Endpoint configuration */
    private final ChannelSyncEndpointConfiguration endpointConfiguration;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(ChannelEndpointAdapter.class);

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public ChannelEndpointAdapter(ChannelSyncEndpointConfiguration endpointConfiguration) {
        this.endpointConfiguration = endpointConfiguration;

        this.endpointConfiguration.setFilterInternalHeaders(false);

        endpoint = new ChannelSyncEndpoint(endpointConfiguration);
        endpoint.setName(getName());
        producer = new ChannelSyncProducer(endpoint.getProducerName(), endpointConfiguration);
    }

    @Override
    public Message handleMessageInternal(Message request) {
        logger.debug("Forwarding request to message channel ...");

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
    public ChannelEndpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public ChannelSyncEndpointConfiguration getEndpointConfiguration() {
        return endpointConfiguration;
    }

    /**
     * Sets the bean factory that constructed this endpoint adapter.
     * @param beanFactory
     * @throws BeansException
     */
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        endpointConfiguration.setBeanFactory(beanFactory);
    }
}
