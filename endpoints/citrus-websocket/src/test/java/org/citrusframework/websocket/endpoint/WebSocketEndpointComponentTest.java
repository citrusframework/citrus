/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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
import org.citrusframework.http.client.HttpEndpointComponent;
import org.citrusframework.http.client.HttpsEndpointComponent;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Martin Maher
 * @since 2.3
 */
public class WebSocketEndpointComponentTest {
    private TestContext context = new TestContext();

    @Test
    public void testCreateClientEndpoint() throws Exception {
        WebSocketEndpointComponent component = new WebSocketEndpointComponent();

        Endpoint endpoint = component.createEndpoint("ws://localhost:8088/test", context);

        Assert.assertEquals(endpoint.getClass(), WebSocketEndpoint.class);

        Assert.assertEquals(((WebSocketEndpoint) endpoint).getEndpointConfiguration().getEndpointUri(), "ws://localhost:8088/test");
        Assert.assertEquals(((WebSocketEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

        endpoint = component.createEndpoint("websocket://localhost:8088/test", context);

        Assert.assertEquals(endpoint.getClass(), WebSocketEndpoint.class);

        Assert.assertEquals(((WebSocketEndpoint) endpoint).getEndpointConfiguration().getEndpointUri(), "ws://localhost:8088/test");
        Assert.assertEquals(((WebSocketEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testCreateClientEndpointWithParameters() throws Exception {
        WebSocketEndpointComponent component = new WebSocketEndpointComponent();

        Endpoint endpoint = component.createEndpoint("ws:localhost:8088?timeout=10000", context);

        Assert.assertEquals(endpoint.getClass(), WebSocketEndpoint.class);

        Assert.assertEquals(((WebSocketEndpoint) endpoint).getEndpointConfiguration().getEndpointUri(), "ws://localhost:8088");
        Assert.assertEquals(((WebSocketEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 10000L);
    }

    @Test
    public void testCreateClientEndpointWithCustomParameters() throws Exception {
        WebSocketEndpointComponent component = new WebSocketEndpointComponent();

        Endpoint endpoint = component.createEndpoint("ws://localhost:8088/test?customParam=foo", context);

        Assert.assertEquals(endpoint.getClass(), WebSocketEndpoint.class);

        Assert.assertEquals(((WebSocketEndpoint) endpoint).getEndpointConfiguration().getEndpointUri(), "ws://localhost:8088/test?customParam=foo");
        Assert.assertEquals(((WebSocketEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assert.assertEquals(validators.size(), 5L);
        Assert.assertNotNull(validators.get("direct"));
        Assert.assertEquals(validators.get("direct").getClass(), DirectEndpointComponent.class);
        Assert.assertNotNull(validators.get("http"));
        Assert.assertEquals(validators.get("http").getClass(), HttpEndpointComponent.class);
        Assert.assertNotNull(validators.get("https"));
        Assert.assertEquals(validators.get("https").getClass(), HttpsEndpointComponent.class);
        Assert.assertNotNull(validators.get("websocket"));
        Assert.assertEquals(validators.get("websocket").getClass(), WebSocketEndpointComponent.class);
        Assert.assertNotNull(validators.get("ws"));
        Assert.assertEquals(validators.get("ws").getClass(), WebSocketEndpointComponent.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(EndpointComponent.lookup("websocket").isPresent());
        Assert.assertTrue(EndpointComponent.lookup("ws").isPresent());
    }
}
