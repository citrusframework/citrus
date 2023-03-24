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
import org.citrusframework.rmi.client.RmiClient;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.rmi.registry.Registry;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class RmiClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testRmiEndpointParser() {
        Map<String, RmiClient> endpoints = beanDefinitionContext.getBeansOfType(RmiClient.class);

        Assert.assertEquals(endpoints.size(), 3);

        // 1st client
        RmiClient rmiClient = endpoints.get("rmiClient1");
        Assert.assertEquals(rmiClient.getEndpointConfiguration().getMethod(), "sayHello");
        Assert.assertEquals(rmiClient.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(rmiClient.getEndpointConfiguration().getPort(), Registry.REGISTRY_PORT);
        Assert.assertEquals(rmiClient.getEndpointConfiguration().getBinding(), "helloService");
        Assert.assertEquals(rmiClient.getEndpointConfiguration().getMethod(), "sayHello");
        Assert.assertEquals(rmiClient.getEndpointConfiguration().getTimeout(), 5000L);

        // 2nd client
        rmiClient = endpoints.get("rmiClient2");
        Assert.assertEquals(rmiClient.getEndpointConfiguration().getMessageConverter(), beanDefinitionContext.getBean("messageConverter"));
        Assert.assertEquals(rmiClient.getEndpointConfiguration().getHost(), "127.0.0.1");
        Assert.assertEquals(rmiClient.getEndpointConfiguration().getPort(), 2099);
        Assert.assertEquals(rmiClient.getEndpointConfiguration().getBinding(), "newsService");
        Assert.assertEquals(rmiClient.getEndpointConfiguration().getMethod(), "getNews");
        Assert.assertEquals(rmiClient.getEndpointConfiguration().getTimeout(), 10000L);

        // 3rd client
        rmiClient = endpoints.get("rmiClient3");
        Assert.assertNotNull(rmiClient.getActor());
        Assert.assertNull(rmiClient.getEndpointConfiguration().getMethod());
        Assert.assertEquals(rmiClient.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
    }

}
