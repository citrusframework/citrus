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

package org.citrusframework.jms.config.xml;

import jakarta.jms.ConnectionFactory;
import java.util.Map;

import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.jms.endpoint.JmsEndpointAdapter;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 1.4
 */
public class JmsEndpointAdapterParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testParseBeanDefinition() {
        Map<String, JmsEndpointAdapter> adapters = beanDefinitionContext.getBeansOfType(JmsEndpointAdapter.class);

        Assertions.assertEquals(2, adapters.size());

        // 1st endpoint adapter
        JmsEndpointAdapter adapter = adapters.get("endpointAdapter1");
        Assertions.assertEquals("endpointAdapter1", adapter.getName());
        Assertions.assertEquals(5000L, adapter.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals("serverQueue", (adapter.getEndpointConfiguration()).getDestinationName());
        Assertions.assertFalse(adapter.getEndpointConfiguration().isUseObjectMessages());
        Assertions.assertFalse(adapter.getEndpointConfiguration().isFilterInternalHeaders());
        Assertions.assertEquals(beanDefinitionContext.getBean("connectionFactory", ConnectionFactory.class), (adapter.getEndpointConfiguration()).getConnectionFactory());
        Assertions.assertNull(adapter.getFallbackEndpointAdapter());

        adapter = adapters.get("endpointAdapter2");
        Assertions.assertEquals(2500L, adapter.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals("replyQueue", (adapter.getEndpointConfiguration()).getReplyDestinationName());
        Assertions.assertEquals("fooQueue", (adapter.getEndpointConfiguration()).getDestinationName());
        Assertions.assertTrue(adapter.getEndpointConfiguration().isUseObjectMessages());
        Assertions.assertFalse(adapter.getEndpointConfiguration().isFilterInternalHeaders());
        Assertions.assertEquals(beanDefinitionContext.getBean("connectionFactory", ConnectionFactory.class), (adapter.getEndpointConfiguration()).getConnectionFactory());
        Assertions.assertEquals(beanDefinitionContext.getBean("mockEndpointAdapter", EndpointAdapter.class), adapter.getFallbackEndpointAdapter());
    }
}
