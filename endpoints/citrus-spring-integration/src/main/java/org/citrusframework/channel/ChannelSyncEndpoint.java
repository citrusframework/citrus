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

package org.citrusframework.channel;

import org.citrusframework.messaging.Producer;
import org.citrusframework.messaging.SelectiveConsumer;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class ChannelSyncEndpoint extends ChannelEndpoint {

    /** One of producer or consumer for this endpoint */
    private ChannelSyncProducer messageChannelSyncProducer;
    private ChannelSyncConsumer messageChannelSyncConsumer;

    /**
     * Default constructor initializing endpoint.
     */
    public ChannelSyncEndpoint() {
        super(new ChannelSyncEndpointConfiguration());
    }

    /**
     * Constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public ChannelSyncEndpoint(ChannelSyncEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
    }

    @Override
    public ChannelSyncEndpointConfiguration getEndpointConfiguration() {
        return (ChannelSyncEndpointConfiguration) super.getEndpointConfiguration();
    }

    @Override
    public SelectiveConsumer createConsumer() {
        if (messageChannelSyncProducer != null) {
            return messageChannelSyncProducer;
        }

        if (messageChannelSyncConsumer == null) {
            messageChannelSyncConsumer = new ChannelSyncConsumer(getConsumerName(), getEndpointConfiguration());
        }

        return messageChannelSyncConsumer;
    }

    @Override
    public Producer createProducer() {
        if (messageChannelSyncConsumer != null) {
            return messageChannelSyncConsumer;
        }

        if (messageChannelSyncProducer == null) {
            messageChannelSyncProducer = new ChannelSyncProducer(getProducerName(), getEndpointConfiguration());
        }

        return messageChannelSyncProducer;
    }

}
