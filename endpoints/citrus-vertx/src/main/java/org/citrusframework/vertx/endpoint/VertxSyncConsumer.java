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

import io.vertx.core.Vertx;
import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;
import org.citrusframework.message.correlation.CorrelationManager;
import org.citrusframework.message.correlation.PollingCorrelationManager;
import org.citrusframework.messaging.ReplyProducer;
import org.citrusframework.util.ObjectHelper;
import org.citrusframework.vertx.message.CitrusVertxMessageHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class VertxSyncConsumer extends VertxConsumer implements ReplyProducer {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(VertxSyncConsumer.class);

    /** Map of reply destinations */
    private CorrelationManager<String> correlationManager;

    /** Vert.x instance */
    private final Vertx vertx;

    /** Endpoint configuration */
    private final VertxSyncEndpointConfiguration endpointConfiguration;

    /**
     * Default constructor using endpoint configuration.
     * @param name
     * @param vertx
     * @param endpointConfiguration
     */
    public VertxSyncConsumer(String name, Vertx vertx, VertxSyncEndpointConfiguration endpointConfiguration) {
        super(name, vertx, endpointConfiguration);
        this.vertx = vertx;
        this.endpointConfiguration = endpointConfiguration;

        this.correlationManager = new PollingCorrelationManager<>(endpointConfiguration, "Reply Vert.x address not set up yet");
    }

    @Override
    public Message receive(TestContext context, long timeout) {
        Message receivedMessage = super.receive(context, timeout);
        saveReplyDestination(receivedMessage, context);

        return receivedMessage;
    }

    @Override
    public void send(Message message, TestContext context) {
        ObjectHelper.assertNotNull(message, "Message is empty - unable to send empty message");

        String correlationKeyName = endpointConfiguration.getCorrelator().getCorrelationKeyName(getName());
        String correlationKey = correlationManager.getCorrelationKey(correlationKeyName, context);
        String replyAddress = correlationManager.find(correlationKey, endpointConfiguration.getTimeout());
        ObjectHelper.assertNotNull(replyAddress, "Failed to find reply address for message correlation key: '" + correlationKey + "'");

        if (logger.isDebugEnabled()) {
            logger.debug("Sending Vert.x message to event bus address: '" + replyAddress + "'");
        }

        vertx.eventBus().send(replyAddress, message.getPayload());

        context.onOutboundMessage(message);

        logger.info("Message was sent to Vert.x event bus address: '" + replyAddress + "'");
    }

    /**
     * Store the reply address either straight forward or with a given
     * message correlation key.
     *
     * @param receivedMessage
     * @param context
     */
    public void saveReplyDestination(Message receivedMessage, TestContext context) {
        if (receivedMessage.getHeader(CitrusVertxMessageHeaders.VERTX_REPLY_ADDRESS) != null) {
            String correlationKeyName = endpointConfiguration.getCorrelator().getCorrelationKeyName(getName());
            String correlationKey = endpointConfiguration.getCorrelator().getCorrelationKey(receivedMessage);
            correlationManager.saveCorrelationKey(correlationKeyName, correlationKey, context);
            correlationManager.store(correlationKey, receivedMessage.getHeader(CitrusVertxMessageHeaders.VERTX_REPLY_ADDRESS).toString());
        }  else {
            logger.warn("Unable to retrieve reply address for message \n" +
                    receivedMessage + "\n - no reply address found in message headers!");
        }
    }

    /**
     * Sets the correlation manager.
     * @param correlationManager
     */
    public void setCorrelationManager(CorrelationManager<String> correlationManager) {
        this.correlationManager = correlationManager;
    }

}
