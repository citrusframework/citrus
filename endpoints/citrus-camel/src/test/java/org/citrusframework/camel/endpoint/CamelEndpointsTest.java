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

package org.citrusframework.camel.endpoint;

import java.util.Map;

import org.citrusframework.endpoint.EndpointBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CamelEndpointsTest {

    @Test
    public void shouldLookupEndpoints() {
        Map<String, EndpointBuilder<?>> endpointBuilders = EndpointBuilder.lookup();
        Assertions.assertTrue(endpointBuilders.containsKey("camel"));
        Assertions.assertTrue(endpointBuilders.containsKey("camel.sync"));
        Assertions.assertTrue(endpointBuilders.containsKey("camel.inOnly"));
        Assertions.assertTrue(endpointBuilders.containsKey("camel.async"));
        Assertions.assertTrue(endpointBuilders.containsKey("camel.inOut"));
    }

    @Test
    public void shouldLookupEndpoint() {
        Assertions.assertTrue(EndpointBuilder.lookup("camel").isPresent());
        Assertions.assertEquals(CamelEndpointsBuilder.class, EndpointBuilder.lookup("camel").get().getClass());
        Assertions.assertTrue(EndpointBuilder.lookup("camel.sync").isPresent());
        Assertions.assertEquals(CamelSyncEndpointBuilder.class, EndpointBuilder.lookup("camel.sync").get().getClass());
        Assertions.assertTrue(EndpointBuilder.lookup("camel.inOut").isPresent());
        Assertions.assertEquals(CamelSyncEndpointBuilder.class, EndpointBuilder.lookup("camel.inOut").get().getClass());
        Assertions.assertTrue(EndpointBuilder.lookup("camel.async").isPresent());
        Assertions.assertEquals(CamelEndpointBuilder.class, EndpointBuilder.lookup("camel.async").get().getClass());
        Assertions.assertTrue(EndpointBuilder.lookup("camel.inOnly").isPresent());
        Assertions.assertEquals(CamelEndpointBuilder.class, EndpointBuilder.lookup("camel.inOnly").get().getClass());
    }
}
