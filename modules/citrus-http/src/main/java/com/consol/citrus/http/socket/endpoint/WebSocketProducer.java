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

package com.consol.citrus.http.socket.endpoint;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.message.Message;
import com.consol.citrus.messaging.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.web.socket.TextMessage;

/**
 * @author Martin Maher
 * @since 2.2.1
 */
public class WebSocketProducer implements Producer {
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(WebSocketProducer.class);

    private final String name;
    private final WebSocketEndpointConfiguration endpointConfiguration;

    /**
     * Default constructor using endpoint configuration.
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

        log.info("Sending WebSocket message ...");

        TextMessage wsMessage = endpointConfiguration.getMessageConverter().convertOutbound(message, endpointConfiguration);
        endpointConfiguration.getHandler().sendMessage(wsMessage);
        context.onOutboundMessage(message);

        log.info("Web-Socket Message was successfully sent");
    }

    @Override
    public String getName() {
        return name;
    }
}
