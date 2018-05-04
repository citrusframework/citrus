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

package com.consol.citrus.http.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.http.message.HttpMessageConverter;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.servlet.Filter;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class HttpServerConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint(name = "httpServer1")
    @HttpServerConfig(autoStart=false,
            port=8081)
    private HttpServer httpServer1;

    @CitrusEndpoint
    @HttpServerConfig(autoStart=false,
            port=8082,
            contextConfigLocation="classpath:com/consol/citrus/http/servlet-context.xml",
            messageConverter="messageConverter",
            handleAttributeHeaders=true,
            handleCookies=true,
            connector="connector",
            resourceBase="src/it/resources",
            rootParentContext=true,
            debugLogging=true,
            binaryMediaTypes = {MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/custom"},
            defaultStatus = HttpStatus.NOT_FOUND,
            contextPath="/citrus",
            servletName="citrus-http",
            servletMappingPath="/foo")
    private HttpServer httpServer2;

    @CitrusEndpoint
    @HttpServerConfig(autoStart=false,
            port=8083,
            connectors={"connector1", "connector2"},
            filters={"filter1", "filter2"},
            filterMappings={"filter2=/filter2/*"})
    private HttpServer httpServer3;

    @CitrusEndpoint
    @HttpServerConfig(autoStart=false,
            port=8084,
            servletHandler="servletHandler")
    private HttpServer httpServer4;

    @CitrusEndpoint
    @HttpServerConfig(autoStart=false,
            port=8085,
            securityHandler="securityHandler",
            interceptors={ "clientInterceptor1", "clientInterceptor2" },
            actor = "testActor")
    private HttpServer httpServer5;

    @CitrusEndpoint
    @HttpServerConfig(autoStart=false,
            port=8086,
            endpointAdapter="endpointAdapter")
    private HttpServer httpServer6;

    @Autowired
    private SpringBeanReferenceResolver referenceResolver;

    @Mock
    private Connector connector1 = Mockito.mock(Connector.class);
    @Mock
    private Connector connector2 = Mockito.mock(Connector.class);
    @Mock
    private Filter filter1 = Mockito.mock(Filter.class);
    @Mock
    private Filter filter2 = Mockito.mock(Filter.class);
    @Mock
    private SecurityHandler securityHandler = Mockito.mock(SecurityHandler.class);
    @Mock
    private HttpMessageConverter messageConverter = Mockito.mock(HttpMessageConverter.class);
    @Mock
    private EndpointAdapter endpointAdapter = Mockito.mock(EndpointAdapter.class);
    @Mock
    private ServletHandler servletHandler = Mockito.mock(ServletHandler.class);
    @Mock
    private HandlerInterceptor clientInterceptor1 = Mockito.mock(HandlerInterceptor.class);
    @Mock
    private HandlerInterceptor clientInterceptor2 = Mockito.mock(HandlerInterceptor.class);
    @Mock
    private TestActor testActor = Mockito.mock(TestActor.class);
    @Mock
    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);

        referenceResolver.setApplicationContext(applicationContext);

        when(applicationContext.getBean("securityHandler", SecurityHandler.class)).thenReturn(securityHandler);
        when(applicationContext.getBean("messageConverter", HttpMessageConverter.class)).thenReturn(messageConverter);
        when(applicationContext.getBean("servletHandler", ServletHandler.class)).thenReturn(servletHandler);
        when(applicationContext.getBean("connector", Connector.class)).thenReturn(connector1);
        when(applicationContext.getBean("connector1", Connector.class)).thenReturn(connector1);
        when(applicationContext.getBean("connector2", Connector.class)).thenReturn(connector2);
        when(applicationContext.getBean("filter1", Filter.class)).thenReturn(filter1);
        when(applicationContext.getBean("filter2", Filter.class)).thenReturn(filter2);
        when(applicationContext.getBean("testActor", TestActor.class)).thenReturn(testActor);
        when(applicationContext.getBean("clientInterceptor1", HandlerInterceptor.class)).thenReturn(clientInterceptor1);
        when(applicationContext.getBean("clientInterceptor2", HandlerInterceptor.class)).thenReturn(clientInterceptor2);
        when(applicationContext.getBean("endpointAdapter", EndpointAdapter.class)).thenReturn(endpointAdapter);
    }

    @Test
    public void testHttpServerParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message sender
        Assert.assertNull(httpServer1.getConnector());
        Assert.assertNull(httpServer1.getServletHandler());
        Assert.assertNull(httpServer1.getSecurityHandler());
        Assert.assertEquals(httpServer1.getConnectors().length, 0);
        Assert.assertEquals(httpServer1.getFilters().size(), 0);
        Assert.assertEquals(httpServer1.getFilterMappings().size(), 0);
        Assert.assertEquals(httpServer1.getName(), "httpServer1");
        Assert.assertEquals(httpServer1.getPort(), 8081);
        Assert.assertEquals(httpServer1.getContextConfigLocation(), "classpath:com/consol/citrus/http/citrus-servlet-context.xml");
        Assert.assertEquals(httpServer1.getResourceBase(), "src/main/resources");
        Assert.assertFalse(httpServer1.isHandleAttributeHeaders());
        Assert.assertFalse(httpServer1.isHandleCookies());
        Assert.assertFalse(httpServer1.isAutoStart());
        Assert.assertFalse(httpServer1.isDebugLogging());
        Assert.assertFalse(httpServer1.isUseRootContextAsParent());
        Assert.assertEquals(httpServer1.getDefaultStatusCode(), HttpStatus.OK.value());
        Assert.assertEquals(httpServer1.getContextPath(), "/");
        Assert.assertEquals(httpServer1.getServletName(), "httpServer1-servlet");
        Assert.assertEquals(httpServer1.getServletMappingPath(), "/*");
        Assert.assertEquals(httpServer1.getBinaryMediaTypes().size(), 6L);

        // 2nd message sender
        Assert.assertNotNull(httpServer2.getConnector());
        Assert.assertEquals(httpServer2.getMessageConverter(), messageConverter);
        Assert.assertEquals(httpServer2.getConnector(), connector1);
        Assert.assertEquals(httpServer2.getConnectors().length, 0);
        Assert.assertEquals(httpServer2.getFilters().size(), 0);
        Assert.assertEquals(httpServer2.getFilterMappings().size(), 0);
        Assert.assertEquals(httpServer2.getName(), "httpServer2");
        Assert.assertEquals(httpServer2.getPort(), 8082);
        Assert.assertEquals(httpServer2.getContextConfigLocation(), "classpath:com/consol/citrus/http/servlet-context.xml");
        Assert.assertEquals(httpServer2.getResourceBase(), "src/it/resources");
        Assert.assertTrue(httpServer2.isHandleAttributeHeaders());
        Assert.assertTrue(httpServer2.isHandleCookies());
        Assert.assertEquals(httpServer2.getDefaultStatusCode(), HttpStatus.NOT_FOUND.value());
        Assert.assertFalse(httpServer2.isAutoStart());
        Assert.assertTrue(httpServer2.isDebugLogging());
        Assert.assertTrue(httpServer2.isUseRootContextAsParent());
        Assert.assertEquals(httpServer2.getContextPath(), "/citrus");
        Assert.assertEquals(httpServer2.getServletName(), "citrus-http");
        Assert.assertEquals(httpServer2.getServletMappingPath(), "/foo");
        Assert.assertEquals(httpServer2.getBinaryMediaTypes().size(), 2L);
        Assert.assertTrue(httpServer2.getBinaryMediaTypes().contains(MediaType.valueOf("application/custom")));
        
        // 3rd message sender
        Assert.assertNull(httpServer3.getConnector());
        Assert.assertNotNull(httpServer3.getConnectors());
        Assert.assertEquals(httpServer3.getConnectors().length, 2L);
        Assert.assertNotNull(httpServer3.getFilters());
        Assert.assertEquals(httpServer3.getFilters().size(), 2L);
        Assert.assertNotNull(httpServer3.getFilterMappings());
        Assert.assertEquals(httpServer3.getFilterMappings().size(), 1L);
        Assert.assertEquals(httpServer3.getName(), "httpServer3");
        Assert.assertEquals(httpServer3.getPort(), 8083);
        Assert.assertEquals(httpServer3.getContextConfigLocation(), "classpath:com/consol/citrus/http/citrus-servlet-context.xml");
        Assert.assertEquals(httpServer3.getResourceBase(), "src/main/resources");
        Assert.assertFalse(httpServer3.isAutoStart());
        Assert.assertFalse(httpServer3.isUseRootContextAsParent());
        Assert.assertEquals(httpServer3.getServletName(), "httpServer3-servlet");
        
        // 4th message sender
        Assert.assertNull(httpServer4.getConnector());
        Assert.assertNotNull(httpServer4.getServletHandler());
        Assert.assertEquals(httpServer4.getServletHandler(), servletHandler);
        Assert.assertEquals(httpServer4.getName(), "httpServer4");
        Assert.assertEquals(httpServer4.getPort(), 8084);
        Assert.assertEquals(httpServer4.getContextConfigLocation(), "classpath:com/consol/citrus/http/citrus-servlet-context.xml");
        Assert.assertEquals(httpServer4.getResourceBase(), "src/main/resources");
        Assert.assertFalse(httpServer4.isAutoStart());
        Assert.assertFalse(httpServer4.isUseRootContextAsParent());
        Assert.assertEquals(httpServer4.getServletName(), "httpServer4-servlet");
        Assert.assertNotNull(httpServer4.getInterceptors());
        Assert.assertEquals(httpServer4.getInterceptors().size(), 0L);
        
        // 5th message sender
        Assert.assertNull(httpServer5.getConnector());
        Assert.assertNotNull(httpServer5.getSecurityHandler());
        Assert.assertEquals(httpServer5.getSecurityHandler(), securityHandler);
        Assert.assertEquals(httpServer5.getName(), "httpServer5");
        Assert.assertEquals(httpServer5.getPort(), 8085);
        Assert.assertEquals(httpServer5.getContextConfigLocation(), "classpath:com/consol/citrus/http/citrus-servlet-context.xml");
        Assert.assertEquals(httpServer5.getResourceBase(), "src/main/resources");
        Assert.assertFalse(httpServer5.isAutoStart());
        Assert.assertFalse(httpServer5.isUseRootContextAsParent());
        Assert.assertEquals(httpServer5.getServletName(), "httpServer5-servlet");
        Assert.assertNotNull(httpServer5.getInterceptors());
        Assert.assertEquals(httpServer5.getInterceptors().size(), 2L);

        // 6th message sender
        Assert.assertNull(httpServer6.getConnector());
        Assert.assertNotNull(httpServer6.getEndpointAdapter());
        Assert.assertEquals(httpServer6.getEndpointAdapter(), endpointAdapter);
        Assert.assertEquals(httpServer6.getName(), "httpServer6");
        Assert.assertEquals(httpServer6.getPort(), 8086);
        Assert.assertEquals(httpServer6.getContextConfigLocation(), "classpath:com/consol/citrus/http/citrus-servlet-context.xml");
        Assert.assertEquals(httpServer6.getResourceBase(), "src/main/resources");
        Assert.assertFalse(httpServer6.isAutoStart());
        Assert.assertFalse(httpServer6.isUseRootContextAsParent());
        Assert.assertEquals(httpServer6.getServletName(), "httpServer6-servlet");
    }
}
