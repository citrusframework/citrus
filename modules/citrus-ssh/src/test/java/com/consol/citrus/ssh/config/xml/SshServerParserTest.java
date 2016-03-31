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

package com.consol.citrus.ssh.config.xml;

import com.consol.citrus.channel.ChannelEndpointAdapter;
import com.consol.citrus.ssh.server.SshServer;
import com.consol.citrus.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Roland Huss, Christoph Deppisch
 */
public class SshServerParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testSshServerParser() {
        Map<String, SshServer> servers = beanDefinitionContext.getBeansOfType(SshServer.class);

        Assert.assertEquals(servers.size(), 3);

        // 1st server
        SshServer server = servers.get("sshServer1");
        Assert.assertEquals(server.getName(), "sshServer1");
        Assert.assertEquals(server.getPort(), 22);
        Assert.assertFalse(server.isAutoStart());
        Assert.assertNull(server.getAllowedKeyPath());
        Assert.assertNull(server.getHostKeyPath());
        Assert.assertNull(server.getUser());
        Assert.assertNull(server.getPassword());
        Assert.assertTrue(server.getEndpointAdapter() instanceof ChannelEndpointAdapter);
        Assert.assertNotNull(server.getMessageConverter());
        Assert.assertNull(server.getActor());

        // 2nd server
        server = servers.get("sshServer2");
        Assert.assertEquals(server.getName(), "sshServer2");
        Assert.assertEquals(server.getPort(), 10022);
        Assert.assertFalse(server.isAutoStart());
        Assert.assertEquals(server.getAllowedKeyPath(), "classpath:com/consol/citrus/ssh/citrus_pub.pem");
        Assert.assertEquals(server.getHostKeyPath(), "classpath:com/consol/citrus/ssh/citrus.pem");
        Assert.assertEquals(server.getUser(), "foo");
        Assert.assertEquals(server.getPassword(), "bar");
        Assert.assertTrue(server.getEndpointAdapter() instanceof ChannelEndpointAdapter);
        Assert.assertEquals(server.getMessageConverter(), beanDefinitionContext.getBean("sshMessageConverter"));
        Assert.assertNull(server.getActor());

        // 3rd server
        server = servers.get("sshServer3");
        Assert.assertEquals(server.getName(), "sshServer3");
        Assert.assertEquals(server.getPort(), 22);
        Assert.assertFalse(server.isAutoStart());
        Assert.assertNull(server.getAllowedKeyPath());
        Assert.assertNull(server.getHostKeyPath());
        Assert.assertNull(server.getUser());
        Assert.assertNull(server.getPassword());
        Assert.assertEquals(server.getEndpointAdapter(), beanDefinitionContext.getBean("sshServerAdapter"));
        Assert.assertNull(server.getActor());
    }

}
