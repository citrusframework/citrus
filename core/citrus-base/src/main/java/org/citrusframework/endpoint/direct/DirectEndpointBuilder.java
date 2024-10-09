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
import org.citrusframework.message.MessageQueue;

public class DirectEndpointBuilder extends AbstractEndpointBuilder<DirectEndpoint> {

    /** Endpoint target */
    private DirectEndpoint endpoint = new DirectEndpoint();

    @Override
    protected DirectEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the queueName property.
     * @param queueName
     * @return
     */
    public DirectEndpointBuilder queue(String queueName) {
        endpoint.getEndpointConfiguration().setQueueName(queueName);
        return this;
    }

    /**
     * Sets the queue property.
     * @param queue
     * @return
     */
    public DirectEndpointBuilder queue(MessageQueue queue) {
        endpoint.getEndpointConfiguration().setQueue(queue);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public DirectEndpointBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }
}
