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

package org.citrusframework.ftp.config.xml;

import java.util.Map;

import org.citrusframework.springintegration.channel.ChannelEndpointAdapter;
import org.citrusframework.springintegration.channel.ChannelEndpointConfiguration;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.base.endpoint.adapter.EmptyResponseEndpointAdapter;
import org.citrusframework.base.endpoint.adapter.StaticResponseEndpointAdapter;
import org.citrusframework.base.endpoint.adapter.TimeoutProducingEndpointAdapter;
import org.citrusframework.ftp.server.FtpServer;
import org.citrusframework.jms.endpoint.JmsEndpointAdapter;
import org.citrusframework.jms.endpoint.JmsEndpointConfiguration;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import jakarta.jms.ConnectionFactory;
import org.springframework.context.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 2.0
 */
public class FtpServerParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testFtpServerParser() {
        Map<String, FtpServer> servers = beanDefinitionContext.getBeansOfType(FtpServer.class);

        Assertions.assertEquals(4, servers.size());

        // 1st message sender
        FtpServer server = servers.get("ftpServer1");
        Assertions.assertEquals("ftpServer1", server.getName());
        Assertions.assertEquals(22222, server.getEndpointConfiguration().getPort());
        Assertions.assertFalse(server.isAutoStart());
        Assertions.assertTrue(server.getEndpointConfiguration().isAutoConnect());
        Assertions.assertTrue(server.getEndpointConfiguration().isAutoLogin());
        Assertions.assertEquals("PORT,TYPE", server.getEndpointConfiguration().getAutoHandleCommands());

        // 2nd message sender
        server = servers.get("ftpServer2");
        Assertions.assertEquals("ftpServer2", server.getName());
        Assertions.assertEquals(22222, server.getEndpointConfiguration().getPort());
        Assertions.assertEquals(beanDefinitionContext.getBean("apacheFtpServer"), server.getFtpServer());
        Assertions.assertFalse(server.isAutoStart());
        Assertions.assertFalse(server.getEndpointConfiguration().isAutoConnect());
        Assertions.assertFalse(server.getEndpointConfiguration().isAutoLogin());
        Assertions.assertEquals("PORT,TYPE,PWD", server.getEndpointConfiguration().getAutoHandleCommands());

        // 3rd message sender
        server = servers.get("ftpServer3");
        Assertions.assertEquals("ftpServer3", server.getName());
        Assertions.assertEquals(22222, server.getEndpointConfiguration().getPort());
        Assertions.assertEquals(beanDefinitionContext.getBean("userManager"), server.getUserManager());
        Assertions.assertFalse(server.isAutoStart());

        // 4th message sender
        server = servers.get("ftpServer4");
        Assertions.assertEquals("ftpServer4", server.getName());
        Assertions.assertEquals(22222, server.getEndpointConfiguration().getPort());

        Assertions.assertNotNull(server.getUserManagerProperties().file());
        Assertions.assertFalse(server.isAutoStart());
        Assertions.assertNotNull(server.getInterceptors());
        Assertions.assertEquals(0L, server.getInterceptors().size());
    }

    @Test
    public void testEndpointAdapter() {
        ApplicationContext beanDefinitionContext = createApplicationContext("adapter");

        Map<String, FtpServer> servers = beanDefinitionContext.getBeansOfType(FtpServer.class);

        Assertions.assertEquals(6, servers.size());

        // 1st message sender
        FtpServer server = servers.get("ftpServer1");
        Assertions.assertEquals("ftpServer1", server.getName());
        Assertions.assertEquals(22222, server.getEndpointConfiguration().getPort());
        Assertions.assertNotNull(server.getEndpointAdapter());
        Assertions.assertEquals(ChannelEndpointAdapter.class, server.getEndpointAdapter().getClass());
        Assertions.assertNotNull(server.getEndpointAdapter().getEndpoint());
        Assertions.assertEquals(10000L, server.getEndpointAdapter().getEndpoint().getEndpointConfiguration().getTimeout());
        Assertions.assertEquals("serverChannel", ((ChannelEndpointConfiguration)server.getEndpointAdapter().getEndpoint().getEndpointConfiguration()).getChannelName());

        // 2nd message sender
        server = servers.get("ftpServer2");
        Assertions.assertEquals("ftpServer2", server.getName());
        Assertions.assertEquals(22222, server.getEndpointConfiguration().getPort());
        Assertions.assertNotNull(server.getEndpointAdapter());
        Assertions.assertEquals(JmsEndpointAdapter.class, server.getEndpointAdapter().getClass());
        Assertions.assertNotNull(server.getEndpointAdapter().getEndpoint());
        Assertions.assertEquals(2500, server.getEndpointAdapter().getEndpoint().getEndpointConfiguration().getTimeout());
        Assertions.assertEquals("serverQueue", ((JmsEndpointConfiguration)server.getEndpointAdapter().getEndpoint().getEndpointConfiguration()).getDestinationName());
        Assertions.assertEquals(beanDefinitionContext.getBean("connectionFactory", ConnectionFactory.class), ((JmsEndpointConfiguration)server.getEndpointAdapter().getEndpoint().getEndpointConfiguration()).getConnectionFactory());

        // 3rd message sender
        server = servers.get("ftpServer3");
        Assertions.assertEquals("ftpServer3", server.getName());
        Assertions.assertEquals(22222, server.getEndpointConfiguration().getPort());
        Assertions.assertNotNull(server.getEndpointAdapter());
        Assertions.assertEquals(EmptyResponseEndpointAdapter.class, server.getEndpointAdapter().getClass());

        // 4th message sender
        server = servers.get("ftpServer4");
        Assertions.assertEquals("ftpServer4", server.getName());
        Assertions.assertEquals(22222, server.getEndpointConfiguration().getPort());
        Assertions.assertNotNull(server.getEndpointAdapter());
        Assertions.assertEquals(StaticResponseEndpointAdapter.class, server.getEndpointAdapter().getClass());
        Assertions.assertEquals("<TestMessage><Text>Hello!</Text></TestMessage>", (((StaticResponseEndpointAdapter) server.getEndpointAdapter()).getMessagePayload()).replaceAll("\\s", ""));
        Assertions.assertEquals("sayHello", ((StaticResponseEndpointAdapter) server.getEndpointAdapter()).getMessageHeader().get("Operation"));

        // 5th message sender
        server = servers.get("ftpServer5");
        Assertions.assertEquals("ftpServer5", server.getName());
        Assertions.assertEquals(22222, server.getEndpointConfiguration().getPort());
        Assertions.assertNotNull(server.getEndpointAdapter());
        Assertions.assertEquals(TimeoutProducingEndpointAdapter.class, server.getEndpointAdapter().getClass());

        // 6th message sender
        server = servers.get("ftpServer6");
        Assertions.assertEquals("ftpServer6", server.getName());
        Assertions.assertEquals(22222, server.getEndpointConfiguration().getPort());
        Assertions.assertNotNull(server.getEndpointAdapter());
        Assertions.assertEquals(beanDefinitionContext.getBean("ftpServerAdapter6", EndpointAdapter.class), server.getEndpointAdapter());
    }
}
