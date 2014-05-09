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

package com.consol.citrus.ssh.client;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class SshEndpointComponentTest {

    private ApplicationContext applicationContext = EasyMock.createMock(ApplicationContext.class);
    private TestContext context = new TestContext();

    @BeforeClass
    public void setup() {
        context.setApplicationContext(applicationContext);
    }

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
        Assert.assertEquals(((SshClient)endpoint).getEndpointConfiguration().isStrictHostChecking(), true);
        Assert.assertEquals(((SshClient) endpoint).getEndpointConfiguration().getTimeout(), 10000L);
    }
}
