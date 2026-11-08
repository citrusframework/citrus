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
import org.citrusframework.camel.endpoint.CamelEndpoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 1.4.1
 */
public class CamelEndpointParserTest extends org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest {

    @Test
    public void testCamelEndpointParser() {
        Map<String, CamelEndpoint> endpoints = beanDefinitionContext.getBeansOfType(CamelEndpoint.class);

        Assertions.assertEquals(3, endpoints.size());

        // 1st message receiver
        CamelEndpoint camelEndpoint = endpoints.get("camelEndpoint1");
        Assertions.assertEquals(camelEndpoint.getEndpointConfiguration().getCamelContext(), beanDefinitionContext.getBean(CamelSettings.getContextName()));
        Assertions.assertEquals("direct:news-feed1", camelEndpoint.getEndpointConfiguration().getEndpointUri());
        Assertions.assertEquals(5000L, camelEndpoint.getEndpointConfiguration().getTimeout());

        // 2nd message receiver
        camelEndpoint = endpoints.get("camelEndpoint2");
        Assertions.assertEquals(beanDefinitionContext.getBean("specialCamelContext"), camelEndpoint.getEndpointConfiguration().getCamelContext());
        Assertions.assertEquals("direct:news-feed2", camelEndpoint.getEndpointConfiguration().getEndpointUri());
        Assertions.assertEquals(10000L, camelEndpoint.getEndpointConfiguration().getTimeout());

        // 3rd message receiver
        camelEndpoint = endpoints.get("camelEndpoint3");
        Assertions.assertEquals(beanDefinitionContext.getBean("messageConverter"), camelEndpoint.getEndpointConfiguration().getMessageConverter());
        Assertions.assertEquals("direct:news-feed3", camelEndpoint.getEndpointConfiguration().getEndpointUri());
        Assertions.assertEquals(beanDefinitionContext.getBean("testActor", TestActor.class), camelEndpoint.getActor());
    }
}
