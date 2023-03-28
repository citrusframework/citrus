/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.ws.config.xml;

import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.citrusframework.ws.server.WebServiceServer;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Iterator;

/**
 * @author Christoph Deppisch
 */
public class WebServiceServerParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testWebServerParser() {
        Iterator<WebServiceServer> servers = beanDefinitionContext.getBeansOfType(WebServiceServer.class).values().iterator();
        
        // 1st server
        WebServiceServer server = servers.next();
        Assert.assertEquals(server.getName(), "soapServer1");
        Assert.assertFalse(server.isAutoStart());
        Assert.assertFalse(server.isRunning());
        Assert.assertEquals(server.getPort(), 8080);
        Assert.assertEquals(server.getResourceBase(), "src/main/resources");
        Assert.assertEquals(server.getContextConfigLocation(), "classpath:org/citrusframework/ws/citrus-servlet-context.xml");
        Assert.assertEquals(server.getContextPath(), "/");
        Assert.assertEquals(server.getServletName(), "soapServer1-servlet");
        Assert.assertEquals(server.getServletMappingPath(), "/*");
        Assert.assertFalse(server.isUseRootContextAsParent());
        Assert.assertNull(server.getSecurityHandler());
        Assert.assertEquals(server.getConnectors().length, 0);
        Assert.assertNull(server.getConnector());
        Assert.assertFalse(server.isHandleMimeHeaders());
        Assert.assertFalse(server.isHandleAttributeHeaders());
        Assert.assertFalse(server.isKeepSoapEnvelope());
        Assert.assertNull(server.getSoapHeaderNamespace());
        Assert.assertEquals(server.getSoapHeaderPrefix(), "");
        Assert.assertEquals(server.getMessageFactoryName(), MessageDispatcherServlet.DEFAULT_MESSAGE_FACTORY_BEAN_NAME);

        // 2nd server
        server = servers.next();
        Assert.assertEquals(server.getName(), "soapServer2");
        Assert.assertFalse(server.isAutoStart());
        Assert.assertFalse(server.isRunning());
        Assert.assertEquals(server.getPort(), 8081);
        Assert.assertEquals(server.getResourceBase(), "src/it/resources");
        Assert.assertEquals(server.getContextConfigLocation(), "classpath:org/citrusframework/ws/citrus-ws-servlet.xml");
        Assert.assertEquals(server.getContextPath(), "/citrus");
        Assert.assertEquals(server.getServletName(), "citrus-ws");
        Assert.assertEquals(server.getServletMappingPath(), "/foo");
        Assert.assertTrue(server.isUseRootContextAsParent());
        Assert.assertNull(server.getSecurityHandler());
        Assert.assertEquals(server.getConnectors().length, 0);
        Assert.assertNull(server.getConnector());
        Assert.assertTrue(server.isHandleMimeHeaders());
        Assert.assertTrue(server.isHandleAttributeHeaders());
        Assert.assertTrue(server.isKeepSoapEnvelope());
        Assert.assertEquals(server.getSoapHeaderNamespace(), "http://citrusframework.org");
        Assert.assertEquals(server.getSoapHeaderPrefix(), "CITRUS");
        Assert.assertEquals(server.getMessageConverter(), beanDefinitionContext.getBean("messageConverter"));
        Assert.assertEquals(server.getMessageFactoryName(), "soap12MessageFactory");

        // 3rd server
        server = servers.next();
        Assert.assertEquals(server.getName(), "soapServer3");
        Assert.assertFalse(server.isAutoStart());
        Assert.assertFalse(server.isRunning());
        Assert.assertEquals(server.getPort(), 8080);
        Assert.assertEquals(server.getResourceBase(), "src/main/resources");
        Assert.assertEquals(server.getContextConfigLocation(), "classpath:org/citrusframework/ws/citrus-servlet-context.xml");
        Assert.assertEquals(server.getContextPath(), "/");
        Assert.assertEquals(server.getServletName(), "soapServer3-servlet");
        Assert.assertEquals(server.getServletMappingPath(), "/*");
        Assert.assertFalse(server.isUseRootContextAsParent());
        Assert.assertNull(server.getSecurityHandler());
        Assert.assertEquals(server.getConnectors().length, 0);
        Assert.assertNotNull(server.getConnector());
        Assert.assertEquals(server.getConnector(), beanDefinitionContext.getBean("connector"));

        // 4th server
        server = servers.next();
        Assert.assertEquals(server.getName(), "soapServer4");
        Assert.assertFalse(server.isAutoStart());
        Assert.assertFalse(server.isRunning());
        Assert.assertEquals(server.getPort(), 8080);
        Assert.assertEquals(server.getResourceBase(), "src/main/resources");
        Assert.assertEquals(server.getContextConfigLocation(), "classpath:org/citrusframework/ws/citrus-servlet-context.xml");
        Assert.assertEquals(server.getContextPath(), "/");
        Assert.assertEquals(server.getServletName(), "soapServer4-servlet");
        Assert.assertEquals(server.getServletMappingPath(), "/*");
        Assert.assertFalse(server.isUseRootContextAsParent());
        Assert.assertNull(server.getSecurityHandler());
        Assert.assertNotNull(server.getConnectors());
        Assert.assertEquals(server.getConnectors().length, 2);
        Assert.assertNull(server.getConnector());

        // 5th server
        server = servers.next();
        Assert.assertEquals(server.getName(), "soapServer5");
        Assert.assertFalse(server.isAutoStart());
        Assert.assertFalse(server.isRunning());
        Assert.assertEquals(server.getPort(), 8080);
        Assert.assertEquals(server.getResourceBase(), "src/main/resources");
        Assert.assertEquals(server.getContextConfigLocation(), "classpath:org/citrusframework/ws/citrus-servlet-context.xml");
        Assert.assertEquals(server.getContextPath(), "/");
        Assert.assertEquals(server.getServletName(), "soapServer5-servlet");
        Assert.assertEquals(server.getServletMappingPath(), "/*");
        Assert.assertFalse(server.isUseRootContextAsParent());
        Assert.assertNotNull(server.getSecurityHandler());
        Assert.assertEquals(server.getSecurityHandler(), beanDefinitionContext.getBean("securityHandler"));
        Assert.assertEquals(server.getConnectors().length, 0);
        Assert.assertNull(server.getConnector());
        
        // 6th server
        server = servers.next();
        Assert.assertEquals(server.getName(), "soapServer6");
        Assert.assertFalse(server.isAutoStart());
        Assert.assertFalse(server.isRunning());
        Assert.assertEquals(server.getPort(), 8080);
        Assert.assertEquals(server.getResourceBase(), "src/main/resources");
        Assert.assertEquals(server.getContextConfigLocation(), "classpath:org/citrusframework/ws/citrus-servlet-context.xml");
        Assert.assertEquals(server.getContextPath(), "/");
        Assert.assertEquals(server.getServletName(), "soapServer6-servlet");
        Assert.assertEquals(server.getServletMappingPath(), "/*");
        Assert.assertFalse(server.isUseRootContextAsParent());
        Assert.assertNull(server.getSecurityHandler());
        Assert.assertEquals(server.getConnectors().length, 0);
        Assert.assertNull(server.getConnector());
        Assert.assertNotNull(server.getServletHandler());
        Assert.assertEquals(server.getServletHandler(), beanDefinitionContext.getBean("servletHandler"));
    }
}
