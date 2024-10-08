/*
 * Copyright the original author or authors.
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

package org.citrusframework.websocket.handler;

import org.springframework.context.Lifecycle;
import org.springframework.web.socket.server.support.WebSocketHandlerMapping;

import java.util.Map;

/**
 * @since 2.3
 */
public class WebSocketUrlHandlerMapping extends WebSocketHandlerMapping {

    /**
     * Workaround for registering the WebSocket request handlers, after the spring context has been
     * initialised.
     *
     * @param wsHandlers
     */
    public void postRegisterUrlHandlers(Map<String, Object> wsHandlers) {
        registerHandlers(wsHandlers);

        for (Object handler : wsHandlers.values()) {
            if (handler instanceof Lifecycle) {
                ((Lifecycle) handler).start();
            }
        }
    }
}
