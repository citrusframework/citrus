/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.admin.websocket;

import java.io.IOException;

import org.eclipse.jetty.websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.admin.listener.LoggingListener;

/**
 * WebSocket publishes logging messages to connected clients via web socket connection.
 * 
 * @author Christoph Deppisch
 * @since 1.3
 */
public class LoggingWebSocket implements WebSocket.OnTextMessage, LoggingListener {
    
    /** Web Socket connection */
    private Connection wsConnection;
    
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(LoggingWebSocket.class);

    /**
     * {@inheritDoc}
     */
    public void onOpen(Connection connection) {
        this.wsConnection = connection;
    }

    /**
     * {@inheritDoc}
     */
    public void onClose(int closeCode, String message) {
    }

    /**
     * {@inheritDoc}
     */
    public void onMessage(String data) {
        log.info("Received web socket client message: " + data);
    }
    
    /**
     * Send logging message to connected clients via web socket connection
     * @param message
     */
    public void onLoggingMessage(String message) {
        try {
            wsConnection.sendMessage(message);
        } catch (IOException e) {
            log.error("Failed to publish logging message via web socket connection", e);
        }
    }
}
