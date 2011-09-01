/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.http.config.xml;

import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.http.HttpServer;
import com.consol.citrus.testng.AbstractBeanDefinitionParserTest;

/**
 * @author Christoph Deppisch
 */
public class HttpServerParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testFailActionParser() {
        Map<String, HttpServer> servers = beanDefinitionContext.getBeansOfType(HttpServer.class);
        
        Assert.assertEquals(servers.size(), 3);
        
        // 1st message sender
        HttpServer server = servers.get("httpServer1");
        Assert.assertNull(server.getConnector());
        Assert.assertEquals(server.getConnectors().length, 0);
        Assert.assertEquals(server.getName(), "httpServer1");
        Assert.assertEquals(server.getPort(), 8081);
        Assert.assertEquals(server.getContextConfigLocation(), "classpath:com/consol/citrus/http/citrus-http-servlet.xml");
        Assert.assertEquals(server.getResourceBase(), "src/main/resources");
        Assert.assertFalse(server.isAutoStart());
        Assert.assertFalse(server.isUseRootContextAsParent());
        
        // 2nd message sender
        server = servers.get("httpServer2");
        Assert.assertNotNull(server.getConnector());
        Assert.assertEquals(server.getConnector(), beanDefinitionContext.getBean("connector"));
        Assert.assertEquals(server.getConnectors().length, 0);
        Assert.assertEquals(server.getName(), "httpServer2");
        Assert.assertEquals(server.getPort(), 8082);
        Assert.assertEquals(server.getContextConfigLocation(), "classpath:com/consol/citrus/http/servlet-context.xml");
        Assert.assertEquals(server.getResourceBase(), "src/citrus/resources");
        Assert.assertFalse(server.isAutoStart());
        Assert.assertTrue(server.isUseRootContextAsParent());
        
        // 3rd message sender
        server = servers.get("httpServer3");
        Assert.assertNull(server.getConnector());
        Assert.assertNotNull(server.getConnectors());
        Assert.assertEquals(server.getConnectors().length, beanDefinitionContext.getBean("connectors", List.class).size());
        Assert.assertEquals(server.getName(), "httpServer3");
        Assert.assertEquals(server.getPort(), 8083);
        Assert.assertEquals(server.getContextConfigLocation(), "classpath:com/consol/citrus/http/citrus-http-servlet.xml");
        Assert.assertEquals(server.getResourceBase(), "src/main/resources");
        Assert.assertFalse(server.isAutoStart());
        Assert.assertFalse(server.isUseRootContextAsParent());
    }
}
