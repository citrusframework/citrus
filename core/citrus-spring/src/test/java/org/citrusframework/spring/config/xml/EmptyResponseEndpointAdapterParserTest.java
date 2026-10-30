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

import org.citrusframework.base.endpoint.adapter.EmptyResponseEndpointAdapter;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 1.4
 */
public class EmptyResponseEndpointAdapterParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testParseBeanDefinition() {
        Map<String, EmptyResponseEndpointAdapter> adapters = beanDefinitionContext.getBeansOfType(EmptyResponseEndpointAdapter.class);

        Assertions.assertEquals(1, adapters.size());

        // 1st endpoint adapter
        EmptyResponseEndpointAdapter adapter = adapters.get("endpointAdapter");
        Assertions.assertNotNull(adapter);
    }
}
