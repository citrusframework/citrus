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

package org.citrusframework.spring.endpoint.adapter.mapping;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * @since 1.4
 */
public class ContextLoadingMappingStrategyTest {

    @Test
    public void testGetEndpointAdapter() {
        ContextLoadingMappingStrategy mappingStrategy = new ContextLoadingMappingStrategy();
        mappingStrategy.setContextConfigLocation("classpath:org/citrusframework/spring/endpoint/adapter-mapping-context.xml");

        Assertions.assertNotNull(mappingStrategy.getEndpointAdapter("emptyResponseEndpointAdapter"));
        Assertions.assertNotNull(mappingStrategy.getEndpointAdapter("staticResponseEndpointAdapter"));

        try {
            mappingStrategy.getEndpointAdapter("Unknown");
            Assertions.fail("Missing exception due to unknown mapping key");
        } catch (CitrusRuntimeException e) {
            Assertions.assertTrue(e.getCause() instanceof NoSuchBeanDefinitionException);
        }
    }
}
