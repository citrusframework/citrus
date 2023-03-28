/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.rmi.config.xml;

import org.citrusframework.TestActor;
import org.citrusframework.rmi.remote.HelloService;
import org.citrusframework.rmi.remote.NewsService;
import org.citrusframework.rmi.server.RmiServer;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.rmi.registry.Registry;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class RmiServerParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testRmiServerParser() {
        Map<String, RmiServer> endpoints = beanDefinitionContext.getBeansOfType(RmiServer.class);

        Assert.assertEquals(endpoints.size(), 3);

        // 1st server
        RmiServer rmiServer = endpoints.get("rmiServer1");
        Assert.assertNull(rmiServer.getEndpointConfiguration().getMethod());
        Assert.assertNull(rmiServer.getEndpointConfiguration().getHost());
        Assert.assertEquals(rmiServer.getEndpointConfiguration().getPort(), Registry.REGISTRY_PORT);
        Assert.assertEquals(rmiServer.getEndpointConfiguration().getBinding(), "helloService");
        Assert.assertFalse(rmiServer.isCreateRegistry());
        Assert.assertEquals(rmiServer.getRemoteInterfaces().size(), 1L);
        Assert.assertEquals(rmiServer.getRemoteInterfaces().get(0), HelloService.class);
        Assert.assertEquals(rmiServer.getEndpointConfiguration().getTimeout(), 5000L);

        // 2nd server
        rmiServer = endpoints.get("rmiServer2");
        Assert.assertEquals(rmiServer.getEndpointConfiguration().getMessageConverter(), beanDefinitionContext.getBean("messageConverter"));
        Assert.assertEquals(rmiServer.getEndpointConfiguration().getHost(), "127.0.0.1");
        Assert.assertEquals(rmiServer.getEndpointConfiguration().getPort(), 2099);
        Assert.assertEquals(rmiServer.getEndpointConfiguration().getBinding(), "newsService");
        Assert.assertTrue(rmiServer.isCreateRegistry());
        Assert.assertEquals(rmiServer.getRemoteInterfaces().size(), 1L);
        Assert.assertEquals(rmiServer.getRemoteInterfaces().get(0), NewsService.class);
        Assert.assertEquals(rmiServer.getEndpointConfiguration().getTimeout(), 10000L);

        // 3rd server
        rmiServer = endpoints.get("rmiServer3");
        Assert.assertNotNull(rmiServer.getActor());
        Assert.assertEquals(rmiServer.getRemoteInterfaces().size(), 1L);
        Assert.assertEquals(rmiServer.getRemoteInterfaces().get(0), HelloService.class);
        Assert.assertEquals(rmiServer.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
    }

}
