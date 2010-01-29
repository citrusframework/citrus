/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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
            Assert.assertEquals(e.getMessage(), "Message timeout validation failed! Received message while waiting for timeout on destiantion");
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
