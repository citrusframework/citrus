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

import static org.easymock.EasyMock.*;

import org.easymock.EasyMock;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Christoph Deppisch
 */
public class ReceiveTimeoutActionTest extends AbstractBaseTest {
	
    private MessageReceiver messageReceiver = EasyMock.createMock(MessageReceiver.class);
    
	@Test
	public void testReceiveTimeout() {
		ReceiveTimeoutAction receiveTimeout = new ReceiveTimeoutAction();
		receiveTimeout.setMessageReceiver(messageReceiver);
		
		reset(messageReceiver);
        expect(messageReceiver.receive(1000L)).andReturn(null).once();
        replay(messageReceiver);
        
		receiveTimeout.execute(context);
		
		verify(messageReceiver);
	}
	
	@Test
    public void testReceiveTimeoutCustomTimeout() {
        ReceiveTimeoutAction receiveTimeout = new ReceiveTimeoutAction();
        receiveTimeout.setMessageReceiver(messageReceiver);
        
        receiveTimeout.setTimeout(500L);
        
        reset(messageReceiver);
        expect(messageReceiver.receive(500L)).andReturn(null).once();
        replay(messageReceiver);
        
        receiveTimeout.execute(context);
        
        verify(messageReceiver);
    }
	
    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveTimeoutFail() {
        ReceiveTimeoutAction receiveTimeout = new ReceiveTimeoutAction();
        receiveTimeout.setMessageReceiver(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<TestMessage>Hello World!</TestMessage>").build();
        
        reset(messageReceiver);
        expect(messageReceiver.receive(1000L)).andReturn(message).once();
        replay(messageReceiver);
        
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
        receiveTimeout.setMessageReceiver(messageReceiver);
        receiveTimeout.setMessageSelector("Operation = 'sayHello'");
        
        reset(messageReceiver);
        expect(messageReceiver.receiveSelected("Operation = 'sayHello'", 1000L)).andReturn(null).once();
        replay(messageReceiver);
        
        receiveTimeout.execute(context);
        
        verify(messageReceiver);
    }
}
