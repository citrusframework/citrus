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
import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.correlation.CorrelationManager;
import com.consol.citrus.message.correlation.PollingCorrelationManager;
import com.consol.citrus.messaging.ReplyConsumer;
import io.vertx.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class VertxSyncProducer extends VertxProducer implements ReplyConsumer {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(VertxSyncProducer.class);

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
        if (log.isDebugEnabled()) {
            log.debug("Sending message to Vert.x event bus address: '" + endpointConfiguration.getAddress() + "'");
        }

        String correlationKeyName = endpointConfiguration.getCorrelator().getCorrelationKeyName(getName());
        final String correlationKey = endpointConfiguration.getCorrelator().getCorrelationKey(message);
        correlationManager.saveCorrelationKey(correlationKeyName, correlationKey, context);
        context.onOutboundMessage(message);

        log.info("Message was sent to Vert.x event bus address: '" + endpointConfiguration.getAddress() + "'");

        vertx.eventBus().send(endpointConfiguration.getAddress(), message.getPayload(),
            new Handler<AsyncResult<io.vertx.core.eventbus.Message<Object>>>() {
                @Override
                public void handle(AsyncResult<io.vertx.core.eventbus.Message<Object>> event) {
                    log.info("Received synchronous response on Vert.x event bus reply address");

                    Message responseMessage = endpointConfiguration.getMessageConverter().convertInbound(event.result(), endpointConfiguration, context);

                    context.onInboundMessage(responseMessage);
                    correlationManager.store(correlationKey, responseMessage);
                }
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
            throw new ActionTimeoutException("Action timeout while receiving synchronous reply message on Vert.x event bus address");
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
