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
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.message.Message;
import com.consol.citrus.messaging.ReplyProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.*;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

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
        saveReplyMessageChannel(receivedMessage);

        return receivedMessage;
    }

    @Override
    public void send(Message message) {
        Assert.notNull(message, "Can not send empty message");

        MessageChannel replyChannel;
        if (endpointConfiguration.getCorrelator() != null) {
            Assert.notNull(message.getHeader(MessageHeaders.SYNC_MESSAGE_CORRELATOR), "Can not correlate reply destination - " +
                    "you need to set " + MessageHeaders.SYNC_MESSAGE_CORRELATOR + " in message header");

            String correlationKey = endpointConfiguration.getCorrelator().getCorrelationKey(message.getHeader(MessageHeaders.SYNC_MESSAGE_CORRELATOR).toString());
            replyChannel = findReplyChannel(correlationKey);
            Assert.notNull(replyChannel, "Unable to locate reply channel with correlation key: " + correlationKey);

            //remove citrus specific header from message
            message.removeHeader(com.consol.citrus.message.MessageHeaders.SYNC_MESSAGE_CORRELATOR);
        } else {
            replyChannel = findReplyChannel("");
            Assert.notNull(replyChannel, "Unable to locate reply channel");
        }

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
     */
    public void saveReplyMessageChannel(Message receivedMessage) {
        MessageChannel replyChannel;

        if (receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.REPLY_CHANNEL) instanceof MessageChannel) {
            replyChannel = (MessageChannel)receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.REPLY_CHANNEL);
        } else if (StringUtils.hasText((String) receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.REPLY_CHANNEL))){
            replyChannel = resolveChannelName(receivedMessage.getHeader(org.springframework.messaging.MessageHeaders.REPLY_CHANNEL).toString());
        } else {
            log.warn("Unable to retrieve reply message channel for message \n" +
                    receivedMessage + "\n - no reply channel found in message headers!");
            return;
        }

        if (endpointConfiguration.getCorrelator() != null) {
            replyChannels.put(endpointConfiguration.getCorrelator().getCorrelationKey(receivedMessage), replyChannel);
        } else {
            replyChannels.put("", replyChannel);
        }
    }

    /**
     * Get the reply message channel with given correlation key.
     */
    public MessageChannel findReplyChannel(String correlationKey) {
        return replyChannels.remove(correlationKey);
    }

}
