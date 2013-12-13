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

import com.consol.citrus.endpoint.ReplyEndpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.CitrusMessageHeaders;
import com.consol.citrus.message.ReplyMessageCorrelator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.*;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MessageChannelSyncEndpoint extends MessageChannelEndpoint implements ReplyEndpoint {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(MessageChannelSyncEndpoint.class);

    /** Reply channel store */
    private Map<String, MessageChannel> replyChannels = new HashMap<String, MessageChannel>();

    /** Store of reply messages */
    private Map<String, Message<?>> replyMessages = new HashMap<String, Message<?>>();

    /** Reply message correlator */
    private ReplyMessageCorrelator correlator = null;

    /** Polling interval when waiting for synchronous reply message to arrive */
    private long pollingInterval = 500;

    /** Retry logger */
    private static final Logger RETRY_LOG = LoggerFactory.getLogger("com.consol.citrus.MessageRetryLogger");

    @Override
    public Message<?> receive(String selector, long timeout) {
        Message<?> receivedMessage = super.receive(selector, timeout);
        saveReplyMessageChannel(receivedMessage);

        return receivedMessage;
    }

    @Override
    public void send(Message<?> message) {
        String destinationChannelName = getDestinationChannelName();

        log.info("Sending message to channel: '" + destinationChannelName + "'");

        if (log.isDebugEnabled()) {
            log.debug("Message to sent is:\n" + message.toString());
        }

        getMessagingTemplate().setReceiveTimeout(getTimeout());
        Message<?> replyMessage;

        log.info("Message was successfully sent to channel: '" + destinationChannelName + "'");

        replyMessage = getMessagingTemplate().sendAndReceive(getDestinationChannel(), message);

        if (replyMessage == null) {
            throw new CitrusRuntimeException("Reply timed out after " +
                    getTimeout() + "ms. Did not receive reply message on reply channel");
        } else {
            log.info("Received synchronous repsonse message from reply channel");
        }

        saveReplyMessage(message, replyMessage);
    }

    @Override
    public Message<?> receiveReplyMessage(String correlationKey, long timeout) {
        long timeLeft = timeout;
        Message<?> message = findReplyMessage(correlationKey);

        while (message == null && timeLeft > 0) {
            timeLeft -= pollingInterval;

            if (RETRY_LOG.isDebugEnabled()) {
                RETRY_LOG.debug("Reply message did not arrive yet - retrying in " + (timeLeft > 0 ? pollingInterval : pollingInterval + timeLeft) + "ms");
            }

            try {
                Thread.sleep(timeLeft > 0 ? pollingInterval : pollingInterval + timeLeft);
            } catch (InterruptedException e) {
                RETRY_LOG.warn("Thread interrupted while waiting for retry", e);
            }

            message = findReplyMessage(correlationKey);
        }

        return message;
    }

    @Override
    public void sendReplyMessage(Message<?> message) {
        Assert.notNull(message, "Can not send empty message");

        MessageChannel replyChannel;
        Message<?> replyMessage;

        if (correlator != null) {
            Assert.notNull(message.getHeaders().get(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR), "Can not correlate reply destination - " +
                    "you need to set " + CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR + " in message header");

            String correlationKey = correlator.getCorrelationKey(message.getHeaders().get(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR).toString());
            replyChannel = findReplyChannel(correlationKey);
            Assert.notNull(replyChannel, "Unable to locate reply channel with correlation key: " + correlationKey);

            //remove citrus specific header from message
            replyMessage = MessageBuilder.fromMessage(message).removeHeader(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR).build();
        } else {
            replyMessage = message;
            replyChannel = findReplyChannel();
            Assert.notNull(replyChannel, "Unable to locate reply channel");
        }

        log.info("Sending message to reply channel: '" + replyChannel + "'");

        if (log.isDebugEnabled()) {
            log.debug("Message to send is:\n" + replyMessage.toString());
        }

        try {
            getMessagingTemplate().send(replyChannel, replyMessage);
        } catch (MessageDeliveryException e) {
            throw new CitrusRuntimeException("Failed to send message to channel: '" + replyChannel + "'", e);
        }

        log.info("Message was successfully sent to reply channel: '" + replyChannel + "'");
    }

    /**
     * Store reply message channel.
     * @param receivedMessage
     */
    public void saveReplyMessageChannel(Message<?> receivedMessage) {
        MessageChannel replyChannel;

        if (receivedMessage.getHeaders().getReplyChannel() instanceof MessageChannel) {
            replyChannel = (MessageChannel)receivedMessage.getHeaders().getReplyChannel();
        } else if (StringUtils.hasText((String) receivedMessage.getHeaders().getReplyChannel())){
            replyChannel = resolveChannelName(receivedMessage.getHeaders().getReplyChannel().toString());
        } else {
            log.warn("Unable to retrieve reply message channel for message \n" +
                    receivedMessage + "\n - no reply channel found in message headers!");
            return;
        }

        if (correlator != null) {
            replyChannels.put(correlator.getCorrelationKey(receivedMessage), replyChannel);
        } else {
            replyChannels.put("", replyChannel);
        }
    }

    /**
     * Saves reply message with correlation key to local store for later processing.
     * @param correlationKey
     * @param replyMessage the reply message.
     */
    public void saveReplyMessage(String correlationKey, Message<?> replyMessage) {
        replyMessages.put(correlationKey, replyMessage);
    }

    /**
     * Saves reply message to local store for later processing. Constructs correlation key from initial request.
     * @param requestMessage
     * @param replyMessage
     */
    public void saveReplyMessage(Message<?> requestMessage, Message<?> replyMessage) {
        if (correlator != null) {
            saveReplyMessage(correlator.getCorrelationKey(requestMessage), replyMessage);
        } else {
            saveReplyMessage("", replyMessage);
        }
    }

    /**
     * Tries to find reply message for correlation key from local store.
     * @param correlationKey
     * @return
     */
    public Message<?> findReplyMessage(String correlationKey) {
        return replyMessages.remove(correlationKey);
    }

    /**
     * Get the reply message channel with given correlation key.
     */
    public MessageChannel findReplyChannel(String correlationKey) {
        return replyChannels.remove(correlationKey);
    }

    /**
     * Get the reply message channel.
     */
    public MessageChannel findReplyChannel() {
        return replyChannels.remove("");
    }

    /**
     * Set the reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        this.correlator = correlator;
    }

    /**
     * Gets the correlator.
     * @return the correlator
     */
    public ReplyMessageCorrelator getCorrelator() {
        return correlator;
    }

    /**
     * Gets the pollingInterval.
     * @return the pollingInterval the pollingInterval to get.
     */
    public long getPollingInterval() {
        return pollingInterval;
    }

    /**
     * Sets the pollingInterval.
     * @param pollingInterval the pollingInterval to set
     */
    public void setPollingInterval(long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

}
