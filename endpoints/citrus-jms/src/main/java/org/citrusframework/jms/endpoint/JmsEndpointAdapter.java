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

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpointAdapter;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.jms.message.JmsMessageHeaderMapper;
import org.citrusframework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Endpoint adapter forwards incoming requests to jms destination and waits for response
 * on reply destination. Provides jms endpoint for clients to connect to destinations in order to provide proper
 * response message.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class JmsEndpointAdapter extends AbstractEndpointAdapter {

    /** Endpoint handling incoming requests */
    private final JmsSyncEndpoint endpoint;
    private final JmsSyncProducer producer;

    /** Endpoint configuration */
    private final JmsSyncEndpointConfiguration endpointConfiguration;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(JmsEndpointAdapter.class);

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public JmsEndpointAdapter(JmsSyncEndpointConfiguration endpointConfiguration) {
        this.endpointConfiguration = endpointConfiguration;

        if (this.endpointConfiguration.getHeaderMapper() instanceof JmsMessageHeaderMapper) {
            this.endpointConfiguration.setFilterInternalHeaders(false);
        }

        endpoint = new JmsSyncEndpoint(this.endpointConfiguration);
        endpoint.setName(getName());
        producer = new JmsSyncProducer(endpoint.getProducerName(), this.endpointConfiguration);
    }

    @Override
    protected Message handleMessageInternal(Message request) {
        logger.debug("Forwarding request to jms destination ...");

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
    public Endpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public JmsSyncEndpointConfiguration getEndpointConfiguration() {
        return endpointConfiguration;
    }
}
