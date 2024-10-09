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

import org.citrusframework.endpoint.AbstractEndpointConfiguration;
import org.citrusframework.message.MessageQueue;

public class DirectEndpointConfiguration extends AbstractEndpointConfiguration {

    /** Destination queue */
    private MessageQueue queue;

    /** Destination queue name */
    private String queueName;

    /**
     * Set the message queue.
     * @param queue the queue to set
     */
    public void setQueue(MessageQueue queue) {
        this.queue = queue;
    }

    /**
     * Sets the destination queue name.
     * @param queueName the queueName to set
     */
    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    /**
     * Gets the queue.
     * @return the queue
     */
    public MessageQueue getQueue() {
        return queue;
    }

    /**
     * Gets the queueName.
     * @return the queueName
     */
    public String getQueueName() {
        return queueName;
    }
}
