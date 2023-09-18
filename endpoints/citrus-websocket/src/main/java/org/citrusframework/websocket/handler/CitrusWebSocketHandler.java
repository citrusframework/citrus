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

package org.citrusframework.websocket.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Web Socket Handler for handling incoming and sending outgoing Web Socket messages
 *
 * @author Martin Maher
 * @since 2.3
 */
public class CitrusWebSocketHandler extends AbstractWebSocketHandler {
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CitrusWebSocketHandler.class);

    /** Inbound message cache */
    private final Queue<WebSocketMessage<?>> inboundMessages = new LinkedList<>();

    /** Web socket sessions */
    private final Map<String, WebSocketSession> sessions = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.debug(String.format("WebSocket connection established (%s)", session.getId()));
        sessions.put(session.getId(), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.debug(String.format("WebSocket endpoint (%s) received text message", session.getId()));
        inboundMessages.add(message);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        logger.debug(String.format("WebSocket endpoint (%s) received binary message", session.getId()));
        inboundMessages.add(message);
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        logger.debug(String.format("WebSocket endpoint (%s) received pong message", session.getId()));
        inboundMessages.add(message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error(String.format("WebSocket transport error (%s)", session.getId()), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.debug(String.format("WebSocket session (%s) closed - status : %s", session.getId(), status));
        sessions.remove(session.getId());
    }

    /**
     * Polls message from internal cache.
     * @return
     */
    public WebSocketMessage<?> getMessage() {
        return inboundMessages.poll();
    }

    /**
     * Publish message to all sessions known to this handler.
     * @param message
     * @return
     */
    public boolean sendMessage(WebSocketMessage<?> message) {
        boolean sentSuccessfully = false;
        if (sessions.isEmpty()) {
            logger.warn("No Web Socket session exists - message cannot be sent");
        }

        for (WebSocketSession session : sessions.values()) {
            if (session != null && session.isOpen()) {
                try {
                    session.sendMessage(message);
                    sentSuccessfully = true;
                } catch (IOException e) {
                    logger.error(String.format("(%s) error sending message", session.getId()), e);
                }
            }
        }
        return sentSuccessfully;
    }
}
