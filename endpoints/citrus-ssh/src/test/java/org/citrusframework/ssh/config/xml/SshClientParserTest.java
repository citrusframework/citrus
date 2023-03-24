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

package org.citrusframework.ssh.config.xml;

import org.citrusframework.ssh.client.SshClient;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Christoph Deppisch
 */
public class SshClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testSshServerParser() {
        Map<String, SshClient> clients = beanDefinitionContext.getBeansOfType(SshClient.class);

        Assert.assertEquals(clients.size(), 2);

        // 1st client
        SshClient client = clients.get("sshClient1");
        Assert.assertEquals(client.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(client.getEndpointConfiguration().getPort(), 2222);
        Assert.assertEquals(client.getEndpointConfiguration().getUser(), "citrus");
        Assert.assertNull(client.getEndpointConfiguration().getPassword());
        Assert.assertNull(client.getEndpointConfiguration().getPrivateKeyPath());
        Assert.assertNull(client.getEndpointConfiguration().getPrivateKeyPassword());
        Assert.assertNull(client.getEndpointConfiguration().getKnownHosts());
        Assert.assertEquals(client.getEndpointConfiguration().getCommandTimeout(), 1000 * 60 * 5);
        Assert.assertEquals(client.getEndpointConfiguration().getConnectionTimeout(), 1000 * 60 * 1);
        Assert.assertFalse(client.getEndpointConfiguration().isStrictHostChecking());
        Assert.assertNotNull(client.getEndpointConfiguration().getMessageConverter());

        // 2nd client
        client = clients.get("sshClient2");
        Assert.assertEquals(client.getEndpointConfiguration().getHost(), "dev7");
        Assert.assertEquals(client.getEndpointConfiguration().getPort(), 10022);
        Assert.assertEquals(client.getEndpointConfiguration().getUser(), "foo");
        Assert.assertEquals(client.getEndpointConfiguration().getPassword(), "bar");
        Assert.assertEquals(client.getEndpointConfiguration().getPrivateKeyPath(), "classpath:org/citrusframework/ssh/citrus.priv");
        Assert.assertEquals(client.getEndpointConfiguration().getPrivateKeyPassword(), "consol");
        Assert.assertEquals(client.getEndpointConfiguration().getKnownHosts(), "classpath:org/citrusframework/ssh/known_hosts");
        Assert.assertEquals(client.getEndpointConfiguration().getCommandTimeout(), 10000);
        Assert.assertEquals(client.getEndpointConfiguration().getConnectionTimeout(), 5000);
        Assert.assertTrue(client.getEndpointConfiguration().isStrictHostChecking());
        Assert.assertEquals(client.getEndpointConfiguration().getMessageConverter(), beanDefinitionContext.getBean("sshMessageConverter"));
    }
}
