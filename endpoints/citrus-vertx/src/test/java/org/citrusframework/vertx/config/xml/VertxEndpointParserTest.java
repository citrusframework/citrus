/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.vertx.config.xml;

import org.citrusframework.TestActor;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.citrusframework.vertx.endpoint.VertxEndpoint;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Christoph Deppisch
 */
public class VertxEndpointParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testVertxEndpointParser() {
        Map<String, VertxEndpoint> endpoints = beanDefinitionContext.getBeansOfType(VertxEndpoint.class);

        Assert.assertEquals(endpoints.size(), 4);

        // 1st message receiver
        VertxEndpoint vertxEndpoint = endpoints.get("vertxEndpoint1");
        Assert.assertNotNull(vertxEndpoint.getVertxInstanceFactory());
        Assert.assertEquals(vertxEndpoint.getVertxInstanceFactory(), beanDefinitionContext.getBean("vertxInstanceFactory"));
        Assert.assertEquals(vertxEndpoint.getEndpointConfiguration().getAddress(), "news-feed1");
        Assert.assertEquals(vertxEndpoint.getEndpointConfiguration().getTimeout(), 5000L);

        // 2nd message receiver
        vertxEndpoint = endpoints.get("vertxEndpoint2");
        Assert.assertNotNull(vertxEndpoint.getVertxInstanceFactory());
        Assert.assertEquals(vertxEndpoint.getVertxInstanceFactory(), beanDefinitionContext.getBean("specialVertxInstanceFactory"));
        Assert.assertEquals(vertxEndpoint.getEndpointConfiguration().getMessageConverter(), beanDefinitionContext.getBean("messageConverter"));
        Assert.assertEquals(vertxEndpoint.getEndpointConfiguration().getHost(), "127.0.0.1");
        Assert.assertEquals(vertxEndpoint.getEndpointConfiguration().getPort(), 10105);
        Assert.assertEquals(vertxEndpoint.getEndpointConfiguration().getAddress(), "news-feed2");
        Assert.assertEquals(vertxEndpoint.getEndpointConfiguration().getTimeout(), 10000L);

        // 3rd message receiver
        vertxEndpoint = endpoints.get("vertxEndpoint3");
        Assert.assertEquals(vertxEndpoint.getEndpointConfiguration().getAddress(), "news-feed3");
        Assert.assertEquals(vertxEndpoint.getEndpointConfiguration().isPubSubDomain(), true);

        // 4th message receiver
        vertxEndpoint = endpoints.get("vertxEndpoint4");
        Assert.assertNotNull(vertxEndpoint.getActor());
        Assert.assertEquals(vertxEndpoint.getEndpointConfiguration().getAddress(), "news-feed4");
        Assert.assertEquals(vertxEndpoint.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
    }
}
