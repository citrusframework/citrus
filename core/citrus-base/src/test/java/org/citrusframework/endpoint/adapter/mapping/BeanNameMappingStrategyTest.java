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

import org.citrusframework.spi.SimpleReferenceResolver;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class BeanNameMappingStrategyTest {

    private SimpleReferenceResolver referenceResolver = new SimpleReferenceResolver();
    private EndpointAdapter fooEndpointAdapter = Mockito.mock(EndpointAdapter.class);
    private EndpointAdapter barEndpointAdapter = Mockito.mock(EndpointAdapter.class);

    @Test
    public void testGetEndpointAdapter() {
        BeanNameMappingStrategy mappingStrategy = new BeanNameMappingStrategy(referenceResolver);

        referenceResolver.bind("foo", fooEndpointAdapter);
        referenceResolver.bind("bar", barEndpointAdapter);

        Assert.assertEquals(mappingStrategy.getEndpointAdapter("foo"), fooEndpointAdapter);
        Assert.assertEquals(mappingStrategy.getEndpointAdapter("bar"), barEndpointAdapter);

        try {
            mappingStrategy.getEndpointAdapter("unknown");
            Assert.fail("Missing exception due to unknown mapping key");
        } catch (CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Unable to find bean reference for name 'unknown'");
        }

    }
}
