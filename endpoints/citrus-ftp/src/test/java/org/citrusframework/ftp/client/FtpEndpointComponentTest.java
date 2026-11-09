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

package org.citrusframework.ftp.client;

import java.util.Map;

import org.citrusframework.springintegration.channel.ChannelEndpointComponent;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.endpoint.direct.DirectEndpointComponent;
import org.citrusframework.endpoint.context.MessageStoreEndpointComponent;
import org.citrusframework.jms.endpoint.JmsEndpointComponent;
import org.citrusframework.ssh.client.SshEndpointComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FtpEndpointComponentTest {

    private TestContext context = new TestContext();

    @Test
    public void testCreateClientEndpoint() {
        FtpEndpointComponent component = new FtpEndpointComponent();

        Endpoint endpoint = component.createEndpoint("ftp://localhost:2221", context);

        Assertions.assertEquals(FtpClient.class, endpoint.getClass());

        Assertions.assertEquals("localhost", ((FtpClient)endpoint).getEndpointConfiguration().getHost());
        Assertions.assertEquals(2221, ((FtpClient)endpoint).getEndpointConfiguration().getPort());
        Assertions.assertNull(((FtpClient) endpoint).getEndpointConfiguration().getUser());
        Assertions.assertNull(((FtpClient) endpoint).getEndpointConfiguration().getPassword());
        Assertions.assertEquals(5000L, ((FtpClient) endpoint).getEndpointConfiguration().getTimeout());

        endpoint = component.createEndpoint("ftp:ftp.foo.bar", context);

        Assertions.assertEquals(FtpClient.class, endpoint.getClass());

        Assertions.assertEquals("ftp.foo.bar", ((FtpClient)endpoint).getEndpointConfiguration().getHost());
        Assertions.assertEquals(22222, ((FtpClient)endpoint).getEndpointConfiguration().getPort());
        Assertions.assertNull(((FtpClient) endpoint).getEndpointConfiguration().getUser());
        Assertions.assertNull(((FtpClient) endpoint).getEndpointConfiguration().getPassword());
        Assertions.assertEquals(5000L, ((FtpClient) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testCreateClientEndpointWithParameters() {
        FtpEndpointComponent component = new FtpEndpointComponent();

        Endpoint endpoint = component.createEndpoint("ftp:localhost:22220?user=admin&password=consol&timeout=10000", context);

        Assertions.assertEquals(FtpClient.class, endpoint.getClass());

        Assertions.assertEquals("localhost", ((FtpClient)endpoint).getEndpointConfiguration().getHost());
        Assertions.assertEquals(22220, ((FtpClient)endpoint).getEndpointConfiguration().getPort());
        Assertions.assertEquals("admin", ((FtpClient) endpoint).getEndpointConfiguration().getUser());
        Assertions.assertEquals("consol", ((FtpClient) endpoint).getEndpointConfiguration().getPassword());
        Assertions.assertEquals(10000L, ((FtpClient) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assertions.assertEquals(7L, validators.size());
        Assertions.assertNotNull(validators.get("direct"));
        Assertions.assertEquals(DirectEndpointComponent.class, validators.get("direct").getClass());
        Assertions.assertNotNull(validators.get("message-store"));
        Assertions.assertEquals(MessageStoreEndpointComponent.class, validators.get("message-store").getClass());
        Assertions.assertNotNull(validators.get("jms"));
        Assertions.assertEquals(JmsEndpointComponent.class, validators.get("jms").getClass());
        Assertions.assertNotNull(validators.get("channel"));
        Assertions.assertEquals(ChannelEndpointComponent.class, validators.get("channel").getClass());
        Assertions.assertNotNull(validators.get("ssh"));
        Assertions.assertEquals(SshEndpointComponent.class, validators.get("ssh").getClass());
        Assertions.assertNotNull(validators.get("ftp"));
        Assertions.assertEquals(FtpEndpointComponent.class, validators.get("ftp").getClass());
        Assertions.assertNotNull(validators.get("sftp"));
        Assertions.assertEquals(SftpEndpointComponent.class, validators.get("sftp").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(EndpointComponent.lookup("ftp").isPresent());
    }
}
