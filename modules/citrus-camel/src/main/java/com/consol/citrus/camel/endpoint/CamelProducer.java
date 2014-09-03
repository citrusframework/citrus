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

import com.consol.citrus.messaging.Producer;
import com.consol.citrus.report.MessageListeners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class CamelProducer implements Producer {
    /** Endpoint configuration */
    private final CamelEndpointConfiguration endpointConfiguration;

    /** Message listener  */
    private final MessageListeners messageListener;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CamelProducer.class);

    /**
     * Constructor using endpoint configuration and fields.
     * @param endpointConfiguration
     * @param messageListener
     */
    public CamelProducer(CamelEndpointConfiguration endpointConfiguration, MessageListeners messageListener) {
        this.endpointConfiguration = endpointConfiguration;
        this.messageListener = messageListener;
    }

    @Override
    public void send(Message<?> message) {
        log.info("Sending message to camel endpoint: '" + endpointConfiguration.getEndpointUri() + "'");

        endpointConfiguration.getCamelContext().createProducerTemplate()
                .sendBodyAndHeaders(endpointConfiguration.getEndpointUri(), message.getPayload(), message.getHeaders());

        onOutboundMessage(message);

        log.info("Message was successfully sent to camel endpoint '" + endpointConfiguration.getEndpointUri() + "'");
    }

    /**
     * Informs message listeners if present.
     * @param message
     */
    protected void onOutboundMessage(Message<?> message) {
        if (messageListener != null) {
            messageListener.onOutboundMessage(message.toString());
        } else {
            log.info("Sent message is:" + System.getProperty("line.separator") + message.toString());
        }
    }

    /**
     * Gets the message listeners on this producer.
     * @return
     */
    public MessageListeners getMessageListener() {
        return messageListener;
    }
}
