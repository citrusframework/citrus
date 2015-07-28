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

import com.consol.citrus.http.socket.handler.CitrusWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Maher
 * @since 2.2.1
 */
public class WebSocketServerEndpointConfiguration extends WebSocketEndpointConfiguration {
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketServerEndpointConfiguration.class);

    private CitrusWebSocketHandler handler;

    public CitrusWebSocketHandler getHandler() {
        return handler;
    }

    public void setHandler(CitrusWebSocketHandler handler) {
        if (this.handler != null) {
            LOG.warn(String.format("Handler already set for Web Socket endpoint (path='%s'). Check configuration to ensure that the Web Socket endpoint is not being used by multiple http-servers", getEndpointUri()));
        }
        this.handler = handler;
    }
}
