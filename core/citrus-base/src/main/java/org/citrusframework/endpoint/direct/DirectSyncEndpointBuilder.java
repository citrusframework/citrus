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
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.util.PropertyUtils;
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaProperty;

public class DirectSyncEndpointBuilder extends AbstractEndpointBuilder<DirectSyncEndpoint> {

    /** Endpoint target */
    private final DirectSyncEndpoint endpoint = new DirectSyncEndpoint();

    private String correlator;
    private boolean autoCreateQueue;

    @Override
    public DirectSyncEndpoint build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(correlator)) {
                correlator(referenceResolver.resolve(correlator, MessageCorrelator.class));
            }

            if (autoCreateQueue) {
                String createdQueue = null;
                if (StringUtils.hasText(endpoint.getEndpointConfiguration().getQueueName()) &&
                        !referenceResolver.isResolvable(endpoint.getEndpointConfiguration().getQueueName(), MessageQueue.class)) {
                    DefaultMessageQueue messageQueue = new DefaultMessageQueue(endpoint.getEndpointConfiguration().getQueueName());
                    PropertyUtils.configure(endpoint.getEndpointConfiguration().getQueueName(), messageQueue, referenceResolver);
                    referenceResolver.bind(endpoint.getEndpointConfiguration().getQueueName(), messageQueue);
                    createdQueue = messageQueue.getName();
                } else if (endpoint.getEndpointConfiguration().getQueue() != null &&
                        !referenceResolver.isResolvable(endpoint.getEndpointConfiguration().getQueue().getName(), MessageQueue.class)) {
                    referenceResolver.bind(endpoint.getEndpointConfiguration().getQueue().getName(), endpoint.getEndpointConfiguration().getQueue());
                    createdQueue = endpoint.getEndpointConfiguration().getQueue().getName();
                }

                if (StringUtils.hasText(createdQueue)) {
                    logger.info("Automatically created queue '{}'", createdQueue);
                }
            }
        }

        return super.build();
    }

    @Override
    protected DirectSyncEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the queueName property.
     */
    public DirectSyncEndpointBuilder queue(String queueName) {
        endpoint.getEndpointConfiguration().setQueueName(queueName);
        return this;
    }

    @SchemaProperty(description = "The queue name.")
    public void setQueue(String queueName) {
        queue(queueName);
    }

    /**
     * Sets the queue property.
     */
    public DirectSyncEndpointBuilder queue(MessageQueue queue) {
        endpoint.getEndpointConfiguration().setQueue(queue);
        return this;
    }

    /**
     * When set the queue is automatically created when it does not exist in bean registry.
     */
    public DirectSyncEndpointBuilder autoCreateQueue(boolean autoCreate) {
        this.autoCreateQueue = autoCreate;
        return this;
    }

    @SchemaProperty(description = "When set the queue is automatically created when it does not exist in bean registry.")
    public void setAutoCreateQueue(boolean autoCreate) {
        autoCreateQueue(autoCreate);
    }

    /**
     * Sets the polling interval.
     */
    public DirectSyncEndpointBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    @SchemaProperty(description = "Sets the polling interval when consuming messages.")
    public void setPollingInterval(int pollingInterval) {
        pollingInterval(pollingInterval);
    }

    /**
     * Sets the message correlator.
     */
    public DirectSyncEndpointBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the message correlator.")
    public void setCorrelator(String correlator) {
        this.correlator = correlator;
    }

    /**
     * Sets the default timeout.
     */
    public DirectSyncEndpointBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }

    @SchemaProperty(description = "The timeout when receiving messages from the queue.")
    public void setTimeout(long timeout) {
        timeout(timeout);
    }
}
