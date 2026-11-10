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

package org.citrusframework.ws.config.annotation;

import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.ws.message.converter.WebServiceMessageConverter;
import org.citrusframework.ws.server.WebServiceServer;
import org.eclipse.jetty.ee11.servlet.ServletHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;

import static org.citrusframework.base.annotations.CitrusAnnotations.injectEndpoints;
import static org.citrusframework.config.annotation.AnnotationConfigParser.lookup;
import static org.mockito.Mockito.lenient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class WebServiceServerConfigParserTest extends AbstractUnitTest {

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

    @BeforeEach
    @Override
    public void prepareTest() {
        super.prepareTest();

        lenient().when(referenceResolver.resolve("securityHandler", SecurityHandler.class)).thenReturn(securityHandler);
        lenient().when(referenceResolver.resolve("messageConverter", WebServiceMessageConverter.class)).thenReturn(messageConverter);
        lenient().when(referenceResolver.resolve("servletHandler", ServletHandler.class)).thenReturn(servletHandler);
        lenient().when(referenceResolver.resolve("connector", Connector.class)).thenReturn(connector1);
        lenient().when(referenceResolver.resolve("connector1", Connector.class)).thenReturn(connector1);
        lenient().when(referenceResolver.resolve("connector2", Connector.class)).thenReturn(connector2);
        lenient().when(referenceResolver.resolve(new String[]{"connector1", "connector2"}, Connector.class)).thenReturn(Arrays.asList(connector1, connector2));
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);

        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testWebServerParser() {
        injectEndpoints(this, context);

        // 1st server
        assertEquals("soapServer1", soapServer1.getName());
        assertFalse(soapServer1.isAutoStart());
        assertFalse(soapServer1.isRunning());
        assertEquals(8080, soapServer1.getPort());
        assertNull(soapServer1.getResourceBase());
        assertEquals("classpath:org/citrusframework/ws/citrus-servlet-context.xml", soapServer1.getContextConfigLocation());
        assertEquals("/", soapServer1.getContextPath());
        assertEquals("soapServer1-servlet", soapServer1.getServletName());
        assertEquals("/*", soapServer1.getServletMappingPath());
        assertFalse(soapServer1.isUseRootContextAsParent());
        assertNull(soapServer1.getSecurityHandler());
        assertEquals(0, soapServer1.getConnectors().length);
        assertNull(soapServer1.getConnector());
        assertFalse(soapServer1.isHandleMimeHeaders());
        assertFalse(soapServer1.isHandleAttributeHeaders());
        assertFalse(soapServer1.isKeepSoapEnvelope());
        assertNull(soapServer1.getSoapHeaderNamespace());
        assertEquals("", soapServer1.getSoapHeaderPrefix());
        assertEquals(MessageDispatcherServlet.DEFAULT_MESSAGE_FACTORY_BEAN_NAME, soapServer1.getMessageFactoryName());

        // 2nd server
        assertEquals("soapServer2", soapServer2.getName());
        assertFalse(soapServer2.isAutoStart());
        assertFalse(soapServer2.isRunning());
        assertEquals(8081, soapServer2.getPort());
        assertEquals("src/it/resources", soapServer2.getResourceBase());
        assertEquals("classpath:org/citrusframework/ws/citrus-ws-servlet.xml", soapServer2.getContextConfigLocation());
        assertEquals("/citrus", soapServer2.getContextPath());
        assertEquals("citrus-ws", soapServer2.getServletName());
        assertEquals("/foo", soapServer2.getServletMappingPath());
        assertTrue(soapServer2.isUseRootContextAsParent());
        assertNull(soapServer2.getSecurityHandler());
        assertEquals(0, soapServer2.getConnectors().length);
        assertNull(soapServer2.getConnector());
        assertTrue(soapServer2.isHandleMimeHeaders());
        assertTrue(soapServer2.isHandleAttributeHeaders());
        assertTrue(soapServer2.isKeepSoapEnvelope());
        assertEquals("http://citrusframework.org", soapServer2.getSoapHeaderNamespace());
        assertEquals("CITRUS", soapServer2.getSoapHeaderPrefix());
        assertEquals(messageConverter, soapServer2.getMessageConverter());
        assertEquals("soap12MessageFactory", soapServer2.getMessageFactoryName());

        // 3rd server
        assertEquals("soapServer3", soapServer3.getName());
        assertFalse(soapServer3.isAutoStart());
        assertFalse(soapServer3.isRunning());
        assertEquals(8080, soapServer3.getPort());
        assertNull(soapServer3.getResourceBase());
        assertEquals("classpath:org/citrusframework/ws/citrus-servlet-context.xml", soapServer3.getContextConfigLocation());
        assertEquals("/", soapServer3.getContextPath());
        assertEquals("soapServer3-servlet", soapServer3.getServletName());
        assertEquals("/*", soapServer3.getServletMappingPath());
        assertFalse(soapServer3.isUseRootContextAsParent());
        assertNull(soapServer3.getSecurityHandler());
        assertEquals(0, soapServer3.getConnectors().length);
        assertNotNull(soapServer3.getConnector());
        assertEquals(connector1, soapServer3.getConnector());

        // 4th server
        assertEquals("soapServer4", soapServer4.getName());
        assertFalse(soapServer4.isAutoStart());
        assertFalse(soapServer4.isRunning());
        assertEquals(8080, soapServer4.getPort());
        assertNull(soapServer4.getResourceBase());
        assertEquals("classpath:org/citrusframework/ws/citrus-servlet-context.xml", soapServer4.getContextConfigLocation());
        assertEquals("/", soapServer4.getContextPath());
        assertEquals("soapServer4-servlet", soapServer4.getServletName());
        assertEquals("/*", soapServer4.getServletMappingPath());
        assertFalse(soapServer4.isUseRootContextAsParent());
        assertNull(soapServer4.getSecurityHandler());
        assertNotNull(soapServer4.getConnectors());
        assertEquals(2, soapServer4.getConnectors().length);
        assertNull(soapServer4.getConnector());

        // 5th server
        assertEquals("soapServer5", soapServer5.getName());
        assertFalse(soapServer5.isAutoStart());
        assertFalse(soapServer5.isRunning());
        assertEquals(8080, soapServer5.getPort());
        assertNull(soapServer5.getResourceBase());
        assertEquals("classpath:org/citrusframework/ws/citrus-servlet-context.xml", soapServer5.getContextConfigLocation());
        assertEquals("/", soapServer5.getContextPath());
        assertEquals("soapServer5-servlet", soapServer5.getServletName());
        assertEquals("/*", soapServer5.getServletMappingPath());
        assertFalse(soapServer5.isUseRootContextAsParent());
        assertNotNull(soapServer5.getSecurityHandler());
        assertEquals(securityHandler, soapServer5.getSecurityHandler());
        assertEquals(0, soapServer5.getConnectors().length);
        assertNull(soapServer5.getConnector());

        // 6th server
        assertEquals("soapServer6", soapServer6.getName());
        assertFalse(soapServer6.isAutoStart());
        assertFalse(soapServer6.isRunning());
        assertEquals(8080, soapServer6.getPort());
        assertNull(soapServer6.getResourceBase());
        assertEquals("classpath:org/citrusframework/ws/citrus-servlet-context.xml", soapServer6.getContextConfigLocation());
        assertEquals("/", soapServer6.getContextPath());
        assertEquals("soapServer6-servlet", soapServer6.getServletName());
        assertEquals("/*", soapServer6.getServletMappingPath());
        assertFalse(soapServer6.isUseRootContextAsParent());
        assertNull(soapServer6.getSecurityHandler());
        assertEquals(0, soapServer6.getConnectors().length);
        assertNull(soapServer6.getConnector());
        assertNotNull(soapServer6.getServletHandler());
        assertEquals(servletHandler, soapServer6.getServletHandler());
    }

    @Test
    public void testLookupByQualifier() {
        assertTrue(lookup("soap.server").isPresent());
    }
}
