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

package org.citrusframework.ssh.config.xml;

import org.citrusframework.ssh.client.SshClient;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class SshClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testSshServerParser() {
        Map<String, SshClient> clients = beanDefinitionContext.getBeansOfType(SshClient.class);

        Assertions.assertEquals(2, clients.size());

        // 1st client
        SshClient client = clients.get("sshClient1");
        Assertions.assertEquals("localhost", client.getEndpointConfiguration().getHost());
        Assertions.assertEquals(2222, client.getEndpointConfiguration().getPort());
        Assertions.assertEquals("citrus", client.getEndpointConfiguration().getUser());
        Assertions.assertNull(client.getEndpointConfiguration().getPassword());
        Assertions.assertNull(client.getEndpointConfiguration().getPrivateKeyPath());
        Assertions.assertNull(client.getEndpointConfiguration().getPrivateKeyPassword());
        Assertions.assertNull(client.getEndpointConfiguration().getKnownHosts());
        Assertions.assertEquals(1000 * 60 * 5, client.getEndpointConfiguration().getCommandTimeout());
        Assertions.assertEquals(1000 * 60 * 1, client.getEndpointConfiguration().getConnectionTimeout());
        Assertions.assertFalse(client.getEndpointConfiguration().isStrictHostChecking());
        Assertions.assertNotNull(client.getEndpointConfiguration().getMessageConverter());

        // 2nd client
        client = clients.get("sshClient2");
        Assertions.assertEquals("dev7", client.getEndpointConfiguration().getHost());
        Assertions.assertEquals(10022, client.getEndpointConfiguration().getPort());
        Assertions.assertEquals("foo", client.getEndpointConfiguration().getUser());
        Assertions.assertEquals("bar", client.getEndpointConfiguration().getPassword());
        Assertions.assertEquals("classpath:org/citrusframework/ssh/citrus.priv", client.getEndpointConfiguration().getPrivateKeyPath());
        Assertions.assertEquals("consol", client.getEndpointConfiguration().getPrivateKeyPassword());
        Assertions.assertEquals("classpath:org/citrusframework/ssh/known_hosts", client.getEndpointConfiguration().getKnownHosts());
        Assertions.assertEquals(10000, client.getEndpointConfiguration().getCommandTimeout());
        Assertions.assertEquals(5000, client.getEndpointConfiguration().getConnectionTimeout());
        Assertions.assertTrue(client.getEndpointConfiguration().isStrictHostChecking());
        Assertions.assertEquals(beanDefinitionContext.getBean("sshMessageConverter"), client.getEndpointConfiguration().getMessageConverter());
    }
}
