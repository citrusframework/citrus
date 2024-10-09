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

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.message.MessageQueue;

public class DirectSyncEndpointBuilder extends AbstractEndpointBuilder<DirectSyncEndpoint> {

    /** Endpoint target */
    private DirectSyncEndpoint endpoint = new DirectSyncEndpoint();

    @Override
    protected DirectSyncEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the queueName property.
     * @param queueName
     * @return
     */
    public DirectSyncEndpointBuilder queue(String queueName) {
        endpoint.getEndpointConfiguration().setQueueName(queueName);
        return this;
    }

    /**
     * Sets the queue property.
     * @param queue
     * @return
     */
    public DirectSyncEndpointBuilder queue(MessageQueue queue) {
        endpoint.getEndpointConfiguration().setQueue(queue);
        return this;
    }

    /**
     * Sets the polling interval.
     * @param pollingInterval
     * @return
     */
    public DirectSyncEndpointBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    /**
     * Sets the message correlator.
     * @param correlator
     * @return
     */
    public DirectSyncEndpointBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public DirectSyncEndpointBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }
}
