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

package org.citrusframework.http.config.xml;

import java.util.List;
import java.util.Map;

import org.citrusframework.springintegration.channel.ChannelEndpointAdapter;
import org.citrusframework.springintegration.channel.ChannelEndpointConfiguration;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.base.endpoint.adapter.EmptyResponseEndpointAdapter;
import org.citrusframework.base.endpoint.adapter.StaticResponseEndpointAdapter;
import org.citrusframework.base.endpoint.adapter.TimeoutProducingEndpointAdapter;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerSettings;
import org.citrusframework.jms.endpoint.JmsEndpointAdapter;
import org.citrusframework.jms.endpoint.JmsEndpointConfiguration;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import jakarta.jms.ConnectionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpServerParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testHttpServerParser() {
        Map<String, HttpServer> servers = beanDefinitionContext.getBeansOfType(HttpServer.class);

        assertEquals(5, servers.size());

        // 1st message sender
        HttpServer server = servers.get("httpServer1");
        assertNull(server.getConnector());
        assertNull(server.getServletHandler());
        assertNull(server.getSecurityHandler());
        assertEquals(0, server.getConnectors().length);
        assertEquals(0, server.getFilters().size());
        assertEquals(0, server.getFilterMappings().size());
        assertEquals("httpServer1", server.getName());
        assertEquals(8081, server.getPort());
        assertEquals("classpath:org/citrusframework/http/citrus-servlet-context.xml", server.getContextConfigLocation());
        assertNull(server.getResourceBase());
        assertFalse(server.isAutoStart());
        assertFalse(server.isDebugLogging());
        assertFalse(server.isUseRootContextAsParent());
        assertEquals(HttpStatus.OK.value(), server.getDefaultStatusCode());
        assertEquals(HttpServerSettings.responseCacheSize(), server.getResponseCacheSize());
        assertEquals("/", server.getContextPath());
        assertEquals("httpServer1-servlet", server.getServletName());
        assertEquals("/*", server.getServletMappingPath());
        assertFalse(server.isHandleAttributeHeaders());
        assertFalse(server.isHandleCookies());
        assertEquals(6L, server.getBinaryMediaTypes().size());

        // 2nd message sender
        server = servers.get("httpServer2");
        assertNotNull(server.getConnector());
        assertEquals(beanDefinitionContext.getBean("messageConverter"), server.getMessageConverter());
        assertEquals(beanDefinitionContext.getBean("connector"), server.getConnector());
        assertEquals(0, server.getConnectors().length);
        assertEquals("httpServer2", server.getName());
        assertEquals(8082, server.getPort());
        assertEquals("classpath:org/citrusframework/http/servlet-context.xml", server.getContextConfigLocation());
        assertEquals("src/it/resources", server.getResourceBase());
        assertFalse(server.isAutoStart());
        assertTrue(server.isDebugLogging());
        assertTrue(server.isUseRootContextAsParent());
        assertEquals(HttpStatus.NOT_FOUND.value(), server.getDefaultStatusCode());
        assertEquals(1000, server.getResponseCacheSize());
        assertEquals("/citrus", server.getContextPath());
        assertEquals("citrus-http", server.getServletName());
        assertEquals("/foo", server.getServletMappingPath());
        assertTrue(server.isHandleAttributeHeaders());
        assertTrue(server.isHandleCookies());
        assertEquals(2L, server.getBinaryMediaTypes().size());
        assertTrue(server.getBinaryMediaTypes().contains(MediaType.valueOf("application/custom")));

        // 3rd message sender
        server = servers.get("httpServer3");
        assertNull(server.getConnector());
        assertNotNull(server.getConnectors());
        assertEquals(beanDefinitionContext.getBean("connectors", List.class).size(), server.getConnectors().length);
        assertNotNull(server.getFilters());
        assertEquals(beanDefinitionContext.getBean("filters", Map.class).size(), server.getFilters().size());
        assertNotNull(server.getFilterMappings());
        assertEquals(beanDefinitionContext.getBean("filterMappings", Map.class).size(), server.getFilterMappings().size());
        assertEquals("httpServer3", server.getName());
        assertEquals(8083, server.getPort());
        assertEquals("classpath:org/citrusframework/http/citrus-servlet-context.xml", server.getContextConfigLocation());
        assertNull(server.getResourceBase());
        assertFalse(server.isAutoStart());
        assertFalse(server.isUseRootContextAsParent());
        assertEquals("httpServer3-servlet", server.getServletName());

        // 4th message sender
        server = servers.get("httpServer4");
        assertNull(server.getConnector());
        assertNotNull(server.getServletHandler());
        assertEquals(beanDefinitionContext.getBean("servletHandler"), server.getServletHandler());
        assertEquals("httpServer4", server.getName());
        assertEquals(8084, server.getPort());
        assertEquals("classpath:org/citrusframework/http/citrus-servlet-context.xml", server.getContextConfigLocation());
        assertNull(server.getResourceBase());
        assertFalse(server.isAutoStart());
        assertFalse(server.isUseRootContextAsParent());
        assertEquals("httpServer4-servlet", server.getServletName());
        assertNotNull(server.getInterceptors());
        assertEquals(0L, server.getInterceptors().size());

        // 5th message sender
        server = servers.get("httpServer5");
        assertNull(server.getConnector());
        assertNotNull(server.getSecurityHandler());
        assertEquals(beanDefinitionContext.getBean("securityHandler"), server.getSecurityHandler());
        assertEquals("httpServer5", server.getName());
        assertEquals(8085, server.getPort());
        assertEquals("classpath:org/citrusframework/http/citrus-servlet-context.xml", server.getContextConfigLocation());
        assertNull(server.getResourceBase());
        assertFalse(server.isAutoStart());
        assertFalse(server.isUseRootContextAsParent());
        assertEquals("httpServer5-servlet", server.getServletName());
        assertNotNull(server.getInterceptors());
        assertEquals(2L, server.getInterceptors().size());
    }

    @Test
    public void testEndpointAdapter() {
        ApplicationContext beanDefinitionContext = createApplicationContext("adapter");

        Map<String, HttpServer> servers = beanDefinitionContext.getBeansOfType(HttpServer.class);

        assertEquals(6, servers.size());

        // 1st message sender
        HttpServer server = servers.get("httpServer1");
        assertEquals("httpServer1", server.getName());
        assertEquals(8081, server.getPort());
        assertNotNull(server.getEndpointAdapter());
        assertEquals(ChannelEndpointAdapter.class, server.getEndpointAdapter().getClass());
        assertNotNull(server.getEndpointAdapter().getEndpoint());
        assertEquals(10000L, server.getEndpointAdapter().getEndpoint().getEndpointConfiguration().getTimeout());
        assertEquals("serverChannel", ((ChannelEndpointConfiguration)server.getEndpointAdapter().getEndpoint().getEndpointConfiguration()).getChannelName());

        // 2nd message sender
        server = servers.get("httpServer2");
        assertEquals("httpServer2", server.getName());
        assertEquals(8082, server.getPort());
        assertNotNull(server.getEndpointAdapter());
        assertEquals(JmsEndpointAdapter.class, server.getEndpointAdapter().getClass());
        assertNotNull(server.getEndpointAdapter().getEndpoint());
        assertEquals(2500, server.getEndpointAdapter().getEndpoint().getEndpointConfiguration().getTimeout());
        assertEquals("serverQueue", ((JmsEndpointConfiguration)server.getEndpointAdapter().getEndpoint().getEndpointConfiguration()).getDestinationName());
        assertEquals(beanDefinitionContext.getBean("connectionFactory", ConnectionFactory.class), ((JmsEndpointConfiguration)server.getEndpointAdapter().getEndpoint().getEndpointConfiguration()).getConnectionFactory());

        // 3rd message sender
        server = servers.get("httpServer3");
        assertEquals("httpServer3", server.getName());
        assertEquals(8083, server.getPort());
        assertNotNull(server.getEndpointAdapter());
        assertEquals(EmptyResponseEndpointAdapter.class, server.getEndpointAdapter().getClass());

        // 4th message sender
        server = servers.get("httpServer4");
        assertEquals("httpServer4", server.getName());
        assertEquals(8084, server.getPort());
        assertNotNull(server.getEndpointAdapter());
        assertEquals(StaticResponseEndpointAdapter.class, server.getEndpointAdapter().getClass());
        assertEquals("<TestMessage><Text>Hello!</Text></TestMessage>", ((StaticResponseEndpointAdapter) server.getEndpointAdapter()).getMessagePayload().replaceAll("\\s", ""));
        assertEquals("sayHello", ((StaticResponseEndpointAdapter) server.getEndpointAdapter()).getMessageHeader().get("Operation"));

        // 5th message sender
        server = servers.get("httpServer5");
        assertEquals("httpServer5", server.getName());
        assertEquals(8085, server.getPort());
        assertNotNull(server.getEndpointAdapter());
        assertEquals(TimeoutProducingEndpointAdapter.class, server.getEndpointAdapter().getClass());

        // 6th message sender
        server = servers.get("httpServer6");
        assertEquals("httpServer6", server.getName());
        assertEquals(8086, server.getPort());
        assertNotNull(server.getEndpointAdapter());
        assertEquals(beanDefinitionContext.getBean("httpServerAdapter6", EndpointAdapter.class), server.getEndpointAdapter());
    }
}
