/*
 * Copyright 2006-2014 the original author or authors.
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
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class SshEndpointComponentTest {

    private TestContext context = new TestContext();

    @Test
    public void testCreateEndpoint() throws Exception {
        SshEndpointComponent component = new SshEndpointComponent();

        Endpoint endpoint = component.createEndpoint("ssh://localhost:2200", context);

        Assert.assertEquals(endpoint.getClass(), SshClient.class);

        Assert.assertEquals(((SshClient)endpoint).getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(((SshClient)endpoint).getEndpointConfiguration().getPort(), 2200);
        Assert.assertEquals(((SshClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testCreateEndpointWithoutPort() throws Exception {
        SshEndpointComponent component = new SshEndpointComponent();

        Endpoint endpoint = component.createEndpoint("ssh:127.0.0.1", context);

        Assert.assertEquals(endpoint.getClass(), SshClient.class);

        Assert.assertEquals(((SshClient)endpoint).getEndpointConfiguration().getHost(), "127.0.0.1");
        Assert.assertEquals(((SshClient)endpoint).getEndpointConfiguration().getPort(), 2222);
        Assert.assertEquals(((SshClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testCreateEndpointWithParameters() throws Exception {
        SshEndpointComponent component = new SshEndpointComponent();

        Endpoint endpoint = component.createEndpoint("ssh://localhost:2200?timeout=10000&strictHostChecking=true&user=foo&password=12345678", context);

        Assert.assertEquals(endpoint.getClass(), SshClient.class);

        Assert.assertEquals(((SshClient)endpoint).getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(((SshClient)endpoint).getEndpointConfiguration().getPort(), 2200);
        Assert.assertEquals(((SshClient)endpoint).getEndpointConfiguration().getUser(), "foo");
        Assert.assertEquals(((SshClient)endpoint).getEndpointConfiguration().getPassword(), "12345678");
        Assert.assertTrue(((SshClient) endpoint).getEndpointConfiguration().isStrictHostChecking());
        Assert.assertEquals(((SshClient) endpoint).getEndpointConfiguration().getTimeout(), 10000L);
    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assert.assertEquals(validators.size(), 2L);
        Assert.assertNotNull(validators.get("direct"));
        Assert.assertEquals(validators.get("direct").getClass(), DirectEndpointComponent.class);
        Assert.assertNotNull(validators.get("ssh"));
        Assert.assertEquals(validators.get("ssh").getClass(), SshEndpointComponent.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(EndpointComponent.lookup("ssh").isPresent());
    }
}
