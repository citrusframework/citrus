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

package com.consol.citrus.ws.config.xml;

import com.consol.citrus.testng.AbstractBeanDefinitionParserTest;
import com.consol.citrus.ws.server.WebServer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Iterator;

/**
 * @author Christoph Deppisch
 */
public class WebServerParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testAssertSoapFaultParser() {
        Iterator<WebServer> servers = beanDefinitionContext.getBeansOfType(WebServer.class).values().iterator();
        
        // 1st server
        WebServer server = servers.next();
        Assert.assertEquals(server.getName(), "jettyServer1");
        Assert.assertFalse(server.isAutoStart());
        Assert.assertFalse(server.isRunning());
        Assert.assertEquals(server.getPort(), 8080);
        Assert.assertEquals(server.getResourceBase(), "src/main/resources");
        Assert.assertEquals(server.getContextConfigLocation(), "classpath:citrus-ws-servlet.xml");
        Assert.assertEquals(server.getContextPath(), "/");
        Assert.assertEquals(server.getServletName(), "jettyServer1-servlet");
        Assert.assertEquals(server.getServletMappingPath(), "/*");
        Assert.assertFalse(server.isUseRootContextAsParent());
        Assert.assertNull(server.getSecurityHandler());
        Assert.assertEquals(server.getConnectors().length, 0);
        Assert.assertNull(server.getConnector());
        
        // 2nd server
        server = servers.next();
        Assert.assertEquals(server.getName(), "jettyServer2");
        Assert.assertFalse(server.isAutoStart());
        Assert.assertFalse(server.isRunning());
        Assert.assertEquals(server.getPort(), 8081);
        Assert.assertEquals(server.getResourceBase(), "src/citrus/resources");
        Assert.assertEquals(server.getContextConfigLocation(), "classpath:com/consol/citrus/ws/citrus-ws-servlet.xml");
        Assert.assertEquals(server.getContextPath(), "/citrus");
        Assert.assertEquals(server.getServletName(), "citrus-ws");
        Assert.assertEquals(server.getServletMappingPath(), "/foo");
        Assert.assertTrue(server.isUseRootContextAsParent());
        Assert.assertNull(server.getSecurityHandler());
        Assert.assertEquals(server.getConnectors().length, 0);
        Assert.assertNull(server.getConnector());
        
        // 3rd server
        server = servers.next();
        Assert.assertEquals(server.getName(), "jettyServer3");
        Assert.assertFalse(server.isAutoStart());
        Assert.assertFalse(server.isRunning());
        Assert.assertEquals(server.getPort(), 8080);
        Assert.assertEquals(server.getResourceBase(), "src/main/resources");
        Assert.assertEquals(server.getContextConfigLocation(), "classpath:citrus-ws-servlet.xml");
        Assert.assertEquals(server.getContextPath(), "/");
        Assert.assertEquals(server.getServletName(), "jettyServer3-servlet");
        Assert.assertEquals(server.getServletMappingPath(), "/*");
        Assert.assertFalse(server.isUseRootContextAsParent());
        Assert.assertNull(server.getSecurityHandler());
        Assert.assertEquals(server.getConnectors().length, 0);
        Assert.assertNotNull(server.getConnector());
        Assert.assertEquals(server.getConnector(), beanDefinitionContext.getBean("connector"));
        
        // 4th server
        server = servers.next();
        Assert.assertEquals(server.getName(), "jettyServer4");
        Assert.assertFalse(server.isAutoStart());
        Assert.assertFalse(server.isRunning());
        Assert.assertEquals(server.getPort(), 8080);
        Assert.assertEquals(server.getResourceBase(), "src/main/resources");
        Assert.assertEquals(server.getContextConfigLocation(), "classpath:citrus-ws-servlet.xml");
        Assert.assertEquals(server.getContextPath(), "/");
        Assert.assertEquals(server.getServletName(), "jettyServer4-servlet");
        Assert.assertEquals(server.getServletMappingPath(), "/*");
        Assert.assertFalse(server.isUseRootContextAsParent());
        Assert.assertNull(server.getSecurityHandler());
        Assert.assertNotNull(server.getConnectors());
        Assert.assertEquals(server.getConnectors().length, 2);
        Assert.assertNull(server.getConnector());
        
        // 5th server
        server = servers.next();
        Assert.assertEquals(server.getName(), "jettyServer5");
        Assert.assertFalse(server.isAutoStart());
        Assert.assertFalse(server.isRunning());
        Assert.assertEquals(server.getPort(), 8080);
        Assert.assertEquals(server.getResourceBase(), "src/main/resources");
        Assert.assertEquals(server.getContextConfigLocation(), "classpath:citrus-ws-servlet.xml");
        Assert.assertEquals(server.getContextPath(), "/");
        Assert.assertEquals(server.getServletName(), "jettyServer5-servlet");
        Assert.assertEquals(server.getServletMappingPath(), "/*");
        Assert.assertFalse(server.isUseRootContextAsParent());
        Assert.assertNotNull(server.getSecurityHandler());
        Assert.assertEquals(server.getSecurityHandler(), beanDefinitionContext.getBean("securityHandler"));
        Assert.assertEquals(server.getConnectors().length, 0);
        Assert.assertNull(server.getConnector());
        
        // 6th server
        server = servers.next();
        Assert.assertEquals(server.getName(), "jettyServer6");
        Assert.assertFalse(server.isAutoStart());
        Assert.assertFalse(server.isRunning());
        Assert.assertEquals(server.getPort(), 8080);
        Assert.assertEquals(server.getResourceBase(), "src/main/resources");
        Assert.assertEquals(server.getContextConfigLocation(), "classpath:citrus-ws-servlet.xml");
        Assert.assertEquals(server.getContextPath(), "/");
        Assert.assertEquals(server.getServletName(), "jettyServer6-servlet");
        Assert.assertEquals(server.getServletMappingPath(), "/*");
        Assert.assertFalse(server.isUseRootContextAsParent());
        Assert.assertNull(server.getSecurityHandler());
        Assert.assertEquals(server.getConnectors().length, 0);
        Assert.assertNull(server.getConnector());
        Assert.assertNotNull(server.getServletHandler());
        Assert.assertEquals(server.getServletHandler(), beanDefinitionContext.getBean("servletHandler"));
    }
}
