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

package org.citrusframework.base.endpoint.adapter;

import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StaticEndpointAdapterTest extends UnitTestSupport {

    @Test
    public void testEndpointAdapter() {
        StaticEndpointAdapter endpointAdapter = new StaticEndpointAdapter(new DefaultMessage("just works!"));

        Message received = endpointAdapter.handleMessage(new DefaultMessage("hello"));
        Assertions.assertEquals("just works!", received.getPayload());

        received = endpointAdapter.getEndpoint().createConsumer().receive(context);
        Assertions.assertEquals("just works!", received.getPayload());

        Message received2nd = endpointAdapter.handleMessage(new DefaultMessage("something else"));
        Assertions.assertEquals("just works!", received2nd.getPayload());

        Assertions.assertEquals(received2nd.getId(), received.getId());
    }

    @Test
    public void testEndpointAdapterNewMessageInstances() {
        StaticEndpointAdapter endpointAdapter = new StaticEndpointAdapter(new DefaultMessage("just works!"))
                .withReuseMessage(false);

        Message received = endpointAdapter.handleMessage(new DefaultMessage("hello"));
        Assertions.assertEquals("just works!", received.getPayload());

        Message received2nd = endpointAdapter.handleMessage(new DefaultMessage("something else"));
        Assertions.assertEquals("just works!", received2nd.getPayload());

        Assertions.assertNotEquals(received2nd.getId(), received.getId());
    }

    @Test
    public void testEmptyMessageEndpointAdapter() {
        Message request = new DefaultMessage("hello");
        StaticEndpointAdapter endpointAdapter = new StaticEndpointAdapter();

        Message received = endpointAdapter.handleMessage(request);
        Assertions.assertEquals("", received.getPayload());

        received = endpointAdapter.getEndpoint().createConsumer().receive(context);
        Assertions.assertEquals("", received.getPayload());

        endpointAdapter.getEndpoint().createProducer().send(request, context);
    }
}
