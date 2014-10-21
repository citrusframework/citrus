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

package com.consol.citrus.actions;

import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.messaging.SelectiveConsumer;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class ReceiveTimeoutActionTest extends AbstractTestNGUnitTest {

    private Endpoint endpoint = EasyMock.createMock(Endpoint.class);
    private SelectiveConsumer consumer = EasyMock.createMock(SelectiveConsumer.class);
    private EndpointConfiguration endpointConfiguration = EasyMock.createMock(EndpointConfiguration.class);

	@Test
	public void testReceiveTimeout() {
		ReceiveTimeoutAction receiveTimeout = new ReceiveTimeoutAction();
		receiveTimeout.setEndpoint(endpoint);

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(context, 1000L)).andReturn(null).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
		receiveTimeout.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
	}
	
	@Test
    public void testReceiveTimeoutCustomTimeout() {
        ReceiveTimeoutAction receiveTimeout = new ReceiveTimeoutAction();
        receiveTimeout.setEndpoint(endpoint);
        
        receiveTimeout.setTimeout(500L);

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(context, 500L)).andReturn(null).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        receiveTimeout.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
	
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveTimeoutFail() {
        ReceiveTimeoutAction receiveTimeout = new ReceiveTimeoutAction();
        receiveTimeout.setEndpoint(endpoint);
        
        Message message = new DefaultMessage("<TestMessage>Hello World!</TestMessage>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(context, 1000L)).andReturn(message).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        try {
            receiveTimeout.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Message timeout validation failed! Received message while waiting for timeout on destination");
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " because action did receive a message");
    }
    
    @Test
    public void testReceiveTimeoutWithMessageSelectorString() {
        ReceiveTimeoutAction receiveTimeout = new ReceiveTimeoutAction();
        receiveTimeout.setEndpoint(endpoint);
        receiveTimeout.setMessageSelector("Operation = 'sayHello'");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive("Operation = 'sayHello'", context, 1000L)).andReturn(null).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        receiveTimeout.execute(context);
        
        verify(endpoint, consumer, endpointConfiguration);
    }
}
