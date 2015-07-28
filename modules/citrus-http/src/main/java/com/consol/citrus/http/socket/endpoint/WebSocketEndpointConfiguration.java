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

import com.consol.citrus.endpoint.AbstractEndpointConfiguration;
import com.consol.citrus.http.socket.handler.CitrusWebSocketHandler;
import com.consol.citrus.http.socket.message.WebSocketMessageConverter;

/**
 * @author Martin Maher
 * @since 2.3
 */
public abstract class WebSocketEndpointConfiguration extends AbstractEndpointConfiguration {
    private String endpointUri;

    /**
     * The message converter
     */
    private WebSocketMessageConverter messageConverter = new WebSocketMessageConverter();

    /**
     * Gets the message converter.
     *
     * @return
     */
    public WebSocketMessageConverter getMessageConverter() {
        return messageConverter;
    }

    /**
     * Sets the message converter.
     *
     * @param messageConverter
     */
    public void setMessageConverter(WebSocketMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    public abstract CitrusWebSocketHandler getHandler();

    public abstract void setHandler(CitrusWebSocketHandler handler);

    public String getEndpointUri() {
        return endpointUri;
    }

    public void setEndpointUri(String endpointUri) {
        this.endpointUri = endpointUri;
    }
}
