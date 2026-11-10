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

package org.citrusframework.websocket.endpoint;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.endpoint.direct.DirectEndpointComponent;
import org.citrusframework.endpoint.context.MessageStoreEndpointComponent;
import org.citrusframework.http.client.HttpEndpointComponent;
import org.citrusframework.http.client.HttpsEndpointComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 2.3
 */
public class WebSocketEndpointComponentTest {
    private TestContext context = new TestContext();

    @Test
    public void testCreateClientEndpoint() {
        WebSocketEndpointComponent component = new WebSocketEndpointComponent();

        Endpoint endpoint = component.createEndpoint("ws://localhost:8088/test", context);

        Assertions.assertEquals(WebSocketEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("ws://localhost:8088/test", ((WebSocketEndpoint) endpoint).getEndpointConfiguration().getEndpointUri());
        Assertions.assertEquals(5000L, ((WebSocketEndpoint) endpoint).getEndpointConfiguration().getTimeout());

        endpoint = component.createEndpoint("websocket://localhost:8088/test", context);

        Assertions.assertEquals(WebSocketEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("ws://localhost:8088/test", ((WebSocketEndpoint) endpoint).getEndpointConfiguration().getEndpointUri());
        Assertions.assertEquals(5000L, ((WebSocketEndpoint) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testCreateClientEndpointWithParameters() {
        WebSocketEndpointComponent component = new WebSocketEndpointComponent();

        Endpoint endpoint = component.createEndpoint("ws:localhost:8088?timeout=10000", context);

        Assertions.assertEquals(WebSocketEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("ws://localhost:8088", ((WebSocketEndpoint) endpoint).getEndpointConfiguration().getEndpointUri());
        Assertions.assertEquals(10000L, ((WebSocketEndpoint) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testCreateClientEndpointWithCustomParameters() {
        WebSocketEndpointComponent component = new WebSocketEndpointComponent();

        Endpoint endpoint = component.createEndpoint("ws://localhost:8088/test?customParam=foo", context);

        Assertions.assertEquals(WebSocketEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("ws://localhost:8088/test?customParam=foo", ((WebSocketEndpoint) endpoint).getEndpointConfiguration().getEndpointUri());
        Assertions.assertEquals(5000L, ((WebSocketEndpoint) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assertions.assertEquals(6L, validators.size());
        Assertions.assertNotNull(validators.get("direct"));
        Assertions.assertEquals(DirectEndpointComponent.class, validators.get("direct").getClass());
        Assertions.assertNotNull(validators.get("message-store"));
        Assertions.assertEquals(MessageStoreEndpointComponent.class, validators.get("message-store").getClass());
        Assertions.assertNotNull(validators.get("http"));
        Assertions.assertEquals(HttpEndpointComponent.class, validators.get("http").getClass());
        Assertions.assertNotNull(validators.get("https"));
        Assertions.assertEquals(HttpsEndpointComponent.class, validators.get("https").getClass());
        Assertions.assertNotNull(validators.get("websocket"));
        Assertions.assertEquals(WebSocketEndpointComponent.class, validators.get("websocket").getClass());
        Assertions.assertNotNull(validators.get("ws"));
        Assertions.assertEquals(WebSocketEndpointComponent.class, validators.get("ws").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(EndpointComponent.lookup("websocket").isPresent());
        Assertions.assertTrue(EndpointComponent.lookup("ws").isPresent());
    }
}
