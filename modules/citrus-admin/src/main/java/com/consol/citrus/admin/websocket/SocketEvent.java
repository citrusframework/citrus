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

import net.minidev.json.JSONObject;

/**
 * All socket event types with proper JSON data generation.
 *
 * @author Christoph Deppisch
 */
enum SocketEvent {
    PING,
    LOG_MESSAGE,
    INBOUND_MESSAGE,
    OUTBOUND_MESSAGE,
    TEST_START,
    TEST_SUCCESS,
    TEST_FAILED,
    TEST_FINISHED,
    TEST_SKIP,
    TEST_ACTION_START,
    TEST_ACTION_FINISH,
    TEST_ACTION_SKIP,
    PROCESS_START,
    PROCESS_SUCCESS,
    PROCESS_FAILED;

    /**
     * Creates proper JSON message for socket event.
     * @param processId the process id
     * @param pushEvent the type of event
     * @param message the event message
     * @return a json representation of the message
     */
    @SuppressWarnings("unchecked")
    public static JSONObject createEvent(String processId, SocketEvent pushEvent, String message) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("processId", processId);
        jsonObject.put("event", pushEvent.name());
        jsonObject.put("msg", message);
        return jsonObject;
    }
}
