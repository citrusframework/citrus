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
import net.minidev.json.JSONObject;
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

    /** Last message data collected by multiple lines of process output */
    private JSONObject messageDataEvent;

    @Override
    public void onProcessActivity(String processId, String output) {

        // first check if we have a pending message data event to handle
        if (messageDataEvent != null) {
            if (isProcessOutputLine(output)) {
                // message data collecting is obviously finished so push event now
                loggingWebSocket.push(messageDataEvent);
                messageDataEvent = null; // reset data event storage
            } else {
                // collect another line of message data
                messageDataEvent.put("msg", messageDataEvent.get("msg") + System.getProperty("line.separator") + output);
            }
        }

        if (output.contains("STARTING TEST")) {
            loggingWebSocket.push(SocketEvent.createEvent(processId, SocketEvent.TEST_START, output));
        } else if (output.contains("TEST SUCCESS")) {
            loggingWebSocket.push(SocketEvent.createEvent(processId, SocketEvent.TEST_SUCCESS, output));
        } else if (output.contains("TEST FAILED")) {
            loggingWebSocket.push(SocketEvent.createEvent(processId, SocketEvent.TEST_FAILED, output));
        } else if (output.contains("TEST STEP") && output.contains("SUCCESS")) {
            String[] progress = output.substring(output.indexOf("TEST STEP") + 9, (output.indexOf("SUCCESS") - 1)).split("/");
            long progressValue = Math.round((Double.valueOf(progress[0]) / Double.valueOf(progress[1])) * 100);

            JSONObject event = SocketEvent.createEvent(processId, SocketEvent.TEST_ACTION_FINISH,
                    "TEST ACTION " + progress[0] + "/" + progress[1]);
            event.put("progress", String.valueOf(progressValue));
            loggingWebSocket.push(event);
        } else if (output.contains("Logger.Message_OUT")) {
            messageDataEvent = SocketEvent.createEvent(processId, SocketEvent.OUTBOUND_MESSAGE, output.substring(output.indexOf("Logger.Message_OUT") + 20));
        } else if (output.contains("Logger.Message_IN")) {
            messageDataEvent = SocketEvent.createEvent(processId, SocketEvent.INBOUND_MESSAGE, output.substring(output.indexOf("Logger.Message_IN") + 19));
        }
    }

    @Override
    public void onProcessOutput(String processId, String output) {
        loggingWebSocket.push(SocketEvent.createEvent(processId, SocketEvent.LOG_MESSAGE, output));
    }

    @Override
    public void onProcessStart(String processId) {
        loggingWebSocket.push(SocketEvent.createEvent(processId, SocketEvent.PROCESS_START, "process started"));
    }

    @Override
    public void onProcessSuccess(String processId) {
        loggingWebSocket.push(SocketEvent.createEvent(processId, SocketEvent.PROCESS_SUCCESS, "process completed successfully"));
    }

    @Override
    public void onProcessFail(String processId, int exitCode) {
        loggingWebSocket.push(SocketEvent.createEvent(processId, SocketEvent.PROCESS_FAILED, "process failed with exit code " + exitCode));
    }

    @Override
    public void onProcessFail(String processId, Throwable e) {
        loggingWebSocket.push(SocketEvent.createEvent(processId, SocketEvent.PROCESS_FAILED, "process failed with exception " + e.getLocalizedMessage()));
    }

    /**
     * Setter for dependency injection of loggingWebSocket instance.
     * @param loggingWebSocket the logging web socket
     */
    public void setLoggingWebSocket(LoggingWebSocket loggingWebSocket) {
        this.loggingWebSocket = loggingWebSocket;
    }

    /**
     * Checks if output line is normal log output in log4j format.
     * @param output the output to check
     * @return true if normal log4j output
     */
    private boolean isProcessOutputLine(String output) {
        return output.contains("INFO") || output.contains("DEBUG") || output.contains("ERROR") || output.contains("WARN") || output.contains("TRACE");
    }

}
