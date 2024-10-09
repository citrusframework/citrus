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
import org.testng.Assert;
import org.testng.annotations.Test;

public class WebServiceEndpointsTest {

    @Test
    public void shouldLookupEndpoints() {
        Map<String, EndpointBuilder<?>> endpointBuilders = EndpointBuilder.lookup();
        Assert.assertTrue(endpointBuilders.containsKey("soap.client"));
        Assert.assertTrue(endpointBuilders.containsKey("soap.server"));
    }

    @Test
    public void shouldLookupEndpoint() {
        Assert.assertTrue(EndpointBuilder.lookup("soap.client").isPresent());
        Assert.assertEquals(EndpointBuilder.lookup("soap.client").get().getClass(), WebServiceClientBuilder.class);
        Assert.assertTrue(EndpointBuilder.lookup("soap.server").isPresent());
        Assert.assertEquals(EndpointBuilder.lookup("soap.server").get().getClass(), WebServiceServerBuilder.class);
    }
}
