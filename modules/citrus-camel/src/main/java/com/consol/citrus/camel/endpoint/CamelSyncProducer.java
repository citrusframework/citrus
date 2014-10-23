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

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.message.*;
import com.consol.citrus.messaging.ReplyConsumer;
import com.consol.citrus.report.MessageListeners;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class CamelSyncProducer extends CamelProducer implements ReplyConsumer {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CamelSyncProducer.class);

    /** Store of reply messages */
    private CorrelationManager<Message> replyManager = new DefaultCorrelationManager<Message>();

    /** Endpoint configuration */
    private final CamelSyncEndpointConfiguration endpointConfiguration;

    /** Retry logger */
    private static final Logger RETRY_LOG = LoggerFactory.getLogger("com.consol.citrus.MessageRetryLogger");

    /**
     * Constructor using endpoint configuration and fields.
     *
     * @param name
     * @param endpointConfiguration
     * @param messageListener
     */
    public CamelSyncProducer(String name, CamelSyncEndpointConfiguration endpointConfiguration, MessageListeners messageListener) {
        super(name, endpointConfiguration, messageListener);
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public void send(final Message message, TestContext context) {
        log.info("Sending message to camel endpoint: '" + endpointConfiguration.getEndpointUri() + "'");

        String correlationKey = endpointConfiguration.getCorrelator().getCorrelationKey(message);
        context.saveCorrelationKey(correlationKey, this);

        onOutboundMessage(message);

        log.info("Message was successfully sent to camel endpoint: '" + endpointConfiguration.getEndpointUri() + "'");

        Exchange response = endpointConfiguration.getCamelContext().createProducerTemplate()
                .request(endpointConfiguration.getEndpointUri(), new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        endpointConfiguration.getMessageConverter().convertOutbound(exchange, message, endpointConfiguration);
                    }
                });


        log.info("Received synchronous response message on camel endpoint: '" + endpointConfiguration.getEndpointUri() + "'");
        Message replyMessage = endpointConfiguration.getMessageConverter().convertInbound(response, endpointConfiguration);
        onInboundMessage(replyMessage);
        replyManager.store(correlationKey, replyMessage);
    }

    @Override
    public Message receive(TestContext context) {
        return receive(context.getCorrelationKey(this), context);
    }

    @Override
    public Message receive(String selector, TestContext context) {
        return receive(selector, context, endpointConfiguration.getTimeout());
    }

    @Override
    public Message receive(TestContext context, long timeout) {
        return receive(context.getCorrelationKey(this), context, timeout);
    }

    @Override
    public Message receive(String selector, TestContext context, long timeout) {
        long timeLeft = timeout;
        Message message = replyManager.find(selector);

        while (message == null && timeLeft > 0) {
            timeLeft -= endpointConfiguration.getPollingInterval();

            if (RETRY_LOG.isDebugEnabled()) {
                RETRY_LOG.debug("Reply message did not arrive yet - retrying in " + (timeLeft > 0 ? endpointConfiguration.getPollingInterval() : endpointConfiguration.getPollingInterval() + timeLeft) + "ms");
            }

            try {
                Thread.sleep(timeLeft > 0 ? endpointConfiguration.getPollingInterval() : endpointConfiguration.getPollingInterval() + timeLeft);
            } catch (InterruptedException e) {
                RETRY_LOG.warn("Thread interrupted while waiting for retry", e);
            }

            message = replyManager.find(selector);
        }

        if (message == null) {
            throw new ActionTimeoutException("Action timeout while receiving synchronous reply on channel");
        }

        return message;
    }

    /**
     * Informs message listeners if present.
     * @param receivedMessage
     */
    protected void onInboundMessage(Message receivedMessage) {
        if (getMessageListener() != null) {
            getMessageListener().onInboundMessage(receivedMessage);
        } else {
            log.debug("Received message is:" + System.getProperty("line.separator") + (receivedMessage != null ? receivedMessage.toString() : ""));
        }
    }
}
