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

package org.citrusframework.endpoint.adapter;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.testng.Assert;
import org.testng.annotations.Test;

public class StaticEndpointAdapterTest extends UnitTestSupport {

    @Test
    public void testEndpointAdapter() {
        StaticEndpointAdapter endpointAdapter = new StaticEndpointAdapter(new DefaultMessage("just works!"));

        Message received = endpointAdapter.handleMessage(new DefaultMessage("hello"));
        Assert.assertEquals(received.getPayload(), "just works!");

        received = endpointAdapter.getEndpoint().createConsumer().receive(context);
        Assert.assertEquals(received.getPayload(), "just works!");

        Message received2nd = endpointAdapter.handleMessage(new DefaultMessage("something else"));
        Assert.assertEquals(received2nd.getPayload(), "just works!");

        Assert.assertEquals(received.getId(), received2nd.getId());
    }

    @Test
    public void testEndpointAdapterNewMessageInstances() {
        StaticEndpointAdapter endpointAdapter = new StaticEndpointAdapter(new DefaultMessage("just works!"))
                .withReuseMessage(false);

        Message received = endpointAdapter.handleMessage(new DefaultMessage("hello"));
        Assert.assertEquals(received.getPayload(), "just works!");

        Message received2nd = endpointAdapter.handleMessage(new DefaultMessage("something else"));
        Assert.assertEquals(received2nd.getPayload(), "just works!");

        Assert.assertNotEquals(received.getId(), received2nd.getId());
    }

    @Test
    public void testEmptyMessageEndpointAdapter() {
        Message request = new DefaultMessage("hello");
        StaticEndpointAdapter endpointAdapter = new StaticEndpointAdapter();

        Message received = endpointAdapter.handleMessage(request);
        Assert.assertEquals(received.getPayload(), "");

        received = endpointAdapter.getEndpoint().createConsumer().receive(context);
        Assert.assertEquals(received.getPayload(), "");

        endpointAdapter.getEndpoint().createProducer().send(request, context);
    }
}
