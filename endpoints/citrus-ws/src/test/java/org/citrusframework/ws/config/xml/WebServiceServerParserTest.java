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

package org.citrusframework.ws.config.xml;

import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.citrusframework.ws.server.WebServiceServer;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

public class WebServiceServerParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testWebServerParser() {
        Iterator<WebServiceServer> servers = beanDefinitionContext.getBeansOfType(WebServiceServer.class).values().iterator();

        // 1st server
        WebServiceServer server = servers.next();
        Assertions.assertEquals("soapServer1", server.getName());
        Assertions.assertFalse(server.isAutoStart());
        Assertions.assertFalse(server.isRunning());
        Assertions.assertEquals(8080, server.getPort());
        Assertions.assertNull(server.getResourceBase());
        Assertions.assertEquals("classpath:org/citrusframework/ws/citrus-servlet-context.xml", server.getContextConfigLocation());
        Assertions.assertEquals("/", server.getContextPath());
        Assertions.assertEquals("soapServer1-servlet", server.getServletName());
        Assertions.assertEquals("/*", server.getServletMappingPath());
        Assertions.assertFalse(server.isUseRootContextAsParent());
        Assertions.assertNull(server.getSecurityHandler());
        Assertions.assertEquals(0, server.getConnectors().length);
        Assertions.assertNull(server.getConnector());
        Assertions.assertFalse(server.isHandleMimeHeaders());
        Assertions.assertFalse(server.isHandleAttributeHeaders());
        Assertions.assertFalse(server.isKeepSoapEnvelope());
        Assertions.assertNull(server.getSoapHeaderNamespace());
        Assertions.assertEquals("", server.getSoapHeaderPrefix());
        Assertions.assertEquals(MessageDispatcherServlet.DEFAULT_MESSAGE_FACTORY_BEAN_NAME, server.getMessageFactoryName());

        // 2nd server
        server = servers.next();
        Assertions.assertEquals("soapServer2", server.getName());
        Assertions.assertFalse(server.isAutoStart());
        Assertions.assertFalse(server.isRunning());
        Assertions.assertEquals(8081, server.getPort());
        Assertions.assertEquals("src/it/resources", server.getResourceBase());
        Assertions.assertEquals("classpath:org/citrusframework/ws/citrus-ws-servlet.xml", server.getContextConfigLocation());
        Assertions.assertEquals("/citrus", server.getContextPath());
        Assertions.assertEquals("citrus-ws", server.getServletName());
        Assertions.assertEquals("/foo", server.getServletMappingPath());
        Assertions.assertTrue(server.isUseRootContextAsParent());
        Assertions.assertNull(server.getSecurityHandler());
        Assertions.assertEquals(0, server.getConnectors().length);
        Assertions.assertNull(server.getConnector());
        Assertions.assertTrue(server.isHandleMimeHeaders());
        Assertions.assertTrue(server.isHandleAttributeHeaders());
        Assertions.assertTrue(server.isKeepSoapEnvelope());
        Assertions.assertEquals("http://citrusframework.org", server.getSoapHeaderNamespace());
        Assertions.assertEquals("CITRUS", server.getSoapHeaderPrefix());
        Assertions.assertEquals(server.getMessageConverter(), beanDefinitionContext.getBean("messageConverter"));
        Assertions.assertEquals("soap12MessageFactory", server.getMessageFactoryName());

        // 3rd server
        server = servers.next();
        Assertions.assertEquals("soapServer3", server.getName());
        Assertions.assertFalse(server.isAutoStart());
        Assertions.assertFalse(server.isRunning());
        Assertions.assertEquals(8080, server.getPort());
        Assertions.assertNull(server.getResourceBase());
        Assertions.assertEquals("classpath:org/citrusframework/ws/citrus-servlet-context.xml", server.getContextConfigLocation());
        Assertions.assertEquals("/", server.getContextPath());
        Assertions.assertEquals("soapServer3-servlet", server.getServletName());
        Assertions.assertEquals("/*", server.getServletMappingPath());
        Assertions.assertFalse(server.isUseRootContextAsParent());
        Assertions.assertNull(server.getSecurityHandler());
        Assertions.assertEquals(0, server.getConnectors().length);
        Assertions.assertNotNull(server.getConnector());
        Assertions.assertEquals(server.getConnector(), beanDefinitionContext.getBean("connector"));

        // 4th server
        server = servers.next();
        Assertions.assertEquals("soapServer4", server.getName());
        Assertions.assertFalse(server.isAutoStart());
        Assertions.assertFalse(server.isRunning());
        Assertions.assertEquals(8080, server.getPort());
        Assertions.assertNull(server.getResourceBase());
        Assertions.assertEquals("classpath:org/citrusframework/ws/citrus-servlet-context.xml", server.getContextConfigLocation());
        Assertions.assertEquals("/", server.getContextPath());
        Assertions.assertEquals("soapServer4-servlet", server.getServletName());
        Assertions.assertEquals("/*", server.getServletMappingPath());
        Assertions.assertFalse(server.isUseRootContextAsParent());
        Assertions.assertNull(server.getSecurityHandler());
        Assertions.assertNotNull(server.getConnectors());
        Assertions.assertEquals(2, server.getConnectors().length);
        Assertions.assertNull(server.getConnector());

        // 5th server
        server = servers.next();
        Assertions.assertEquals("soapServer5", server.getName());
        Assertions.assertFalse(server.isAutoStart());
        Assertions.assertFalse(server.isRunning());
        Assertions.assertEquals(8080, server.getPort());
        Assertions.assertNull(server.getResourceBase());
        Assertions.assertEquals("classpath:org/citrusframework/ws/citrus-servlet-context.xml", server.getContextConfigLocation());
        Assertions.assertEquals("/", server.getContextPath());
        Assertions.assertEquals("soapServer5-servlet", server.getServletName());
        Assertions.assertEquals("/*", server.getServletMappingPath());
        Assertions.assertFalse(server.isUseRootContextAsParent());
        Assertions.assertNotNull(server.getSecurityHandler());
        Assertions.assertEquals(server.getSecurityHandler(), beanDefinitionContext.getBean("securityHandler"));
        Assertions.assertEquals(0, server.getConnectors().length);
        Assertions.assertNull(server.getConnector());

        // 6th server
        server = servers.next();
        Assertions.assertEquals("soapServer6", server.getName());
        Assertions.assertFalse(server.isAutoStart());
        Assertions.assertFalse(server.isRunning());
        Assertions.assertEquals(8080, server.getPort());
        Assertions.assertNull(server.getResourceBase());
        Assertions.assertEquals("classpath:org/citrusframework/ws/citrus-servlet-context.xml", server.getContextConfigLocation());
        Assertions.assertEquals("/", server.getContextPath());
        Assertions.assertEquals("soapServer6-servlet", server.getServletName());
        Assertions.assertEquals("/*", server.getServletMappingPath());
        Assertions.assertFalse(server.isUseRootContextAsParent());
        Assertions.assertNull(server.getSecurityHandler());
        Assertions.assertEquals(0, server.getConnectors().length);
        Assertions.assertNull(server.getConnector());
        Assertions.assertNotNull(server.getServletHandler());
        Assertions.assertEquals(server.getServletHandler(), beanDefinitionContext.getBean("servletHandler"));
    }
}
