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
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.message.correlation.CorrelationManager;
import org.citrusframework.message.correlation.PollingCorrelationManager;
import org.citrusframework.messaging.ReplyProducer;
import org.citrusframework.util.ObjectHelper;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class ChannelSyncConsumer extends ChannelConsumer implements ReplyProducer {
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(ChannelSyncConsumer.class);

    /** Reply channel store */
    private CorrelationManager<MessageChannel> correlationManager;

    /** Endpoint configuration */
    private final ChannelSyncEndpointConfiguration endpointConfiguration;

    /**
     * Default constructor using emdpoint configuration.
     * @param name
     * @param endpointConfiguration
     */
    public ChannelSyncConsumer(String name, ChannelSyncEndpointConfiguration endpointConfiguration) {
        super(name, endpointConfiguration);
        this.endpointConfiguration = endpointConfiguration;

        this.correlationManager = new PollingCorrelationManager<>(endpointConfiguration, "Reply channel not set up yet");
    }

    @Override
    public Message receive(String selector, TestContext context, long timeout) {
        Message receivedMessage = super.receive(selector, context, timeout);
        saveReplyMessageChannel(receivedMessage, context);

        return receivedMessage;
    }

    @Override
    public void send(Message message, TestContext context) {
        ObjectHelper.assertNotNull(message, "Can not send empty message");

        String correlationKeyName = endpointConfiguration.getCorrelator().getCorrelationKeyName(getName());
        String correlationKey = correlationManager.getCorrelationKey(correlationKeyName, context);
        MessageChannel replyChannel = correlationManager.find(correlationKey, endpointConfiguration.getTimeout());
        ObjectHelper.assertNotNull(replyChannel, "Failed to find reply channel for message correlation key: " + correlationKey);

        if (logger.isDebugEnabled()) {
            logger.debug("Sending message to reply channel: '" + replyChannel + "'");
            logger.debug("Message to send is:\n" + message.toString());
        }

        try {
            endpointConfiguration.getMessagingTemplate().send(replyChannel,
                    endpointConfiguration.getMessageConverter().convertOutbound(message, endpointConfiguration, context));
        } catch (MessageDeliveryException e) {
            throw new CitrusRuntimeException("Failed to send message to channel: '" + replyChannel + "'", e);
        }

        logger.info("Message was sent to reply channel: '" + replyChannel + "'");
    }

    /**
     * Store reply message channel.
     * @param receivedMessage
     * @param context
     */
    public void saveReplyMessageChannel(Message receivedMessage, TestContext context) {
        MessageChannel replyChannel = null;
        if (receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.REPLY_CHANNEL) instanceof MessageChannel) {
            replyChannel = (MessageChannel)receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.REPLY_CHANNEL);
        } else if (StringUtils.hasText((String) receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.REPLY_CHANNEL))) {
            replyChannel = resolveChannelName(receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.REPLY_CHANNEL).toString(), context);
        }

        if (replyChannel != null) {
            String correlationKeyName = endpointConfiguration.getCorrelator().getCorrelationKeyName(getName());
            String correlationKey = endpointConfiguration.getCorrelator().getCorrelationKey(receivedMessage);
            correlationManager.saveCorrelationKey(correlationKeyName, correlationKey, context);
            correlationManager.store(correlationKey, replyChannel);
        } else {
            logger.warn("Unable to retrieve reply message channel for message \n" +
                    receivedMessage + "\n - no reply channel found in message headers!");
        }
    }

    /**
     * Gets the correlation manager.
     * @return
     */
    public CorrelationManager<MessageChannel> getCorrelationManager() {
        return correlationManager;
    }

    /**
     * Sets the correlation manager.
     * @param correlationManager
     */
    public void setCorrelationManager(CorrelationManager<MessageChannel> correlationManager) {
        this.correlationManager = correlationManager;
    }

}
