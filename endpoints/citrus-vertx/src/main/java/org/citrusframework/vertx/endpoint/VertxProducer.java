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
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Producer;
import io.vertx.core.eventbus.DeliveryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.Vertx;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class VertxProducer implements Producer {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(VertxProducer.class);

    /** The producer name. */
    private final String name;

    /** Vert.x instance */
    private final Vertx vertx;

    /** Endpoint configuration */
    private final VertxEndpointConfiguration endpointConfiguration;

    /**
     * Default constructor using endpoint configuration.
     * @param name
     * @param endpointConfiguration
     */
    public VertxProducer(String name, Vertx vertx, VertxEndpointConfiguration endpointConfiguration) {
        this.name = name;
        this.vertx = vertx;
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public void send(Message message, TestContext context) {
        try {
            sendOrPublishMessage(message);
        } catch (IllegalStateException e) {
            if (e.getMessage().equals("Event Bus is not started")) {
                logger.warn("Event bus not started yet - retrying in 2000 ms");

                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException ex) {
                    logger.warn("Interrupted while waiting fot event bus to start", ex);
                }

                sendOrPublishMessage(message);
            } else {
                throw e;
            }
        }

        context.onOutboundMessage(message);

        logger.info("Message was sent to Vert.x event bus address: '" + endpointConfiguration.getAddress() + "'");
    }

    /**
     * Sends or publishes new outbound message depending on eventbus nature.
     * @param message
     */
    private void sendOrPublishMessage(Message message) {
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setSendTimeout(endpointConfiguration.getTimeout());

        if (endpointConfiguration.isPubSubDomain()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Publish Vert.x event bus message to address: '" + endpointConfiguration.getAddress() + "'");
            }
            vertx.eventBus().publish(endpointConfiguration.getAddress(), message.getPayload(), deliveryOptions);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Sending Vert.x event bus message to address: '" + endpointConfiguration.getAddress() + "'");
            }
            vertx.eventBus().send(endpointConfiguration.getAddress(), message.getPayload(), deliveryOptions);
        }
    }

    @Override
    public String getName() {
        return name;
    }

}
