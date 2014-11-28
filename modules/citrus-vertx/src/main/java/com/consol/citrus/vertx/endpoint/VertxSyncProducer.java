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

import com.consol.citrus.context.TestContext;
import com.consol.citrus.message.*;
import com.consol.citrus.messaging.ReplyConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class VertxSyncProducer extends VertxProducer implements ReplyConsumer {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(VertxSyncProducer.class);

    /** Store of reply messages */
    private CorrelationManager<Message> replyManager = new DefaultCorrelationManager<Message>();

    /** Vert.x instance */
    private final Vertx vertx;

    /** Endpoint configuration */
    private final VertxSyncEndpointConfiguration endpointConfiguration;

    /** Retry logger */
    private static final Logger RETRY_LOG = LoggerFactory.getLogger("com.consol.citrus.MessageRetryLogger");

    /**
     * Default constructor using endpoint configuration.
     *
     * @param name
     * @param vertx
     * @param endpointConfiguration
     */
    public VertxSyncProducer(String name, Vertx vertx, VertxSyncEndpointConfiguration endpointConfiguration) {
        super(name, vertx, endpointConfiguration);
        this.vertx = vertx;
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public void send(Message message, final TestContext context) {
        log.info("Sending message to Vert.x event bus address: '" + endpointConfiguration.getAddress() + "'");

        final String correlationKey = endpointConfiguration.getCorrelator().getCorrelationKey(message);
        context.saveCorrelationKey(correlationKey, this);
        onOutboundMessage(message, context);

        log.info("Message was successfully sent to Vert.x event bus address: '" + endpointConfiguration.getAddress() + "'");

        vertx.eventBus().send(endpointConfiguration.getAddress(), message.getPayload(),
            new Handler<org.vertx.java.core.eventbus.Message>() {
                @Override
                public void handle(org.vertx.java.core.eventbus.Message event) {
                    log.info("Received synchronous response message on event bus reply address");

                    Message responseMessage = endpointConfiguration.getMessageConverter().convertInbound(event, endpointConfiguration);

                    onInboundMessage(responseMessage, context);
                    onReplyMessage(correlationKey, responseMessage);
                }
            });
    }

    @Override
    public Message receive(TestContext context) {
        return receive(context.getCorrelationKey(this), context);
    }

    @Override
    public Message receive(String selector, TestContext context) {
        return receive(selector, context, endpointConfiguration.getTimeout());
    }

    @Override
    public Message receive(TestContext context, long timeout) {
        return receive(context.getCorrelationKey(this), context, timeout);
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
        replyManager.store(correlationKey, replyMessage);
    }

    /**
     * Tries to find reply message for correlation key from local store.
     * @param correlationKey
     * @return
     */
    public Message findReplyMessage(String correlationKey) {
        return replyManager.find(correlationKey);
    }

    /**
     * Informs message listeners if present.
     * @param receivedMessage
     * @param context
     */
    protected void onInboundMessage(Message receivedMessage, TestContext context) {
        if (context.getMessageListeners() != null) {
            context.getMessageListeners().onInboundMessage(receivedMessage, context);
        } else {
            log.debug("Received message is:" + System.getProperty("line.separator") + (receivedMessage != null ? receivedMessage.toString() : ""));
        }
    }
}
