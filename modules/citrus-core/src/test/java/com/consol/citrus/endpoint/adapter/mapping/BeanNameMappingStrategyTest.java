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

package com.consol.citrus.endpoint.adapter.mapping;

import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.mockito.Mockito;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;


/**
 * @author Christoph Deppisch
 */
public class BeanNameMappingStrategyTest {

    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
    private EndpointAdapter fooEndpointAdapter = Mockito.mock(EndpointAdapter.class);
    private EndpointAdapter barEndpointAdapter = Mockito.mock(EndpointAdapter.class);

    @Test
    public void testGetEndpointAdapter() throws Exception {
        BeanNameMappingStrategy mappingStrategy = new BeanNameMappingStrategy();

        mappingStrategy.setApplicationContext(applicationContext);

        reset(applicationContext);

        when(applicationContext.getBean("foo", EndpointAdapter.class)).thenReturn(fooEndpointAdapter);
        when(applicationContext.getBean("bar", EndpointAdapter.class)).thenReturn(barEndpointAdapter);
        doThrow(new NoSuchBeanDefinitionException("unknown")).when(applicationContext).getBean("unknown", EndpointAdapter.class);

        Assert.assertEquals(mappingStrategy.getEndpointAdapter("foo"), fooEndpointAdapter);
        Assert.assertEquals(mappingStrategy.getEndpointAdapter("bar"), barEndpointAdapter);

        try {
            mappingStrategy.getEndpointAdapter("unknown");
            Assert.fail("Missing exception due to unknown mapping key");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof NoSuchBeanDefinitionException);
        }

    }
}
