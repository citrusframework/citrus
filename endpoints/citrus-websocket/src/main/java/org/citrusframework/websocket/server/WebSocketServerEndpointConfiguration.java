/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.websocket.server;

import org.citrusframework.websocket.endpoint.AbstractWebSocketEndpointConfiguration;
import org.citrusframework.websocket.handler.CitrusWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web socket endpoint configuration for server side web socket communication.
 * @author Martin Maher
 * @since 2.3
 */
public class WebSocketServerEndpointConfiguration extends AbstractWebSocketEndpointConfiguration {
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(WebSocketServerEndpointConfiguration.class);

    /** Web socket handler */
    private CitrusWebSocketHandler handler;

    @Override
    public CitrusWebSocketHandler getHandler() {
        return handler;
    }

    @Override
    public void setHandler(CitrusWebSocketHandler handler) {
        if (this.handler != null) {
            logger.warn(String.format("Handler already set for Web Socket endpoint (path='%s'). " +
                    "Check configuration to ensure that the Web Socket endpoint is not being used by multiple http-servers", getEndpointUri()));
        }
        this.handler = handler;
    }
}
