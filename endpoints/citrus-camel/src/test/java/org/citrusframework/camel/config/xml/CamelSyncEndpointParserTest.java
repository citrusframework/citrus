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

package org.citrusframework.camel.config.xml;

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.camel.CamelSettings;
import org.citrusframework.camel.endpoint.CamelSyncEndpoint;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 1.4.1
 */
public class CamelSyncEndpointParserTest extends org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest {

    @Test
    public void testCamelSyncEndpointParser() {
        Map<String, CamelSyncEndpoint> endpoints = beanDefinitionContext.getBeansOfType(CamelSyncEndpoint.class);

        Assertions.assertEquals(3, endpoints.size());

        // 1st message receiver
        CamelSyncEndpoint camelEndpoint = endpoints.get("camelSyncEndpoint1");
        Assertions.assertEquals(camelEndpoint.getEndpointConfiguration().getCamelContext(), beanDefinitionContext.getBean(CamelSettings.getContextName()));
        Assertions.assertEquals(DefaultMessageCorrelator.class, camelEndpoint.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertEquals("direct:news-feed1", camelEndpoint.getEndpointConfiguration().getEndpointUri());
        Assertions.assertEquals(500L, camelEndpoint.getEndpointConfiguration().getPollingInterval());
        Assertions.assertEquals(5000L, camelEndpoint.getEndpointConfiguration().getTimeout());

        // 2nd message receiver
        camelEndpoint = endpoints.get("camelSyncEndpoint2");
        Assertions.assertEquals(beanDefinitionContext.getBean("specialCamelContext"), camelEndpoint.getEndpointConfiguration().getCamelContext());
        Assertions.assertEquals(beanDefinitionContext.getBean("replyMessageCorrelator"), camelEndpoint.getEndpointConfiguration().getCorrelator());
        Assertions.assertEquals("direct:news-feed2", camelEndpoint.getEndpointConfiguration().getEndpointUri());
        Assertions.assertEquals(500L, camelEndpoint.getEndpointConfiguration().getPollingInterval());
        Assertions.assertEquals(10000L, camelEndpoint.getEndpointConfiguration().getTimeout());

        // 3rd message receiver
        camelEndpoint = endpoints.get("camelSyncEndpoint3");
        Assertions.assertEquals(beanDefinitionContext.getBean("messageConverter"), camelEndpoint.getEndpointConfiguration().getMessageConverter());
        Assertions.assertEquals("direct:news-feed3", camelEndpoint.getEndpointConfiguration().getEndpointUri());
        Assertions.assertEquals(200L, camelEndpoint.getEndpointConfiguration().getPollingInterval());
        Assertions.assertEquals(beanDefinitionContext.getBean("testActor", TestActor.class), camelEndpoint.getActor());
    }
}
