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
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.exceptions.ReplyMessageTimeoutException;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.message.correlation.CorrelationManager;
import org.citrusframework.message.correlation.PollingCorrelationManager;
import org.citrusframework.messaging.ReplyConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DirectSyncProducer extends DirectProducer implements ReplyConsumer {
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(DirectSyncProducer.class);

    /** Store of reply messages */
    private CorrelationManager<Message> correlationManager;

    /** Endpoint configuration */
    private final DirectSyncEndpointConfiguration endpointConfiguration;

    /**
     * Default constructor using endpoint configuration.
     *
     * @param name
     * @param endpointConfiguration
     */
    public DirectSyncProducer(String name, DirectSyncEndpointConfiguration endpointConfiguration) {
        super(name, endpointConfiguration);
        this.endpointConfiguration = endpointConfiguration;

        this.correlationManager = new PollingCorrelationManager<>(endpointConfiguration, "Reply message did not arrive yet");
    }

    @Override
    public void send(Message message, TestContext context) {
        String correlationKeyName = endpointConfiguration.getCorrelator().getCorrelationKeyName(getName());
        String correlationKey = endpointConfiguration.getCorrelator().getCorrelationKey(message);
        correlationManager.saveCorrelationKey(correlationKeyName, correlationKey, context);

        String destinationQueueName = getDestinationQueueName();

        if (logger.isDebugEnabled()) {
            logger.debug("Sending message to queue: '{}'", destinationQueueName);
            logger.debug("Message to send is:\n{}", message.toString());
        }

        logger.info("Message was sent to queue: '{}'", destinationQueueName);

        MessageQueue replyQueue = getReplyQueue(message, context);
        getDestinationQueue(context).send(message);
        Message replyMessage = replyQueue.receive(endpointConfiguration.getTimeout());

        if (replyMessage == null) {
            throw new ReplyMessageTimeoutException(endpointConfiguration.getTimeout(), destinationQueueName);
        } else {
            logger.info("Received synchronous response from reply queue");
        }

        correlationManager.store(correlationKey, replyMessage);
    }

    /**
     * Reads reply queue from message header or creates a new temporary queue.
     * @param message
     * @param context
     * @return
     */
    private MessageQueue getReplyQueue(Message message, TestContext context) {
        if (message.getHeader(DirectMessageHeaders.REPLY_QUEUE) == null) {
            MessageQueue temporaryQueue = new DefaultMessageQueue(getName() + "." + UUID.randomUUID());
            message.setHeader(DirectMessageHeaders.REPLY_QUEUE, temporaryQueue);
            return temporaryQueue;
        }

        if (message.getHeader(DirectMessageHeaders.REPLY_QUEUE) instanceof MessageQueue) {
            return (MessageQueue)message.getHeader(DirectMessageHeaders.REPLY_QUEUE);
        } else {
            return resolveQueueName(message.getHeader(DirectMessageHeaders.REPLY_QUEUE).toString(), context);
        }
    }

    @Override
    public Message receive(TestContext context) {
        return receive(correlationManager.getCorrelationKey(
                endpointConfiguration.getCorrelator().getCorrelationKeyName(getName()), context), context);
    }

    @Override
    public Message receive(String selector, TestContext context) {
        return receive(selector, context, endpointConfiguration.getTimeout());
    }

    @Override
    public Message receive(TestContext context, long timeout) {
        return receive(correlationManager.getCorrelationKey(
                endpointConfiguration.getCorrelator().getCorrelationKeyName(getName()), context), context, timeout);
    }

    @Override
    public Message receive(String selector, TestContext context, long timeout) {
        Message message = correlationManager.find(selector, timeout);

        if (message == null) {
            throw new MessageTimeoutException(timeout, getDestinationQueueName());
        }

        return message;
    }

    /**
     * Gets the correlation manager.
     * @return
     */
    public CorrelationManager<Message> getCorrelationManager() {
        return correlationManager;
    }

    /**
     * Sets the correlation manager.
     * @param correlationManager
     */
    public void setCorrelationManager(CorrelationManager<Message> correlationManager) {
        this.correlationManager = correlationManager;
    }
}
