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

import org.citrusframework.messaging.Producer;
import org.citrusframework.messaging.SelectiveConsumer;

public class DirectSyncEndpoint extends DirectEndpoint {

    /** One of producer or consumer for this endpoint */
    private DirectSyncProducer syncProducer;
    private DirectSyncConsumer syncConsumer;

    /**
     * Default constructor initializing endpoint.
     */
    public DirectSyncEndpoint() {
        super(new DirectSyncEndpointConfiguration());
    }

    /**
     * Constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public DirectSyncEndpoint(DirectSyncEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
    }

    @Override
    public DirectSyncEndpointConfiguration getEndpointConfiguration() {
        return (DirectSyncEndpointConfiguration) super.getEndpointConfiguration();
    }

    @Override
    public SelectiveConsumer createConsumer() {
        if (syncProducer != null) {
            return syncProducer;
        }

        if (syncConsumer == null) {
            syncConsumer = new DirectSyncConsumer(getConsumerName(), getEndpointConfiguration());
        }

        return syncConsumer;
    }

    @Override
    public Producer createProducer() {
        if (syncConsumer != null) {
            return syncConsumer;
        }

        if (syncProducer == null) {
            syncProducer = new DirectSyncProducer(getProducerName(), getEndpointConfiguration());
        }

        return syncProducer;
    }
}
