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

package com.consol.citrus.ws.actions;

import static org.easymock.EasyMock.*;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.integration.core.Message;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageSender;
import com.consol.citrus.testng.AbstractBaseTest;
import com.consol.citrus.ws.message.CitrusSoapMessageHeaders;

/**
 * @author Christoph Deppisch
 */
public class SendSoapFaultActionTest extends AbstractBaseTest {

    private MessageSender messageSender = EasyMock.createMock(MessageSender.class);
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendSoapFault() {
        SendSoapFaultAction sendSoapFaultAction = new SendSoapFaultAction();
        sendSoapFaultAction.setMessageSender(messageSender);
        sendSoapFaultAction.setFaultCode("{http://citrusframework.org}ws:TEC-1000");
        sendSoapFaultAction.setFaultString("Internal server error");
        
        reset(messageSender);
        
        messageSender.send((Message)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Message<?> sentMessage = (Message)EasyMock.getCurrentArguments()[0];
                Assert.assertNotNull(sentMessage.getHeaders().get(CitrusSoapMessageHeaders.SOAP_FAULT));
                Assert.assertEquals(sentMessage.getHeaders().get(CitrusSoapMessageHeaders.SOAP_FAULT), "{http://citrusframework.org}ws:TEC-1000,Internal server error");
                
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        sendSoapFaultAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendSoapFaultMissingFaultString() {
        SendSoapFaultAction sendSoapFaultAction = new SendSoapFaultAction();
        sendSoapFaultAction.setMessageSender(messageSender);
        sendSoapFaultAction.setFaultCode("{http://citrusframework.org}ws:TEC-1000");
        
        reset(messageSender);
        
        messageSender.send((Message)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Message<?> sentMessage = (Message)EasyMock.getCurrentArguments()[0];
                Assert.assertNotNull(sentMessage.getHeaders().get(CitrusSoapMessageHeaders.SOAP_FAULT));
                Assert.assertEquals(sentMessage.getHeaders().get(CitrusSoapMessageHeaders.SOAP_FAULT), "{http://citrusframework.org}ws:TEC-1000");
                
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        sendSoapFaultAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendSoapFaultWithVariableSupport() {
        SendSoapFaultAction sendSoapFaultAction = new SendSoapFaultAction();
        sendSoapFaultAction.setMessageSender(messageSender);
        sendSoapFaultAction.setFaultCode("citrus:concat('{http://citrusframework.org}ws:', ${faultCode})");
        sendSoapFaultAction.setFaultString("${faultString}");
        
        context.setVariable("faultCode", "TEC-1000");
        context.setVariable("faultString", "Internal server error");
        
        reset(messageSender);
        
        messageSender.send((Message)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Message<?> sentMessage = (Message)EasyMock.getCurrentArguments()[0];
                Assert.assertNotNull(sentMessage.getHeaders().get(CitrusSoapMessageHeaders.SOAP_FAULT));
                Assert.assertEquals(sentMessage.getHeaders().get(CitrusSoapMessageHeaders.SOAP_FAULT), "{http://citrusframework.org}ws:TEC-1000,Internal server error");
                
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        sendSoapFaultAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    public void testSendSoapFaultMissingFaultCode() {
        SendSoapFaultAction sendSoapFaultAction = new SendSoapFaultAction();
        sendSoapFaultAction.setMessageSender(messageSender);
        
        reset(messageSender);
        replay(messageSender);
        
        try {
            sendSoapFaultAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getLocalizedMessage(), "Missing fault code definition for SOAP fault generation. Please specify a proper SOAP fault code!");
            verify(messageSender);
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " because of missing SOAP fault code");
    }
}
