/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.actions;

import java.util.Collections;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.spi.SimpleReferenceResolver;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class PurgeEndpointActionTest extends UnitTestSupport {

    private Endpoint mockEndpoint = Mockito.mock(Endpoint.class);
    private Endpoint emptyEndpoint = Mockito.mock(Endpoint.class);

    private Consumer consumer = Mockito.mock(Consumer.class);
    private SelectiveConsumer selectiveConsumer = Mockito.mock(SelectiveConsumer.class);

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory factory = super.createTestContextFactory();
        factory.getReferenceResolver().bind("mockEndpoint", mockEndpoint);
        return factory;
    }

    @Test
    public void testPurgeWithEndpointNames() {
        reset(mockEndpoint, consumer, selectiveConsumer);

        when(mockEndpoint.getName()).thenReturn("mockEndpoint");
        when(mockEndpoint.createConsumer()).thenReturn(consumer);
        when(consumer.receive(context, 100L)).thenReturn(new DefaultMessage());
        doThrow(new ActionTimeoutException()).when(consumer).receive(context, 100L);

        PurgeEndpointAction purgeEndpointAction = new PurgeEndpointAction.Builder()
                .referenceResolver(context.getReferenceResolver())
                .endpointNames("mockEndpoint")
                .build();
        purgeEndpointAction.execute(context);
    }

	@SuppressWarnings("unchecked")
    @Test
    public void testPurgeWithEndpointObjects() {
        reset(mockEndpoint, emptyEndpoint, consumer, selectiveConsumer);

        when(mockEndpoint.getName()).thenReturn("mockEndpoint");
        when(emptyEndpoint.getName()).thenReturn("emptyEndpoint");
        when(mockEndpoint.createConsumer()).thenReturn(consumer);
        when(emptyEndpoint.createConsumer()).thenReturn(consumer);

        when(consumer.receive(context, 100L)).thenReturn(new DefaultMessage());
        doThrow(new ActionTimeoutException()).when(consumer).receive(context, 100L);
        doThrow(new ActionTimeoutException()).when(consumer).receive(context, 100L);

        PurgeEndpointAction purgeEndpointAction = new PurgeEndpointAction.Builder()
                .referenceResolver(context.getReferenceResolver())
                .endpoints(mockEndpoint, emptyEndpoint)
                .build();
        purgeEndpointAction.execute(context);
    }

	@Test
    public void testPurgeWithMessageSelector() throws Exception {
        reset(mockEndpoint, consumer, selectiveConsumer);

        when(mockEndpoint.getName()).thenReturn("mockEndpoint");
        when(mockEndpoint.createConsumer()).thenReturn(selectiveConsumer);
        when(selectiveConsumer.receive("operation = 'sayHello'", context, 100L)).thenReturn(new DefaultMessage());
        doThrow(new ActionTimeoutException()).when(selectiveConsumer).receive("operation = 'sayHello'", context, 100L);

        PurgeEndpointAction purgeEndpointAction = new PurgeEndpointAction.Builder()
                .referenceResolver(context.getReferenceResolver())
                .endpoints(mockEndpoint)
                .selector("operation = 'sayHello'")
                .build();
        purgeEndpointAction.execute(context);
    }

    @Test
    public void testPurgeWithMessageSelectorMap() throws Exception {
        reset(mockEndpoint, consumer, selectiveConsumer);

        when(mockEndpoint.getName()).thenReturn("mockEndpoint");
        when(mockEndpoint.createConsumer()).thenReturn(selectiveConsumer);
        when(selectiveConsumer.receive("operation = 'sayHello'", context, 100L)).thenReturn(new DefaultMessage());
        doThrow(new ActionTimeoutException()).when(selectiveConsumer).receive("operation = 'sayHello'", context, 100L);

        PurgeEndpointAction purgeEndpointAction = new PurgeEndpointAction.Builder()
                .referenceResolver(context.getReferenceResolver())
                .endpoints(mockEndpoint)
                .selector(Collections.singletonMap("operation", "sayHello"))
                .build();
        purgeEndpointAction.execute(context);
    }

}
