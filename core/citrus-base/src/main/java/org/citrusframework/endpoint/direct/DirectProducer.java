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
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.messaging.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.citrusframework.util.StringUtils;

public class DirectProducer implements Producer {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(DirectProducer.class);

    /** The producer name */
    private final String name;

    /** Endpoint configuration*/
    private final DirectEndpointConfiguration endpointConfiguration;

    /**
     * Default constructor using endpoint configuration.
     * @param name
     * @param endpointConfiguration
     */
    public DirectProducer(String name, DirectEndpointConfiguration endpointConfiguration) {
        this.name = name;
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public void send(Message message, TestContext context) {
        String destinationQueueName = getDestinationQueueName();

        logger.debug("Sending message to queue: '{}'", destinationQueueName);
        logger.debug("Message to send is:\n{}", message);

        try {
            getDestinationQueue(context).send(message);
        } catch (Exception e) {
            throw new CitrusRuntimeException(String.format("Failed to send message to queue: '%s'", destinationQueueName), e);
        }

        logger.info("Message was sent to queue: '{}'", destinationQueueName);
    }

    /**
     * Get the destination queue depending on settings in this message sender.
     * Either a direct queue object is set or a queue name which will be resolved
     * to a queue.
     *
     * @return the destination queue object.
     * @param context
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
     * @param context the test context
     * @return the MessageQueue object
     */
    protected MessageQueue resolveQueueName(String queueName, TestContext context) {
        if (context.getReferenceResolver() != null) {
            return context.getReferenceResolver().resolve(queueName, MessageQueue.class);
        }

        throw new CitrusRuntimeException("Unable to resolve message queue - missing proper reference resolver in context");
    }

    @Override
    public String getName() {
        return name;
    }
}
