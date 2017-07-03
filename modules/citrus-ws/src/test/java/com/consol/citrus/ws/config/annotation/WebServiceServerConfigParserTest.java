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

package com.consol.citrus.ws.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.ws.message.converter.WebServiceMessageConverter;
import com.consol.citrus.ws.server.WebServiceServer;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class WebServiceServerConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint(name = "soapServer1")
    @WebServiceServerConfig()
    private WebServiceServer soapServer1;

    @CitrusEndpoint
    @WebServiceServerConfig(port=8081,
            rootParentContext=true,
            contextConfigLocation="classpath:com/consol/citrus/ws/citrus-ws-servlet.xml",
            resourceBase="src/it/resources",
            contextPath="/citrus",
            servletName="citrus-ws",
            servletMappingPath="/foo",
            handleMimeHeaders=true,
            handleAttributeHeaders=true,
            keepSoapEnvelope=true,
            messageConverter="messageConverter",
            messageFactory="soap12MessageFactory",
            soapHeaderNamespace="http://citrusframework.org",
            soapHeaderPrefix="CITRUS")
    private WebServiceServer soapServer2;

    @CitrusEndpoint
    @WebServiceServerConfig(connector="connector")
    private WebServiceServer soapServer3;

    @CitrusEndpoint
    @WebServiceServerConfig(connectors={ "connector1", "connector2" })
    private WebServiceServer soapServer4;

    @CitrusEndpoint
    @WebServiceServerConfig(securityHandler="securityHandler")
    private WebServiceServer soapServer5;

    @CitrusEndpoint
    @WebServiceServerConfig(servletHandler="servletHandler")
    private WebServiceServer soapServer6;

    @Autowired
    private SpringBeanReferenceResolver referenceResolver;

    @Mock
    private Connector connector1 = Mockito.mock(Connector.class);
    @Mock
    private Connector connector2 = Mockito.mock(Connector.class);
    @Mock
    private SecurityHandler securityHandler = Mockito.mock(SecurityHandler.class);
    @Mock
    private WebServiceMessageConverter messageConverter = Mockito.mock(WebServiceMessageConverter.class);
    @Mock
    private ServletHandler servletHandler = Mockito.mock(ServletHandler.class);
    @Mock
    private TestActor testActor = Mockito.mock(TestActor.class);
    @Mock
    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);

        referenceResolver.setApplicationContext(applicationContext);

        when(applicationContext.getBean("securityHandler", SecurityHandler.class)).thenReturn(securityHandler);
        when(applicationContext.getBean("messageConverter", WebServiceMessageConverter.class)).thenReturn(messageConverter);
        when(applicationContext.getBean("servletHandler", ServletHandler.class)).thenReturn(servletHandler);
        when(applicationContext.getBean("connector", Connector.class)).thenReturn(connector1);
        when(applicationContext.getBean("connector1", Connector.class)).thenReturn(connector1);
        when(applicationContext.getBean("connector2", Connector.class)).thenReturn(connector2);
        when(applicationContext.getBean("testActor", TestActor.class)).thenReturn(testActor);
    }

    @Test
    public void testWebServerParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st server
        Assert.assertEquals(soapServer1.getName(), "soapServer1");
        Assert.assertFalse(soapServer1.isAutoStart());
        Assert.assertFalse(soapServer1.isRunning());
        Assert.assertEquals(soapServer1.getPort(), 8080);
        Assert.assertEquals(soapServer1.getResourceBase(), "src/main/resources");
        Assert.assertEquals(soapServer1.getContextConfigLocation(), "classpath:com/consol/citrus/ws/citrus-servlet-context.xml");
        Assert.assertEquals(soapServer1.getContextPath(), "/");
        Assert.assertEquals(soapServer1.getServletName(), "soapServer1-servlet");
        Assert.assertEquals(soapServer1.getServletMappingPath(), "/*");
        Assert.assertFalse(soapServer1.isUseRootContextAsParent());
        Assert.assertNull(soapServer1.getSecurityHandler());
        Assert.assertEquals(soapServer1.getConnectors().length, 0);
        Assert.assertNull(soapServer1.getConnector());
        Assert.assertFalse(soapServer1.isHandleMimeHeaders());
        Assert.assertFalse(soapServer1.isHandleAttributeHeaders());
        Assert.assertFalse(soapServer1.isKeepSoapEnvelope());
        Assert.assertNull(soapServer1.getSoapHeaderNamespace());
        Assert.assertEquals(soapServer1.getSoapHeaderPrefix(), "");
        Assert.assertEquals(soapServer1.getMessageFactoryName(), MessageDispatcherServlet.DEFAULT_MESSAGE_FACTORY_BEAN_NAME);

        // 2nd server
        Assert.assertEquals(soapServer2.getName(), "soapServer2");
        Assert.assertFalse(soapServer2.isAutoStart());
        Assert.assertFalse(soapServer2.isRunning());
        Assert.assertEquals(soapServer2.getPort(), 8081);
        Assert.assertEquals(soapServer2.getResourceBase(), "src/it/resources");
        Assert.assertEquals(soapServer2.getContextConfigLocation(), "classpath:com/consol/citrus/ws/citrus-ws-servlet.xml");
        Assert.assertEquals(soapServer2.getContextPath(), "/citrus");
        Assert.assertEquals(soapServer2.getServletName(), "citrus-ws");
        Assert.assertEquals(soapServer2.getServletMappingPath(), "/foo");
        Assert.assertTrue(soapServer2.isUseRootContextAsParent());
        Assert.assertNull(soapServer2.getSecurityHandler());
        Assert.assertEquals(soapServer2.getConnectors().length, 0);
        Assert.assertNull(soapServer2.getConnector());
        Assert.assertTrue(soapServer2.isHandleMimeHeaders());
        Assert.assertTrue(soapServer2.isHandleAttributeHeaders());
        Assert.assertTrue(soapServer2.isKeepSoapEnvelope());
        Assert.assertEquals(soapServer2.getSoapHeaderNamespace(), "http://citrusframework.org");
        Assert.assertEquals(soapServer2.getSoapHeaderPrefix(), "CITRUS");
        Assert.assertEquals(soapServer2.getMessageConverter(), messageConverter);
        Assert.assertEquals(soapServer2.getMessageFactoryName(), "soap12MessageFactory");

        // 3rd server
        Assert.assertEquals(soapServer3.getName(), "soapServer3");
        Assert.assertFalse(soapServer3.isAutoStart());
        Assert.assertFalse(soapServer3.isRunning());
        Assert.assertEquals(soapServer3.getPort(), 8080);
        Assert.assertEquals(soapServer3.getResourceBase(), "src/main/resources");
        Assert.assertEquals(soapServer3.getContextConfigLocation(), "classpath:com/consol/citrus/ws/citrus-servlet-context.xml");
        Assert.assertEquals(soapServer3.getContextPath(), "/");
        Assert.assertEquals(soapServer3.getServletName(), "soapServer3-servlet");
        Assert.assertEquals(soapServer3.getServletMappingPath(), "/*");
        Assert.assertFalse(soapServer3.isUseRootContextAsParent());
        Assert.assertNull(soapServer3.getSecurityHandler());
        Assert.assertEquals(soapServer3.getConnectors().length, 0);
        Assert.assertNotNull(soapServer3.getConnector());
        Assert.assertEquals(soapServer3.getConnector(), connector1);

        // 4th server
        Assert.assertEquals(soapServer4.getName(), "soapServer4");
        Assert.assertFalse(soapServer4.isAutoStart());
        Assert.assertFalse(soapServer4.isRunning());
        Assert.assertEquals(soapServer4.getPort(), 8080);
        Assert.assertEquals(soapServer4.getResourceBase(), "src/main/resources");
        Assert.assertEquals(soapServer4.getContextConfigLocation(), "classpath:com/consol/citrus/ws/citrus-servlet-context.xml");
        Assert.assertEquals(soapServer4.getContextPath(), "/");
        Assert.assertEquals(soapServer4.getServletName(), "soapServer4-servlet");
        Assert.assertEquals(soapServer4.getServletMappingPath(), "/*");
        Assert.assertFalse(soapServer4.isUseRootContextAsParent());
        Assert.assertNull(soapServer4.getSecurityHandler());
        Assert.assertNotNull(soapServer4.getConnectors());
        Assert.assertEquals(soapServer4.getConnectors().length, 2);
        Assert.assertNull(soapServer4.getConnector());

        // 5th server
        Assert.assertEquals(soapServer5.getName(), "soapServer5");
        Assert.assertFalse(soapServer5.isAutoStart());
        Assert.assertFalse(soapServer5.isRunning());
        Assert.assertEquals(soapServer5.getPort(), 8080);
        Assert.assertEquals(soapServer5.getResourceBase(), "src/main/resources");
        Assert.assertEquals(soapServer5.getContextConfigLocation(), "classpath:com/consol/citrus/ws/citrus-servlet-context.xml");
        Assert.assertEquals(soapServer5.getContextPath(), "/");
        Assert.assertEquals(soapServer5.getServletName(), "soapServer5-servlet");
        Assert.assertEquals(soapServer5.getServletMappingPath(), "/*");
        Assert.assertFalse(soapServer5.isUseRootContextAsParent());
        Assert.assertNotNull(soapServer5.getSecurityHandler());
        Assert.assertEquals(soapServer5.getSecurityHandler(), securityHandler);
        Assert.assertEquals(soapServer5.getConnectors().length, 0);
        Assert.assertNull(soapServer5.getConnector());
        
        // 6th server
        Assert.assertEquals(soapServer6.getName(), "soapServer6");
        Assert.assertFalse(soapServer6.isAutoStart());
        Assert.assertFalse(soapServer6.isRunning());
        Assert.assertEquals(soapServer6.getPort(), 8080);
        Assert.assertEquals(soapServer6.getResourceBase(), "src/main/resources");
        Assert.assertEquals(soapServer6.getContextConfigLocation(), "classpath:com/consol/citrus/ws/citrus-servlet-context.xml");
        Assert.assertEquals(soapServer6.getContextPath(), "/");
        Assert.assertEquals(soapServer6.getServletName(), "soapServer6-servlet");
        Assert.assertEquals(soapServer6.getServletMappingPath(), "/*");
        Assert.assertFalse(soapServer6.isUseRootContextAsParent());
        Assert.assertNull(soapServer6.getSecurityHandler());
        Assert.assertEquals(soapServer6.getConnectors().length, 0);
        Assert.assertNull(soapServer6.getConnector());
        Assert.assertNotNull(soapServer6.getServletHandler());
        Assert.assertEquals(soapServer6.getServletHandler(), servletHandler);
    }
}
