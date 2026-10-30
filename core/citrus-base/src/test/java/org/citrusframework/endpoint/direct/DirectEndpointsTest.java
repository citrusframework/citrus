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

import java.util.Map;

import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DirectEndpointsTest {

    @Test
    public void shouldLookupEndpoints() {
        Map<String, EndpointBuilder<?>> endpointBuilders = EndpointBuilder.lookup();
        Assertions.assertTrue(endpointBuilders.containsKey("direct"));
        Assertions.assertTrue(endpointBuilders.containsKey("direct.sync"));
        Assertions.assertTrue(endpointBuilders.containsKey("direct.async"));
    }

    @Test
    public void shouldLookupEndpoint() {
        Assertions.assertTrue(EndpointBuilder.lookup("direct").isPresent());
        Assertions.assertEquals(DirectEndpointsBuilder.class, EndpointBuilder.lookup("direct").get().getClass());
        Assertions.assertTrue(EndpointBuilder.lookup("direct.sync").isPresent());
        Assertions.assertEquals(DirectSyncEndpointBuilder.class, EndpointBuilder.lookup("direct.sync").get().getClass());
        Assertions.assertTrue(EndpointBuilder.lookup("direct.async").isPresent());
        Assertions.assertEquals(DirectEndpointBuilder.class, EndpointBuilder.lookup("direct.async").get().getClass());
    }

    @Test
    public void shouldCreateAsyncEndpointFromDirectProperties() {
        DirectEndpointsBuilder builder = new DirectEndpointsBuilder();
        builder.setName("myEndpoint");
        builder.setQueue("myQueue");
        builder.setTimeout(5000);

        Endpoint endpoint = builder.build();
        Assertions.assertInstanceOf(DirectEndpoint.class, endpoint);
        Assertions.assertEquals("myEndpoint", endpoint.getName());

        DirectEndpoint directEndpoint = (DirectEndpoint) endpoint;
        Assertions.assertEquals("myQueue", directEndpoint.getEndpointConfiguration().getQueueName());
        Assertions.assertEquals(5000L, directEndpoint.getEndpointConfiguration().getTimeout());
    }

    @Test
    public void shouldCreateAsyncEndpointWithExplicitSubElement() {
        DirectEndpointsBuilder builder = new DirectEndpointsBuilder();

        DirectEndpointBuilder asyncBuilder = new DirectEndpointBuilder();
        asyncBuilder.setName("explicitAsync");
        asyncBuilder.setQueue("asyncQueue");
        builder.setAsynchronous(asyncBuilder);

        Endpoint endpoint = builder.build();
        Assertions.assertInstanceOf(DirectEndpoint.class, endpoint);
        Assertions.assertEquals("explicitAsync", endpoint.getName());
    }

    @Test
    public void shouldCreateSyncEndpointWithExplicitSubElement() {
        DirectEndpointsBuilder builder = new DirectEndpointsBuilder();

        DirectSyncEndpointBuilder syncBuilder = new DirectSyncEndpointBuilder();
        syncBuilder.setName("explicitSync");
        syncBuilder.setQueue("syncQueue");
        builder.setSynchronous(syncBuilder);

        Endpoint endpoint = builder.build();
        Assertions.assertInstanceOf(DirectSyncEndpoint.class, endpoint);
        Assertions.assertEquals("explicitSync", endpoint.getName());
    }

    @Test
    public void shouldSetPropertiesOnSyncDelegate() {
        DirectEndpointsBuilder builder = new DirectEndpointsBuilder();
        builder.setSynchronous(new DirectSyncEndpointBuilder());
        builder.setName("syncEndpoint");
        builder.setQueue("syncQueue");
        builder.setTimeout(3000);

        Endpoint endpoint = builder.build();
        Assertions.assertInstanceOf(DirectSyncEndpoint.class, endpoint);
        Assertions.assertEquals("syncEndpoint", endpoint.getName());

        DirectSyncEndpoint syncEndpoint = (DirectSyncEndpoint) endpoint;
        Assertions.assertEquals("syncQueue", syncEndpoint.getEndpointConfiguration().getQueueName());
        Assertions.assertEquals(3000L, syncEndpoint.getEndpointConfiguration().getTimeout());
    }

    @Test
    public void shouldPreserveTopLevelPropertiesWhenSynchronousReplacesDelegate() {
        DirectEndpointsBuilder builder = new DirectEndpointsBuilder();
        // Simulates YAML processing order: top-level properties set first, then synchronous replaces delegate
        builder.setName("myEndpoint");
        builder.setQueue("myQueue");
        builder.setTimeout(5000);

        DirectSyncEndpointBuilder syncBuilder = new DirectSyncEndpointBuilder();
        syncBuilder.setPollingInterval(2000);
        builder.setSynchronous(syncBuilder);

        Endpoint endpoint = builder.build();
        Assertions.assertInstanceOf(DirectSyncEndpoint.class, endpoint);
        Assertions.assertEquals("myEndpoint", endpoint.getName());

        DirectSyncEndpoint syncEndpoint = (DirectSyncEndpoint) endpoint;
        Assertions.assertEquals("myQueue", syncEndpoint.getEndpointConfiguration().getQueueName());
        Assertions.assertEquals(5000L, syncEndpoint.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(2000, syncEndpoint.getEndpointConfiguration().getPollingInterval());
    }

    @Test
    public void shouldCreateDefaultAsyncEndpointWhenNoDelegateConfigured() {
        DirectEndpointsBuilder builder = new DirectEndpointsBuilder();
        Endpoint endpoint = builder.build();
        Assertions.assertInstanceOf(DirectEndpoint.class, endpoint);
    }
}
