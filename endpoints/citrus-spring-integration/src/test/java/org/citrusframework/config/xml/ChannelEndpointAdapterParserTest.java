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

package org.citrusframework.config.xml;

import java.util.Map;

import org.citrusframework.channel.ChannelEndpointAdapter;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class ChannelEndpointAdapterParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testParseBeanDefinition() throws Exception {
        Map<String, ChannelEndpointAdapter> adapters = beanDefinitionContext.getBeansOfType(ChannelEndpointAdapter.class);

        Assert.assertEquals(adapters.size(), 2);

        // 1st endpoint adapter
        ChannelEndpointAdapter adapter = adapters.get("endpointAdapter1");
        Assert.assertEquals(adapter.getName(), "endpointAdapter1");
        Assert.assertEquals(adapter.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertEquals(adapter.getEndpointConfiguration().getPollingInterval(), 500L);
        Assert.assertEquals((adapter.getEndpointConfiguration()).getChannelName(), "serverChannel");
        Assert.assertFalse(adapter.getEndpointConfiguration().isUseObjectMessages());
        Assert.assertFalse(adapter.getEndpointConfiguration().isFilterInternalHeaders());
        Assert.assertNull(adapter.getFallbackEndpointAdapter());

        adapter = adapters.get("endpointAdapter2");
        Assert.assertEquals(adapter.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertEquals(adapter.getEndpointConfiguration().getPollingInterval(), 250L);
        Assert.assertEquals((adapter.getEndpointConfiguration()).getChannelName(), "fooChannel");
        Assert.assertTrue(adapter.getEndpointConfiguration().isUseObjectMessages());
        Assert.assertFalse(adapter.getEndpointConfiguration().isFilterInternalHeaders());
        Assert.assertEquals(adapter.getFallbackEndpointAdapter(), beanDefinitionContext.getBean("mockEndpointAdapter"));
    }
}
