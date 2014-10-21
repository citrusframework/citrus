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

import com.consol.citrus.camel.message.CitrusCamelMessageHeaders;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.message.Message;
import com.consol.citrus.messaging.ReplyProducer;
import com.consol.citrus.report.MessageListeners;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.*;

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
    public Message receive(TestContext context, long timeout) {
        log.info("Receiving message from camel endpoint: '" + endpointConfiguration.getEndpointUri() + "'");

        Exchange exchange = endpointConfiguration.getCamelContext().createConsumerTemplate().receive(endpointConfiguration.getEndpointUri(), timeout);

        if (exchange == null) {
            throw new ActionTimeoutException("Action timed out while receiving message from camel endpoint '" + endpointConfiguration.getEndpointUri() + "'");
        }

        log.info("Received message from camel endpoint: '" + endpointConfiguration.getEndpointUri() + "'");

        Message message = endpointConfiguration.getMessageConverter().convertInbound(exchange, endpointConfiguration);
        onInboundMessage(message);

        exchanges.put(createCorrelationKey(message, context), exchange);

        return message;
    }

    @Override
    public void send(Message message, TestContext context) {
        Assert.notNull(message, "Message is empty - unable to send empty message");

        String correlationKey = getDefaultCorrelationId(message, context);
        Exchange exchange = exchanges.remove(correlationKey);
        Assert.notNull(exchange, "Failed to find camel exchange for message correlation key: '" + correlationKey + "'");

        buildOutMessage(exchange, message);

        log.info("Sending reply message to camel endpoint: '" + exchange.getFromEndpoint() + "'");

        endpointConfiguration.getCamelContext().createConsumerTemplate().doneUoW(exchange);

        onOutboundMessage(message);

        log.info("Message was successfully sent to camel endpoint: '" + exchange.getFromEndpoint() + "'");
    }

    /**
     * Builds response and sets it as out message on given Camel exchange.
     * @param message
     * @param exchange
     * @return
     */
    private void buildOutMessage(Exchange exchange, Message message) {
        org.apache.camel.Message reply = exchange.getOut();
        for (Map.Entry<String, Object> header : message.copyHeaders().entrySet()) {
            if (!header.getKey().startsWith(MessageHeaders.PREFIX)) {
                reply.setHeader(header.getKey(), header.getValue());
            }
        }

        if (message.getHeader(CitrusCamelMessageHeaders.EXCHANGE_EXCEPTION) != null) {
            String exceptionClass = message.getHeader(CitrusCamelMessageHeaders.EXCHANGE_EXCEPTION).toString();
            String exceptionMsg = null;

            if (message.getHeader(CitrusCamelMessageHeaders.EXCHANGE_EXCEPTION_MESSAGE) != null) {
                exceptionMsg = message.getHeader(CitrusCamelMessageHeaders.EXCHANGE_EXCEPTION_MESSAGE).toString();
            }

            try {
                Class<?> exception = Class.forName(exceptionClass);
                if (exceptionMsg != null) {
                    exchange.setException((Throwable) exception.getConstructor(String.class).newInstance(exceptionMsg));
                } else {
                    exchange.setException((Throwable) exception.newInstance());
                }
            } catch (RuntimeException e) {
                log.warn("Unable to create proper exception instance for exchange!", e);
            } catch (Exception e) {
                log.warn("Unable to create proper exception instance for exchange!", e);
            }
        }

        reply.setBody(message.getPayload());
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
