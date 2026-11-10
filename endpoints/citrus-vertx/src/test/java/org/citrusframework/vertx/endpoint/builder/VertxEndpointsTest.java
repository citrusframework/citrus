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

package org.citrusframework.vertx.endpoint.builder;

import java.util.Map;

import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.vertx.endpoint.VertxEndpointBuilder;
import org.citrusframework.vertx.endpoint.VertxSyncEndpointBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VertxEndpointsTest {

    @Test
    public void shouldLookupEndpoints() {
        Map<String, EndpointBuilder<?>> endpointBuilders = EndpointBuilder.lookup();
        Assertions.assertTrue(endpointBuilders.containsKey("vertx"));
        Assertions.assertTrue(endpointBuilders.containsKey("vertx.sync"));
        Assertions.assertTrue(endpointBuilders.containsKey("vertx.async"));
    }

    @Test
    public void shouldLookupEndpoint() {
        Assertions.assertTrue(EndpointBuilder.lookup("vertx").isPresent());
        Assertions.assertEquals(VertxEndpointsBuilder.class, EndpointBuilder.lookup("vertx").get().getClass());
        Assertions.assertTrue(EndpointBuilder.lookup("vertx.sync").isPresent());
        Assertions.assertEquals(VertxSyncEndpointBuilder.class, EndpointBuilder.lookup("vertx.sync").get().getClass());
        Assertions.assertTrue(EndpointBuilder.lookup("vertx.async").isPresent());
        Assertions.assertEquals(VertxEndpointBuilder.class, EndpointBuilder.lookup("vertx.async").get().getClass());
    }
}
