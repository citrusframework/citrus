/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.http.config.xml;

import com.consol.citrus.channel.ChannelEndpointAdapter;
import com.consol.citrus.channel.ChannelEndpointConfiguration;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.endpoint.adapter.*;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.jms.endpoint.JmsEndpointAdapter;
import com.consol.citrus.jms.endpoint.JmsEndpointConfiguration;
import com.consol.citrus.testng.AbstractBeanDefinitionParserTest;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.jms.ConnectionFactory;
import java.util.List;
import java.util.Map;

/**
 * @author Christoph Deppisch
 */
public class HttpServerParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testHttpServerParser() {
        Map<String, HttpServer> servers = beanDefinitionContext.getBeansOfType(HttpServer.class);
        
        Assert.assertEquals(servers.size(), 5);
        
        // 1st message sender
        HttpServer server = servers.get("httpServer1");
        Assert.assertNull(server.getConnector());
        Assert.assertNull(server.getServletHandler());
        Assert.assertNull(server.getSecurityHandler());
        Assert.assertEquals(server.getConnectors().length, 0);
        Assert.assertEquals(server.getName(), "httpServer1");
        Assert.assertEquals(server.getPort(), 8081);
        Assert.assertEquals(server.getContextConfigLocation(), "classpath:com/consol/citrus/http/citrus-servlet-context.xml");
        Assert.assertEquals(server.getResourceBase(), "src/main/resources");
        Assert.assertFalse(server.isAutoStart());
        Assert.assertFalse(server.isUseRootContextAsParent());
        Assert.assertEquals(server.getContextPath(), "/");
        Assert.assertEquals(server.getServletName(), "httpServer1-servlet");
        Assert.assertEquals(server.getServletMappingPath(), "/*");

        // 2nd message sender
        server = servers.get("httpServer2");
        Assert.assertNotNull(server.getConnector());
        Assert.assertEquals(server.getMessageConverter(), beanDefinitionContext.getBean("messageConverter"));
        Assert.assertEquals(server.getConnector(), beanDefinitionContext.getBean("connector"));
        Assert.assertEquals(server.getConnectors().length, 0);
        Assert.assertEquals(server.getName(), "httpServer2");
        Assert.assertEquals(server.getPort(), 8082);
        Assert.assertEquals(server.getContextConfigLocation(), "classpath:com/consol/citrus/http/servlet-context.xml");
        Assert.assertEquals(server.getResourceBase(), "src/it/resources");
        Assert.assertFalse(server.isAutoStart());
        Assert.assertTrue(server.isUseRootContextAsParent());
        Assert.assertEquals(server.getContextPath(), "/citrus");
        Assert.assertEquals(server.getServletName(), "citrus-http");
        Assert.assertEquals(server.getServletMappingPath(), "/foo");
        
        // 3rd message sender
        server = servers.get("httpServer3");
        Assert.assertNull(server.getConnector());
        Assert.assertNotNull(server.getConnectors());
        Assert.assertEquals(server.getConnectors().length, beanDefinitionContext.getBean("connectors", List.class).size());
        Assert.assertEquals(server.getName(), "httpServer3");
        Assert.assertEquals(server.getPort(), 8083);
        Assert.assertEquals(server.getContextConfigLocation(), "classpath:com/consol/citrus/http/citrus-servlet-context.xml");
        Assert.assertEquals(server.getResourceBase(), "src/main/resources");
        Assert.assertFalse(server.isAutoStart());
        Assert.assertFalse(server.isUseRootContextAsParent());
        Assert.assertEquals(server.getServletName(), "httpServer3-servlet");
        
        // 4th message sender
        server = servers.get("httpServer4");
        Assert.assertNull(server.getConnector());
        Assert.assertNotNull(server.getServletHandler());
        Assert.assertEquals(server.getServletHandler(), beanDefinitionContext.getBean("servletHandler"));
        Assert.assertEquals(server.getName(), "httpServer4");
        Assert.assertEquals(server.getPort(), 8084);
        Assert.assertEquals(server.getContextConfigLocation(), "classpath:com/consol/citrus/http/citrus-servlet-context.xml");
        Assert.assertEquals(server.getResourceBase(), "src/main/resources");
        Assert.assertFalse(server.isAutoStart());
        Assert.assertFalse(server.isUseRootContextAsParent());
        Assert.assertEquals(server.getServletName(), "httpServer4-servlet");
        Assert.assertNotNull(server.getInterceptors());
        Assert.assertEquals(server.getInterceptors().size(), 0L);
        
