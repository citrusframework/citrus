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
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.AbstractMessageConsumer;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class VertxConsumer extends AbstractMessageConsumer {

    /** Vert.x instance */
    private final Vertx vertx;

    /** Endpoint configuration */
    private final VertxEndpointConfiguration endpointConfiguration;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(VertxConsumer.class);

    /** Retry logger */
    private static final Logger RETRY_LOG = LoggerFactory.getLogger("org.citrusframework.RetryLogger");

    /**
     * Default constructor using endpoint.
     * @param name
     * @param vertx
     * @param endpointConfiguration
     */
    public VertxConsumer(String name, Vertx vertx, VertxEndpointConfiguration endpointConfiguration) {
        super(name, endpointConfiguration);
        this.vertx = vertx;
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public Message receive(TestContext context, long timeout) {
        if (logger.isDebugEnabled()) {
            logger.debug("Receiving message on Vert.x event bus address: '" + endpointConfiguration.getAddress() + "'");
        }

        VertxSingleMessageHandler vertxMessageHandler = new VertxSingleMessageHandler();
        MessageConsumer<Object> vertxConsumer = vertx.eventBus().consumer(endpointConfiguration.getAddress(), vertxMessageHandler);

        try {
            long timeLeft = timeout;
            Message message = endpointConfiguration.getMessageConverter().convertInbound(vertxMessageHandler.getMessage(), endpointConfiguration, context);

            while (message == null && timeLeft > 0) {
                timeLeft -= endpointConfiguration.getPollingInterval();

                if (RETRY_LOG.isDebugEnabled()) {
                    RETRY_LOG.debug(String.format("Waiting for message on Vert.x event bus address '%s' - retrying in %s ms",
                            endpointConfiguration.getAddress(),
                            (timeLeft > 0 ? endpointConfiguration.getPollingInterval() : endpointConfiguration.getPollingInterval() + timeLeft)));
                }

                try {
                    Thread.sleep(timeLeft > 0 ? endpointConfiguration.getPollingInterval() : endpointConfiguration.getPollingInterval() + timeLeft);
                } catch (InterruptedException e) {
                    RETRY_LOG.warn("Thread interrupted while waiting for message on Vert.x event bus", e);
                }

                message = endpointConfiguration.getMessageConverter().convertInbound(vertxMessageHandler.getMessage(), endpointConfiguration, context);
            }

            if (message == null) {
                throw new MessageTimeoutException(timeout, endpointConfiguration.getAddress());
            }

            logger.info("Received message on Vert.x event bus address: '" + endpointConfiguration.getAddress() + "'");

            context.onInboundMessage(message);

            return message;
        } finally {
            vertxConsumer.unregister();
        }
    }

    /**
     * Simple Vert.x message handler stores first message received on event bus and ignores all further messages
     * until subscription is unregistered automatically.
     */
    private class VertxSingleMessageHandler implements Handler<io.vertx.core.eventbus.Message<Object>> {
        private io.vertx.core.eventbus.Message message;

        @Override
        public void handle(io.vertx.core.eventbus.Message event) {
            if (message == null) {
                this.message = event;
            } else {
                logger.warn("Vert.x message handler ignored message on event bus address '" + endpointConfiguration.getAddress() + "'");
                logger.debug("Vert.x message ignored is " + event);
            }
        }

        /**
         * Gets the vert.x message received on event bus.
         * @return
         */
        public io.vertx.core.eventbus.Message getMessage() {
            return message;
        }
    }

}
