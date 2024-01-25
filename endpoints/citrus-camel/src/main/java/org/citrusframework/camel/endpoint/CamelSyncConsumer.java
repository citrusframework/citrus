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

package org.citrusframework.camel.endpoint;

import java.util.Map;

import org.apache.camel.Exchange;
import org.citrusframework.camel.message.CamelMessageHeaders;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.message.correlation.CorrelationManager;
import org.citrusframework.message.correlation.PollingCorrelationManager;
import org.citrusframework.messaging.ReplyProducer;
import org.citrusframework.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class CamelSyncConsumer extends CamelConsumer implements ReplyProducer {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CamelSyncConsumer.class);

    /** Storage for in flight exchanges */
    private CorrelationManager<Exchange> correlationManager;

    /** Endpoint configuration */
    private final CamelSyncEndpointConfiguration endpointConfiguration;

    /**
     * Constructor using endpoint configuration and fields.
     * @param name
     * @param endpointConfiguration
     */
    public CamelSyncConsumer(String name, CamelSyncEndpointConfiguration endpointConfiguration) {
        super(name, endpointConfiguration);
        this.endpointConfiguration = endpointConfiguration;

        this.correlationManager = new PollingCorrelationManager<>(endpointConfiguration, "Camel exchange not set up yet");
    }

    @Override
    public Message receive(TestContext context, long timeout) {
        String endpointUri;
        if (endpointConfiguration.getEndpointUri() != null) {
            endpointUri = context.replaceDynamicContentInString(endpointConfiguration.getEndpointUri());
        } else if (endpointConfiguration.getEndpoint() != null) {
            endpointUri = endpointConfiguration.getEndpoint().getEndpointUri();
        } else {
            throw new CitrusRuntimeException("Missing endpoint or endpointUri on Camel consumer");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Receiving message from camel endpoint: '" + endpointUri + "'");
        }

        Exchange exchange;
        if (endpointConfiguration.getEndpoint() != null) {
            exchange = getConsumerTemplate(context).receive(endpointConfiguration.getEndpoint(), timeout);
        } else {
            exchange = getConsumerTemplate(context).receive(endpointUri, timeout);
        }

        if (exchange == null) {
            throw new MessageTimeoutException(timeout, endpointUri);
        }

        logger.info("Received message from camel endpoint: '" + endpointUri + "'");

        Message message = endpointConfiguration.getMessageConverter().convertInbound(exchange, endpointConfiguration, context);
        context.onInboundMessage(message);

        String correlationKeyName = endpointConfiguration.getCorrelator().getCorrelationKeyName(getName());
        String correlationKey = endpointConfiguration.getCorrelator().getCorrelationKey(message);
        correlationManager.saveCorrelationKey(correlationKeyName, correlationKey, context);
        correlationManager.store(correlationKey, exchange);

        return message;
    }

    @Override
    public void send(Message message, TestContext context) {
        ObjectHelper.assertNotNull(message, "Message is empty - unable to send empty message");

        String correlationKeyName = endpointConfiguration.getCorrelator().getCorrelationKeyName(getName());
        String correlationKey = correlationManager.getCorrelationKey(correlationKeyName, context);
        Exchange exchange = correlationManager.find(correlationKey, endpointConfiguration.getTimeout());
        ObjectHelper.assertNotNull(exchange, "Failed to find camel exchange for message correlation key: '" + correlationKey + "'");

        buildOutMessage(exchange, message);

        if (logger.isDebugEnabled()) {
            logger.debug("Sending reply message to camel endpoint: '" + exchange.getFromEndpoint() + "'");
        }

        getConsumerTemplate(context).doneUoW(exchange);

        context.onOutboundMessage(message);

        logger.info("Message was sent to camel endpoint: '" + exchange.getFromEndpoint() + "'");
    }

    /**
     * Builds response and sets it as out message on given Camel exchange.
     * @param message
     * @param exchange
     * @return
     */
    private void buildOutMessage(Exchange exchange, Message message) {
        org.apache.camel.Message reply = exchange.getOut();
        for (Map.Entry<String, Object> header : message.getHeaders().entrySet()) {
            if (!header.getKey().startsWith(MessageHeaders.PREFIX)) {
                reply.setHeader(header.getKey(), header.getValue());
            }
        }

        if (message.getHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION) != null) {
            String exceptionClass = message.getHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION).toString();
            String exceptionMsg = null;

            if (message.getHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION_MESSAGE) != null) {
                exceptionMsg = message.getHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION_MESSAGE).toString();
            }

            try {
                Class<?> exception = Class.forName(exceptionClass);
                if (exceptionMsg != null) {
                    exchange.setException((Throwable) exception.getConstructor(String.class).newInstance(exceptionMsg));
                } else {
                    exchange.setException((Throwable) exception.getDeclaredConstructor().newInstance());
                }
            } catch (Exception e) {
                logger.warn("Unable to create proper exception instance for exchange!", e);
            }
        }

        reply.setBody(message.getPayload());
    }

    /**
     * Sets the correlation manager.
     * @param correlationManager
     */
    public void setCorrelationManager(CorrelationManager<Exchange> correlationManager) {
        this.correlationManager = correlationManager;
    }

}
