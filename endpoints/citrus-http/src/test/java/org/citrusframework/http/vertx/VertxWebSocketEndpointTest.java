/*
 * Copyright the original author or authors.
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

package org.citrusframework.http.vertx;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class VertxWebSocketEndpointTest {
    @Test public void rejectsInvalidConfiguration() {
        for (String path : new String[]{"ws", "/ws/*", "/a/../ws", "/ws?x", "/%77s", "/ws;v=1"})
            expectThrows(IllegalArgumentException.class, () -> new VertxWebSocketEndpoint(path));
        expectThrows(IllegalArgumentException.class, () -> new VertxWebSocketEndpoint("/ws", 0, 1, 1));
        expectThrows(IllegalArgumentException.class, () -> new VertxWebSocketEndpoint("/ws", 1, 0, 1));
        expectThrows(IllegalArgumentException.class, () -> new VertxWebSocketEndpoint("/ws", 1, 1, 0));
    }
    @Test public void rejectsStoppedEndpointsAndUnsupportedPayloads() {
        var endpoint = new VertxWebSocketEndpoint("/ws"); var context = new org.citrusframework.context.TestContext();
        expectThrows(org.citrusframework.exceptions.CitrusRuntimeException.class,
                () -> endpoint.createConsumer().receive(context, 1));
        var state = endpoint.open();
        try {
            expectThrows(IllegalArgumentException.class, () -> endpoint.createProducer().send(new org.citrusframework.message.DefaultMessage(42), context));
            expectThrows(IllegalArgumentException.class, () -> endpoint.createProducer().send(new org.citrusframework.message.DefaultMessage(new byte[65537]), context));
            expectThrows(org.citrusframework.exceptions.CitrusRuntimeException.class,
                    () -> endpoint.createProducer().send(new org.citrusframework.message.DefaultMessage("no sessions"), context));
        } finally { state.close(); }
    }
}
