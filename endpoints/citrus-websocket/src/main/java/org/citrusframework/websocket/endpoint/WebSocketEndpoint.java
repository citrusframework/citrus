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

import org.citrusframework.endpoint.AbstractEndpoint;
import org.citrusframework.websocket.handler.CitrusWebSocketHandler;
import org.citrusframework.messaging.Producer;
import org.citrusframework.messaging.SelectiveConsumer;

/**
 * Endpoint constructs web socket consumer and producer with given endpoint configuration.
 * @author Martin Maher
 * @since 2.3
 */
public class WebSocketEndpoint extends AbstractEndpoint {
    /**
     * Cached producer or consumer
     */
    private WebSocketProducer wsProducer;
    private WebSocketConsumer wsConsumer;

    /**
     * Default constructor using endpoint configuration.
     *
     * @param endpointConfiguration
     */
    public WebSocketEndpoint(WebSocketEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
    }

    @Override
    public SelectiveConsumer createConsumer() {
        if (wsConsumer == null) {
            wsConsumer = new WebSocketConsumer(getConsumerName(), getEndpointConfiguration());
        }

        return wsConsumer;
    }

    @Override
    public Producer createProducer() {
        if (wsProducer == null) {
            wsProducer = new WebSocketProducer(getProducerName(), getEndpointConfiguration());
        }

        return wsProducer;
    }

    @Override
    public WebSocketEndpointConfiguration getEndpointConfiguration() {
        return (WebSocketEndpointConfiguration) super.getEndpointConfiguration();
    }

    /**
     * Sets the web socket handler.
     * @param handler
     */
    public void setWebSocketHandler(CitrusWebSocketHandler handler) {
        getEndpointConfiguration().setHandler(handler);
    }
}
