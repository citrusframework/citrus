/*
 * Copyright 2006-2024 the original author or authors.
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

package org.citrusframework.http.config.xml;

import java.util.List;
import java.util.Map;

import org.citrusframework.channel.ChannelEndpointAdapter;
import org.citrusframework.channel.ChannelEndpointConfiguration;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.adapter.EmptyResponseEndpointAdapter;
import org.citrusframework.endpoint.adapter.StaticResponseEndpointAdapter;
import org.citrusframework.endpoint.adapter.TimeoutProducingEndpointAdapter;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerSettings;
import org.citrusframework.jms.endpoint.JmsEndpointAdapter;
import org.citrusframework.jms.endpoint.JmsEndpointConfiguration;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import jakarta.jms.ConnectionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Christoph Deppisch
 */
public class HttpServerParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testHttpServerParser() {
        Map<String, HttpServer> servers = beanDefinitionContext.getBeansOfType(HttpServer.class);

        assertEquals(servers.size(), 5);

        // 1st message sender
        HttpServer server = servers.get("httpServer1");
        assertNull(server.getConnector());
        assertNull(server.getServletHandler());
        assertNull(server.getSecurityHandler());
        assertEquals(server.getConnectors().length, 0);
        assertEquals(server.getFilters().size(), 0);
        assertEquals(server.getFilterMappings().size(), 0);
        assertEquals(server.getName(), "httpServer1");
        assertEquals(server.getPort(), 8081);
        assertEquals(server.getContextConfigLocation(), "classpath:org/citrusframework/http/citrus-servlet-context.xml");
        assertEquals(server.getResourceBase(), "src/main/resources");
        assertFalse(server.isAutoStart());
        assertFalse(server.isDebugLogging());
        assertFalse(server.isUseRootContextAsParent());
        assertEquals(server.getDefaultStatusCode(), HttpStatus.OK.value());
        assertEquals(server.getResponseCacheSize(), HttpServerSettings.responseCacheSize());
        assertEquals(server.getContextPath(), "/");
        assertEquals(server.getServletName(), "httpServer1-servlet");
        assertEquals(server.getServletMappingPath(), "/*");
        assertFalse(server.isHandleAttributeHeaders());
        assertFalse(server.isHandleCookies());
        assertEquals(server.getBinaryMediaTypes().size(), 6L);

        // 2nd message sender
        server = servers.get("httpServer2");
        assertNotNull(server.getConnector());
        assertEquals(server.getMessageConverter(), beanDefinitionContext.getBean("messageConverter"));
        assertEquals(server.getConnector(), beanDefinitionContext.getBean("connector"));
        assertEquals(server.getConnectors().length, 0);
        assertEquals(server.getName(), "httpServer2");
        assertEquals(server.getPort(), 8082);
        assertEquals(server.getContextConfigLocation(), "classpath:org/citrusframework/http/servlet-context.xml");
        assertEquals(server.getResourceBase(), "src/it/resources");
        assertFalse(server.isAutoStart());
        assertTrue(server.isDebugLogging());
        assertTrue(server.isUseRootContextAsParent());
        assertEquals(server.getDefaultStatusCode(), HttpStatus.NOT_FOUND.value());
        assertEquals(server.getResponseCacheSize(), 1000);
        assertEquals(server.getContextPath(), "/citrus");
        assertEquals(server.getServletName(), "citrus-http");
        assertEquals(server.getServletMappingPath(), "/foo");
        assertTrue(server.isHandleAttributeHeaders());
        assertTrue(server.isHandleCookies());
        assertEquals(server.getBinaryMediaTypes().size(), 2L);
        assertTrue(server.getBinaryMediaTypes().contains(MediaType.valueOf("application/custom")));

        // 3rd message sender
        server = servers.get("httpServer3");
        assertNull(server.getConnector());
        assertNotNull(server.getConnectors());
        assertEquals(server.getConnectors().length, beanDefinitionContext.getBean("connectors", List.class).size());
        assertNotNull(server.getFilters());
        assertEquals(server.getFilters().size(), beanDefinitionContext.getBean("filters", Map.class).size());
        assertNotNull(server.getFilterMappings());
        assertEquals(server.getFilterMappings().size(), beanDefinitionContext.getBean("filterMappings", Map.class).size());
        assertEquals(server.getName(), "httpServer3");
        assertEquals(server.getPort(), 8083);
        assertEquals(server.getContextConfigLocation(), "classpath:org/citrusframework/http/citrus-servlet-context.xml");
        assertEquals(server.getResourceBase(), "src/main/resources");
        assertFalse(server.isAutoStart());
        assertFalse(server.isUseRootContextAsParent());
        assertEquals(server.getServletName(), "httpServer3-servlet");

