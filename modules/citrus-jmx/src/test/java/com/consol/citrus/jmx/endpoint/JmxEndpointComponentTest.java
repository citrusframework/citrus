/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.jmx.endpoint;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.jmx.client.JmxClient;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JmxEndpointComponentTest {

    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
    private TestContext context = new TestContext();

    @BeforeClass
    public void setup() {
        context.setApplicationContext(applicationContext);
    }

    @Test
    public void testCreateClientEndpoint() throws Exception {
        JmxEndpointComponent component = new JmxEndpointComponent();

        Endpoint endpoint = component.createEndpoint("jmx:rmi:///jndi/rmi://localhost:1099/someService", context);

        Assert.assertEquals(endpoint.getClass(), JmxClient.class);

        Assert.assertEquals(((JmxClient)endpoint).getEndpointConfiguration().getServerUrl(), "service:jmx:rmi:///jndi/rmi://localhost:1099/someService");

        endpoint = component.createEndpoint("jmx:platform", context);

        Assert.assertEquals(endpoint.getClass(), JmxClient.class);

        Assert.assertEquals(((JmxClient)endpoint).getEndpointConfiguration().getServerUrl(), "platform");
        Assert.assertEquals(((JmxClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testCreateClientEndpointWithParameters() throws Exception {
        JmxEndpointComponent component = new JmxEndpointComponent();

        reset(applicationContext);
        Endpoint endpoint = component.createEndpoint("jmx:rmi:///jndi/rmi://localhost:1099/someService?autoReconnect=false&timeout=10000", context);

        Assert.assertEquals(endpoint.getClass(), JmxClient.class);

        Assert.assertEquals(((JmxClient)endpoint).getEndpointConfiguration().getServerUrl(), "service:jmx:rmi:///jndi/rmi://localhost:1099/someService");
        Assert.assertEquals(((JmxClient)endpoint).getEndpointConfiguration().isAutoReconnect(), false);
        Assert.assertEquals(((JmxClient) endpoint).getEndpointConfiguration().getTimeout(), 10000L);

        endpoint = component.createEndpoint("jmx:platform?autoReconnect=true", context);

        Assert.assertEquals(endpoint.getClass(), JmxClient.class);

        Assert.assertEquals(((JmxClient)endpoint).getEndpointConfiguration().getServerUrl(), "platform");
        Assert.assertEquals(((JmxClient)endpoint).getEndpointConfiguration().isAutoReconnect(), true);
        Assert.assertEquals(((JmxClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

}