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
import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.messaging.ReplyConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.consol.citrus.message.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class ChannelSyncProducer extends ChannelProducer implements ReplyConsumer {
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(ChannelSyncProducer.class);

    /** Store of reply messages */
    private Map<String, Message> replyMessages = new HashMap<String, Message>();

    /** Endpoint configuration */
    private final ChannelSyncEndpointConfiguration endpointConfiguration;

    /** Retry logger */
    private static final Logger RETRY_LOG = LoggerFactory.getLogger("com.consol.citrus.MessageRetryLogger");

    /**
     * Default constructor using endpoint configuration.
     *
     * @param endpointConfiguration
     */
    public ChannelSyncProducer(ChannelSyncEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public void send(Message message) {
        String destinationChannelName = getDestinationChannelName();

        log.info("Sending message to channel: '" + destinationChannelName + "'");

        if (log.isDebugEnabled()) {
            log.debug("Message to sent is:\n" + message.toString());
        }

        endpointConfiguration.getMessagingTemplate().setReceiveTimeout(endpointConfiguration.getTimeout());

        log.info("Message was successfully sent to channel: '" + destinationChannelName + "'");

        org.springframework.messaging.Message replyMessage = endpointConfiguration.getMessagingTemplate().sendAndReceive(getDestinationChannel(),
                endpointConfiguration.getMessageConverter().convertOutbound(message, endpointConfiguration));

        if (replyMessage == null) {
            throw new ActionTimeoutException("Reply timed out after " +
                    endpointConfiguration.getTimeout() + "ms. Did not receive reply message on reply channel");
        } else {
            log.info("Received synchronous response message from reply channel");
        }

        onReplyMessage(message, endpointConfiguration.getMessageConverter().convertInbound(replyMessage, endpointConfiguration));
    }

    @Override
    public Message receive(TestContext context) {
        return receive("", context);
    }

    @Override
    public Message receive(String selector, TestContext context) {
        return receive(selector, context, endpointConfiguration.getTimeout());
    }

    @Override
    public Message receive(TestContext context, long timeout) {
        return receive("", context, timeout);
    }

    @Override
    public Message receive(String selector, TestContext context, long timeout) {
        long timeLeft = timeout;
        Message message = findReplyMessage(selector);

        while (message == null && timeLeft > 0) {
            timeLeft -= endpointConfiguration.getPollingInterval();

            if (RETRY_LOG.isDebugEnabled()) {
                RETRY_LOG.debug("Reply message did not arrive yet - retrying in " + (timeLeft > 0 ? endpointConfiguration.getPollingInterval() : endpointConfiguration.getPollingInterval() + timeLeft) + "ms");
            }

            try {
                Thread.sleep(timeLeft > 0 ? endpointConfiguration.getPollingInterval() : endpointConfiguration.getPollingInterval() + timeLeft);
            } catch (InterruptedException e) {
                RETRY_LOG.warn("Thread interrupted while waiting for retry", e);
            }

            message = findReplyMessage(selector);
        }

        return message;
    }

    /**
     * Saves reply message with correlation key to local store for later processing.
     * @param correlationKey
     * @param replyMessage the reply message.
     */
    public void onReplyMessage(String correlationKey, Message replyMessage) {
        replyMessages.put(correlationKey, replyMessage);
    }

    /**
     * Saves reply message to local store for later processing. Constructs correlation key from initial request.
     * @param requestMessage
     * @param replyMessage
     */
    public void onReplyMessage(Message requestMessage, Message replyMessage) {
        if (endpointConfiguration.getCorrelator() != null) {
            onReplyMessage(endpointConfiguration.getCorrelator().getCorrelationKey(requestMessage), replyMessage);
        } else {
            onReplyMessage("", replyMessage);
        }
    }

    /**
     * Tries to find reply message for correlation key from local store.
     * @param correlationKey
     * @return
     */
    public Message findReplyMessage(String correlationKey) {
        return replyMessages.remove(correlationKey);
    }
}
