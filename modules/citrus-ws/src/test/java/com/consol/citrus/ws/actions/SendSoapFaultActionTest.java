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
import org.springframework.integration.Message;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageSender;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.ws.message.CitrusSoapMessageHeaders;
import com.consol.citrus.ws.message.builder.SoapFaultAwareMessageBuilder;

/**
 * @author Christoph Deppisch
 */
public class SendSoapFaultActionTest extends AbstractTestNGUnitTest {

    private MessageSender messageSender = EasyMock.createMock(MessageSender.class);
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSendSoapFault() {
        SendMessageAction sendSoapFaultAction = new SendMessageAction();
        sendSoapFaultAction.setMessageSender(messageSender);
        
        SoapFaultAwareMessageBuilder messageBuilder = new SoapFaultAwareMessageBuilder();
        messageBuilder.setFaultCode("{http://citrusframework.org}ws:TEC-1000");
        messageBuilder.setFaultString("Internal server error");
        
        sendSoapFaultAction.setMessageBuilder(messageBuilder);
        
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
        
        expect(messageSender.getActor()).andReturn(null).anyTimes();
        
        replay(messageSender);
        
        sendSoapFaultAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSendSoapFaultMissingFaultString() {
        SendMessageAction sendSoapFaultAction = new SendMessageAction();
        sendSoapFaultAction.setMessageSender(messageSender);
        
        SoapFaultAwareMessageBuilder messageBuilder = new SoapFaultAwareMessageBuilder();
        messageBuilder.setFaultCode("{http://citrusframework.org}ws:TEC-1000");
        
        sendSoapFaultAction.setMessageBuilder(messageBuilder);
        
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
        
        expect(messageSender.getActor()).andReturn(null).anyTimes();
        
        replay(messageSender);
        
        sendSoapFaultAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSendSoapFaultWithVariableSupport() {
        SendMessageAction sendSoapFaultAction = new SendMessageAction();
        sendSoapFaultAction.setMessageSender(messageSender);
        
        SoapFaultAwareMessageBuilder messageBuilder = new SoapFaultAwareMessageBuilder();
        messageBuilder.setFaultCode("citrus:concat('{http://citrusframework.org}ws:', ${faultCode})");
        messageBuilder.setFaultString("${faultString}");
        
        sendSoapFaultAction.setMessageBuilder(messageBuilder);
        
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
        
        expect(messageSender.getActor()).andReturn(null).anyTimes();
        
        replay(messageSender);
        
        sendSoapFaultAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    public void testSendSoapFaultMissingFaultCode() {
        SendMessageAction sendSoapFaultAction = new SendMessageAction();
        sendSoapFaultAction.setMessageSender(messageSender);
        
        SoapFaultAwareMessageBuilder messageBuilder = new SoapFaultAwareMessageBuilder();
        sendSoapFaultAction.setMessageBuilder(messageBuilder);
        
        reset(messageSender);
        expect(messageSender.getActor()).andReturn(null).anyTimes();
        
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
