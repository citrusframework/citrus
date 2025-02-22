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

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.message.MessageSelector;
import org.citrusframework.message.selector.DelegatingMessageSelector;
import org.citrusframework.messaging.AbstractSelectiveMessageConsumer;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectConsumer extends AbstractSelectiveMessageConsumer {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(DirectConsumer.class);

    /** Endpoint configuration */
    private final DirectEndpointConfiguration endpointConfiguration;

    /**
     * Default constructor using endpoint configuration.
     * @param name
     * @param endpointConfiguration
     */
    public DirectConsumer(String name, DirectEndpointConfiguration endpointConfiguration) {
        super(name, endpointConfiguration);
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public Message receive(String selector, TestContext context, long timeout) {
        String destinationQueueName;
        MessageQueue destinationQueue = getDestinationQueue(context);

        if (StringUtils.hasText(selector)) {
            destinationQueueName = getDestinationQueueName() + "(" + selector + ")";
        } else {
            destinationQueueName = getDestinationQueueName();
        }

        logger.debug("Receiving message from queue: '{}'", destinationQueueName);

        Message message;
        if (StringUtils.hasText(selector)) {
            MessageSelector messageSelector = new DelegatingMessageSelector(selector, context);

            if (timeout <= 0) {
                message = destinationQueue.receive(messageSelector);
            } else {
                message = destinationQueue.receive(messageSelector, timeout);
            }
        } else {
            if (timeout <= 0) {
                message = destinationQueue.receive();
            } else {
                message = destinationQueue.receive(timeout);
            }
        }

        if (message == null) {
            throw new MessageTimeoutException(timeout, destinationQueueName);
        }

        logger.info("Received message from queue: '{}'", destinationQueueName);
        return message;
    }

    /**
     * Get the destination queue depending on settings in this message sender.
     * Either a direct queue object is set or a queue name which will be resolved
     * to a queue.
     *
     * @param context the test context
     * @return the destination queue object.
     */
    protected MessageQueue getDestinationQueue(TestContext context) {
        if (endpointConfiguration.getQueue() != null) {
            return endpointConfiguration.getQueue();
        } else if (StringUtils.hasText(endpointConfiguration.getQueueName())) {
            return resolveQueueName(endpointConfiguration.getQueueName(), context);
        } else {
            throw new CitrusRuntimeException("Neither queue name nor queue object is set - " +
                    "please specify destination queue");
        }
    }

    /**
     * Gets the queue name depending on what is set in this message sender.
     * Either queue name is set directly or queue object is consulted for queue name.
     *
     * @return the queue name.
     */
    protected String getDestinationQueueName() {
        if (endpointConfiguration.getQueue() != null) {
            return endpointConfiguration.getQueue().toString();
        } else if (StringUtils.hasText(endpointConfiguration.getQueueName())) {
            return endpointConfiguration.getQueueName();
        } else {
            throw new CitrusRuntimeException("Neither queue name nor queue object is set - " +
                    "please specify destination queue");
        }
    }

    /**
     * Resolve the queue by name.
     * @param queueName the name to resolve
     * @param context
     * @return the MessageQueue object
     */
    protected MessageQueue resolveQueueName(String queueName, TestContext context) {
        if (context.getReferenceResolver() != null) {
            return context.getReferenceResolver().resolve(queueName, MessageQueue.class);
        }

        throw new CitrusRuntimeException("Unable to resolve message queue - missing proper reference resolver in context");
    }
}
