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

package org.citrusframework.websocket.message;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.websocket.endpoint.WebSocketEndpointConfiguration;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageConverter;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;

import java.nio.ByteBuffer;

/**
 * Message converter able to convert internal and external message representations for web socket messages. Converter
 * converts inbound and outbound messages.
 * @author Martin Maher
 * @since 2.3
 */
public class WebSocketMessageConverter implements MessageConverter<org.springframework.web.socket.WebSocketMessage, org.springframework.web.socket.WebSocketMessage, WebSocketEndpointConfiguration> {

    @Override
    public org.springframework.web.socket.WebSocketMessage convertOutbound(Message message, WebSocketEndpointConfiguration endpointConfiguration, TestContext context) {
        WebSocketMessage internalMessage;
        if (message instanceof WebSocketMessage) {
            internalMessage = (WebSocketMessage) message;
        } else {
            internalMessage = new WebSocketMessage(message);
        }

        org.springframework.web.socket.WebSocketMessage webSocketMessage;
        Object payload = internalMessage.getPayload();
        if (payload instanceof String) {
            webSocketMessage = new TextMessage(payload.toString(), internalMessage.isLast());
        } else if (payload instanceof ByteBuffer) {
            webSocketMessage = new BinaryMessage((ByteBuffer) payload, internalMessage.isLast());
        } else if (payload instanceof byte[]) {
            webSocketMessage = new BinaryMessage((byte[]) payload, internalMessage.isLast());
        } else {
            try {
                webSocketMessage = new TextMessage(internalMessage.getPayload(String.class), internalMessage.isLast());
            } catch (ConversionNotSupportedException e) {
                throw new CitrusRuntimeException(String.format("Found unsupported payload type: '%s'", payload.getClass().getCanonicalName()), e);
            }
        }

        convertOutbound(webSocketMessage, internalMessage, endpointConfiguration, context);
        return webSocketMessage;
    }

    @Override
    public void convertOutbound(org.springframework.web.socket.WebSocketMessage externalMessage, Message internalMessage, WebSocketEndpointConfiguration endpointConfiguration, TestContext context) {
    }

    @Override
    public Message convertInbound(org.springframework.web.socket.WebSocketMessage externalMessage, WebSocketEndpointConfiguration endpointConfiguration, TestContext context) {
        return new WebSocketMessage(externalMessage.getPayload())
                        .last(externalMessage.isLast());
    }
}
