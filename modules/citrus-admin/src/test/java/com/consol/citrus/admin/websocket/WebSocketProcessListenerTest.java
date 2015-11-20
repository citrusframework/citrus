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
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class WebSocketProcessListenerTest {

    private LoggingWebSocket loggingWebSocket = Mockito.mock(LoggingWebSocket.class);

    @Test
    public void testTestEventExtraction() throws IOException {
        reset(loggingWebSocket);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                JSONObject event = (JSONObject)invocation.getArguments()[0];

                Assert.assertEquals(event.get("progress").toString(), "50");
                return null;
            }
        }).when(loggingWebSocket).push(any(JSONObject.class));

        WebSocketProcessListener listener = new WebSocketProcessListener();
        listener.setLoggingWebSocket(loggingWebSocket);

        listener.onProcessActivity("test", "INFO: TEST STEP 1/2 SUCCESS");
    }

    @Test
    public void testTestEventExtractionHighNumbers() throws IOException {
        reset(loggingWebSocket);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                JSONObject event = (JSONObject)invocation.getArguments()[0];

                Assert.assertEquals(event.get("progress").toString(), "9");
                return null;
            }
        }).when(loggingWebSocket).push(any(JSONObject.class));

        WebSocketProcessListener listener = new WebSocketProcessListener();
        listener.setLoggingWebSocket(loggingWebSocket);

        listener.onProcessActivity("test", "INFO: TEST STEP 20/229 SUCCESS");
    }
}
