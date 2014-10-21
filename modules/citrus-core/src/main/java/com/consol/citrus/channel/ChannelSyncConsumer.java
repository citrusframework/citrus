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

package com.consol.citrus.channel;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.messaging.ReplyProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class ChannelSyncConsumer extends ChannelConsumer implements ReplyProducer {
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(ChannelSyncConsumer.class);

    /** Reply channel store */
    private Map<String, MessageChannel> replyChannels = new HashMap<String, MessageChannel>();

    /** Endpoint configuration */
    private final ChannelSyncEndpointConfiguration endpointConfiguration;

    /**
     * Default constructor using emdpoint configuration.
     * @param endpointConfiguration
     */
    public ChannelSyncConsumer(ChannelSyncEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public Message receive(String selector, TestContext context, long timeout) {
        Message receivedMessage = super.receive(selector, context, timeout);
        saveReplyMessageChannel(receivedMessage, context);

        return receivedMessage;
    }

    @Override
    public void send(Message message, TestContext context) {
        Assert.notNull(message, "Can not send empty message");

        String correlationKey = getDefaultCorrelationId(message, context);
        MessageChannel replyChannel = findReplyChannel(correlationKey);
        Assert.notNull(replyChannel, "Failed to find reply channel for message correlation key: " + correlationKey);

        log.info("Sending message to reply channel: '" + replyChannel + "'");

        if (log.isDebugEnabled()) {
            log.debug("Message to send is:\n" + message.toString());
        }

        try {
            endpointConfiguration.getMessagingTemplate().send(replyChannel,
                    endpointConfiguration.getMessageConverter().convertOutbound(message, endpointConfiguration));
        } catch (MessageDeliveryException e) {
            throw new CitrusRuntimeException("Failed to send message to channel: '" + replyChannel + "'", e);
        }

        log.info("Message was successfully sent to reply channel: '" + replyChannel + "'");
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
        } else if (StringUtils.hasText((String) receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.REPLY_CHANNEL))){
            replyChannel = resolveChannelName(receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.REPLY_CHANNEL).toString());
        }

        if (replyChannel != null) {
            replyChannels.put(createCorrelationKey(receivedMessage, context), replyChannel);
        } else {
            log.warn("Unable to retrieve reply message channel for message \n" +
                    receivedMessage + "\n - no reply channel found in message headers!");
        }
    }

    /**
     * Get the reply message channel with given correlation key.
     */
    public MessageChannel findReplyChannel(String correlationKey) {
        return replyChannels.remove(correlationKey);
    }

    /**
     * Creates new correlation key either from correlator implementation in endpoint configuration or with default uuid generation.
     * Also saves created correlation key as test variable so according reply message polling can use the correlation key.
     *
     * @param message
     * @param context
     * @return
     */
    private String createCorrelationKey(Message message, TestContext context) {
        String correlationKey;
        if (endpointConfiguration.getCorrelator() != null) {
            correlationKey = endpointConfiguration.getCorrelator().getCorrelationKey(message);
        } else {
            correlationKey = UUID.randomUUID().toString();
        }
        context.setVariable(MessageHeaders.MESSAGE_CORRELATION_KEY + this.hashCode(), correlationKey);
        return correlationKey;
    }

    /**
     * Looks for default correlation id in message header and test context. If not present constructs default correlation key.
     * @param message
     * @param context
     * @return
     */
    private String getDefaultCorrelationId(Message message, TestContext context) {
        if (message.getHeader(MessageHeaders.MESSAGE_CORRELATION_KEY) != null) {
            String correlationKey = message.getHeader(MessageHeaders.MESSAGE_CORRELATION_KEY).toString();

            if (endpointConfiguration.getCorrelator() != null) {
                correlationKey = endpointConfiguration.getCorrelator().getCorrelationKey(correlationKey);
            }

            //remove citrus specific header from message
            message.removeHeader(MessageHeaders.MESSAGE_CORRELATION_KEY);
            return correlationKey;
        }

        if (context.getVariables().containsKey(MessageHeaders.MESSAGE_CORRELATION_KEY + this.hashCode())) {
            return context.getVariable(MessageHeaders.MESSAGE_CORRELATION_KEY + this.hashCode());
        }

        return "";
    }

}
