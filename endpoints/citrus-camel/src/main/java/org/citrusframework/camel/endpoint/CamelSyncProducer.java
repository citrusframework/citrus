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

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ReplyMessageTimeoutException;
import org.citrusframework.message.Message;
import org.citrusframework.message.correlation.CorrelationManager;
import org.citrusframework.message.correlation.PollingCorrelationManager;
import org.citrusframework.messaging.ReplyConsumer;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Synchronous producer creates synchronous Camel exchange for sending message and receiving synchronous reply.
 * Reply message is correlated and stored in correlation manager. This way test cases are able to receive synchronous
 * message asynchronously at later time.
 *
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class CamelSyncProducer extends CamelProducer implements ReplyConsumer {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CamelSyncProducer.class);

    /** Store of reply messages */
    private CorrelationManager<Message> correlationManager;

    /** Endpoint configuration */
    private final CamelSyncEndpointConfiguration endpointConfiguration;

    /**
     * Constructor using endpoint configuration and fields.
     *
     * @param name
     * @param endpointConfiguration
     */
    public CamelSyncProducer(String name, CamelSyncEndpointConfiguration endpointConfiguration) {
        super(name, endpointConfiguration);
        this.endpointConfiguration = endpointConfiguration;

        this.correlationManager = new PollingCorrelationManager<>(endpointConfiguration, "Reply message did not arrive yet");
    }

    @Override
    public void send(final Message message, final TestContext context) {
        String endpointUri;
        if (endpointConfiguration.getEndpointUri() != null) {
            endpointUri = context.replaceDynamicContentInString(endpointConfiguration.getEndpointUri());
        } else if (endpointConfiguration.getEndpoint() != null){
            endpointUri = endpointConfiguration.getEndpoint().getEndpointUri();
        } else {
            throw new CitrusRuntimeException("Missing endpoint or endpointUri on Camel producer");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Sending message to camel endpoint: '" + endpointUri + "'");
        }

        String correlationKeyName = endpointConfiguration.getCorrelator().getCorrelationKeyName(getName());
        String correlationKey = endpointConfiguration.getCorrelator().getCorrelationKey(message);
        correlationManager.saveCorrelationKey(correlationKeyName, correlationKey, context);

        context.onOutboundMessage(message);

        Exchange response = getProducerTemplate(context)
                .request(endpointUri, new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        endpointConfiguration.getMessageConverter().convertOutbound(exchange, message, endpointConfiguration, context);
                        logger.info("Message was sent to camel endpoint: '" + endpointUri + "'");
                    }
                });


        logger.info("Received synchronous reply message on camel endpoint: '" + endpointUri + "'");
        Message replyMessage = endpointConfiguration.getMessageConverter().convertInbound(response, endpointConfiguration, context);
        context.onInboundMessage(replyMessage);
        correlationManager.store(correlationKey, replyMessage);
    }

    @Override
    public Message receive(TestContext context) {
        return receive(correlationManager.getCorrelationKey(
                endpointConfiguration.getCorrelator().getCorrelationKeyName(getName()), context), context);
    }

    @Override
    public Message receive(String selector, TestContext context) {
        return receive(selector, context, endpointConfiguration.getTimeout());
    }

    @Override
    public Message receive(TestContext context, long timeout) {
        return receive(correlationManager.getCorrelationKey(
                endpointConfiguration.getCorrelator().getCorrelationKeyName(getName()), context), context, timeout);
    }

    @Override
    public Message receive(String selector, TestContext context, long timeout) {
        Message message = correlationManager.find(selector, timeout);

        if (message == null) {
            String endpointUri;
            if (endpointConfiguration.getEndpointUri() != null) {
                endpointUri = context.replaceDynamicContentInString(endpointConfiguration.getEndpointUri());
            } else if (endpointConfiguration.getEndpoint() != null) {
                endpointUri = endpointConfiguration.getEndpoint().getEndpointUri();
            } else {
                throw new CitrusRuntimeException("Missing endpoint or endpointUri on Camel consumer");
            }

            throw new ReplyMessageTimeoutException(timeout, endpointUri);
        }

        return message;
    }

    /**
     * Sets the correlation manager.
     * @param correlationManager
     */
    public void setCorrelationManager(CorrelationManager<Message> correlationManager) {
        this.correlationManager = correlationManager;
    }
}
