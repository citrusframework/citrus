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

package com.consol.citrus.http.socket.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.*;

/**
 * @author Martin Maher
 * @since 2.2.1
 * TODO MM document me
 */
public class CitrusWebSocketHandler extends AbstractWebSocketHandler {
    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(CitrusWebSocketHandler.class);

    private final Queue<AbstractWebSocketMessage<?>> messages = new LinkedList<>();

    private final Map<String, WebSocketSession> sessions = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        LOG.debug(String.format("(%s) connection established", session.getId()));
        sessions.put(session.getId(), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        LOG.debug(String.format("(%s) received text message", session.getId()));
        messages.add(message);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        LOG.debug(String.format("(%s) received binary message", session.getId()));
        messages.add(message);
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        LOG.debug(String.format("(%s) received pong message", session.getId()));
        messages.add(message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        LOG.error(String.format("(%s) WebSocket transport error", session.getId()), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        LOG.debug(String.format("(%s) session closed - status : %s", session.getId(), status));
        sessions.remove(session.getId());
    }

    public AbstractWebSocketMessage<?> getMessage() {
        return messages.poll();
    }

    public void sendMessage(AbstractWebSocketMessage<?> message) {
        for (WebSocketSession session : sessions.values()) {
            if (session != null && session.isOpen()) {
                try {
                    session.sendMessage(message);
                } catch (IOException e) {
                    LOG.error(String.format("(%s) error sending message", session.getId()), e);
                }
            }
        }
    }
}