        // 4th message sender
        server = servers.get("httpServer4");
        assertNull(server.getConnector());
        assertNotNull(server.getServletHandler());
        assertEquals(server.getServletHandler(), beanDefinitionContext.getBean("servletHandler"));
        assertEquals(server.getName(), "httpServer4");
        assertEquals(server.getPort(), 8084);
        assertEquals(server.getContextConfigLocation(), "classpath:org/citrusframework/http/citrus-servlet-context.xml");
        assertEquals(server.getResourceBase(), "src/main/resources");
        assertFalse(server.isAutoStart());
        assertFalse(server.isUseRootContextAsParent());
        assertEquals(server.getServletName(), "httpServer4-servlet");
        assertNotNull(server.getInterceptors());
        assertEquals(server.getInterceptors().size(), 0L);

        // 5th message sender
        server = servers.get("httpServer5");
        assertNull(server.getConnector());
        assertNotNull(server.getSecurityHandler());
        assertEquals(server.getSecurityHandler(), beanDefinitionContext.getBean("securityHandler"));
        assertEquals(server.getName(), "httpServer5");
        assertEquals(server.getPort(), 8085);
        assertEquals(server.getContextConfigLocation(), "classpath:org/citrusframework/http/citrus-servlet-context.xml");
        assertEquals(server.getResourceBase(), "src/main/resources");
        assertFalse(server.isAutoStart());
        assertFalse(server.isUseRootContextAsParent());
        assertEquals(server.getServletName(), "httpServer5-servlet");
        assertNotNull(server.getInterceptors());
        assertEquals(server.getInterceptors().size(), 2L);
    }

    @Test
    public void testEndpointAdapter() {
        ApplicationContext beanDefinitionContext = createApplicationContext("adapter");

        Map<String, HttpServer> servers = beanDefinitionContext.getBeansOfType(HttpServer.class);

        assertEquals(servers.size(), 6);

        // 1st message sender
        HttpServer server = servers.get("httpServer1");
        assertEquals(server.getName(), "httpServer1");
        assertEquals(server.getPort(), 8081);
        assertNotNull(server.getEndpointAdapter());
        assertEquals(server.getEndpointAdapter().getClass(), ChannelEndpointAdapter.class);
        assertNotNull(server.getEndpointAdapter().getEndpoint());
        assertEquals(server.getEndpointAdapter().getEndpoint().getEndpointConfiguration().getTimeout(), 10000L);
        assertEquals(((ChannelEndpointConfiguration)server.getEndpointAdapter().getEndpoint().getEndpointConfiguration()).getChannelName(), "serverChannel");

        // 2nd message sender
        server = servers.get("httpServer2");
        assertEquals(server.getName(), "httpServer2");
        assertEquals(server.getPort(), 8082);
        assertNotNull(server.getEndpointAdapter());
        assertEquals(server.getEndpointAdapter().getClass(), JmsEndpointAdapter.class);
        assertNotNull(server.getEndpointAdapter().getEndpoint());
        assertEquals(server.getEndpointAdapter().getEndpoint().getEndpointConfiguration().getTimeout(), 2500);
        assertEquals(((JmsEndpointConfiguration)server.getEndpointAdapter().getEndpoint().getEndpointConfiguration()).getDestinationName(), "serverQueue");
        assertEquals(((JmsEndpointConfiguration)server.getEndpointAdapter().getEndpoint().getEndpointConfiguration()).getConnectionFactory(), beanDefinitionContext.getBean("connectionFactory", ConnectionFactory.class));

        // 3rd message sender
        server = servers.get("httpServer3");
        assertEquals(server.getName(), "httpServer3");
        assertEquals(server.getPort(), 8083);
        assertNotNull(server.getEndpointAdapter());
        assertEquals(server.getEndpointAdapter().getClass(), EmptyResponseEndpointAdapter.class);

        // 4th message sender
        server = servers.get("httpServer4");
        assertEquals(server.getName(), "httpServer4");
        assertEquals(server.getPort(), 8084);
        assertNotNull(server.getEndpointAdapter());
        assertEquals(server.getEndpointAdapter().getClass(), StaticResponseEndpointAdapter.class);
        assertEquals(((StaticResponseEndpointAdapter) server.getEndpointAdapter()).getMessagePayload().replaceAll("\\s", ""), "<TestMessage><Text>Hello!</Text></TestMessage>");
        assertEquals(((StaticResponseEndpointAdapter) server.getEndpointAdapter()).getMessageHeader().get("Operation"), "sayHello");

        // 5th message sender
        server = servers.get("httpServer5");
        assertEquals(server.getName(), "httpServer5");
        assertEquals(server.getPort(), 8085);
        assertNotNull(server.getEndpointAdapter());
        assertEquals(server.getEndpointAdapter().getClass(), TimeoutProducingEndpointAdapter.class);

        // 6th message sender
        server = servers.get("httpServer6");
        assertEquals(server.getName(), "httpServer6");
        assertEquals(server.getPort(), 8086);
        assertNotNull(server.getEndpointAdapter());
        assertEquals(server.getEndpointAdapter(), beanDefinitionContext.getBean("httpServerAdapter6", EndpointAdapter.class));
    }
}
