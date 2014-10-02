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

package com.consol.citrus.jms.endpoint;

import com.consol.citrus.endpoint.AbstractEndpointAdapter;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.message.Message;
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
    private JmsSyncEndpoint endpoint;
    private JmsSyncProducer producer;

    /** Endpoint configuration */
    private final JmsSyncEndpointConfiguration endpointConfiguration;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JmsEndpointAdapter.class);

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public JmsEndpointAdapter(JmsSyncEndpointConfiguration endpointConfiguration) {
        this.endpointConfiguration = endpointConfiguration;

        endpoint = new JmsSyncEndpoint(endpointConfiguration);
        producer = new JmsSyncProducer(endpointConfiguration, null, getName());
    }

    @Override
    protected Message handleMessageInternal(Message request) {
        log.info("Forwarding request to jms destination ...");

        producer.send(request);
        Message replyMessage = producer.receive(endpointConfiguration.getTimeout());

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
