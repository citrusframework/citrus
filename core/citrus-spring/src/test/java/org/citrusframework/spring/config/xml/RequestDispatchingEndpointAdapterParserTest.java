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

package org.citrusframework.spring.config.xml;

import java.util.Map;

import org.citrusframework.base.endpoint.adapter.RequestDispatchingEndpointAdapter;
import org.citrusframework.endpoint.adapter.mapping.EndpointAdapterMappingStrategy;
import org.citrusframework.endpoint.adapter.mapping.MappingKeyExtractor;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RequestDispatchingEndpointAdapterParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testParseBeanDefinition() {
        Map<String, RequestDispatchingEndpointAdapter> adapters = beanDefinitionContext.getBeansOfType(RequestDispatchingEndpointAdapter.class);

        Assertions.assertEquals(1, adapters.size());

        // 1st endpoint adapter
        RequestDispatchingEndpointAdapter adapter = adapters.get("endpointAdapter");
        Assertions.assertEquals(beanDefinitionContext.getBean("mappingKeyExtractor", MappingKeyExtractor.class), adapter.getMappingKeyExtractor());
        Assertions.assertEquals(beanDefinitionContext.getBean("mappingStrategy", EndpointAdapterMappingStrategy.class), adapter.getMappingStrategy());
    }
}
