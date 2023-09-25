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

package org.citrusframework.vertx.endpoint;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ReplyMessageTimeoutException;
import org.citrusframework.message.Message;
import org.citrusframework.message.correlation.CorrelationManager;
import org.citrusframework.message.correlation.PollingCorrelationManager;
import org.citrusframework.messaging.ReplyConsumer;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class VertxSyncProducer extends VertxProducer implements ReplyConsumer {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(VertxSyncProducer.class);

    /** Store of reply messages */
    private CorrelationManager<Message> correlationManager;

    /** Vert.x instance */
    private final Vertx vertx;

    /** Endpoint configuration */
    private final VertxSyncEndpointConfiguration endpointConfiguration;

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

        this.correlationManager = new PollingCorrelationManager<>(endpointConfiguration, "Reply message did not arrive yet");
    }

    @Override
    public void send(Message message, final TestContext context) {
        if (logger.isDebugEnabled()) {
            logger.debug("Sending message to Vert.x event bus address: '" + endpointConfiguration.getAddress() + "'");
        }

        String correlationKeyName = endpointConfiguration.getCorrelator().getCorrelationKeyName(getName());
        final String correlationKey = endpointConfiguration.getCorrelator().getCorrelationKey(message);
        correlationManager.saveCorrelationKey(correlationKeyName, correlationKey, context);
        context.onOutboundMessage(message);

        logger.info("Message was sent to Vert.x event bus address: '" + endpointConfiguration.getAddress() + "'");

        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setSendTimeout(endpointConfiguration.getTimeout());
        vertx.eventBus().request(endpointConfiguration.getAddress(), message.getPayload(), deliveryOptions,
            event -> {
                logger.info("Received synchronous response on Vert.x event bus reply address");

                Message responseMessage = endpointConfiguration.getMessageConverter().convertInbound(event.result(), endpointConfiguration, context);

                context.onInboundMessage(responseMessage);
                correlationManager.store(correlationKey, responseMessage);
            });
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
            throw new ReplyMessageTimeoutException(timeout, endpointConfiguration.getAddress());
        }

        return message;
    }

    /**
     * Sets the correlation manager.
     * @param correlationManager
     */
    public void setCorrelationManager(CorrelationManager<Message> correlationManager) {
        this.correlationManager = correlationManager;
    }

}
