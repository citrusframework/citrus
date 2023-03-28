/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.ftp.client;

import java.util.Map;

import org.citrusframework.channel.ChannelEndpointComponent;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.endpoint.direct.DirectEndpointComponent;
import org.citrusframework.jms.endpoint.JmsEndpointComponent;
import org.citrusframework.ssh.client.SshEndpointComponent;
import org.testng.Assert;
import org.testng.annotations.Test;


public class SftpEndpointComponentTest {

    private TestContext context = new TestContext();

    @Test
    public void testCreateClientEndpoint() throws Exception {
        SftpEndpointComponent component = new SftpEndpointComponent();

        Endpoint endpoint = component.createEndpoint("sftp://localhost:2221", context);

        Assert.assertEquals(endpoint.getClass(), SftpClient.class);

        Assert.assertEquals(((SftpClient)endpoint).getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(((SftpClient)endpoint).getEndpointConfiguration().getPort(), 2221);
        Assert.assertNull(((SftpClient) endpoint).getEndpointConfiguration().getUser());
        Assert.assertNull(((SftpClient) endpoint).getEndpointConfiguration().getPassword());
        Assert.assertEquals(((SftpClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

        endpoint = component.createEndpoint("sftp:ftp.foo.bar", context);

        Assert.assertEquals(endpoint.getClass(), SftpClient.class);

        Assert.assertEquals(((SftpClient)endpoint).getEndpointConfiguration().getHost(), "ftp.foo.bar");
        Assert.assertEquals(((SftpClient)endpoint).getEndpointConfiguration().getPort(), 22222);
        Assert.assertNull(((SftpClient) endpoint).getEndpointConfiguration().getUser());
        Assert.assertNull(((SftpClient) endpoint).getEndpointConfiguration().getPassword());
        Assert.assertEquals(((SftpClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testCreateClientEndpointWithParameters() throws Exception {
        SftpEndpointComponent component = new SftpEndpointComponent();

        Endpoint endpoint = component.createEndpoint("sftp:localhost:22220?user=admin&password=consol&timeout=10000", context);

        Assert.assertEquals(endpoint.getClass(), SftpClient.class);

        Assert.assertEquals(((SftpClient)endpoint).getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(((SftpClient)endpoint).getEndpointConfiguration().getPort(), 22220);
        Assert.assertEquals(((SftpClient) endpoint).getEndpointConfiguration().getUser(), "admin");
        Assert.assertEquals(((SftpClient) endpoint).getEndpointConfiguration().getPassword(), "consol");
        Assert.assertEquals(((SftpClient) endpoint).getEndpointConfiguration().getTimeout(), 10000L);

    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assert.assertEquals(validators.size(), 6L);
        Assert.assertNotNull(validators.get("direct"));
        Assert.assertEquals(validators.get("direct").getClass(), DirectEndpointComponent.class);
        Assert.assertNotNull(validators.get("jms"));
        Assert.assertEquals(validators.get("jms").getClass(), JmsEndpointComponent.class);
        Assert.assertNotNull(validators.get("channel"));
        Assert.assertEquals(validators.get("channel").getClass(), ChannelEndpointComponent.class);
        Assert.assertNotNull(validators.get("ssh"));
        Assert.assertEquals(validators.get("ssh").getClass(), SshEndpointComponent.class);
        Assert.assertNotNull(validators.get("ftp"));
        Assert.assertEquals(validators.get("ftp").getClass(), FtpEndpointComponent.class);
        Assert.assertNotNull(validators.get("sftp"));
        Assert.assertEquals(validators.get("sftp").getClass(), SftpEndpointComponent.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(EndpointComponent.lookup("sftp").isPresent());
    }
}
