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

package org.citrusframework.endpoint;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.endpoint.direct.DirectEndpointComponent;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class DefaultEndpointFactoryTest {

    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);

    @Test
    public void testResolveDirectEndpoint() throws Exception {
        reset(referenceResolver);
        when(referenceResolver.resolve("myEndpoint", Endpoint.class)).thenReturn(Mockito.mock(Endpoint.class));
        TestContext context = new TestContext();
        context.setReferenceResolver(referenceResolver);

        DefaultEndpointFactory factory = new DefaultEndpointFactory();
        Endpoint endpoint = factory.create("myEndpoint", context);

        Assert.assertNotNull(endpoint);
    }

    @Test
    public void testResolveCustomEndpoint() throws Exception {
        Map<String, EndpointComponent> components = new HashMap<String, EndpointComponent>();
        components.put("custom", new DirectEndpointComponent());

        reset(referenceResolver);
        when(referenceResolver.resolveAll(EndpointComponent.class)).thenReturn(components);
        TestContext context = new TestContext();
        context.setReferenceResolver(referenceResolver);

        DefaultEndpointFactory factory = new DefaultEndpointFactory();
        Endpoint endpoint = factory.create("custom:custom.queue", context);

        Assert.assertEquals(endpoint.getClass(), DirectEndpoint.class);
        Assert.assertEquals(((DirectEndpoint)endpoint).getEndpointConfiguration().getQueueName(), "custom.queue");
    }

    @Test
    public void testOverwriteEndpointComponent() throws Exception {
        Map<String, EndpointComponent> components = new HashMap<String, EndpointComponent>();
        components.put("jms", new DirectEndpointComponent());

        reset(referenceResolver);
        when(referenceResolver.resolveAll(EndpointComponent.class)).thenReturn(components);
        TestContext context = new TestContext();
        context.setReferenceResolver(referenceResolver);

        DefaultEndpointFactory factory = new DefaultEndpointFactory();
        Endpoint endpoint = factory.create("jms:custom.queue", context);

        Assert.assertEquals(endpoint.getClass(), DirectEndpoint.class);
        Assert.assertEquals(((DirectEndpoint)endpoint).getEndpointConfiguration().getQueueName(), "custom.queue");
    }

    @Test
    public void testResolveUnknownEndpointComponent() throws Exception {
        reset(referenceResolver);
        when(referenceResolver.resolveAll(EndpointComponent.class)).thenReturn(Collections.emptyMap());
        TestContext context = new TestContext();
        context.setReferenceResolver(referenceResolver);

        DefaultEndpointFactory factory = new DefaultEndpointFactory();
        try {
            factory.create("unknown:unknown", context);
            Assert.fail("Missing exception due to unknown endpoint component");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unable to create endpoint component"));
        }
    }

    @Test
    public void testResolveInvalidEndpointUri() throws Exception {
        reset(referenceResolver);
        TestContext context = new TestContext();
        context.setReferenceResolver(referenceResolver);

        DefaultEndpointFactory factory = new DefaultEndpointFactory();
        try {
            factory.create("jms:", context);
            Assert.fail("Missing exception due to invalid endpoint uri");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Invalid endpoint uri"));
        }
    }
}
