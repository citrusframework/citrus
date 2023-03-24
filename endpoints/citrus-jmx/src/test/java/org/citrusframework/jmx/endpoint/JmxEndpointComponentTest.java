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

package org.citrusframework.jmx.endpoint;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.endpoint.direct.DirectEndpointComponent;
import org.citrusframework.jmx.client.JmxClient;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JmxEndpointComponentTest {

    private TestContext context = new TestContext();

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

        Endpoint endpoint = component.createEndpoint("jmx:rmi:///jndi/rmi://localhost:1099/someService?autoReconnect=false&timeout=10000", context);

        Assert.assertEquals(endpoint.getClass(), JmxClient.class);

        Assert.assertEquals(((JmxClient)endpoint).getEndpointConfiguration().getServerUrl(), "service:jmx:rmi:///jndi/rmi://localhost:1099/someService");
        Assert.assertFalse(((JmxClient) endpoint).getEndpointConfiguration().isAutoReconnect());
        Assert.assertEquals(((JmxClient) endpoint).getEndpointConfiguration().getTimeout(), 10000L);

        endpoint = component.createEndpoint("jmx:platform?autoReconnect=true", context);

        Assert.assertEquals(endpoint.getClass(), JmxClient.class);

        Assert.assertEquals(((JmxClient)endpoint).getEndpointConfiguration().getServerUrl(), "platform");
        Assert.assertTrue(((JmxClient) endpoint).getEndpointConfiguration().isAutoReconnect());
        Assert.assertEquals(((JmxClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assert.assertEquals(validators.size(), 2L);
        Assert.assertNotNull(validators.get("direct"));
        Assert.assertEquals(validators.get("direct").getClass(), DirectEndpointComponent.class);
        Assert.assertNotNull(validators.get("jmx"));
        Assert.assertEquals(validators.get("jmx").getClass(), JmxEndpointComponent.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(EndpointComponent.lookup("jmx").isPresent());
    }

}
