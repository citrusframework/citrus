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

import com.consol.citrus.admin.launcher.ProcessListener;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Process listener implementation pushes incoming listener notifications
 * to connected web socket clients.
 *
 * @author Christoph Deppisch
 */
public class WebSocketProcessListener implements ProcessListener {

    @Autowired
    private LoggingWebSocket loggingWebSocket;

    /**
     * {@inheritDoc}
     */
    public void onProcessActivity(String processId, String output) {
        if (output.contains("STARTING TEST")) {
            loggingWebSocket.push(SocketEvent.createEvent(processId, SocketEvent.TEST_START, output));
        } else if (output.contains("TEST SUCCESS")) {
            loggingWebSocket.push(SocketEvent.createEvent(processId, SocketEvent.TEST_SUCCESS, output));
        } else if (output.contains("TEST FAILED")) {
            loggingWebSocket.push(SocketEvent.createEvent(processId, SocketEvent.TEST_FAILED, output));
        } else if (output.contains("TEST STEP") && output.contains("done")) {
            String[] progress = output.substring(output.indexOf("TEST STEP") + 9, (output.indexOf("done") - 1)).split("/");
            long progressValue = Math.round((Double.valueOf(progress[0]) / Double.valueOf(progress[1])) * 100);

            JSONObject event = SocketEvent.createEvent(processId, SocketEvent.TEST_ACTION_FINISH,
                    "TEST ACTION " + progress[0] + "/" + progress[1]);
            event.put("progress", String.valueOf(progressValue));
            loggingWebSocket.push(event);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onProcessOutput(String processId, String output) {
        loggingWebSocket.push(SocketEvent.createEvent(processId, SocketEvent.LOG_MESSAGE, output));
    }

    /**
     * {@inheritDoc}
     */
    public void onProcessStart(String processId) {
        loggingWebSocket.push(SocketEvent.createEvent(processId, SocketEvent.PROCESS_START, "process started"));
    }

    /**
     * {@inheritDoc}
     */
    public void onProcessSuccess(String processId) {
        loggingWebSocket.push(SocketEvent.createEvent(processId, SocketEvent.PROCESS_SUCCESS, "process completed successfully"));
    }

    /**
     * {@inheritDoc}
     */
    public void onProcessFail(String processId, int exitCode) {
        loggingWebSocket.push(SocketEvent.createEvent(processId, SocketEvent.PROCESS_FAILED, "process failed with exit code " + exitCode));
    }

    /**
     * {@inheritDoc}
     */
    public void onProcessFail(String processId, Throwable e) {
        loggingWebSocket.push(SocketEvent.createEvent(processId, SocketEvent.PROCESS_FAILED, "process failed with exception " + e.getLocalizedMessage()));
    }

    /**
     * Setter for dependency injection of loggingWebSocket instance.
     * @param loggingWebSocket
     */
    public void setLoggingWebSocket(LoggingWebSocket loggingWebSocket) {
        this.loggingWebSocket = loggingWebSocket;
    }
}
