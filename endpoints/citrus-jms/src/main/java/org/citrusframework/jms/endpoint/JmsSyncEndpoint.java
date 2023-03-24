/*
 * Copyright 2006-2013 the original author or authors.
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

import org.citrusframework.common.ShutdownPhase;
import org.citrusframework.messaging.Producer;
import org.citrusframework.messaging.SelectiveConsumer;

/**
 * Synchronous Jms message endpoint. When sending messages endpoint sets replyTo message header and waits for synchronous response.
 * When receiving messages endpoint reads replyTo header from incoming request and sends synchronous response back.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class JmsSyncEndpoint extends JmsEndpoint implements ShutdownPhase {

    /** One of producer or consumer for this endpoint */
    private JmsSyncProducer jmsSyncMessageProducer;
    private JmsSyncConsumer jmsSyncMessageConsumer;

    /**
     * Default constructor initializing endpoint.
     */
    public JmsSyncEndpoint() {
        super(new JmsSyncEndpointConfiguration());
    }

    /**
     * Constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public JmsSyncEndpoint(JmsSyncEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
    }

    @Override
    public JmsSyncEndpointConfiguration getEndpointConfiguration() {
        return (JmsSyncEndpointConfiguration) super.getEndpointConfiguration();
    }

    @Override
    public SelectiveConsumer createConsumer() {
        if (jmsSyncMessageProducer != null) {
            return jmsSyncMessageProducer;
        }

        if (jmsSyncMessageConsumer == null) {
            jmsSyncMessageConsumer = new JmsSyncConsumer(getConsumerName(), getEndpointConfiguration());
        }

        return jmsSyncMessageConsumer;
    }

    @Override
    public Producer createProducer() {
        if (jmsSyncMessageConsumer != null) {
            return jmsSyncMessageConsumer;
        }

        if (jmsSyncMessageProducer == null) {
            jmsSyncMessageProducer = new JmsSyncProducer(getProducerName(), getEndpointConfiguration());
        }

        return jmsSyncMessageProducer;
    }

    @Override
    public void destroy() {
        if (jmsSyncMessageProducer != null) {
            jmsSyncMessageProducer.destroy();
        }
    }

}
