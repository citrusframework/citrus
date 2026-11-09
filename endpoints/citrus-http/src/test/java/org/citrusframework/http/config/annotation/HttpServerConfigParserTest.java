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

package org.citrusframework.http.config.annotation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.Filter;
import org.citrusframework.TestActor;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.springintegration.config.annotation.ChannelEndpointConfigParser;
import org.citrusframework.springintegration.config.annotation.ChannelSyncEndpointConfigParser;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.StaticEndpoint;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.http.message.HttpMessageConverter;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.jms.config.annotation.JmsEndpointConfigParser;
import org.citrusframework.jms.config.annotation.JmsSyncEndpointConfigParser;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.eclipse.jetty.ee11.servlet.ServletHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;

import static org.citrusframework.config.annotation.AnnotationConfigParser.lookup;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class HttpServerConfigParserTest extends AbstractUnitTest {

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

    @BeforeEach
    public void setup() {
        HashMap<String, Filter> filters = new HashMap<>();
        filters.put("filter1", filter1);
        filters.put("filter2", filter2);
        filters.put("filter3", Mockito.mock(Filter.class));

        lenient().when(referenceResolver.resolve("securityHandler", SecurityHandler.class)).thenReturn(securityHandler);
        lenient().when(referenceResolver.resolve("messageConverter", HttpMessageConverter.class)).thenReturn(messageConverter);
        lenient().when(referenceResolver.resolve("servletHandler", ServletHandler.class)).thenReturn(servletHandler);
        lenient().when(referenceResolver.resolve("connector", Connector.class)).thenReturn(connector1);
        lenient().when(referenceResolver.resolve("connector1", Connector.class)).thenReturn(connector1);
        lenient().when(referenceResolver.resolve("connector2", Connector.class)).thenReturn(connector2);
        lenient().when(referenceResolver.resolve(new String[]{"connector1", "connector2"}, Connector.class)).thenReturn(Arrays.asList(connector1, connector2));
        lenient().when(referenceResolver.resolve("filter1", Filter.class)).thenReturn(filter1);
        lenient().when(referenceResolver.resolve("filter2", Filter.class)).thenReturn(filter2);
        lenient().when(referenceResolver.resolveAll(Filter.class)).thenReturn(filters);
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
        lenient().when(referenceResolver.resolve("clientInterceptor1", HandlerInterceptor.class)).thenReturn(clientInterceptor1);
        lenient().when(referenceResolver.resolve("clientInterceptor2", HandlerInterceptor.class)).thenReturn(clientInterceptor2);
        lenient().when(referenceResolver.resolve(new String[]{"clientInterceptor1", "clientInterceptor2"}, HandlerInterceptor.class)).thenReturn(Arrays.asList(clientInterceptor1, clientInterceptor2));
        lenient().when(referenceResolver.resolve("endpointAdapter", EndpointAdapter.class)).thenReturn(endpointAdapter);

        lenient().when(endpointAdapter.getEndpoint()).thenReturn(new StaticEndpoint());
        lenient().when(endpointAdapter.getEndpointConfiguration()).thenReturn(new StaticEndpoint.StaticEndpointConfiguration());
    }

    @BeforeEach
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
        assertEquals( 0,httpServer1.getConnectors().length);
        assertEquals( 0,httpServer1.getFilters().size());
        assertEquals( 0,httpServer1.getFilterMappings().size());
        assertEquals( "httpServer1",httpServer1.getName());
        assertEquals( 8081,httpServer1.getPort());
        assertEquals( "classpath:org/citrusframework/http/citrus-servlet-context.xml",httpServer1.getContextConfigLocation());
        assertNull(httpServer1.getResourceBase());
        assertFalse(httpServer1.isHandleAttributeHeaders());
        assertFalse(httpServer1.isHandleCookies());
        assertFalse(httpServer1.isAutoStart());
        assertFalse(httpServer1.isDebugLogging());
        assertFalse(httpServer1.isUseRootContextAsParent());
        assertEquals( HttpStatus.OK.value(),httpServer1.getDefaultStatusCode());
        assertEquals( "/",httpServer1.getContextPath());
        assertEquals( "httpServer1-servlet",httpServer1.getServletName());
        assertEquals( "/*",httpServer1.getServletMappingPath());
        assertEquals( 6L,httpServer1.getBinaryMediaTypes().size());

        // 2nd message sender
        assertNotNull(httpServer2.getConnector());
        assertEquals( messageConverter,httpServer2.getMessageConverter());
        assertEquals( connector1,httpServer2.getConnector());
        assertEquals( 0,httpServer2.getConnectors().length);
        assertEquals( 0,httpServer2.getFilters().size());
        assertEquals( 0,httpServer2.getFilterMappings().size());
        assertEquals( "httpServer2",httpServer2.getName());
        assertEquals( 8082,httpServer2.getPort());
        assertEquals( "classpath:org/citrusframework/http/servlet-context.xml",httpServer2.getContextConfigLocation());
        assertEquals( "src/it/resources",httpServer2.getResourceBase());
        assertTrue(httpServer2.isHandleAttributeHeaders());
        assertTrue(httpServer2.isHandleCookies());
        assertEquals( HttpStatus.NOT_FOUND.value(),httpServer2.getDefaultStatusCode());
        assertFalse(httpServer2.isAutoStart());
        assertTrue(httpServer2.isDebugLogging());
        assertTrue(httpServer2.isUseRootContextAsParent());
        assertEquals( "/citrus",httpServer2.getContextPath());
        assertEquals( "citrus-http",httpServer2.getServletName());
        assertEquals( "/foo",httpServer2.getServletMappingPath());
        assertEquals( 2L,httpServer2.getBinaryMediaTypes().size());
        assertTrue(httpServer2.getBinaryMediaTypes().contains(MediaType.valueOf("application/custom")));

        // 3rd message sender
        assertNull(httpServer3.getConnector());
        assertNotNull(httpServer3.getConnectors());
        assertEquals( 2L,httpServer3.getConnectors().length);
        assertNotNull(httpServer3.getFilters());
        assertEquals( 2L,httpServer3.getFilters().size());
        assertNotNull(httpServer3.getFilterMappings());
        assertEquals( 1L,httpServer3.getFilterMappings().size());
        assertEquals( "httpServer3",httpServer3.getName());
        assertEquals( 8083,httpServer3.getPort());
        assertEquals( "classpath:org/citrusframework/http/citrus-servlet-context.xml",httpServer3.getContextConfigLocation());
        assertNull(httpServer3.getResourceBase());
        assertFalse(httpServer3.isAutoStart());
        assertFalse(httpServer3.isUseRootContextAsParent());
        assertEquals( "httpServer3-servlet",httpServer3.getServletName());

        // 4th message sender
        assertNull(httpServer4.getConnector());
        assertNotNull(httpServer4.getServletHandler());
        assertEquals( servletHandler,httpServer4.getServletHandler());
        assertEquals( "httpServer4",httpServer4.getName());
        assertEquals( 8084,httpServer4.getPort());
        assertEquals( "classpath:org/citrusframework/http/citrus-servlet-context.xml",httpServer4.getContextConfigLocation());
        assertNull(httpServer4.getResourceBase());
        assertFalse(httpServer4.isAutoStart());
        assertFalse(httpServer4.isUseRootContextAsParent());
        assertEquals( "httpServer4-servlet",httpServer4.getServletName());
        assertNotNull(httpServer4.getInterceptors());
        assertEquals( 0L,httpServer4.getInterceptors().size());

        // 5th message sender
        assertNull(httpServer5.getConnector());
        assertNotNull(httpServer5.getSecurityHandler());
        assertEquals( securityHandler,httpServer5.getSecurityHandler());
        assertEquals( "httpServer5",httpServer5.getName());
        assertEquals( 8085,httpServer5.getPort());
        assertEquals( "classpath:org/citrusframework/http/citrus-servlet-context.xml",httpServer5.getContextConfigLocation());
        assertNull(httpServer5.getResourceBase());
        assertFalse(httpServer5.isAutoStart());
        assertFalse(httpServer5.isUseRootContextAsParent());
        assertEquals( "httpServer5-servlet",httpServer5.getServletName());
        assertNotNull(httpServer5.getInterceptors());
        assertEquals( 2L,httpServer5.getInterceptors().size());

        // 6th message sender
        assertNull(httpServer6.getConnector());
        assertNotNull(httpServer6.getEndpointAdapter());
        assertEquals( endpointAdapter,httpServer6.getEndpointAdapter());
        assertEquals( "httpServer6",httpServer6.getName());
        assertEquals( 8086,httpServer6.getPort());
        assertEquals( "classpath:org/citrusframework/http/citrus-servlet-context.xml",httpServer6.getContextConfigLocation());
        assertNull(httpServer6.getResourceBase());
        assertFalse(httpServer6.isAutoStart());
        assertFalse(httpServer6.isUseRootContextAsParent());
        assertEquals( "httpServer6-servlet",httpServer6.getServletName());
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser<?, ?>> validators = lookup();
        assertEquals( 8L,validators.size());
        assertNotNull(validators.get("direct.async"));
        assertEquals( DirectEndpointConfigParser.class,validators.get("direct.async").getClass());
        assertNotNull(validators.get("direct.sync"));
        assertEquals( DirectSyncEndpointConfigParser.class,validators.get("direct.sync").getClass());
        assertNotNull(validators.get("jms.async"));
        assertEquals( JmsEndpointConfigParser.class,validators.get("jms.async").getClass());
        assertNotNull(validators.get("jms.sync"));
        assertEquals( JmsSyncEndpointConfigParser.class,validators.get("jms.sync").getClass());
        assertNotNull(validators.get("channel.async"));
        assertEquals( ChannelEndpointConfigParser.class,validators.get("channel.async").getClass());
        assertNotNull(validators.get("channel.sync"));
        assertEquals( ChannelSyncEndpointConfigParser.class,validators.get("channel.sync").getClass());
        assertNotNull(validators.get("http.client"));
        assertEquals( HttpClientConfigParser.class,validators.get("http.client").getClass());
        assertNotNull(validators.get("http.server"));
        assertEquals( HttpServerConfigParser.class,validators.get("http.server").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        assertTrue(lookup("http.server").isPresent());
    }
}
