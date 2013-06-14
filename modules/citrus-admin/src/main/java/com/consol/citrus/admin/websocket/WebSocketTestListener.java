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

import com.consol.citrus.TestAction;
import com.consol.citrus.TestCase;
import com.consol.citrus.report.MessageListener;
import com.consol.citrus.report.TestActionListener;
import com.consol.citrus.report.TestListener;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test event listener implementation pushes incoming notifications
 * to connected web socket clients.
 *
 * @author Christoph Deppisch
 */
public class WebSocketTestListener implements TestListener, TestActionListener, MessageListener {

    @Autowired
    private LoggingWebSocket loggingWebSocket;

    /**
     * {@inheritDoc}
     */
    public void onTestActionStart(TestCase testCase, TestAction testAction) {
        loggingWebSocket.push(SocketEvent.createEvent(testCase.getName(), SocketEvent.TEST_ACTION_START, testAction.getName()));
    }

    /**
     * {@inheritDoc}
     */
    public void onTestActionFinish(TestCase testCase, TestAction testAction) {
        loggingWebSocket.push(SocketEvent.createEvent(testCase.getName(), SocketEvent.TEST_ACTION_FINISH, testAction.getName()));
    }

    /**
     * {@inheritDoc}
     */
    public void onTestActionSkipped(TestCase testCase, TestAction testAction) {
        loggingWebSocket.push(SocketEvent.createEvent(testCase.getName(), SocketEvent.TEST_ACTION_SKIP, testAction.getName()));
    }

    /**
     * {@inheritDoc}
     */
    public void onTestStart(TestCase test) {
        loggingWebSocket.push(SocketEvent.createEvent(test.getName(), SocketEvent.TEST_START, test.getName()));
    }

    /**
     * {@inheritDoc}
     */
    public void onTestSuccess(TestCase test) {
        loggingWebSocket.push(SocketEvent.createEvent(test.getName(), SocketEvent.TEST_SUCCESS, test.getName()));
    }

    /**
     * {@inheritDoc}
     */
    public void onTestFailure(TestCase test, Throwable cause) {
        loggingWebSocket.push(SocketEvent.createEvent(test.getName(), SocketEvent.TEST_FAILED, test.getName()));
    }

    /**
     * {@inheritDoc}
     */
    public void onTestSkipped(TestCase test) {
        loggingWebSocket.push(SocketEvent.createEvent(test.getName(), SocketEvent.TEST_SKIP, test.getName()));
    }

    /**
     * {@inheritDoc}
     */
    public void onTestFinish(TestCase test) {
    }

    /**
     * {@inheritDoc}
     */
    public void onInboundMessage(String message) {
        loggingWebSocket.push(SocketEvent.createEvent("INBOUND", SocketEvent.INBOUND_MESSAGE, message));
    }

    /**
     * {@inheritDoc}
     */
    public void onOutboundMessage(String message) {
        loggingWebSocket.push(SocketEvent.createEvent("OUTBOUND", SocketEvent.OUTBOUND_MESSAGE, message));
    }
}