        // 5th message sender
        server = servers.get("httpServer5");
        Assert.assertNull(server.getConnector());
        Assert.assertNotNull(server.getSecurityHandler());
        Assert.assertEquals(server.getSecurityHandler(), beanDefinitionContext.getBean("securityHandler"));
        Assert.assertEquals(server.getName(), "httpServer5");
        Assert.assertEquals(server.getPort(), 8085);
        Assert.assertEquals(server.getContextConfigLocation(), "classpath:com/consol/citrus/http/citrus-servlet-context.xml");
        Assert.assertEquals(server.getResourceBase(), "src/main/resources");
        Assert.assertFalse(server.isAutoStart());
        Assert.assertFalse(server.isUseRootContextAsParent());
        Assert.assertEquals(server.getServletName(), "httpServer5-servlet");
        Assert.assertNotNull(server.getInterceptors());
        Assert.assertEquals(server.getInterceptors().size(), 2L);
    }

    @Test
    public void testEndpointAdapter() {
        ApplicationContext beanDefinitionContext = createApplicationContext("adapter");

        Map<String, HttpServer> servers = beanDefinitionContext.getBeansOfType(HttpServer.class);

        Assert.assertEquals(servers.size(), 6);

        // 1st message sender
        HttpServer server = servers.get("httpServer1");
        Assert.assertEquals(server.getName(), "httpServer1");
        Assert.assertEquals(server.getPort(), 8081);
        Assert.assertNotNull(server.getEndpointAdapter());
        Assert.assertEquals(server.getEndpointAdapter().getClass(), ChannelEndpointAdapter.class);
        Assert.assertNotNull(server.getEndpointAdapter().getEndpoint());
        Assert.assertEquals(server.getEndpointAdapter().getEndpoint().getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertEquals(((ChannelEndpointConfiguration)server.getEndpointAdapter().getEndpoint().getEndpointConfiguration()).getChannelName(), "serverChannel");

        // 2nd message sender
        server = servers.get("httpServer2");
        Assert.assertEquals(server.getName(), "httpServer2");
        Assert.assertEquals(server.getPort(), 8082);
        Assert.assertNotNull(server.getEndpointAdapter());
        Assert.assertEquals(server.getEndpointAdapter().getClass(), JmsEndpointAdapter.class);
        Assert.assertNotNull(server.getEndpointAdapter().getEndpoint());
        Assert.assertEquals(server.getEndpointAdapter().getEndpoint().getEndpointConfiguration().getTimeout(), 2500);
        Assert.assertEquals(((JmsEndpointConfiguration)server.getEndpointAdapter().getEndpoint().getEndpointConfiguration()).getDestinationName(), "serverQueue");
        Assert.assertEquals(((JmsEndpointConfiguration)server.getEndpointAdapter().getEndpoint().getEndpointConfiguration()).getConnectionFactory(), beanDefinitionContext.getBean("connectionFactory", ConnectionFactory.class));

        // 3rd message sender
        server = servers.get("httpServer3");
        Assert.assertEquals(server.getName(), "httpServer3");
        Assert.assertEquals(server.getPort(), 8083);
        Assert.assertNotNull(server.getEndpointAdapter());
        Assert.assertEquals(server.getEndpointAdapter().getClass(), EmptyResponseEndpointAdapter.class);

        // 4th message sender
        server = servers.get("httpServer4");
        Assert.assertEquals(server.getName(), "httpServer4");
        Assert.assertEquals(server.getPort(), 8084);
        Assert.assertNotNull(server.getEndpointAdapter());
        Assert.assertEquals(server.getEndpointAdapter().getClass(), StaticResponseEndpointAdapter.class);
        Assert.assertEquals(StringUtils.trimAllWhitespace(((StaticResponseEndpointAdapter) server.getEndpointAdapter()).getMessagePayload()), "<TestMessage><Text>Hello!</Text></TestMessage>");
        Assert.assertEquals(((StaticResponseEndpointAdapter) server.getEndpointAdapter()).getMessageHeader().get("Operation"), "sayHello");

        // 5th message sender
        server = servers.get("httpServer5");
        Assert.assertEquals(server.getName(), "httpServer5");
        Assert.assertEquals(server.getPort(), 8085);
        Assert.assertNotNull(server.getEndpointAdapter());
        Assert.assertEquals(server.getEndpointAdapter().getClass(), TimeoutProducingEndpointAdapter.class);

        // 6th message sender
        server = servers.get("httpServer6");
        Assert.assertEquals(server.getName(), "httpServer6");
        Assert.assertEquals(server.getPort(), 8086);
        Assert.assertNotNull(server.getEndpointAdapter());
        Assert.assertEquals(server.getEndpointAdapter(), beanDefinitionContext.getBean("httpServerAdapter6", EndpointAdapter.class));
    }
}
