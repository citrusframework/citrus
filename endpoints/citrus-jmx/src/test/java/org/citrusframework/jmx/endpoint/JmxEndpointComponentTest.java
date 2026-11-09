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

package org.citrusframework.jmx.endpoint;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.endpoint.direct.DirectEndpointComponent;
import org.citrusframework.endpoint.context.MessageStoreEndpointComponent;
import org.citrusframework.jmx.client.JmxClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 2.5
 */
public class JmxEndpointComponentTest {

    private TestContext context = new TestContext();

    @Test
    public void testCreateClientEndpoint() {
        JmxEndpointComponent component = new JmxEndpointComponent();

        Endpoint endpoint = component.createEndpoint("jmx:rmi:///jndi/rmi://localhost:1099/someService", context);

        Assertions.assertEquals(JmxClient.class, endpoint.getClass());

        Assertions.assertEquals("service:jmx:rmi:///jndi/rmi://localhost:1099/someService", ((JmxClient)endpoint).getEndpointConfiguration().getServerUrl());

        endpoint = component.createEndpoint("jmx:platform", context);

        Assertions.assertEquals(JmxClient.class, endpoint.getClass());

        Assertions.assertEquals("platform", ((JmxClient)endpoint).getEndpointConfiguration().getServerUrl());
        Assertions.assertEquals(5000L, ((JmxClient) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testCreateClientEndpointWithParameters() {
        JmxEndpointComponent component = new JmxEndpointComponent();

        Endpoint endpoint = component.createEndpoint("jmx:rmi:///jndi/rmi://localhost:1099/someService?autoReconnect=false&timeout=10000", context);

        Assertions.assertEquals(JmxClient.class, endpoint.getClass());

        Assertions.assertEquals("service:jmx:rmi:///jndi/rmi://localhost:1099/someService", ((JmxClient)endpoint).getEndpointConfiguration().getServerUrl());
        Assertions.assertFalse(((JmxClient) endpoint).getEndpointConfiguration().isAutoReconnect());
        Assertions.assertEquals(10000L, ((JmxClient) endpoint).getEndpointConfiguration().getTimeout());

        endpoint = component.createEndpoint("jmx:platform?autoReconnect=true", context);

        Assertions.assertEquals(JmxClient.class, endpoint.getClass());

        Assertions.assertEquals("platform", ((JmxClient)endpoint).getEndpointConfiguration().getServerUrl());
        Assertions.assertTrue(((JmxClient) endpoint).getEndpointConfiguration().isAutoReconnect());
        Assertions.assertEquals(5000L, ((JmxClient) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assertions.assertEquals(3L, validators.size());
        Assertions.assertNotNull(validators.get("direct"));
        Assertions.assertEquals(DirectEndpointComponent.class, validators.get("direct").getClass());
        Assertions.assertNotNull(validators.get("message-store"));
        Assertions.assertEquals(MessageStoreEndpointComponent.class, validators.get("message-store").getClass());
        Assertions.assertNotNull(validators.get("jmx"));
        Assertions.assertEquals(JmxEndpointComponent.class, validators.get("jmx").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(EndpointComponent.lookup("jmx").isPresent());
    }
}
