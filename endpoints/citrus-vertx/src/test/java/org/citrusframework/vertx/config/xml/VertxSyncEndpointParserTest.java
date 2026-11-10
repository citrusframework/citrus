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

package org.citrusframework.vertx.config.xml;

import org.citrusframework.TestActor;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.citrusframework.vertx.endpoint.VertxSyncEndpoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @since 1.4.1
 */
public class VertxSyncEndpointParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testVertxSyncEndpointParser() {
        Map<String, VertxSyncEndpoint> endpoints = beanDefinitionContext.getBeansOfType(VertxSyncEndpoint.class);

        Assertions.assertEquals(4, endpoints.size());

        // 1st message receiver
        VertxSyncEndpoint vertxEndpoint = endpoints.get("vertxEndpoint1");
        Assertions.assertNotNull(vertxEndpoint.getVertxInstanceFactory());
        Assertions.assertEquals(beanDefinitionContext.getBean("vertxInstanceFactory"), vertxEndpoint.getVertxInstanceFactory());
        Assertions.assertEquals(DefaultMessageCorrelator.class, vertxEndpoint.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertEquals("news-feed1", vertxEndpoint.getEndpointConfiguration().getAddress());
        Assertions.assertEquals(5000L, vertxEndpoint.getEndpointConfiguration().getTimeout());

        // 2nd message receiver
        vertxEndpoint = endpoints.get("vertxEndpoint2");
        Assertions.assertNotNull(vertxEndpoint.getVertxInstanceFactory());
        Assertions.assertEquals(beanDefinitionContext.getBean("specialVertxInstanceFactory"), vertxEndpoint.getVertxInstanceFactory());
        Assertions.assertEquals(beanDefinitionContext.getBean("replyMessageCorrelator"), vertxEndpoint.getEndpointConfiguration().getCorrelator());
        Assertions.assertEquals(beanDefinitionContext.getBean("messageConverter"), vertxEndpoint.getEndpointConfiguration().getMessageConverter());
        Assertions.assertEquals("127.0.0.1", vertxEndpoint.getEndpointConfiguration().getHost());
        Assertions.assertEquals(10105, vertxEndpoint.getEndpointConfiguration().getPort());
        Assertions.assertEquals("news-feed2", vertxEndpoint.getEndpointConfiguration().getAddress());
        Assertions.assertEquals(10000L, vertxEndpoint.getEndpointConfiguration().getTimeout());

        // 3rd message receiver
        vertxEndpoint = endpoints.get("vertxEndpoint3");
        Assertions.assertEquals("news-feed3", vertxEndpoint.getEndpointConfiguration().getAddress());
        Assertions.assertTrue(vertxEndpoint.getEndpointConfiguration().isPubSubDomain());

        // 4th message receiver
        vertxEndpoint = endpoints.get("vertxEndpoint4");
        Assertions.assertNotNull(vertxEndpoint.getActor());
        Assertions.assertEquals("news-feed4", vertxEndpoint.getEndpointConfiguration().getAddress());
        Assertions.assertEquals(beanDefinitionContext.getBean("testActor", TestActor.class), vertxEndpoint.getActor());
    }
}
