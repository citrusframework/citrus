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
import org.citrusframework.rmi.client.RmiClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.rmi.registry.Registry;
import java.util.Map;

/**
 * @since 2.5
 */
public class RmiClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testRmiEndpointParser() {
        Map<String, RmiClient> endpoints = beanDefinitionContext.getBeansOfType(RmiClient.class);

        Assertions.assertEquals(3, endpoints.size());

        // 1st client
        RmiClient rmiClient = endpoints.get("rmiClient1");
        Assertions.assertEquals("sayHello", rmiClient.getEndpointConfiguration().getMethod());
        Assertions.assertEquals("localhost", rmiClient.getEndpointConfiguration().getHost());
        Assertions.assertEquals(Registry.REGISTRY_PORT, rmiClient.getEndpointConfiguration().getPort());
        Assertions.assertEquals("helloService", rmiClient.getEndpointConfiguration().getBinding());
        Assertions.assertEquals("sayHello", rmiClient.getEndpointConfiguration().getMethod());
        Assertions.assertEquals(5000L, rmiClient.getEndpointConfiguration().getTimeout());

        // 2nd client
        rmiClient = endpoints.get("rmiClient2");
        Assertions.assertEquals(beanDefinitionContext.getBean("messageConverter"), rmiClient.getEndpointConfiguration().getMessageConverter());
        Assertions.assertEquals("127.0.0.1", rmiClient.getEndpointConfiguration().getHost());
        Assertions.assertEquals(2099, rmiClient.getEndpointConfiguration().getPort());
        Assertions.assertEquals("newsService", rmiClient.getEndpointConfiguration().getBinding());
        Assertions.assertEquals("getNews", rmiClient.getEndpointConfiguration().getMethod());
        Assertions.assertEquals(10000L, rmiClient.getEndpointConfiguration().getTimeout());

        // 3rd client
        rmiClient = endpoints.get("rmiClient3");
        Assertions.assertNotNull(rmiClient.getActor());
        Assertions.assertNull(rmiClient.getEndpointConfiguration().getMethod());
        Assertions.assertEquals(beanDefinitionContext.getBean("testActor", TestActor.class), rmiClient.getActor());
    }
}
