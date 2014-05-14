/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.vertx.endpoint;

import com.consol.citrus.messaging.ReplyConsumer;
import com.consol.citrus.report.MessageListeners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class VertxSyncProducer extends VertxProducer implements ReplyConsumer {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(VertxSyncProducer.class);

    /** Store of reply messages */
    private Map<String, Message<?>> replyMessages = new HashMap<String, Message<?>>();

    /** Vert.x instance */
    private final Vertx vertx;

    /** Endpoint configuration */
    private final VertxSyncEndpointConfiguration endpointConfiguration;

    /** Retry logger */
    private static final Logger RETRY_LOG = LoggerFactory.getLogger("com.consol.citrus.MessageRetryLogger");

    /**
     * Default constructor using endpoint configuration.
     *
     * @param vertx
     * @param endpointConfiguration
     * @param messageListener
     */
    public VertxSyncProducer(Vertx vertx, VertxSyncEndpointConfiguration endpointConfiguration, MessageListeners messageListener) {
        super(vertx, endpointConfiguration, messageListener);
        this.vertx = vertx;
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public void send(final Message<?> message) {
        log.info("Sending message to Vert.x event bus address: '" + endpointConfiguration.getAddress() + "'");

        onOutboundMessage(message);

        log.info("Message was successfully sent to Vert.x event bus address: '" + endpointConfiguration.getAddress() + "'");

        vertx.eventBus().send(endpointConfiguration.getAddress(), message.getPayload(),
            new Handler<org.vertx.java.core.eventbus.Message>() {
                @Override
                public void handle(org.vertx.java.core.eventbus.Message event) {
                    log.info("Received synchronous response message on event bus reply address");
                    onReplyMessage(message, endpointConfiguration.getMessageConverter().convertMessage(event));
                }
            });
    }

    @Override
    public Message<?> receive(long timeout) {
        return receive("", timeout);
    }

    @Override
    public Message<?> receive(String selector, long timeout) {
        long timeLeft = timeout;
        Message<?> message = findReplyMessage(selector);

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
    public void onReplyMessage(String correlationKey, Message<?> replyMessage) {
        replyMessages.put(correlationKey, replyMessage);
    }

    /**
     * Saves reply message to local store for later processing. Constructs correlation key from initial request.
     * @param requestMessage
     * @param replyMessage
     */
    public void onReplyMessage(Message<?> requestMessage, Message<?> replyMessage) {
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
    public Message<?> findReplyMessage(String correlationKey) {
        return replyMessages.remove(correlationKey);
    }
}
