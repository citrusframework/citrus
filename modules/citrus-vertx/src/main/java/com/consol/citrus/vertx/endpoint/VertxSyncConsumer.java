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

import java.util.HashMap;
import java.util.Map;

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
        saveReplyDestination(receivedMessage);

        return receivedMessage;
    }

    @Override
    public void send(Message message) {
        Assert.notNull(message, "Message is empty - unable to send empty message");

        String replyAddress;
        if (endpointConfiguration.getCorrelator() != null) {
            Assert.notNull(message.getHeader(MessageHeaders.SYNC_MESSAGE_CORRELATOR), "Can not correlate reply destination - " +
                    "you need to set " + MessageHeaders.SYNC_MESSAGE_CORRELATOR + " in message header");

            String correlationKey = endpointConfiguration.getCorrelator().getCorrelationKey(message.getHeader(MessageHeaders.SYNC_MESSAGE_CORRELATOR).toString());
            replyAddress = replyAddressMap.remove(correlationKey);
            Assert.notNull(replyAddress, "Unable to locate reply address with correlation key: '" + correlationKey + "'");

            //remove citrus specific header from message
            message.removeHeader(MessageHeaders.SYNC_MESSAGE_CORRELATOR);
        } else {
            replyAddress = replyAddressMap.remove("");
            Assert.notNull(replyAddress, "Unable to locate reply address on event bus");
        }

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
     */
    public void saveReplyDestination(Message receivedMessage) {
        if (endpointConfiguration.getCorrelator() != null) {
            replyAddressMap.put(endpointConfiguration.getCorrelator().getCorrelationKey(receivedMessage), receivedMessage.getHeader(CitrusVertxMessageHeaders.VERTX_REPLY_ADDRESS).toString());
        } else {
            replyAddressMap.put("", receivedMessage.getHeader(CitrusVertxMessageHeaders.VERTX_REPLY_ADDRESS).toString());
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
}
