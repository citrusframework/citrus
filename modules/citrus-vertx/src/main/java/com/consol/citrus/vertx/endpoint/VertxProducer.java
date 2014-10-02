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

import com.consol.citrus.messaging.Producer;
import com.consol.citrus.message.Message;
import com.consol.citrus.report.MessageListeners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Vertx;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class VertxProducer implements Producer {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(VertxProducer.class);

    /** Vert.x instance */
    private final Vertx vertx;

    /** Endpoint configuration */
    private final VertxEndpointConfiguration endpointConfiguration;

    /** Message listener */
    private final MessageListeners messageListener;

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     * @param messageListener
     */
    public VertxProducer(Vertx vertx, VertxEndpointConfiguration endpointConfiguration, MessageListeners messageListener) {
        this.vertx = vertx;
        this.endpointConfiguration = endpointConfiguration;
        this.messageListener = messageListener;
    }

    @Override
    public void send(Message message) {
        try {
            sendOrPublishMessage(message);
        } catch (IllegalStateException e) {
            if (e.getMessage().equals("Event Bus is not started")) {
                log.warn("Event bus not started yet - retrying in 2000 ms");

                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException ex) {
                    log.warn("Interrupted while waiting fot event bus to start", ex);
                }

                sendOrPublishMessage(message);
            } else {
                throw e;
            }
        }

        onOutboundMessage(message);

        log.info("Message was successfully sent to Vert.x event bus address: '" + endpointConfiguration.getAddress() + "'");
    }

    private void sendOrPublishMessage(Message message) {
        if (endpointConfiguration.isPubSubDomain()) {
            log.info("Publish Vert.x event bus message to address: '" + endpointConfiguration.getAddress() + "'");
            vertx.eventBus().publish(endpointConfiguration.getAddress(), message.getPayload());
        } else {
            log.info("Sending Vert.x event bus message to address: '" + endpointConfiguration.getAddress() + "'");
            vertx.eventBus().send(endpointConfiguration.getAddress(), message.getPayload());
        }
    }

    /**
     * Informs message listeners if present.
     * @param message
     */
    protected void onOutboundMessage(Message message) {
        if (messageListener != null) {
            messageListener.onOutboundMessage(message.toString());
        } else {
            log.info("Sent message is:" + System.getProperty("line.separator") + message.toString());
        }
    }

    /**
     * Gets the message listener.
     * @return
     */
    public MessageListeners getMessageListener() {
        return messageListener;
    }
}
