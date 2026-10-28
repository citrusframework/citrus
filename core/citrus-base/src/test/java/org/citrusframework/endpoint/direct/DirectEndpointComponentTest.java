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

package org.citrusframework.endpoint.direct;

import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DirectEndpointComponentTest {

    private TestContext context;

    @BeforeEach
    public void setupMocks() {
        context = TestContextFactory.newInstance().getObject();
    }

    @Test
    public void testCreateDirectEndpointWithQueueName() {
        DirectEndpointComponent component = new DirectEndpointComponent();

        Assertions.assertFalse(context.getReferenceResolver().isResolvable("queueName"));
        Endpoint endpoint = component.createEndpoint("direct?queueName=queueName", context);

        Assertions.assertEquals(DirectEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("queueName", ((DirectEndpoint)endpoint).getEndpointConfiguration().getQueueName());
        Assertions.assertEquals(5000L, ((DirectEndpoint) endpoint).getEndpointConfiguration().getTimeout());
        Assertions.assertTrue(context.getReferenceResolver().isResolvable("queueName"));
    }

    @Test
    public void testCreateDirectEndpoint() {
        DirectEndpointComponent component = new DirectEndpointComponent();

        Assertions.assertFalse(context.getReferenceResolver().isResolvable("queueName"));
        Endpoint endpoint = component.createEndpoint("direct:queueName", context);

        Assertions.assertEquals(DirectEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("queueName", ((DirectEndpoint)endpoint).getEndpointConfiguration().getQueueName());
        Assertions.assertEquals(5000L, ((DirectEndpoint) endpoint).getEndpointConfiguration().getTimeout());
        Assertions.assertTrue(context.getReferenceResolver().isResolvable("queueName"));
    }

    @Test
    public void testCreateSyncDirectEndpoint() {
        DirectEndpointComponent component = new DirectEndpointComponent();

        Assertions.assertFalse(context.getReferenceResolver().isResolvable("queueName"));
        Endpoint endpoint = component.createEndpoint("direct:sync:queueName", context);

        Assertions.assertEquals(DirectSyncEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("queueName", ((DirectSyncEndpoint)endpoint).getEndpointConfiguration().getQueueName());
        Assertions.assertTrue(context.getReferenceResolver().isResolvable("queueName"));
    }

    @Test
    public void testCreateDirectEndpointWithParameters() {
        DirectEndpointComponent component = new DirectEndpointComponent();

        Endpoint endpoint = component.createEndpoint("direct:queueName?timeout=10000", context);

        Assertions.assertEquals(DirectEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("queueName", ((DirectEndpoint)endpoint).getEndpointConfiguration().getQueueName());
        Assertions.assertEquals(10000L, ((DirectEndpoint) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(EndpointComponent.lookup("direct").isPresent());
    }
}
