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

import org.citrusframework.endpoint.adapter.RequestDispatchingEndpointAdapter;
import org.citrusframework.endpoint.adapter.mapping.EndpointAdapterMappingStrategy;
import org.citrusframework.endpoint.adapter.mapping.MappingKeyExtractor;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class RequestDispatchingEndpointAdapterParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testParseBeanDefinition() throws Exception {
        Map<String, RequestDispatchingEndpointAdapter> adapters = beanDefinitionContext.getBeansOfType(RequestDispatchingEndpointAdapter.class);

        Assert.assertEquals(adapters.size(), 1);

        // 1st endpoint adapter
        RequestDispatchingEndpointAdapter adapter = adapters.get("endpointAdapter");
        Assert.assertEquals(adapter.getMappingKeyExtractor(), beanDefinitionContext.getBean("mappingKeyExtractor", MappingKeyExtractor.class));
        Assert.assertEquals(adapter.getMappingStrategy(), beanDefinitionContext.getBean("mappingStrategy", EndpointAdapterMappingStrategy.class));
    }
}
