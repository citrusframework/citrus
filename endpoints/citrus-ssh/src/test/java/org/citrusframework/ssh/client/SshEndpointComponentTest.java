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

package org.citrusframework.ssh.client;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.endpoint.direct.DirectEndpointComponent;
import org.citrusframework.endpoint.context.MessageStoreEndpointComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SshEndpointComponentTest {

    private TestContext context = new TestContext();

    @Test
    public void testCreateEndpoint() {
        SshEndpointComponent component = new SshEndpointComponent();

        Endpoint endpoint = component.createEndpoint("ssh://localhost:2200", context);

        Assertions.assertEquals(SshClient.class, endpoint.getClass());

        Assertions.assertEquals("localhost", ((SshClient)endpoint).getEndpointConfiguration().getHost());
        Assertions.assertEquals(2200, ((SshClient)endpoint).getEndpointConfiguration().getPort());
        Assertions.assertEquals(5000L, ((SshClient) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testCreateEndpointWithoutPort() {
        SshEndpointComponent component = new SshEndpointComponent();

        Endpoint endpoint = component.createEndpoint("ssh:127.0.0.1", context);

        Assertions.assertEquals(SshClient.class, endpoint.getClass());

        Assertions.assertEquals("127.0.0.1", ((SshClient)endpoint).getEndpointConfiguration().getHost());
        Assertions.assertEquals(2222, ((SshClient)endpoint).getEndpointConfiguration().getPort());
        Assertions.assertEquals(5000L, ((SshClient) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testCreateEndpointWithParameters() {
        SshEndpointComponent component = new SshEndpointComponent();

        Endpoint endpoint = component.createEndpoint("ssh://localhost:2200?timeout=10000&strictHostChecking=true&user=foo&password=12345678", context);

        Assertions.assertEquals(SshClient.class, endpoint.getClass());

        Assertions.assertEquals("localhost", ((SshClient)endpoint).getEndpointConfiguration().getHost());
        Assertions.assertEquals(2200, ((SshClient)endpoint).getEndpointConfiguration().getPort());
        Assertions.assertEquals("foo", ((SshClient)endpoint).getEndpointConfiguration().getUser());
        Assertions.assertEquals("12345678", ((SshClient)endpoint).getEndpointConfiguration().getPassword());
        Assertions.assertTrue(((SshClient) endpoint).getEndpointConfiguration().isStrictHostChecking());
        Assertions.assertEquals(10000L, ((SshClient) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assertions.assertEquals(3L, validators.size());
        Assertions.assertNotNull(validators.get("direct"));
        Assertions.assertEquals(DirectEndpointComponent.class, validators.get("direct").getClass());
        Assertions.assertNotNull(validators.get("message-store"));
        Assertions.assertEquals(MessageStoreEndpointComponent.class, validators.get("message-store").getClass());
        Assertions.assertNotNull(validators.get("ssh"));
        Assertions.assertEquals(SshEndpointComponent.class, validators.get("ssh").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(EndpointComponent.lookup("ssh").isPresent());
    }
}
