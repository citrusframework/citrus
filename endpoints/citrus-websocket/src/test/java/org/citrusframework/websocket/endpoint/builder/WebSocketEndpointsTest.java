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

package org.citrusframework.websocket.endpoint.builder;

import java.util.Map;

import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.websocket.client.WebSocketClientBuilder;
import org.citrusframework.websocket.server.WebSocketServerBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WebSocketEndpointsTest {

    @Test
    public void shouldLookupEndpoints() {
        Map<String, EndpointBuilder<?>> endpointBuilders = EndpointBuilder.lookup();
        Assert.assertTrue(endpointBuilders.containsKey("websocket.client"));
        Assert.assertTrue(endpointBuilders.containsKey("websocket.server"));
    }

    @Test
    public void shouldLookupEndpoint() {
        Assert.assertTrue(EndpointBuilder.lookup("websocket.client").isPresent());
        Assert.assertEquals(EndpointBuilder.lookup("websocket.client").get().getClass(), WebSocketClientBuilder.class);
        Assert.assertTrue(EndpointBuilder.lookup("websocket.server").isPresent());
        Assert.assertEquals(EndpointBuilder.lookup("websocket.server").get().getClass(), WebSocketServerBuilder.class);
    }
}
