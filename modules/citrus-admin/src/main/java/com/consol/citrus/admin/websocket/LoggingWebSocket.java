/*
 * Copyright 2006-2013 the original author or authors.
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

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Used for publishing log messages to connected clients via the web socket api.
 *
 * @author Martin.Maher@consol.de
 * @since 1.3
 */
@WebSocket
public class LoggingWebSocket {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(LoggingWebSocket.class);

    /**
     * Web Socket sessions
     */
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());

    /**
     * Default constructor.
     */
    public LoggingWebSocket() {
        Timer timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                ping();
            }
        };
        timer.schedule(task, 60000, 60000);
    }

    @SuppressWarnings("unused")
    @OnWebSocketConnect
    public void handleConnect(Session session) {
        LOG.info("Accepted a new session");
        sessions.add(session);
    }

    @SuppressWarnings({"PMD.CloseResource", "unused"})
    @OnWebSocketClose
    public void handleClose(int statusCode, String reason) {
        LOG.info(String.format("Closing session (%s:%s)", statusCode, reason));
        for (Session session : sessions) {
            if (session == null || !session.isOpen()) {
                sessions.remove(session);
            }
        }
    }

    @SuppressWarnings("unused")
    @OnWebSocketMessage
    public void handleMessage(String message) {
        LOG.info(String.format("Received message from client: %s", message));
    }

    @SuppressWarnings("unused")
    @OnWebSocketError
    public void handleError(Throwable error) {
        LOG.warn("Error in websocket", error);
    }

    /**
     * Send ping event.
     */
    @SuppressWarnings("unchecked")
    private void ping() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("event", SocketEvent.PING.name());
        push(jsonObject);
    }


    /**
     * Push event to connected clients.
     *
     * @param event the event to send to the connected clients
     */
    @SuppressWarnings({"PMD.CloseResource"})
    protected void push(JSONObject event) {
        for (Session session : sessions) {
            sendToSession(session, event);
        }
    }

    private void sendToSession(Session session, JSONObject event) {
        try {
            if (session != null && session.isOpen()) {
                session.getRemote().sendString(event.toString());
            }
        } catch (IOException e) {
            LOG.error("Error sending message", e);
        }
    }

}
