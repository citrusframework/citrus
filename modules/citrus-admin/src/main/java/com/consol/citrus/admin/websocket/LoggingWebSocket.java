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

import com.consol.citrus.TestAction;
import com.consol.citrus.TestCase;
import com.consol.citrus.report.TestActionListener;
import com.consol.citrus.report.TestListener;
import com.consol.citrus.report.TestSuiteListener;
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
public class LoggingWebSocket implements WebSocket.OnTextMessage, ProcessListener, TestListener, TestSuiteListener, TestActionListener {

    /** Log event types */
    private enum PushEvent {
        PING,
        LOG_MESSAGE,
        TEST_START,
        TEST_SUCCESS,
        TEST_FAILED,
        TEST_SKIP,
        TEST_ACTION_START,
        TEST_ACTION_FINISH,
        TEST_ACTION_SKIP,
        PROCESS_START,
        PROCESS_SUCCESS,
        PROCESS_FAILED;
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
     * {@inheritDoc}
     */
    public void start(String processId) {
        push(createProcessEvent(processId, PushEvent.PROCESS_START, "process started"));
    }

    /**
     * {@inheritDoc}
     */
    public void success(String processId) {
        push(createProcessEvent(processId, PushEvent.PROCESS_SUCCESS, "process completed successfully"));
    }

    /**
     * {@inheritDoc}
     */
    public void fail(String processId, int exitCode) {
        push(createProcessEvent(processId, PushEvent.PROCESS_FAILED, "process failed with exit code " + exitCode));
    }

    /**
     * {@inheritDoc}
     */
    public void fail(String processId, Throwable e) {
        push(createProcessEvent(processId, PushEvent.PROCESS_FAILED, "process failed with exception " + e.getLocalizedMessage()));
    }

    /**
     * {@inheritDoc}
     */
    public void output(String processId, String output) {
        push(createProcessEvent(processId, PushEvent.LOG_MESSAGE, output));
    }

    /**
     * {@inheritDoc}
     */
    public void onTestActionStart(TestCase testCase, TestAction testAction) {
        push(createTestEvent(PushEvent.TEST_ACTION_START, testAction.getName()));
    }

    /**
     * {@inheritDoc}
     */
    public void onTestActionFinish(TestCase testCase, TestAction testAction) {
        push(createTestEvent(PushEvent.TEST_ACTION_FINISH, testAction.getName()));
    }

    /**
     * {@inheritDoc}
     */
    public void onTestActionSkipped(TestCase testCase, TestAction testAction) {
        push(createTestEvent(PushEvent.TEST_ACTION_SKIP, testAction.getName()));
    }

    /**
     * {@inheritDoc}
     */
    public void onTestStart(TestCase test) {
        push(createTestEvent(PushEvent.TEST_START, test.getName()));
    }

    /**
     * {@inheritDoc}
     */
    public void onTestFinish(TestCase test) {
    }

    /**
     * {@inheritDoc}
     */
    public void onTestSuccess(TestCase test) {
        push(createTestEvent(PushEvent.TEST_SUCCESS, test.getName()));
    }

    /**
     * {@inheritDoc}
     */
    public void onTestFailure(TestCase test, Throwable cause) {
        push(createTestEvent(PushEvent.TEST_FAILED, test.getName()));
    }

    /**
     * {@inheritDoc}
     */
    public void onTestSkipped(TestCase test) {
        push(createTestEvent(PushEvent.TEST_SKIP, test.getName()));
    }

    /**
     * {@inheritDoc}
     */
    public void onStart() {
        start("");
    }

    /**
     * {@inheritDoc}
     */
    public void onStartSuccess() {
    }

    /**
     * {@inheritDoc}
     */
    public void onStartFailure(Throwable cause) {
    }

    /**
     * {@inheritDoc}
     */
    public void onFinish() {
    }

    /**
     * {@inheritDoc}
     */
    public void onFinishSuccess() {
        success("0");
    }

    /**
     * {@inheritDoc}
     */
    public void onFinishFailure(Throwable cause) {
        fail("0", cause);
    }

    /**
     * Send ping event.
     */
    @SuppressWarnings("unchecked")
    public void ping() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("event", PushEvent.PING.name());
        push(jsonObject);
    }

    /**
     * Creates proper JSON message for process related log event.
     * @param processId
     * @param pushEvent
     * @param message
     * @return
     */
    @SuppressWarnings("unchecked")
    private JSONObject createProcessEvent(String processId, PushEvent pushEvent, String message) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("processId", processId);
        jsonObject.put("event", pushEvent.name());
        jsonObject.put("msg", message);
        return jsonObject;
    }

    /**
     * Creates proper JSON message for test related log event.
     * @param pushEvent
     * @param message
     * @return
     */
    @SuppressWarnings("unchecked")
    private JSONObject createTestEvent(PushEvent pushEvent, String message) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("event", pushEvent.name());
        jsonObject.put("msg", message);
        return jsonObject;
    }

    /**
     * Push event to connected clients.
     * @param event
     */
    @SuppressWarnings({"PMD.CloseResource"})
    private void push(JSONObject event) {
        Iterator<Connection> itor = connections.iterator();
        while (itor.hasNext()) {
            Connection connection = itor.next();
            if (connection != null && connection.isOpen()) {
                try {
                    connection.sendMessage(event.toString());
                } catch (IOException e) {
                    LOG.error("Error sending message", e);
                }
            } else {
                itor.remove();
            }
        }
    }
}
