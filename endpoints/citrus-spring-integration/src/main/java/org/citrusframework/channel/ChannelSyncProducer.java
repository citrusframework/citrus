/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.channel;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.exceptions.ReplyMessageTimeoutException;
import org.citrusframework.message.Message;
import org.citrusframework.message.correlation.CorrelationManager;
import org.citrusframework.message.correlation.PollingCorrelationManager;
import org.citrusframework.messaging.ReplyConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Synchronous producer sends message to in memory message channel and receives synchronous reply.
 * Reply message is correlated and stored in correlation manager. This way test cases are able to receive synchronous
 * message asynchronously at later time.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class ChannelSyncProducer extends ChannelProducer implements ReplyConsumer {
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(ChannelSyncProducer.class);

    /** Store of reply messages */
    private CorrelationManager<Message> correlationManager;

    /** Endpoint configuration */
    private final ChannelSyncEndpointConfiguration endpointConfiguration;

    /**
     * Default constructor using endpoint configuration.
     *
     * @param name
     * @param endpointConfiguration
     */
    public ChannelSyncProducer(String name, ChannelSyncEndpointConfiguration endpointConfiguration) {
        super(name, endpointConfiguration);
        this.endpointConfiguration = endpointConfiguration;

        this.correlationManager = new PollingCorrelationManager<>(endpointConfiguration, "Reply message did not arrive yet");
    }

    @Override
    public void send(Message message, TestContext context) {
        String correlationKeyName = endpointConfiguration.getCorrelator().getCorrelationKeyName(getName());
        String correlationKey = endpointConfiguration.getCorrelator().getCorrelationKey(message);
        correlationManager.saveCorrelationKey(correlationKeyName, correlationKey, context);

        String destinationChannelName = getDestinationChannelName();

        if (logger.isDebugEnabled()) {
            logger.debug("Sending message to channel: '" + destinationChannelName + "'");
            logger.debug("Message to send is:\n" + message.toString());
        }

        endpointConfiguration.getMessagingTemplate().setReceiveTimeout(endpointConfiguration.getTimeout());

        logger.info("Message was sent to channel: '" + destinationChannelName + "'");

        org.springframework.messaging.Message<?> replyMessage = endpointConfiguration.getMessagingTemplate().sendAndReceive(getDestinationChannel(context),
                endpointConfiguration.getMessageConverter().convertOutbound(message, endpointConfiguration, context));

        if (replyMessage == null) {
            throw new ReplyMessageTimeoutException(endpointConfiguration.getTimeout(), destinationChannelName);
        } else {
            logger.info("Received synchronous response from reply channel '" + destinationChannelName + "'");
        }

        correlationManager.store(correlationKey, endpointConfiguration.getMessageConverter().convertInbound(replyMessage, endpointConfiguration, context));
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
            throw new MessageTimeoutException(timeout, getDestinationChannelName());
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
