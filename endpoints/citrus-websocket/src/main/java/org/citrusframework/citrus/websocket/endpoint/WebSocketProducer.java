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

package org.citrusframework.citrus.websocket.endpoint;

import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.message.Message;
import org.citrusframework.citrus.messaging.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.web.socket.WebSocketMessage;

/**
 * Producer sends web socket messages to all open sessions known to the web socket handler.
 * @author Martin Maher
 * @since 2.3
 */
public class WebSocketProducer implements Producer {
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketProducer.class);

    private final String name;
    private final WebSocketEndpointConfiguration endpointConfiguration;

    /**
     * Default constructor using endpoint configuration.
     *
     * @param name
     * @param endpointConfiguration
     */
    public WebSocketProducer(String name, WebSocketEndpointConfiguration endpointConfiguration) {
        this.name = name;
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public void send(Message message, TestContext context) {
        Assert.notNull(message, "Message is empty - unable to send empty message");

        LOG.info("Sending WebSocket message ...");

        context.onOutboundMessage(message);

        WebSocketMessage wsMessage = endpointConfiguration.getMessageConverter().convertOutbound(message, endpointConfiguration, context);
        if (endpointConfiguration.getHandler().sendMessage(wsMessage)) {
            LOG.info("WebSocket Message was successfully sent");
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
