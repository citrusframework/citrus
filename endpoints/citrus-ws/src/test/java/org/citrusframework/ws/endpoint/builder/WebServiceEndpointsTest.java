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

package org.citrusframework.ws.endpoint.builder;

import java.util.Map;

import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.ws.client.WebServiceClientBuilder;
import org.citrusframework.ws.server.WebServiceServerBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WebServiceEndpointsTest {

    @Test
    public void shouldLookupEndpoints() {
        Map<String, EndpointBuilder<?>> endpointBuilders = EndpointBuilder.lookup();
        Assertions.assertTrue(endpointBuilders.containsKey("soap"));
        Assertions.assertTrue(endpointBuilders.containsKey("soap.client"));
        Assertions.assertTrue(endpointBuilders.containsKey("soap.server"));
    }

    @Test
    public void shouldLookupEndpoint() {
        Assertions.assertTrue(EndpointBuilder.lookup("soap").isPresent());
        Assertions.assertEquals(SoapWebServiceEndpointBuilder.class, EndpointBuilder.lookup("soap").get().getClass());
        Assertions.assertTrue(EndpointBuilder.lookup("soap.client").isPresent());
        Assertions.assertEquals(WebServiceClientBuilder.class, EndpointBuilder.lookup("soap.client").get().getClass());
        Assertions.assertTrue(EndpointBuilder.lookup("soap.server").isPresent());
        Assertions.assertEquals(WebServiceServerBuilder.class, EndpointBuilder.lookup("soap.server").get().getClass());
    }
}
