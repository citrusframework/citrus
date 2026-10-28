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

package org.citrusframework.base.endpoint;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.endpoint.direct.DirectEndpointComponent;
import org.citrusframework.endpoint.context.MessageStoreEndpointComponent;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class DefaultEndpointFactoryTest {

    private final ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);

    @Test
    public void testResolveDirectEndpoint() {
        reset(referenceResolver);
        when(referenceResolver.isResolvable("myEndpoint", Endpoint.class)).thenReturn(true);
        when(referenceResolver.resolve("myEndpoint", Endpoint.class)).thenReturn(Mockito.mock(Endpoint.class));
        TestContext context = new TestContext();
        context.setReferenceResolver(referenceResolver);

        DefaultEndpointFactory factory = new DefaultEndpointFactory();
        Endpoint endpoint = factory.create("myEndpoint", context);

        Assertions.assertNotNull(endpoint);
    }

    @Test
    public void testResolveCustomEndpoint() {
        Map<String, EndpointComponent> components = new HashMap<>();
        components.put("custom", new DirectEndpointComponent());

        reset(referenceResolver);
        when(referenceResolver.resolveAll(EndpointComponent.class)).thenReturn(components);
        TestContext context = new TestContext();
        context.setReferenceResolver(referenceResolver);

        DefaultEndpointFactory factory = new DefaultEndpointFactory();
        Endpoint endpoint = factory.create("custom:custom.queue", context);

        Assertions.assertEquals(DirectEndpoint.class, endpoint.getClass());
        Assertions.assertEquals("custom.queue", ((DirectEndpoint)endpoint).getEndpointConfiguration().getQueueName());
    }

    @Test
    public void testResolveCustomEndpointWithParameters() {
        Map<String, EndpointComponent> components = new HashMap<>();
        components.put("custom", new DirectEndpointComponent());

        reset(referenceResolver);
        when(referenceResolver.resolveAll(EndpointComponent.class)).thenReturn(components);
        TestContext context = new TestContext();
        context.setReferenceResolver(referenceResolver);

        DefaultEndpointFactory factory = new DefaultEndpointFactory();
        Endpoint endpoint = factory.create("custom:custom.queue?endpointName=foo", context);

        Assertions.assertEquals(DirectEndpoint.class, endpoint.getClass());
        Assertions.assertEquals("custom.queue", ((DirectEndpoint)endpoint).getEndpointConfiguration().getQueueName());
    }

    @Test
    public void testResolveCustomEndpointQueueNameAsParameters() {
        Map<String, EndpointComponent> components = new HashMap<>();
        components.put("custom", new DirectEndpointComponent());

        reset(referenceResolver);
        when(referenceResolver.resolveAll(EndpointComponent.class)).thenReturn(components);
        TestContext context = new TestContext();
        context.setReferenceResolver(referenceResolver);

        DefaultEndpointFactory factory = new DefaultEndpointFactory();
        Endpoint endpoint = factory.create("custom?queueName=foo:bar", context);

        Assertions.assertEquals(DirectEndpoint.class, endpoint.getClass());
        Assertions.assertEquals("foo:bar", ((DirectEndpoint)endpoint).getEndpointConfiguration().getQueueName());
    }

    @Test
    public void testOverwriteEndpointComponent() {
        Map<String, EndpointComponent> components = new HashMap<>();
        components.put("jms", new DirectEndpointComponent());

        reset(referenceResolver);
        when(referenceResolver.resolveAll(EndpointComponent.class)).thenReturn(components);
        TestContext context = new TestContext();
        context.setReferenceResolver(referenceResolver);

        DefaultEndpointFactory factory = new DefaultEndpointFactory();
        Endpoint endpoint = factory.create("jms:custom.queue", context);

        Assertions.assertEquals(DirectEndpoint.class, endpoint.getClass());
        Assertions.assertEquals("custom.queue", ((DirectEndpoint)endpoint).getEndpointConfiguration().getQueueName());
    }

    @Test
    public void testResolveUnknownEndpointComponent() {
        reset(referenceResolver);
        when(referenceResolver.resolveAll(EndpointComponent.class)).thenReturn(Collections.emptyMap());
        TestContext context = new TestContext();
        context.setReferenceResolver(referenceResolver);

        DefaultEndpointFactory factory = new DefaultEndpointFactory();
        try {
            factory.create("unknown:unknown", context);
            Assertions.fail("Missing exception due to unknown endpoint component");
        } catch (CitrusRuntimeException e) {
            Assertions.assertTrue(e.getMessage().startsWith("Unable to create endpoint component"));
        }
    }

    @Test
    public void testResolveInvalidEndpointUri() {
        reset(referenceResolver);
        TestContext context = new TestContext();
        context.setReferenceResolver(referenceResolver);

        DefaultEndpointFactory factory = new DefaultEndpointFactory();
        try {
            factory.create("jms:", context);
            Assertions.fail("Missing exception due to invalid endpoint uri");
        } catch (CitrusRuntimeException e) {
            Assertions.assertTrue(e.getMessage().startsWith("Invalid endpoint uri"));
        }
    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assertions.assertEquals(2L, validators.size());
        Assertions.assertNotNull(validators.get("direct"));
        Assertions.assertEquals(DirectEndpointComponent.class, validators.get("direct").getClass());
        Assertions.assertNotNull(validators.get("message-store"));
        Assertions.assertEquals(MessageStoreEndpointComponent.class, validators.get("message-store").getClass());
    }
}
