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

package org.citrusframework.springintegration.config.xml;

import java.util.Map;

import org.citrusframework.springintegration.channel.ChannelEndpointAdapter;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 1.4
 */
public class ChannelEndpointAdapterParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testParseBeanDefinition() {
        Map<String, ChannelEndpointAdapter> adapters = beanDefinitionContext.getBeansOfType(ChannelEndpointAdapter.class);

        Assertions.assertEquals(2, adapters.size());

        // 1st endpoint adapter
        ChannelEndpointAdapter adapter = adapters.get("endpointAdapter1");
        Assertions.assertEquals("endpointAdapter1", adapter.getName());
        Assertions.assertEquals(5000L, adapter.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(500L, adapter.getEndpointConfiguration().getPollingInterval());
        Assertions.assertEquals("serverChannel", (adapter.getEndpointConfiguration()).getChannelName());
        Assertions.assertFalse(adapter.getEndpointConfiguration().isUseObjectMessages());
        Assertions.assertFalse(adapter.getEndpointConfiguration().isFilterInternalHeaders());
        Assertions.assertNull(adapter.getFallbackEndpointAdapter());

        adapter = adapters.get("endpointAdapter2");
        Assertions.assertEquals(10000L, adapter.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(250L, adapter.getEndpointConfiguration().getPollingInterval());
        Assertions.assertEquals("fooChannel", (adapter.getEndpointConfiguration()).getChannelName());
        Assertions.assertTrue(adapter.getEndpointConfiguration().isUseObjectMessages());
        Assertions.assertFalse(adapter.getEndpointConfiguration().isFilterInternalHeaders());
        Assertions.assertEquals(beanDefinitionContext.getBean("mockEndpointAdapter"), adapter.getFallbackEndpointAdapter());
    }
}
