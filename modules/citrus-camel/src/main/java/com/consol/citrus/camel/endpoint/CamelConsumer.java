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
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.report.MessageListeners;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class CamelConsumer implements Consumer {
    /** Endpoint configuration */
    private final CamelEndpointConfiguration endpointConfiguration;

    /** Message listener  */
    private final MessageListeners messageListener;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CamelConsumer.class);

    /**
     * Constructor using endpoint configuration and fields.
     * @param endpointConfiguration
     * @param messageListener
     */
    public CamelConsumer(CamelEndpointConfiguration endpointConfiguration, MessageListeners messageListener) {
        this.endpointConfiguration = endpointConfiguration;
        this.messageListener = messageListener;
    }

    @Override
    public Message<?> receive() {
        return receive(endpointConfiguration.getTimeout());
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

        return message;
    }

    /**
     * Informs message listeners if present.
     * @param receivedMessage
     */
    protected void onInboundMessage(Message<?> receivedMessage) {
        if (messageListener != null) {
            messageListener.onInboundMessage((receivedMessage != null ? receivedMessage.toString() : ""));
        } else {
            log.debug("Received message is:" + System.getProperty("line.separator") + (receivedMessage != null ? receivedMessage.toString() : ""));
        }
    }

    /**
     * Gets the message listeners.
     * @return
     */
    public MessageListeners getMessageListener() {
        return messageListener;
    }
}
