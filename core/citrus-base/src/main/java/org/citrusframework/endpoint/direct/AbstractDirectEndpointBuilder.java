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

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.base.endpoint.AbstractEndpointBuilder;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.util.PropertyUtils;
import org.citrusframework.util.StringUtils;
import org.citrusframework.api.yaml.SchemaProperty;

@XmlType(name = "", propOrder = {})
@SuppressWarnings("unchecked")
public abstract class AbstractDirectEndpointBuilder<T extends DirectEndpoint, B extends AbstractDirectEndpointBuilder<T, B>> extends AbstractEndpointBuilder<T> {

    private boolean autoCreateQueue;

    @Override
    public T build() {
        if (referenceResolver != null && autoCreateQueue) {
            String createdQueue = null;
            if (StringUtils.hasText(getEndpoint().getEndpointConfiguration().getQueueName()) &&
                    !referenceResolver.isResolvable(getEndpoint().getEndpointConfiguration().getQueueName(), MessageQueue.class)) {
                DefaultMessageQueue messageQueue = new DefaultMessageQueue(getEndpoint().getEndpointConfiguration().getQueueName());
                PropertyUtils.configure(getEndpoint().getEndpointConfiguration().getQueueName(), messageQueue, referenceResolver);
                referenceResolver.bind(getEndpoint().getEndpointConfiguration().getQueueName(), messageQueue);
                createdQueue = messageQueue.getName();
            } else if (getEndpoint().getEndpointConfiguration().getQueue() != null &&
                    !referenceResolver.isResolvable(getEndpoint().getEndpointConfiguration().getQueue().getName(), MessageQueue.class)) {
                referenceResolver.bind(getEndpoint().getEndpointConfiguration().getQueue().getName(), getEndpoint().getEndpointConfiguration().getQueue());
                createdQueue = getEndpoint().getEndpointConfiguration().getQueue().getName();
            }

            if (StringUtils.hasText(createdQueue)) {
                logger.info("Automatically created queue '{}'", createdQueue);
            }
        }

        return super.build();
    }

    /**
     * Sets the queueName property.
     */
    public B queue(String queueName) {
        getEndpoint().getEndpointConfiguration().setQueueName(queueName);
        return (B) this;
    }

    @SchemaProperty(description = "The queue name.")
    @XmlAttribute
    public void setQueue(String queueName) {
        queue(queueName);
    }

    @XmlAttribute(name = "queue-name")
    public void setQueueName(String queueName) {
        queue(queueName);
    }

    /**
     * Sets the queue property.
     */
    public B queue(MessageQueue queue) {
        getEndpoint().getEndpointConfiguration().setQueue(queue);
        return (B) this;
    }

    /**
     * When set the queue is automatically created when it does not exist in bean registry.
     */
    public B autoCreateQueue(boolean autoCreate) {
        this.autoCreateQueue = autoCreate;
        return (B) this;
    }

    @SchemaProperty(description = "When set the queue is automatically created when it does not exist in bean registry.")
    @XmlAttribute(name = "auto-create-queue")
    public void setAutoCreateQueue(boolean autoCreate) {
        autoCreateQueue(autoCreate);
    }

    /**
     * Sets the default timeout.
     */
    public B timeout(long timeout) {
        getEndpoint().getEndpointConfiguration().setTimeout(timeout);
        return (B) this;
    }

    @SchemaProperty(description = "The timeout when receiving messages from the queue.")
    @XmlAttribute
    public void setTimeout(long timeout) {
        timeout(timeout);
    }
}
