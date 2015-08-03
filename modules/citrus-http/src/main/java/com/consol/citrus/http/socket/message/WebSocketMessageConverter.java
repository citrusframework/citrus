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

package com.consol.citrus.http.socket.message;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.socket.endpoint.WebSocketEndpointConfiguration;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageConverter;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.web.socket.*;
import org.springframework.web.socket.WebSocketMessage;

/**
 * Message converter able to convert internal and external message representations for web socket messages. Converter
 * converts inbound and outbound messages.
 * @author Martin Maher
 * @since 2.3
 */
public class WebSocketMessageConverter implements MessageConverter<WebSocketMessage, WebSocketEndpointConfiguration> {
    @Override
    public WebSocketMessage convertOutbound(Message internalMessage, WebSocketEndpointConfiguration endpointConfiguration) {
        WebSocketMessage webSocketMessage;
        Object payload = internalMessage.getPayload();
        boolean isLast = true;

        if (internalMessage.getHeader(WebSocketMessageHeaders.WEB_SOCKET_IS_LAST) != null) {
            isLast = Boolean.valueOf(internalMessage.getHeader(WebSocketMessageHeaders.WEB_SOCKET_IS_LAST).toString());
        }

        if (payload instanceof String) {
            webSocketMessage = new TextMessage(payload.toString(), isLast);
        } else if (payload instanceof byte[]) {
            webSocketMessage = new BinaryMessage((byte[]) payload, isLast);
        } else {
            try {
                webSocketMessage = new TextMessage(internalMessage.getPayload(String.class), isLast);
            } catch (ConversionNotSupportedException e) {
                throw new CitrusRuntimeException(String.format("Found unsupported payload type: '%s'", payload.getClass().getCanonicalName()), e);
            }
        }

        convertOutbound(webSocketMessage, internalMessage, endpointConfiguration);
        return webSocketMessage;
    }

    @Override
    public void convertOutbound(WebSocketMessage externalMessage, Message internalMessage, WebSocketEndpointConfiguration endpointConfiguration) {
    }

    @Override
    public Message convertInbound(WebSocketMessage externalMessage, WebSocketEndpointConfiguration endpointConfiguration) {
        return new com.consol.citrus.http.socket.message.WebSocketMessage(externalMessage);
    }
}
