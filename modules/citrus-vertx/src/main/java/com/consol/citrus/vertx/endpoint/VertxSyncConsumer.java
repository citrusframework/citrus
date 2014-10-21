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
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.messaging.ReplyProducer;
import com.consol.citrus.report.MessageListeners;
import com.consol.citrus.vertx.message.CitrusVertxMessageHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.vertx.java.core.Vertx;

import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class VertxSyncConsumer extends VertxConsumer implements ReplyProducer {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(VertxSyncConsumer.class);

    /** Map of reply destinations */
    private Map<String, String> replyAddressMap = new HashMap<String, String>();

    /** Vert.x instance */
    private final Vertx vertx;

    /** Endpoint configuration */
    private final VertxSyncEndpointConfiguration endpointConfiguration;

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public VertxSyncConsumer(Vertx vertx, VertxSyncEndpointConfiguration endpointConfiguration, MessageListeners messageListeners) {
        super(vertx, endpointConfiguration, messageListeners);
        this.vertx = vertx;
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public Message receive(TestContext context, long timeout) {
        Message receivedMessage = super.receive(context, timeout);
        saveReplyDestination(receivedMessage, context);

        return receivedMessage;
    }

    @Override
    public void send(Message message, TestContext context) {
        Assert.notNull(message, "Message is empty - unable to send empty message");

        String correlationKey = getDefaultCorrelationId(message, context);
        String replyAddress = replyAddressMap.remove(correlationKey);
        Assert.notNull(replyAddress, "Failed to find reply address for message correlation key: '" + correlationKey + "'");

        log.info("Sending Vert.x message to event bus address: '" + replyAddress + "'");

        vertx.eventBus().send(replyAddress, message.getPayload());

        onOutboundMessage(message);

        log.info("Message was successfully sent to event bus address: '" + replyAddress + "'");
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
            replyAddressMap.put(createCorrelationKey(receivedMessage, context), receivedMessage.getHeader(CitrusVertxMessageHeaders.VERTX_REPLY_ADDRESS).toString());
        }  else {
            log.warn("Unable to retrieve reply address for message \n" +
                    receivedMessage + "\n - no reply address found in message headers!");
        }
    }

    /**
     * Informs message listeners if present.
     * @param message
     */
    protected void onOutboundMessage(Message message) {
        if (getMessageListener() != null) {
            getMessageListener().onOutboundMessage(message);
        } else {
            log.info("Sent message is:" + System.getProperty("line.separator") + message.toString());
        }
    }

    /**
     * Creates new correlation key either from correlator implementation in endpoint configuration or with default uuid generation.
     * Also saves created correlation key as test variable so according reply message polling can use the correlation key.
     *
     * @param message
     * @param context
     * @return
     */
    private String createCorrelationKey(Message message, TestContext context) {
        String correlationKey;
        if (endpointConfiguration.getCorrelator() != null) {
            correlationKey = endpointConfiguration.getCorrelator().getCorrelationKey(message);
        } else {
            correlationKey = UUID.randomUUID().toString();
        }
        context.setVariable(MessageHeaders.MESSAGE_CORRELATION_KEY + this.hashCode(), correlationKey);
        return correlationKey;
    }

    /**
     * Looks for default correlation id in message header and test context. If not present constructs default correlation key.
     * @param message
     * @param context
     * @return
     */
    private String getDefaultCorrelationId(Message message, TestContext context) {
        if (message.getHeader(MessageHeaders.MESSAGE_CORRELATION_KEY) != null) {
            String correlationKey = message.getHeader(MessageHeaders.MESSAGE_CORRELATION_KEY).toString();

            if (endpointConfiguration.getCorrelator() != null) {
                correlationKey = endpointConfiguration.getCorrelator().getCorrelationKey(correlationKey);
            }

            //remove citrus specific header from message
            message.removeHeader(MessageHeaders.MESSAGE_CORRELATION_KEY);
            return correlationKey;
        }

        if (context.getVariables().containsKey(MessageHeaders.MESSAGE_CORRELATION_KEY + this.hashCode())) {
            return context.getVariable(MessageHeaders.MESSAGE_CORRELATION_KEY + this.hashCode());
        }

        return "";
    }
}
