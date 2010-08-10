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
