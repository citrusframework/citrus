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

package org.citrusframework.http.endpoint.builder;

import java.util.Map;

import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.server.HttpServerBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HttpEndpointsTest {

    @Test
    public void shouldLookupEndpoints() {
        Map<String, EndpointBuilder<?>> endpointBuilders = EndpointBuilder.lookup();
        Assertions.assertTrue(endpointBuilders.containsKey("http"));
        Assertions.assertTrue(endpointBuilders.containsKey("http.client"));
        Assertions.assertTrue(endpointBuilders.containsKey("http.server"));
    }

    @Test
    public void shouldLookupEndpoint() {
        Assertions.assertTrue(EndpointBuilder.lookup("http").isPresent());
        Assertions.assertEquals(HttpEndpointBuilder.class, EndpointBuilder.lookup("http").get().getClass());
        Assertions.assertTrue(EndpointBuilder.lookup("http.client").isPresent());
        Assertions.assertEquals(HttpClientBuilder.class, EndpointBuilder.lookup("http.client").get().getClass());
        Assertions.assertTrue(EndpointBuilder.lookup("http.server").isPresent());
        Assertions.assertEquals(HttpServerBuilder.class, EndpointBuilder.lookup("http.server").get().getClass());
    }
}
