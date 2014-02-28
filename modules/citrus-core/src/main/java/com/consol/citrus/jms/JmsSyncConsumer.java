/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.jms;

import com.consol.citrus.message.CitrusMessageHeaders;
import com.consol.citrus.messaging.ReplyProducer;
import com.consol.citrus.report.MessageListeners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.jms.JmsHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.Assert;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class JmsSyncConsumer extends JmsConsumer implements ReplyProducer {

    /** Map of reply destinations */
    private Map<String, Destination> replyDestinations = new HashMap<String, Destination>();

    /** Endpoint configuration */
    private final JmsSyncEndpointConfiguration endpointConfiguration;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JmsSyncConsumer.class);

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public JmsSyncConsumer(JmsSyncEndpointConfiguration endpointConfiguration, MessageListeners messageListeners) {
        super(endpointConfiguration, messageListeners);
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public Message<?> receive(String selector, long timeout) {
        Message<?> receivedMessage = super.receive(selector, timeout);
        saveReplyDestination(receivedMessage);

        return receivedMessage;
    }

    @Override
    public void send(Message<?> message) {
        Assert.notNull(message, "Message is empty - unable to send empty message");

        Destination replyDestination;
        Message<?> replyMessage;

        if (endpointConfiguration.getCorrelator() != null) {
            Assert.notNull(message.getHeaders().get(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR), "Can not correlate reply destination - " +
                    "you need to set " + CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR + " in message header");

            String correlationKey = endpointConfiguration.getCorrelator().getCorrelationKey(message.getHeaders().get(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR).toString());
            replyDestination = replyDestinations.remove(correlationKey);
            Assert.notNull(replyDestination, "Unable to locate JMS reply destination with correlation key: '" + correlationKey + "'");

            //remove citrus specific header from message
            replyMessage = MessageBuilder.fromMessage(message).removeHeader(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR).build();
        } else {
            replyMessage = message;
            replyDestination = replyDestinations.remove("");
            Assert.notNull(replyDestination, "Unable to locate JMS reply destination");
        }

        log.info("Sending JMS message to destination: '" + getDestinationName(replyDestination) + "'");

        endpointConfiguration.getJmsTemplate().convertAndSend(replyDestination, replyMessage);

        onOutboundMessage(replyMessage);

        log.info("Message was successfully sent to destination: '" + getDestinationName(replyDestination) + "'");
    }

    /**
     * Finds reply destination by default correlation key in destination store.
     * @return
     */
    public Destination findReplyDestination() {
        return replyDestinations.remove("");
    }

    /**
     * Finds reply destination by correlation key in destination store.
     * @param correlationKey
     * @return
     */
    public Destination findReplyDestination(String correlationKey) {
        return replyDestinations.remove(correlationKey);
    }

    /**
     * Store the reply destination either straight forward or with a given
     * message correlation key.
     *
     * @param receivedMessage
     */
    public void saveReplyDestination(Message<?> receivedMessage) {
        if (endpointConfiguration.getCorrelator() != null) {
            replyDestinations.put(endpointConfiguration.getCorrelator().getCorrelationKey(receivedMessage), (Destination)receivedMessage.getHeaders().get(JmsHeaders.REPLY_TO));
        } else {
            replyDestinations.put("", (Destination)receivedMessage.getHeaders().get(JmsHeaders.REPLY_TO));
        }
    }

    /**
     * Informs message listeners if present.
     * @param message
     */
    protected void onOutboundMessage(Message<?> message) {
        if (getMessageListener() != null) {
            getMessageListener().onOutboundMessage(message.toString());
        } else {
            log.info("Sent message is:" + System.getProperty("line.separator") + message.toString());
        }
    }

    /**
     * Get the destination name (either a queue name or a topic name).
     * @return the destinationName
     */
    private String getDestinationName(Destination destination) {
        try {
            if (destination != null) {
                if (destination instanceof Queue) {
                    return ((Queue)destination).getQueueName();
                } else if (destination instanceof Topic) {
                    return ((Topic)destination).getTopicName();
                } else {
                    return destination.toString();
                }
            } else {
                return null;
            }
        } catch (JMSException e) {
            log.error("Error while getting destination name", e);
            return "";
        }
    }
}
