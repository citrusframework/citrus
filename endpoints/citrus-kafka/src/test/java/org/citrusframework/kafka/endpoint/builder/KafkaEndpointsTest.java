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

package org.citrusframework.kafka.endpoint.builder;

import java.util.Map;

import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.kafka.endpoint.KafkaEndpointBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class KafkaEndpointsTest {

    @Test
    public void shouldLookupEndpoints() {
        Map<String, EndpointBuilder<?>> endpointBuilders = EndpointBuilder.lookup();
        Assertions.assertTrue(endpointBuilders.containsKey("kafka"));
        Assertions.assertTrue(endpointBuilders.containsKey("kafka.sync"));
        Assertions.assertTrue(endpointBuilders.containsKey("kafka.async"));
    }

    @Test
    public void shouldLookupEndpoint() {
        Assertions.assertTrue(EndpointBuilder.lookup("kafka").isPresent());
        Assertions.assertEquals(KafkaEndpointsBuilder.class, EndpointBuilder.lookup("kafka").get().getClass());
        Assertions.assertTrue(EndpointBuilder.lookup("kafka.sync").isPresent());
        Assertions.assertEquals(KafkaEndpointBuilder.class, EndpointBuilder.lookup("kafka.sync").get().getClass());
        Assertions.assertTrue(EndpointBuilder.lookup("kafka.async").isPresent());
        Assertions.assertEquals(KafkaEndpointBuilder.class, EndpointBuilder.lookup("kafka.async").get().getClass());
    }
}
