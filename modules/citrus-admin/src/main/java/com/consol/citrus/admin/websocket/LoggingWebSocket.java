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

import java.io.IOException;
import java.util.*;

import org.eclipse.jetty.websocket.WebSocket;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.admin.launcher.ProcessListener;

/**
 * Used for publishing log messages to connected clients via the web socket api.
 *
 * @author Martin.Maher@consol.de
 * @since 2013.01.28
 */
public class LoggingWebSocket implements WebSocket.OnTextMessage, ProcessListener {

    /** Log event types */
    private enum LogEvent {
        PING,
        START,
        MESSAGE,
        SUCCESS,
        FAILED;
    }

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(LoggingWebSocket.class);

    /**
     * Web Socket connections
     * TODO MM thread safe
     */
    private List<Connection> connections = new ArrayList<Connection>();

    /**
     * Default constructor.
     */
    public LoggingWebSocket() {
        Timer timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                LOG.debug("Pinging client to keep connection alive");
                ping();
            }
        };
        timer.schedule(task, 60000, 60000);
    }

    /**
     * {@inheritDoc}
     */
    public void onOpen(Connection connection) {
        LOG.info("Accepted a new connection");
        this.connections.add(connection);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"PMD.CloseResource"})
    public void onClose(int closeCode, String message) {
        LOG.debug("Web socket connection closed");
        Iterator<Connection> itor = connections.iterator();
        while (itor.hasNext()) {
            Connection connection = itor.next();
            if (connection == null || !connection.isOpen()) {
                itor.remove();
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    public void onMessage(String data) {
        LOG.info("Received web socket client message: " + data);
    }

    /**
     * Invoked on start process event
     *
     * @param processId the id of the process
     */
    public void start(String processId) {
        logMessage(createMessage(processId, LogEvent.START, "process started"));
    }

    /**
     * Invoked on successful completion event
     *
     * @param processId the id of the completed process
     */
    public void success(String processId) {
        logMessage(createMessage(processId, LogEvent.SUCCESS, "process completed successfully"));
    }

    /**
     * Invoked on failed completion event, with the process exit code
     *
     * @param processId the id of the process
     * @param exitCode the exitcode returned from the process
     */
    public void fail(String processId, int exitCode) {
        logMessage(createMessage(processId, LogEvent.FAILED, "process failed with exit code " + exitCode));
    }

    /**
     * Invoked on failed completion event, with the exception that was caught
     *
     * @param processId the id of the process
     * @param e the exception caught within the ProcessLauncher
     */
    public void fail(String processId, Exception e) {
        logMessage(createMessage(processId, LogEvent.FAILED, "process failed with exception " + e.getLocalizedMessage()));
    }

    /**
     * Invoked on output message event with output data from process
     *
     * @param processId the id of the process
     * @param output
     */
    public void output(String processId, String output) {
        logMessage(createMessage(processId, LogEvent.MESSAGE, output));
    }

    /**
     * Send ping event.
     */
    @SuppressWarnings("unchecked")
    public void ping() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("event", LogEvent.PING.name());
        logMessage(jsonObject.toString());
    }

    /**
     * Creates proper JSON message for log event.
     * @param processId
     * @param logEvent
     * @param message
     * @return
     */
    @SuppressWarnings("unchecked")
    private String createMessage(String processId, LogEvent logEvent, String message) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("processId", processId);
        jsonObject.put("event", logEvent.name());
        jsonObject.put("msg", message);
        return jsonObject.toString();
    }

    /**
     * Push log message to connected clients.
     * @param message
     */
    @SuppressWarnings({"PMD.CloseResource"})
    private void logMessage(String message) {
        Iterator<Connection> itor = connections.iterator();
        while (itor.hasNext()) {
            Connection connection = itor.next();
            if (connection != null && connection.isOpen()) {
                try {
                    connection.sendMessage(message);
                } catch (IOException e) {
                    LOG.error("Error sending message", e);
                }
            } else {
                itor.remove();
            }
        }
    }
}
