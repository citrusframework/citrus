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

package com.consol.citrus.camel.endpoint;

import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.message.CitrusMessageHeaders;
import com.consol.citrus.messaging.ReplyProducer;
import com.consol.citrus.report.MessageListeners;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class CamelSyncConsumer extends CamelConsumer implements ReplyProducer {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CamelSyncConsumer.class);

    /** Map of reply destinations */
    private Map<String, Exchange> exchanges = new HashMap<String, Exchange>();

    /** Endpoint configuration */
    private final CamelSyncEndpointConfiguration endpointConfiguration;

    /**
     * Constructor using endpoint configuration and fields.
     *
     * @param endpointConfiguration
     * @param messageListener
     */
    public CamelSyncConsumer(CamelSyncEndpointConfiguration endpointConfiguration, MessageListeners messageListener) {
        super(endpointConfiguration, messageListener);
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public Message<?> receive(long timeout) {
        log.info("Receiving message from camel endpoint: '" + endpointConfiguration.getEndpointUri() + "'");

        Exchange exchange = endpointConfiguration.getCamelContext().createConsumerTemplate().receive(endpointConfiguration.getEndpointUri(), timeout);

        if (exchange == null) {
            throw new ActionTimeoutException("Action timed out while receiving message from camel endpoint '" + endpointConfiguration.getEndpointUri() + "'");
        }

        log.info("Received message from camel endpoint: '" + endpointConfiguration.getEndpointUri() + "'");

        Message message = endpointConfiguration.getMessageConverter().convertMessage(exchange);
        onInboundMessage(message);

        if (endpointConfiguration.getCorrelator() != null) {
            exchanges.put(endpointConfiguration.getCorrelator().getCorrelationKey(message), exchange);
        } else {
            exchanges.put("", exchange);
        }

        return message;
    }

    @Override
    public void send(Message<?> message) {
        Assert.notNull(message, "Message is empty - unable to send empty message");

        Exchange exchange;
        if (endpointConfiguration.getCorrelator() != null) {
            Assert.notNull(message.getHeaders().get(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR), "Can not correlate reply destination - " +
                    "you need to set " + CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR + " in message header");

            String correlationKey = endpointConfiguration.getCorrelator().getCorrelationKey(message.getHeaders().get(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR).toString());
            exchange = exchanges.remove(correlationKey);
            Assert.notNull(exchange, "Unable to locate camel exchange with correlation key: '" + correlationKey + "'");
        } else {
            exchange = exchanges.remove("");
            Assert.notNull(exchange, "Unable to locate camel exchange");
        }

        org.apache.camel.Message reply = exchange.getOut();
        for (Map.Entry<String, Object> header : message.getHeaders().entrySet()) {
            if (!header.getKey().equals(CitrusMessageHeaders.SYNC_MESSAGE_CORRELATOR)) {
                reply.setHeader(header.getKey(), header.getValue());
            }
        }
        reply.setBody(message.getPayload());

        log.info("Sending reply message to camel endpoint: '" + exchange.getFromEndpoint() + "'");

        endpointConfiguration.getCamelContext().createConsumerTemplate().doneUoW(exchange);

        onOutboundMessage(message);

        log.info("Message was successfully sent to camel endpoint: '" + exchange.getFromEndpoint() + "'");
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
}
