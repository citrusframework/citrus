/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.websocket.endpoint;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.AbstractSelectiveMessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketMessage;

/**
 * Consumer polls for incoming messages on web socket handler.
 * @author Martin Maher
 * @since 2.3
 */
public class WebSocketConsumer extends AbstractSelectiveMessageConsumer {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConsumer.class);

    /**
     * Endpoint configuration
     */
    private final WebSocketEndpointConfiguration endpointConfiguration;

    /**
     * Default constructor using receive timeout setting.
     *
     * @param name
     * @param endpointConfiguration
     */
    public WebSocketConsumer(String name, WebSocketEndpointConfiguration endpointConfiguration) {
        super(name, endpointConfiguration);
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public Message receive(String selector, TestContext context, long timeout) {
        logger.info(String.format("Waiting %s ms for Web Socket message ...", timeout));

        WebSocketMessage<?> message = receive(endpointConfiguration, timeout);
        Message receivedMessage = endpointConfiguration.getMessageConverter().convertInbound(message, endpointConfiguration, context);

        logger.info("Received Web Socket message");
        context.onInboundMessage(receivedMessage);

        return receivedMessage;
    }

    /**
     * Receive web socket message by polling on web socket handler for incoming message.
     * @param config
     * @param timeout
     * @return
     */
    private WebSocketMessage<?> receive(WebSocketEndpointConfiguration config, long timeout) {
        long timeLeft = timeout;

        WebSocketMessage<?> message = config.getHandler().getMessage();
        String endpointUri = endpointConfiguration.getEndpointUri();
        while (message == null && timeLeft > 0) {
            timeLeft -= endpointConfiguration.getPollingInterval();
            long sleep = timeLeft > 0 ? endpointConfiguration.getPollingInterval() : endpointConfiguration.getPollingInterval() + timeLeft;
            if (logger.isDebugEnabled()) {
                String msg = "Waiting for message on '%s' - retrying in %s ms";
                logger.debug(String.format(msg, endpointUri, (sleep)));
            }

            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                logger.warn(String.format("Thread interrupted while waiting for message on '%s'", endpointUri), e);
            }

            message = config.getHandler().getMessage();
        }

        if (message == null) {
            throw new MessageTimeoutException(timeout, endpointUri);
        }
        return message;
    }
}
