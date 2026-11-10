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

package org.citrusframework.rmi.config.xml;

import org.citrusframework.TestActor;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.citrusframework.rmi.remote.HelloService;
import org.citrusframework.rmi.remote.NewsService;
import org.citrusframework.rmi.server.RmiServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.rmi.registry.Registry;
import java.util.Map;

/**
 * @since 2.5
 */
public class RmiServerParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testRmiServerParser() {
        Map<String, RmiServer> endpoints = beanDefinitionContext.getBeansOfType(RmiServer.class);

        Assertions.assertEquals(3, endpoints.size());

        // 1st server
        RmiServer rmiServer = endpoints.get("rmiServer1");
        Assertions.assertNull(rmiServer.getEndpointConfiguration().getMethod());
        Assertions.assertNull(rmiServer.getEndpointConfiguration().getHost());
        Assertions.assertEquals(Registry.REGISTRY_PORT, rmiServer.getEndpointConfiguration().getPort());
        Assertions.assertEquals("helloService", rmiServer.getEndpointConfiguration().getBinding());
        Assertions.assertFalse(rmiServer.isCreateRegistry());
        Assertions.assertEquals(1L, rmiServer.getRemoteInterfaces().size());
        Assertions.assertEquals(HelloService.class, rmiServer.getRemoteInterfaces().get(0));
        Assertions.assertEquals(5000L, rmiServer.getEndpointConfiguration().getTimeout());

        // 2nd server
        rmiServer = endpoints.get("rmiServer2");
        Assertions.assertEquals(beanDefinitionContext.getBean("messageConverter"), rmiServer.getEndpointConfiguration().getMessageConverter());
        Assertions.assertEquals("127.0.0.1", rmiServer.getEndpointConfiguration().getHost());
        Assertions.assertEquals(2099, rmiServer.getEndpointConfiguration().getPort());
        Assertions.assertEquals("newsService", rmiServer.getEndpointConfiguration().getBinding());
        Assertions.assertTrue(rmiServer.isCreateRegistry());
        Assertions.assertEquals(1L, rmiServer.getRemoteInterfaces().size());
        Assertions.assertEquals(NewsService.class, rmiServer.getRemoteInterfaces().get(0));
        Assertions.assertEquals(10000L, rmiServer.getEndpointConfiguration().getTimeout());

        // 3rd server
        rmiServer = endpoints.get("rmiServer3");
        Assertions.assertNotNull(rmiServer.getActor());
        Assertions.assertEquals(1L, rmiServer.getRemoteInterfaces().size());
        Assertions.assertEquals(HelloService.class, rmiServer.getRemoteInterfaces().get(0));
        Assertions.assertEquals(beanDefinitionContext.getBean("testActor", TestActor.class), rmiServer.getActor());
    }
}
