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

import com.consol.citrus.messaging.Producer;
import com.consol.citrus.report.MessageListeners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.util.Assert;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class JmsProducer implements Producer {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JmsProducer.class);

    /** Endpoint configuration */
    private final JmsEndpointConfiguration endpointConfiguration;

    /** Message listener */
    private final MessageListeners messageListener;

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     * @param messageListener
     */
    public JmsProducer(JmsEndpointConfiguration endpointConfiguration, MessageListeners messageListener) {
        this.endpointConfiguration = endpointConfiguration;
        this.messageListener = messageListener;
    }

    @Override
    public void send(Message<?> message) {
        Assert.notNull(message, "Message is empty - unable to send empty message");

        String defaultDestinationName = endpointConfiguration.getDefaultDestinationName();

        log.info("Sending JMS message to destination: '" + defaultDestinationName + "'");

        endpointConfiguration.getJmsTemplate().convertAndSend(message);

        onOutboundMessage(message);

        log.info("Message was successfully sent to destination: '" + defaultDestinationName + "'");
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
     * Gets the message listener.
     * @return
     */
    public MessageListeners getMessageListener() {
        return messageListener;
    }
}
