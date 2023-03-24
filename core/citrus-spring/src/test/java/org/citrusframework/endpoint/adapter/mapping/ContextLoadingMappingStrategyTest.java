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

package org.citrusframework.endpoint.adapter.mapping;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class ContextLoadingMappingStrategyTest {

    @Test
    public void testGetEndpointAdapter() throws Exception {
        ContextLoadingMappingStrategy mappingStrategy = new ContextLoadingMappingStrategy();
        mappingStrategy.setContextConfigLocation("classpath:org/citrusframework/endpoint/adapter-mapping-context.xml");

        Assert.assertNotNull(mappingStrategy.getEndpointAdapter("emptyResponseEndpointAdapter"));
        Assert.assertNotNull(mappingStrategy.getEndpointAdapter("staticResponseEndpointAdapter"));

        try {
            mappingStrategy.getEndpointAdapter("Unknown");
            Assert.fail("Missing exception due to unknown mapping key");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof NoSuchBeanDefinitionException);
        }
    }
}
