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

package org.citrusframework.ws.config.annotation;

import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.ws.message.converter.WebServiceMessageConverter;
import org.citrusframework.ws.server.WebServiceServer;
import org.eclipse.jetty.ee10.servlet.ServletHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.mockito.Mock;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.citrusframework.annotations.CitrusAnnotations.injectEndpoints;
import static org.citrusframework.config.annotation.AnnotationConfigParser.lookup;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Christoph Deppisch
 */
public class WebServiceServerConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint(name = "soapServer1")
    @WebServiceServerConfig()
    private WebServiceServer soapServer1;

    @CitrusEndpoint
    @WebServiceServerConfig(port = 8081,
            rootParentContext = true,
            contextConfigLocation = "classpath:org/citrusframework/ws/citrus-ws-servlet.xml",
            resourceBase = "src/it/resources",
            contextPath = "/citrus",
            servletName = "citrus-ws",
            servletMappingPath = "/foo",
            handleMimeHeaders = true,
            handleAttributeHeaders = true,
            keepSoapEnvelope = true,
            messageConverter = "messageConverter",
            messageFactory = "soap12MessageFactory",
            soapHeaderNamespace = "http://citrusframework.org",
            soapHeaderPrefix = "CITRUS")
    private WebServiceServer soapServer2;

    @CitrusEndpoint
    @WebServiceServerConfig(connector = "connector")
    private WebServiceServer soapServer3;

    @CitrusEndpoint
    @WebServiceServerConfig(connectors = {"connector1", "connector2"})
    private WebServiceServer soapServer4;

    @CitrusEndpoint
    @WebServiceServerConfig(securityHandler = "securityHandler")
    private WebServiceServer soapServer5;

    @CitrusEndpoint
    @WebServiceServerConfig(servletHandler = "servletHandler")
    private WebServiceServer soapServer6;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private Connector connector1;
    @Mock
    private Connector connector2;
    @Mock
    private SecurityHandler securityHandler;
    @Mock
    private WebServiceMessageConverter messageConverter;
    @Mock
    private ServletHandler servletHandler;
    @Mock
    private TestActor testActor;

    @BeforeMethod
    public void setup() {
        openMocks(this);

        when(referenceResolver.resolve("securityHandler", SecurityHandler.class)).thenReturn(securityHandler);
        when(referenceResolver.resolve("messageConverter", WebServiceMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("servletHandler", ServletHandler.class)).thenReturn(servletHandler);
        when(referenceResolver.resolve("connector", Connector.class)).thenReturn(connector1);
        when(referenceResolver.resolve("connector1", Connector.class)).thenReturn(connector1);
        when(referenceResolver.resolve("connector2", Connector.class)).thenReturn(connector2);
        when(referenceResolver.resolve(new String[]{"connector1", "connector2"}, Connector.class)).thenReturn(Arrays.asList(connector1, connector2));
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);

        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testWebServerParser() {
        injectEndpoints(this, context);

        // 1st server
        assertEquals(soapServer1.getName(), "soapServer1");
        assertFalse(soapServer1.isAutoStart());
        assertFalse(soapServer1.isRunning());
        assertEquals(soapServer1.getPort(), 8080);
        assertEquals(soapServer1.getResourceBase(), "src/main/resources");
        assertEquals(soapServer1.getContextConfigLocation(), "classpath:org/citrusframework/ws/citrus-servlet-context.xml");
        assertEquals(soapServer1.getContextPath(), "/");
        assertEquals(soapServer1.getServletName(), "soapServer1-servlet");
        assertEquals(soapServer1.getServletMappingPath(), "/*");
        assertFalse(soapServer1.isUseRootContextAsParent());
        assertNull(soapServer1.getSecurityHandler());
        assertEquals(soapServer1.getConnectors().length, 0);
        assertNull(soapServer1.getConnector());
        assertFalse(soapServer1.isHandleMimeHeaders());
        assertFalse(soapServer1.isHandleAttributeHeaders());
        assertFalse(soapServer1.isKeepSoapEnvelope());
        assertNull(soapServer1.getSoapHeaderNamespace());
        assertEquals(soapServer1.getSoapHeaderPrefix(), "");
        assertEquals(soapServer1.getMessageFactoryName(), MessageDispatcherServlet.DEFAULT_MESSAGE_FACTORY_BEAN_NAME);

        // 2nd server
        assertEquals(soapServer2.getName(), "soapServer2");
        assertFalse(soapServer2.isAutoStart());
        assertFalse(soapServer2.isRunning());
        assertEquals(soapServer2.getPort(), 8081);
        assertEquals(soapServer2.getResourceBase(), "src/it/resources");
        assertEquals(soapServer2.getContextConfigLocation(), "classpath:org/citrusframework/ws/citrus-ws-servlet.xml");
        assertEquals(soapServer2.getContextPath(), "/citrus");
        assertEquals(soapServer2.getServletName(), "citrus-ws");
        assertEquals(soapServer2.getServletMappingPath(), "/foo");
        assertTrue(soapServer2.isUseRootContextAsParent());
        assertNull(soapServer2.getSecurityHandler());
        assertEquals(soapServer2.getConnectors().length, 0);
        assertNull(soapServer2.getConnector());
        assertTrue(soapServer2.isHandleMimeHeaders());
        assertTrue(soapServer2.isHandleAttributeHeaders());
        assertTrue(soapServer2.isKeepSoapEnvelope());
        assertEquals(soapServer2.getSoapHeaderNamespace(), "http://citrusframework.org");
        assertEquals(soapServer2.getSoapHeaderPrefix(), "CITRUS");
        assertEquals(soapServer2.getMessageConverter(), messageConverter);
        assertEquals(soapServer2.getMessageFactoryName(), "soap12MessageFactory");

        // 3rd server
        assertEquals(soapServer3.getName(), "soapServer3");
        assertFalse(soapServer3.isAutoStart());
        assertFalse(soapServer3.isRunning());
        assertEquals(soapServer3.getPort(), 8080);
        assertEquals(soapServer3.getResourceBase(), "src/main/resources");
        assertEquals(soapServer3.getContextConfigLocation(), "classpath:org/citrusframework/ws/citrus-servlet-context.xml");
        assertEquals(soapServer3.getContextPath(), "/");
        assertEquals(soapServer3.getServletName(), "soapServer3-servlet");
        assertEquals(soapServer3.getServletMappingPath(), "/*");
        assertFalse(soapServer3.isUseRootContextAsParent());
        assertNull(soapServer3.getSecurityHandler());
        assertEquals(soapServer3.getConnectors().length, 0);
        assertNotNull(soapServer3.getConnector());
        assertEquals(soapServer3.getConnector(), connector1);

        // 4th server
        assertEquals(soapServer4.getName(), "soapServer4");
        assertFalse(soapServer4.isAutoStart());
        assertFalse(soapServer4.isRunning());
        assertEquals(soapServer4.getPort(), 8080);
        assertEquals(soapServer4.getResourceBase(), "src/main/resources");
        assertEquals(soapServer4.getContextConfigLocation(), "classpath:org/citrusframework/ws/citrus-servlet-context.xml");
        assertEquals(soapServer4.getContextPath(), "/");
        assertEquals(soapServer4.getServletName(), "soapServer4-servlet");
        assertEquals(soapServer4.getServletMappingPath(), "/*");
        assertFalse(soapServer4.isUseRootContextAsParent());
        assertNull(soapServer4.getSecurityHandler());
        assertNotNull(soapServer4.getConnectors());
        assertEquals(soapServer4.getConnectors().length, 2);
        assertNull(soapServer4.getConnector());

        // 5th server
        assertEquals(soapServer5.getName(), "soapServer5");
        assertFalse(soapServer5.isAutoStart());
        assertFalse(soapServer5.isRunning());
        assertEquals(soapServer5.getPort(), 8080);
        assertEquals(soapServer5.getResourceBase(), "src/main/resources");
        assertEquals(soapServer5.getContextConfigLocation(), "classpath:org/citrusframework/ws/citrus-servlet-context.xml");
        assertEquals(soapServer5.getContextPath(), "/");
        assertEquals(soapServer5.getServletName(), "soapServer5-servlet");
        assertEquals(soapServer5.getServletMappingPath(), "/*");
        assertFalse(soapServer5.isUseRootContextAsParent());
        assertNotNull(soapServer5.getSecurityHandler());
        assertEquals(soapServer5.getSecurityHandler(), securityHandler);
        assertEquals(soapServer5.getConnectors().length, 0);
        assertNull(soapServer5.getConnector());

        // 6th server
        assertEquals(soapServer6.getName(), "soapServer6");
        assertFalse(soapServer6.isAutoStart());
        assertFalse(soapServer6.isRunning());
        assertEquals(soapServer6.getPort(), 8080);
        assertEquals(soapServer6.getResourceBase(), "src/main/resources");
        assertEquals(soapServer6.getContextConfigLocation(), "classpath:org/citrusframework/ws/citrus-servlet-context.xml");
        assertEquals(soapServer6.getContextPath(), "/");
        assertEquals(soapServer6.getServletName(), "soapServer6-servlet");
        assertEquals(soapServer6.getServletMappingPath(), "/*");
        assertFalse(soapServer6.isUseRootContextAsParent());
        assertNull(soapServer6.getSecurityHandler());
        assertEquals(soapServer6.getConnectors().length, 0);
        assertNull(soapServer6.getConnector());
        assertNotNull(soapServer6.getServletHandler());
        assertEquals(soapServer6.getServletHandler(), servletHandler);
    }

    @Test
    public void testLookupByQualifier() {
        assertTrue(lookup("soap.server").isPresent());
    }
}
