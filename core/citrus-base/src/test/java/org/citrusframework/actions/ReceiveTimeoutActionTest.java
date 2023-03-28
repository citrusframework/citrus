/*
 * Copyright 2006-2010 the original author or authors.
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

import org.citrusframework.UnitTestSupport;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.SelectiveConsumer;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class ReceiveTimeoutActionTest extends UnitTestSupport {

    private Endpoint endpoint = Mockito.mock(Endpoint.class);
    private SelectiveConsumer consumer = Mockito.mock(SelectiveConsumer.class);
    private EndpointConfiguration endpointConfiguration = Mockito.mock(EndpointConfiguration.class);

	@Test
	public void testReceiveTimeout() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(context, 1000L)).thenReturn(null);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveTimeoutAction receiveTimeout = new ReceiveTimeoutAction.Builder()
                .endpoint(endpoint)
                .build();
		receiveTimeout.execute(context);
	}

	@Test
    public void testReceiveTimeoutCustomTimeout() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(context, 500L)).thenReturn(null);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveTimeoutAction receiveTimeout = new ReceiveTimeoutAction.Builder()
                .endpoint(endpoint)
                .timeout(500L)
                .build();
        receiveTimeout.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveTimeoutFail() {
        Message message = new DefaultMessage("<TestMessage>Hello World!</TestMessage>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(context, 1000L)).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveTimeoutAction receiveTimeout = new ReceiveTimeoutAction.Builder()
                .endpoint(endpoint)
                .build();
        try {
            receiveTimeout.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Message timeout validation failed! Received message while waiting for timeout on destination");
            return;
        }

        Assert.fail("Missing " + CitrusRuntimeException.class + " because action did receive a message");
    }

    @Test
    public void testReceiveTimeoutWithMessageSelector() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive("Operation = 'sayHello'", context, 1000L)).thenReturn(null);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveTimeoutAction receiveTimeout = new ReceiveTimeoutAction.Builder()
                .endpoint(endpoint)
                .selector("Operation = 'sayHello'")
                .build();
        receiveTimeout.execute(context);
    }

    @Test
    public void testReceiveTimeoutWithMessageSelectorVariableSupport() {
	    context.setVariable("operation", "sayHello");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive("Operation = 'sayHello'", context, 1000L)).thenReturn(null);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveTimeoutAction receiveTimeout = new ReceiveTimeoutAction.Builder()
                .endpoint(endpoint)
                .selector("Operation = '${operation}'")
                .build();
        receiveTimeout.execute(context);
    }
}
