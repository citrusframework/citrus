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

package org.citrusframework.camel.actions;

import org.citrusframework.camel.dsl.EndpointBuilderFactorySupport;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class CamelExchangeActionBuilderTest {

    private final EndpointBuilderFactorySupport endpoints = new EndpointBuilderFactorySupport();

    @Test
    public void shouldSetEndpointFromProducerBuilder() {
        var builder = CamelExchangeActionBuilder.send();

        builder.endpoint(endpoints.seda("test"));

        assertEquals(builder.getDelegate().getEndpointUri(), "camel:seda://test");
    }

    @Test
    public void shouldSetSyncEndpointFromProducerBuilder() {
        var builder = CamelExchangeActionBuilder.send();

        builder.endpoint(endpoints.seda("test"), true);

        assertEquals(builder.getDelegate().getEndpointUri(), "camel:sync:seda://test");
    }

    @Test
    public void shouldSetAsyncEndpointFromProducerBuilder() {
        var builder = CamelExchangeActionBuilder.send();

        builder.endpoint(endpoints.seda("test"), false);

        assertEquals(builder.getDelegate().getEndpointUri(), "camel:seda://test");
    }

    @Test
    public void shouldSetEndpointFromProducerBuilderWithOptions() {
        var builder = CamelExchangeActionBuilder.send();

        builder.endpoint(
                endpoints.seda("test").size(100),
                true);

        assertEquals(builder.getDelegate().getEndpointUri(),
                "camel:sync:seda://test?size=100");
    }

    @Test
    public void shouldSetReceiveEndpointFromProducerBuilder() {
        var builder = CamelExchangeActionBuilder.receive();

        builder.endpoint(endpoints.seda("incoming"), true);

        assertEquals(builder.getDelegate().getEndpointUri(), "camel:sync:seda://incoming");
    }
}
