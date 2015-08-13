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

package com.consol.citrus.websocket.endpoint;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Martin Maher
 * @since 2.3
 */
public class WebSocketEndpointComponentTest {
    private ApplicationContext applicationContext = EasyMock.createMock(ApplicationContext.class);
    private TestContext context = new TestContext();

    @BeforeClass
    public void setup() {
        context.setApplicationContext(applicationContext);
    }

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
}