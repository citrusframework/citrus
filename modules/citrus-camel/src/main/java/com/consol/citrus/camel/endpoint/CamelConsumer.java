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
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.message.Message;
import com.consol.citrus.report.MessageListeners;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class CamelConsumer implements Consumer {
    /** Endpoint configuration */
    private final CamelEndpointConfiguration endpointConfiguration;

    /** Message listener  */
    private final MessageListeners messageListener;

    /** The consumer name */
    private final String name;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CamelConsumer.class);

    /**
     * Constructor using endpoint configuration and fields.
     * @param name
     * @param endpointConfiguration
     * @param messageListener
     */
    public CamelConsumer(String name, CamelEndpointConfiguration endpointConfiguration, MessageListeners messageListener) {
        this.name = name;
        this.endpointConfiguration = endpointConfiguration;
        this.messageListener = messageListener;
    }

    @Override
    public Message receive(TestContext context) {
        return receive(context, endpointConfiguration.getTimeout());
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
        onInboundMessage(message, context);

        return message;
    }

    /**
     * Informs message listeners if present.
     * @param receivedMessage
     * @param context
     */
    protected void onInboundMessage(Message receivedMessage, TestContext context) {
        if (messageListener != null) {
            messageListener.onInboundMessage(receivedMessage, context);
        } else {
            log.debug("Received message is:" + System.getProperty("line.separator") + (receivedMessage != null ? receivedMessage.toString() : ""));
        }
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Gets the message listeners.
     * @return
     */
    public MessageListeners getMessageListener() {
        return messageListener;
    }
}
