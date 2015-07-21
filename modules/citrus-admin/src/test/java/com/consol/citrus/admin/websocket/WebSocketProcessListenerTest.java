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

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import net.minidev.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class WebSocketProcessListenerTest {

    private LoggingWebSocket loggingWebSocket = EasyMock.createMock(LoggingWebSocket.class);

    @Test
    public void testTestEventExtraction() throws IOException {
        reset(loggingWebSocket);

        loggingWebSocket.push(anyObject(JSONObject.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                JSONObject event = (JSONObject)getCurrentArguments()[0];

                Assert.assertEquals(event.get("progress").toString(), "50");
                return null;
            }
        }).once();

        replay(loggingWebSocket);

        WebSocketProcessListener listener = new WebSocketProcessListener();
        listener.setLoggingWebSocket(loggingWebSocket);

        listener.onProcessActivity("test", "INFO: TEST STEP 1/2 SUCCESS");

        verify(loggingWebSocket);
    }

    @Test
    public void testTestEventExtractionHighNumbers() throws IOException {
        reset(loggingWebSocket);

        loggingWebSocket.push(anyObject(JSONObject.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                JSONObject event = (JSONObject)getCurrentArguments()[0];

                Assert.assertEquals(event.get("progress").toString(), "9");
                return null;
            }
        }).once();

        replay(loggingWebSocket);

        WebSocketProcessListener listener = new WebSocketProcessListener();
        listener.setLoggingWebSocket(loggingWebSocket);

        listener.onProcessActivity("test", "INFO: TEST STEP 20/229 SUCCESS");

        verify(loggingWebSocket);
    }
}
