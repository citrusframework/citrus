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

package org.citrusframework.jms.config.xml;

import jakarta.jms.ConnectionFactory;
import java.util.Map;

import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.jms.endpoint.JmsEndpointAdapter;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class JmsEndpointAdapterParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testParseBeanDefinition() throws Exception {
        Map<String, JmsEndpointAdapter> adapters = beanDefinitionContext.getBeansOfType(JmsEndpointAdapter.class);

        Assert.assertEquals(adapters.size(), 2);

        // 1st endpoint adapter
        JmsEndpointAdapter adapter = adapters.get("endpointAdapter1");
        Assert.assertEquals(adapter.getName(), "endpointAdapter1");
        Assert.assertEquals(adapter.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertEquals((adapter.getEndpointConfiguration()).getDestinationName(), "serverQueue");
        Assert.assertFalse(adapter.getEndpointConfiguration().isUseObjectMessages());
        Assert.assertFalse(adapter.getEndpointConfiguration().isFilterInternalHeaders());
        Assert.assertEquals((adapter.getEndpointConfiguration()).getConnectionFactory(), beanDefinitionContext.getBean("connectionFactory", ConnectionFactory.class));
        Assert.assertNull(adapter.getFallbackEndpointAdapter());

        adapter = adapters.get("endpointAdapter2");
        Assert.assertEquals(adapter.getEndpointConfiguration().getTimeout(), 2500L);
        Assert.assertEquals((adapter.getEndpointConfiguration()).getReplyDestinationName(), "replyQueue");
        Assert.assertEquals((adapter.getEndpointConfiguration()).getDestinationName(), "fooQueue");
        Assert.assertTrue(adapter.getEndpointConfiguration().isUseObjectMessages());
        Assert.assertFalse(adapter.getEndpointConfiguration().isFilterInternalHeaders());
        Assert.assertEquals((adapter.getEndpointConfiguration()).getConnectionFactory(), beanDefinitionContext.getBean("connectionFactory", ConnectionFactory.class));
        Assert.assertEquals(adapter.getFallbackEndpointAdapter(), beanDefinitionContext.getBean("mockEndpointAdapter", EndpointAdapter.class));
    }
}
