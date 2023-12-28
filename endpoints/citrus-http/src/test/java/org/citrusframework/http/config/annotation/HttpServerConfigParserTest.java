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

package org.citrusframework.http.config.annotation;

import jakarta.servlet.Filter;
import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.config.annotation.ChannelEndpointConfigParser;
import org.citrusframework.config.annotation.ChannelSyncEndpointConfigParser;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.http.message.HttpMessageConverter;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.jms.config.annotation.JmsEndpointConfigParser;
import org.citrusframework.jms.config.annotation.JmsSyncEndpointConfigParser;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.eclipse.jetty.ee10.servlet.ServletHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.citrusframework.config.annotation.AnnotationConfigParser.lookup;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Christoph Deppisch
 */
public class HttpServerConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint(name = "httpServer1")
    @HttpServerConfig(autoStart = false,
            port = 8081)
    private HttpServer httpServer1;

    @CitrusEndpoint
    @HttpServerConfig(autoStart = false,
            port = 8082,
            contextConfigLocation = "classpath:org/citrusframework/http/servlet-context.xml",
            messageConverter = "messageConverter",
            handleAttributeHeaders = true,
            handleCookies = true,
            connector = "connector",
            resourceBase = "src/it/resources",
            rootParentContext = true,
            debugLogging = true,
            binaryMediaTypes = {MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/custom"},
            defaultStatus = HttpStatus.NOT_FOUND,
            contextPath = "/citrus",
            servletName = "citrus-http",
            servletMappingPath = "/foo")
    private HttpServer httpServer2;

    @CitrusEndpoint
    @HttpServerConfig(autoStart = false,
            port = 8083,
            connectors = {"connector1", "connector2"},
            filters = {"filter1", "filter2"},
            filterMappings = {"filter2=/filter2/*"})
    private HttpServer httpServer3;

    @CitrusEndpoint
    @HttpServerConfig(autoStart = false,
            port = 8084,
            servletHandler = "servletHandler")
    private HttpServer httpServer4;

    @CitrusEndpoint
    @HttpServerConfig(autoStart = false,
            port = 8085,
            securityHandler = "securityHandler",
            interceptors = {"clientInterceptor1", "clientInterceptor2"},
            actor = "testActor")
    private HttpServer httpServer5;

    @CitrusEndpoint
    @HttpServerConfig(autoStart = false,
            port = 8086,
            endpointAdapter = "endpointAdapter")
    private HttpServer httpServer6;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private Connector connector1;
    @Mock
    private Connector connector2;
    @Mock
    private Filter filter1;
    @Mock
    private Filter filter2;
    @Mock
    private SecurityHandler securityHandler;
    @Mock
    private HttpMessageConverter messageConverter;
    @Mock
    private EndpointAdapter endpointAdapter;
    @Mock
    private ServletHandler servletHandler;
    @Mock
    private HandlerInterceptor clientInterceptor1;
    @Mock
    private HandlerInterceptor clientInterceptor2;
    @Mock
    private TestActor testActor;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        HashMap<String, Filter> filters = new HashMap<>();
        filters.put("filter1", filter1);
        filters.put("filter2", filter2);
        filters.put("filter3", Mockito.mock(Filter.class));

        when(referenceResolver.resolve("securityHandler", SecurityHandler.class)).thenReturn(securityHandler);
        when(referenceResolver.resolve("messageConverter", HttpMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("servletHandler", ServletHandler.class)).thenReturn(servletHandler);
        when(referenceResolver.resolve("connector", Connector.class)).thenReturn(connector1);
        when(referenceResolver.resolve("connector1", Connector.class)).thenReturn(connector1);
        when(referenceResolver.resolve("connector2", Connector.class)).thenReturn(connector2);
        when(referenceResolver.resolve(new String[]{"connector1", "connector2"}, Connector.class)).thenReturn(Arrays.asList(connector1, connector2));
        when(referenceResolver.resolve("filter1", Filter.class)).thenReturn(filter1);
        when(referenceResolver.resolve("filter2", Filter.class)).thenReturn(filter2);
        when(referenceResolver.resolveAll(Filter.class)).thenReturn(filters);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
        when(referenceResolver.resolve("clientInterceptor1", HandlerInterceptor.class)).thenReturn(clientInterceptor1);
        when(referenceResolver.resolve("clientInterceptor2", HandlerInterceptor.class)).thenReturn(clientInterceptor2);
        when(referenceResolver.resolve(new String[]{"clientInterceptor1", "clientInterceptor2"}, HandlerInterceptor.class)).thenReturn(Arrays.asList(clientInterceptor1, clientInterceptor2));
        when(referenceResolver.resolve("endpointAdapter", EndpointAdapter.class)).thenReturn(endpointAdapter);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testHttpServerParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message sender
        assertNull(httpServer1.getConnector());
        assertNull(httpServer1.getServletHandler());
        assertNull(httpServer1.getSecurityHandler());
        assertEquals(httpServer1.getConnectors().length, 0);
        assertEquals(httpServer1.getFilters().size(), 0);
        assertEquals(httpServer1.getFilterMappings().size(), 0);
        assertEquals(httpServer1.getName(), "httpServer1");
        assertEquals(httpServer1.getPort(), 8081);
        assertEquals(httpServer1.getContextConfigLocation(), "classpath:org/citrusframework/http/citrus-servlet-context.xml");
        assertEquals(httpServer1.getResourceBase(), "src/main/resources");
        assertFalse(httpServer1.isHandleAttributeHeaders());
        assertFalse(httpServer1.isHandleCookies());
        assertFalse(httpServer1.isAutoStart());
        assertFalse(httpServer1.isDebugLogging());
        assertFalse(httpServer1.isUseRootContextAsParent());
        assertEquals(httpServer1.getDefaultStatusCode(), HttpStatus.OK.value());
        assertEquals(httpServer1.getContextPath(), "/");
        assertEquals(httpServer1.getServletName(), "httpServer1-servlet");
        assertEquals(httpServer1.getServletMappingPath(), "/*");
        assertEquals(httpServer1.getBinaryMediaTypes().size(), 6L);

        // 2nd message sender
        assertNotNull(httpServer2.getConnector());
        assertEquals(httpServer2.getMessageConverter(), messageConverter);
        assertEquals(httpServer2.getConnector(), connector1);
        assertEquals(httpServer2.getConnectors().length, 0);
        assertEquals(httpServer2.getFilters().size(), 0);
        assertEquals(httpServer2.getFilterMappings().size(), 0);
        assertEquals(httpServer2.getName(), "httpServer2");
        assertEquals(httpServer2.getPort(), 8082);
        assertEquals(httpServer2.getContextConfigLocation(), "classpath:org/citrusframework/http/servlet-context.xml");
        assertEquals(httpServer2.getResourceBase(), "src/it/resources");
        assertTrue(httpServer2.isHandleAttributeHeaders());
        assertTrue(httpServer2.isHandleCookies());
        assertEquals(httpServer2.getDefaultStatusCode(), HttpStatus.NOT_FOUND.value());
        assertFalse(httpServer2.isAutoStart());
        assertTrue(httpServer2.isDebugLogging());
        assertTrue(httpServer2.isUseRootContextAsParent());
        assertEquals(httpServer2.getContextPath(), "/citrus");
        assertEquals(httpServer2.getServletName(), "citrus-http");
        assertEquals(httpServer2.getServletMappingPath(), "/foo");
        assertEquals(httpServer2.getBinaryMediaTypes().size(), 2L);
        assertTrue(httpServer2.getBinaryMediaTypes().contains(MediaType.valueOf("application/custom")));

        // 3rd message sender
        assertNull(httpServer3.getConnector());
        assertNotNull(httpServer3.getConnectors());
        assertEquals(httpServer3.getConnectors().length, 2L);
        assertNotNull(httpServer3.getFilters());
        assertEquals(httpServer3.getFilters().size(), 2L);
        assertNotNull(httpServer3.getFilterMappings());
        assertEquals(httpServer3.getFilterMappings().size(), 1L);
        assertEquals(httpServer3.getName(), "httpServer3");
        assertEquals(httpServer3.getPort(), 8083);
        assertEquals(httpServer3.getContextConfigLocation(), "classpath:org/citrusframework/http/citrus-servlet-context.xml");
        assertEquals(httpServer3.getResourceBase(), "src/main/resources");
        assertFalse(httpServer3.isAutoStart());
        assertFalse(httpServer3.isUseRootContextAsParent());
        assertEquals(httpServer3.getServletName(), "httpServer3-servlet");

        // 4th message sender
        assertNull(httpServer4.getConnector());
        assertNotNull(httpServer4.getServletHandler());
        assertEquals(httpServer4.getServletHandler(), servletHandler);
        assertEquals(httpServer4.getName(), "httpServer4");
        assertEquals(httpServer4.getPort(), 8084);
        assertEquals(httpServer4.getContextConfigLocation(), "classpath:org/citrusframework/http/citrus-servlet-context.xml");
        assertEquals(httpServer4.getResourceBase(), "src/main/resources");
        assertFalse(httpServer4.isAutoStart());
        assertFalse(httpServer4.isUseRootContextAsParent());
        assertEquals(httpServer4.getServletName(), "httpServer4-servlet");
        assertNotNull(httpServer4.getInterceptors());
        assertEquals(httpServer4.getInterceptors().size(), 0L);

        // 5th message sender
        assertNull(httpServer5.getConnector());
        assertNotNull(httpServer5.getSecurityHandler());
        assertEquals(httpServer5.getSecurityHandler(), securityHandler);
        assertEquals(httpServer5.getName(), "httpServer5");
        assertEquals(httpServer5.getPort(), 8085);
        assertEquals(httpServer5.getContextConfigLocation(), "classpath:org/citrusframework/http/citrus-servlet-context.xml");
        assertEquals(httpServer5.getResourceBase(), "src/main/resources");
        assertFalse(httpServer5.isAutoStart());
        assertFalse(httpServer5.isUseRootContextAsParent());
        assertEquals(httpServer5.getServletName(), "httpServer5-servlet");
        assertNotNull(httpServer5.getInterceptors());
        assertEquals(httpServer5.getInterceptors().size(), 2L);

        // 6th message sender
        assertNull(httpServer6.getConnector());
        assertNotNull(httpServer6.getEndpointAdapter());
        assertEquals(httpServer6.getEndpointAdapter(), endpointAdapter);
        assertEquals(httpServer6.getName(), "httpServer6");
        assertEquals(httpServer6.getPort(), 8086);
        assertEquals(httpServer6.getContextConfigLocation(), "classpath:org/citrusframework/http/citrus-servlet-context.xml");
        assertEquals(httpServer6.getResourceBase(), "src/main/resources");
        assertFalse(httpServer6.isAutoStart());
        assertFalse(httpServer6.isUseRootContextAsParent());
        assertEquals(httpServer6.getServletName(), "httpServer6-servlet");
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser> validators = lookup();
        assertEquals(validators.size(), 8L);
        assertNotNull(validators.get("direct.async"));
        assertEquals(validators.get("direct.async").getClass(), DirectEndpointConfigParser.class);
        assertNotNull(validators.get("direct.sync"));
        assertEquals(validators.get("direct.sync").getClass(), DirectSyncEndpointConfigParser.class);
        assertNotNull(validators.get("jms.async"));
        assertEquals(validators.get("jms.async").getClass(), JmsEndpointConfigParser.class);
        assertNotNull(validators.get("jms.sync"));
        assertEquals(validators.get("jms.sync").getClass(), JmsSyncEndpointConfigParser.class);
        assertNotNull(validators.get("channel.async"));
        assertEquals(validators.get("channel.async").getClass(), ChannelEndpointConfigParser.class);
        assertNotNull(validators.get("channel.sync"));
        assertEquals(validators.get("channel.sync").getClass(), ChannelSyncEndpointConfigParser.class);
        assertNotNull(validators.get("http.client"));
        assertEquals(validators.get("http.client").getClass(), HttpClientConfigParser.class);
        assertNotNull(validators.get("http.server"));
        assertEquals(validators.get("http.server").getClass(), HttpServerConfigParser.class);
    }

    @Test
    public void testLookupByQualifier() {
        assertTrue(lookup("http.server").isPresent());
    }
}
