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

package org.citrusframework.camel.util;

import org.apache.camel.CamelContext;
import org.apache.camel.support.SimpleRegistry;
import org.citrusframework.camel.UnitTestSupport;
import org.citrusframework.camel.context.CamelReferenceResolver;
import org.citrusframework.camel.endpoint.CamelEndpointConfiguration;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

public class CamelUtilsTest extends UnitTestSupport {

    @Mock
    private CamelContext camelContext;

    @BeforeClass
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCamelBeanPropagation() {
        SimpleRegistry registry = new SimpleRegistry();

        when(camelContext.getRegistry()).thenReturn(registry);

        Object myBean = new Object();
        context.getReferenceResolver().bind("myBean", myBean);

        CamelReferenceResolver resolver = new CamelReferenceResolver(camelContext).withFallback(context.getReferenceResolver());
        context.setReferenceResolver(resolver);

        CamelEndpointConfiguration endpointConfiguration = new CamelEndpointConfiguration();
        endpointConfiguration.setCamelContext(camelContext);
        endpointConfiguration.setEndpointUri("direct:test?otherProp=foo&factory=#myBean&anotherProp=bar");
        CamelUtils.resolveEndpointUri(context, endpointConfiguration);

        Assert.assertEquals(registry.lookupByName("myBean"), myBean);
    }

    @Test
    public void testCamelBeanPropagationNotFound() {
        SimpleRegistry registry = new SimpleRegistry();

        when(camelContext.getRegistry()).thenReturn(registry);

        CamelReferenceResolver resolver = new CamelReferenceResolver(camelContext).withFallback(context.getReferenceResolver());
        context.setReferenceResolver(resolver);

        CamelEndpointConfiguration endpointConfiguration = new CamelEndpointConfiguration();
        endpointConfiguration.setCamelContext(camelContext);
        endpointConfiguration.setEndpointUri("direct:test?otherProp=foo&factory=#myBean&anotherProp=bar");
        CamelUtils.resolveEndpointUri(context, endpointConfiguration);

        Assert.assertNull(registry.lookupByName("myBean"));
    }

    @Test
    public void testExistingCamelBean() {
        SimpleRegistry registry = new SimpleRegistry();

        Object existingBean = new Object();
        registry.bind("myBean", existingBean);

        when(camelContext.getRegistry()).thenReturn(registry);

        Object myBean = new Object();
        context.getReferenceResolver().bind("myBean", myBean);

        CamelReferenceResolver resolver = new CamelReferenceResolver(camelContext).withFallback(context.getReferenceResolver());
        context.setReferenceResolver(resolver);

        CamelEndpointConfiguration endpointConfiguration = new CamelEndpointConfiguration();
        endpointConfiguration.setCamelContext(camelContext);
        endpointConfiguration.setEndpointUri("direct:test?otherProp=foo&factory=#myBean&anotherProp=bar");
        CamelUtils.resolveEndpointUri(context, endpointConfiguration);

        Assert.assertEquals(registry.lookupByName("myBean"), existingBean);
    }
}
