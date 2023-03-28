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

import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;

import java.util.Map;

/**
 * Message representing web socket message data.
 *
 * @author Martin Maher
 * @since 2.3
 */
public class WebSocketMessage extends DefaultMessage {

    /**
     * Empty constructor initializing with empty message payload.
     */
    public WebSocketMessage() {
        super();
    }

    /**
     * Constructs copy of given message.
     * @param message
     */
    public WebSocketMessage(Message message) {
        super(message);
    }

    /**
     * Default message using message payload.
     * @param payload
     */
    public WebSocketMessage(Object payload) {
        super(payload);
    }

    /**
     * Default message using message payload and headers.
     * @param payload
     * @param headers
     */
    public WebSocketMessage(Object payload, Map<String, Object> headers) {
        super(payload, headers);
    }

    /**
     * Adds isLast message header.
     * @param last
     * @return
     */
    public WebSocketMessage last(boolean last) {
        setHeader(WebSocketMessageHeaders.WEB_SOCKET_IS_LAST, last);
        return this;
    }

    /**
     * Gets the isLast flag from message headers.
     * @return
     */
    public boolean isLast() {
        Object isLast = getHeader(WebSocketMessageHeaders.WEB_SOCKET_IS_LAST);

        if (isLast != null) {
            if (isLast instanceof String) {
                return Boolean.valueOf(isLast.toString());
            } else {
                return (Boolean) isLast;
            }
        }

        return true;
    }

}
