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

import org.citrusframework.endpoint.AbstractEndpoint;
import org.citrusframework.messaging.Producer;
import org.citrusframework.messaging.SelectiveConsumer;

/**
 * Direct message endpoint implementation sends and receives message from in memory message queue.
 *
 * @since 3.0
 */
public class DirectEndpoint extends AbstractEndpoint {

    /** Cached producer or consumer */
    private DirectConsumer channelConsumer;
    private DirectProducer channelProducer;

    /**
     * Default constructor initializing endpoint configuration.
     */
    public DirectEndpoint() {
        super(new DirectEndpointConfiguration());
    }

    /**
     * Constructor with endpoint configuration.
     * @param endpointConfiguration
     */
    public DirectEndpoint(DirectEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
    }

    @Override
    public SelectiveConsumer createConsumer() {
        if (channelConsumer == null) {
            channelConsumer = new DirectConsumer(getConsumerName(), getEndpointConfiguration());
        }

        return channelConsumer;
    }

    @Override
    public Producer createProducer() {
        if (channelProducer == null) {
            channelProducer = new DirectProducer(getProducerName(), getEndpointConfiguration());
        }

        return channelProducer;
    }

    @Override
    public DirectEndpointConfiguration getEndpointConfiguration() {
        return (DirectEndpointConfiguration) super.getEndpointConfiguration();
    }
}
