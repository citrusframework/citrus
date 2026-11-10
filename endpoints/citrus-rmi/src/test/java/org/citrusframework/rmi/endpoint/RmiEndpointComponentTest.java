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

package org.citrusframework.rmi.endpoint;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.endpoint.direct.DirectEndpointComponent;
import org.citrusframework.endpoint.context.MessageStoreEndpointComponent;
import org.citrusframework.rmi.client.RmiClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 2.5
 */
public class RmiEndpointComponentTest {

    private final TestContext context = new TestContext();

    @Test
    public void testCreateClientEndpoint() {
        RmiEndpointComponent component = new RmiEndpointComponent();

        Endpoint endpoint = component.createEndpoint("rmi://localhost:2099", context);

        Assertions.assertEquals(RmiClient.class, endpoint.getClass());

        Assertions.assertEquals("localhost", ((RmiClient)endpoint).getEndpointConfiguration().getHost());
        Assertions.assertEquals(2099, ((RmiClient)endpoint).getEndpointConfiguration().getPort());
        Assertions.assertEquals(5000L, ((RmiClient) endpoint).getEndpointConfiguration().getTimeout());

        endpoint = component.createEndpoint("rmi://localhost:2099/news", context);

        Assertions.assertEquals(RmiClient.class, endpoint.getClass());

        Assertions.assertEquals("localhost", ((RmiClient)endpoint).getEndpointConfiguration().getHost());
        Assertions.assertEquals(2099, ((RmiClient)endpoint).getEndpointConfiguration().getPort());
        Assertions.assertEquals("news", ((RmiClient) endpoint).getEndpointConfiguration().getBinding());
        Assertions.assertEquals(5000L, ((RmiClient) endpoint).getEndpointConfiguration().getTimeout());

        endpoint = component.createEndpoint("rmi://localhost:2099/service/news", context);

        Assertions.assertEquals(RmiClient.class, endpoint.getClass());

        Assertions.assertEquals("localhost", ((RmiClient)endpoint).getEndpointConfiguration().getHost());
        Assertions.assertEquals(2099, ((RmiClient)endpoint).getEndpointConfiguration().getPort());
        Assertions.assertEquals("service/news", ((RmiClient) endpoint).getEndpointConfiguration().getBinding());
        Assertions.assertEquals(5000L, ((RmiClient) endpoint).getEndpointConfiguration().getTimeout());

        endpoint = component.createEndpoint("rmi:rmiserverhost", context);

        Assertions.assertEquals(RmiClient.class, endpoint.getClass());

        Assertions.assertEquals("rmiserverhost", ((RmiClient)endpoint).getEndpointConfiguration().getHost());
        Assertions.assertEquals(1099, ((RmiClient)endpoint).getEndpointConfiguration().getPort());
        Assertions.assertEquals(5000L, ((RmiClient) endpoint).getEndpointConfiguration().getTimeout());

        endpoint = component.createEndpoint("rmi:localhost/service", context);

        Assertions.assertEquals(RmiClient.class, endpoint.getClass());

        Assertions.assertEquals("localhost", ((RmiClient)endpoint).getEndpointConfiguration().getHost());
        Assertions.assertEquals(1099, ((RmiClient)endpoint).getEndpointConfiguration().getPort());
        Assertions.assertEquals("service", ((RmiClient) endpoint).getEndpointConfiguration().getBinding());
        Assertions.assertEquals(5000L, ((RmiClient) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testCreateClientEndpointWithParameters() {
        RmiEndpointComponent component = new RmiEndpointComponent();

        Endpoint endpoint = component.createEndpoint("rmi://localhost:2099?binding=rmiBinding&timeout=10000", context);

        Assertions.assertEquals(RmiClient.class, endpoint.getClass());

        Assertions.assertEquals("localhost", ((RmiClient)endpoint).getEndpointConfiguration().getHost());
        Assertions.assertEquals(2099, ((RmiClient)endpoint).getEndpointConfiguration().getPort());
        Assertions.assertEquals("rmiBinding", ((RmiClient) endpoint).getEndpointConfiguration().getBinding());
        Assertions.assertEquals(10000L, ((RmiClient) endpoint).getEndpointConfiguration().getTimeout());

        endpoint = component.createEndpoint("rmi://localhost:2099/binding?binding=rmiBinding", context);

        Assertions.assertEquals(RmiClient.class, endpoint.getClass());

        Assertions.assertEquals("localhost", ((RmiClient)endpoint).getEndpointConfiguration().getHost());
        Assertions.assertEquals(2099, ((RmiClient)endpoint).getEndpointConfiguration().getPort());
        Assertions.assertEquals("rmiBinding", ((RmiClient) endpoint).getEndpointConfiguration().getBinding());
        Assertions.assertEquals(5000L, ((RmiClient) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assertions.assertEquals(3L, validators.size());
        Assertions.assertNotNull(validators.get("direct"));
        Assertions.assertEquals(DirectEndpointComponent.class, validators.get("direct").getClass());
        Assertions.assertNotNull(validators.get("message-store"));
        Assertions.assertEquals(MessageStoreEndpointComponent.class, validators.get("message-store").getClass());
        Assertions.assertNotNull(validators.get("rmi"));
        Assertions.assertEquals(RmiEndpointComponent.class, validators.get("rmi").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(EndpointComponent.lookup("rmi").isPresent());
    }
}
