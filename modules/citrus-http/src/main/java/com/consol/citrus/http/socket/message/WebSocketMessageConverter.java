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
import org.springframework.web.socket.AbstractWebSocketMessage;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;

/**
 * @author Martin Maher
 * @since 2.2.1
 */
public class WebSocketMessageConverter implements MessageConverter<AbstractWebSocketMessage, WebSocketEndpointConfiguration> {
    @Override
    public AbstractWebSocketMessage convertOutbound(Message internalMessage, WebSocketEndpointConfiguration endpointConfiguration) {
        Object payload = internalMessage.getPayload();
        if(payload instanceof String) {
            return new TextMessage(payload.toString());
        } else if (payload instanceof byte[]) {
            return new BinaryMessage((byte[])payload);
        }
        else {
            throw new CitrusRuntimeException(String.format("Invalid payload - unsupported type: '%s'", payload.getClass().getCanonicalName()));
        }
    }

    @Override
    public void convertOutbound(AbstractWebSocketMessage externalMessage, Message internalMessage, WebSocketEndpointConfiguration endpointConfiguration) {
        throw new CitrusRuntimeException("Not supported");
    }

    @Override
    public Message convertInbound(AbstractWebSocketMessage externalMessage, WebSocketEndpointConfiguration endpointConfiguration) {
        return new WebSocketMessage(externalMessage);
    }
}
