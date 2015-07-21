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
import com.consol.citrus.context.TestContext;
import com.consol.citrus.message.Message;
import com.consol.citrus.report.*;
import net.minidev.json.JSONObject;
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

    private String processId = "";  //TODO refactor message listener instead of storing processId of last executed test

    @Override
    public void onTestActionStart(TestCase test, TestAction testAction) {
        loggingWebSocket.push(SocketEvent.createEvent(test.getName(), SocketEvent.TEST_ACTION_START, testAction.getName()));
    }

    @Override
    public void onTestActionFinish(TestCase test, TestAction testAction) {
        long totalActions = test.getActionCount();
        int currentAction = test.getActionIndex(testAction) + 1;

        long progressValue = Math.round((Double.valueOf(currentAction) / Double.valueOf(totalActions)) * 100);

        JSONObject event = SocketEvent.createEvent(test.getName(), SocketEvent.TEST_ACTION_FINISH,
                "TEST ACTION " + currentAction + "/" + totalActions);
        event.put("progress", String.valueOf(progressValue));
        loggingWebSocket.push(event);
    }

    @Override
    public void onTestActionSkipped(TestCase test, TestAction testAction) {
        loggingWebSocket.push(SocketEvent.createEvent(test.getName(), SocketEvent.TEST_ACTION_SKIP, testAction.getName()));
    }

    @Override
    public void onTestStart(TestCase test) {
        processId = test.getName();
        loggingWebSocket.push(SocketEvent.createEvent(test.getName(), SocketEvent.PROCESS_START, "process started"));
        loggingWebSocket.push(SocketEvent.createEvent(test.getName(), SocketEvent.TEST_START, test.getName()));
    }

    @Override
    public void onTestSuccess(TestCase test) {
        loggingWebSocket.push(SocketEvent.createEvent(test.getName(), SocketEvent.TEST_SUCCESS, test.getName()));
        loggingWebSocket.push(SocketEvent.createEvent(test.getName(), SocketEvent.PROCESS_SUCCESS, "process completed successfully"));
    }

    @Override
    public void onTestFailure(TestCase test, Throwable cause) {
        loggingWebSocket.push(SocketEvent.createEvent(test.getName(), SocketEvent.TEST_FAILED, test.getName()));
        loggingWebSocket.push(SocketEvent.createEvent(test.getName(), SocketEvent.PROCESS_FAILED, "process failed with exception " + cause.getLocalizedMessage()));
    }

    @Override
    public void onTestSkipped(TestCase test) {
        loggingWebSocket.push(SocketEvent.createEvent(test.getName(), SocketEvent.TEST_SKIP, test.getName()));
    }

    @Override
    public void onTestFinish(TestCase test) {
        loggingWebSocket.push(SocketEvent.createEvent(test.getName(), SocketEvent.TEST_FINISHED, test.getName()));
    }

    @Override
    public void onInboundMessage(Message message, TestContext context) {
        loggingWebSocket.push(SocketEvent.createEvent(processId, SocketEvent.INBOUND_MESSAGE, message.toString()));
    }

    @Override
    public void onOutboundMessage(Message message, TestContext context) {
        loggingWebSocket.push(SocketEvent.createEvent(processId, SocketEvent.OUTBOUND_MESSAGE, message.toString()));
    }

}
