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

package org.citrusframework.ftp.config.xml;

import java.io.IOException;
import java.util.Map;

import org.citrusframework.channel.ChannelEndpointAdapter;
import org.citrusframework.channel.ChannelEndpointConfiguration;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.adapter.EmptyResponseEndpointAdapter;
import org.citrusframework.endpoint.adapter.StaticResponseEndpointAdapter;
import org.citrusframework.endpoint.adapter.TimeoutProducingEndpointAdapter;
import org.citrusframework.ftp.server.FtpServer;
import org.citrusframework.jms.endpoint.JmsEndpointAdapter;
import org.citrusframework.jms.endpoint.JmsEndpointConfiguration;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import jakarta.jms.ConnectionFactory;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class FtpServerParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testFtpServerParser() throws IOException {
        Map<String, FtpServer> servers = beanDefinitionContext.getBeansOfType(FtpServer.class);

        Assert.assertEquals(servers.size(), 4);

        // 1st message sender
        FtpServer server = servers.get("ftpServer1");
        Assert.assertEquals(server.getName(), "ftpServer1");
        Assert.assertEquals(server.getEndpointConfiguration().getPort(), 22222);
        Assert.assertFalse(server.isAutoStart());
        Assert.assertTrue(server.getEndpointConfiguration().isAutoConnect());
        Assert.assertTrue(server.getEndpointConfiguration().isAutoLogin());
        Assert.assertEquals(server.getEndpointConfiguration().getAutoHandleCommands(), "PORT,TYPE");

        // 2nd message sender
        server = servers.get("ftpServer2");
        Assert.assertEquals(server.getName(), "ftpServer2");
        Assert.assertEquals(server.getEndpointConfiguration().getPort(), 22222);
        Assert.assertEquals(server.getFtpServer(), beanDefinitionContext.getBean("apacheFtpServer"));
        Assert.assertFalse(server.isAutoStart());
        Assert.assertFalse(server.getEndpointConfiguration().isAutoConnect());
        Assert.assertFalse(server.getEndpointConfiguration().isAutoLogin());
        Assert.assertEquals(server.getEndpointConfiguration().getAutoHandleCommands(), "PORT,TYPE,PWD");

        // 3rd message sender
        server = servers.get("ftpServer3");
        Assert.assertEquals(server.getName(), "ftpServer3");
        Assert.assertEquals(server.getEndpointConfiguration().getPort(), 22222);
        Assert.assertEquals(server.getUserManager(), beanDefinitionContext.getBean("userManager"));
        Assert.assertFalse(server.isAutoStart());

        // 4th message sender
        server = servers.get("ftpServer4");
        Assert.assertEquals(server.getName(), "ftpServer4");
        Assert.assertEquals(server.getEndpointConfiguration().getPort(), 22222);

        Assert.assertNotNull(server.getUserManagerProperties().getFile());
        Assert.assertFalse(server.isAutoStart());
        Assert.assertNotNull(server.getInterceptors());
        Assert.assertEquals(server.getInterceptors().size(), 0L);
    }

    @Test
    public void testEndpointAdapter() {
        ApplicationContext beanDefinitionContext = createApplicationContext("adapter");

        Map<String, FtpServer> servers = beanDefinitionContext.getBeansOfType(FtpServer.class);

        Assert.assertEquals(servers.size(), 6);

        // 1st message sender
        FtpServer server = servers.get("ftpServer1");
        Assert.assertEquals(server.getName(), "ftpServer1");
        Assert.assertEquals(server.getEndpointConfiguration().getPort(), 22222);
        Assert.assertNotNull(server.getEndpointAdapter());
        Assert.assertEquals(server.getEndpointAdapter().getClass(), ChannelEndpointAdapter.class);
        Assert.assertNotNull(server.getEndpointAdapter().getEndpoint());
        Assert.assertEquals(server.getEndpointAdapter().getEndpoint().getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertEquals(((ChannelEndpointConfiguration)server.getEndpointAdapter().getEndpoint().getEndpointConfiguration()).getChannelName(), "serverChannel");

        // 2nd message sender
        server = servers.get("ftpServer2");
        Assert.assertEquals(server.getName(), "ftpServer2");
        Assert.assertEquals(server.getEndpointConfiguration().getPort(), 22222);
        Assert.assertNotNull(server.getEndpointAdapter());
        Assert.assertEquals(server.getEndpointAdapter().getClass(), JmsEndpointAdapter.class);
        Assert.assertNotNull(server.getEndpointAdapter().getEndpoint());
        Assert.assertEquals(server.getEndpointAdapter().getEndpoint().getEndpointConfiguration().getTimeout(), 2500);
        Assert.assertEquals(((JmsEndpointConfiguration)server.getEndpointAdapter().getEndpoint().getEndpointConfiguration()).getDestinationName(), "serverQueue");
        Assert.assertEquals(((JmsEndpointConfiguration)server.getEndpointAdapter().getEndpoint().getEndpointConfiguration()).getConnectionFactory(), beanDefinitionContext.getBean("connectionFactory", ConnectionFactory.class));

        // 3rd message sender
        server = servers.get("ftpServer3");
        Assert.assertEquals(server.getName(), "ftpServer3");
        Assert.assertEquals(server.getEndpointConfiguration().getPort(), 22222);
        Assert.assertNotNull(server.getEndpointAdapter());
        Assert.assertEquals(server.getEndpointAdapter().getClass(), EmptyResponseEndpointAdapter.class);

        // 4th message sender
        server = servers.get("ftpServer4");
        Assert.assertEquals(server.getName(), "ftpServer4");
        Assert.assertEquals(server.getEndpointConfiguration().getPort(), 22222);
        Assert.assertNotNull(server.getEndpointAdapter());
        Assert.assertEquals(server.getEndpointAdapter().getClass(), StaticResponseEndpointAdapter.class);
        Assert.assertEquals((((StaticResponseEndpointAdapter) server.getEndpointAdapter()).getMessagePayload()).replaceAll("\\s", ""), "<TestMessage><Text>Hello!</Text></TestMessage>");
        Assert.assertEquals(((StaticResponseEndpointAdapter) server.getEndpointAdapter()).getMessageHeader().get("Operation"), "sayHello");

        // 5th message sender
        server = servers.get("ftpServer5");
        Assert.assertEquals(server.getName(), "ftpServer5");
        Assert.assertEquals(server.getEndpointConfiguration().getPort(), 22222);
        Assert.assertNotNull(server.getEndpointAdapter());
        Assert.assertEquals(server.getEndpointAdapter().getClass(), TimeoutProducingEndpointAdapter.class);

        // 6th message sender
        server = servers.get("ftpServer6");
        Assert.assertEquals(server.getName(), "ftpServer6");
        Assert.assertEquals(server.getEndpointConfiguration().getPort(), 22222);
        Assert.assertNotNull(server.getEndpointAdapter());
        Assert.assertEquals(server.getEndpointAdapter(), beanDefinitionContext.getBean("ftpServerAdapter6", EndpointAdapter.class));
    }
}
