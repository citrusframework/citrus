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

package org.citrusframework.kubernetes.endpoint.builder;

import java.util.Map;

import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.kubernetes.client.KubernetesClientBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class KubernetesEndpointsTest {

    @Test
    public void shouldLookupEndpoints() {
        Map<String, EndpointBuilder<?>> endpointBuilders = EndpointBuilder.lookup();
        Assertions.assertTrue(endpointBuilders.containsKey("kubernetes"));
        Assertions.assertTrue(endpointBuilders.containsKey("k8s"));
        Assertions.assertTrue(endpointBuilders.containsKey("kubernetes.client"));
        Assertions.assertTrue(endpointBuilders.containsKey("k8s.client"));
    }

    @Test
    public void shouldLookupEndpoint() {
        Assertions.assertTrue(EndpointBuilder.lookup("kubernetes").isPresent());
        Assertions.assertEquals(KubernetesEndpointBuilder.class, EndpointBuilder.lookup("kubernetes").get().getClass());
        Assertions.assertTrue(EndpointBuilder.lookup("k8s").isPresent());
        Assertions.assertEquals(KubernetesEndpointBuilder.class, EndpointBuilder.lookup("k8s").get().getClass());
        Assertions.assertTrue(EndpointBuilder.lookup("kubernetes.client").isPresent());
        Assertions.assertEquals(KubernetesClientBuilder.class, EndpointBuilder.lookup("kubernetes.client").get().getClass());
        Assertions.assertTrue(EndpointBuilder.lookup("k8s.client").isPresent());
        Assertions.assertEquals(KubernetesClientBuilder.class, EndpointBuilder.lookup("k8s.client").get().getClass());
    }
}
