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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.*;

import org.easymock.IAnswer;
import org.easymock.classextension.EasyMock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.core.Message;
import org.springframework.ws.mime.Attachment;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractBaseTest;
import com.consol.citrus.ws.SoapAttachment;
import com.consol.citrus.ws.message.WebServiceMessageSender;

/**
 * @author Christoph Deppisch
 */
public class SendSoapMessageActionTest extends AbstractBaseTest {
    
    private WebServiceMessageSender messageSender = EasyMock.createMock(WebServiceMessageSender.class);
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSoapMessageWithAttachmentDataTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setMessageSender(messageSender);
        soapMessageAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        soapMessageAction.setAttachmentData("<TestAttachment><Message>Hello World!</Message></TestAttachment>");

        reset(messageSender);
        
        messageSender.send((Message)anyObject(), (Attachment)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment constructedAttachment = (SoapAttachment)EasyMock.getCurrentArguments()[1];
                Assert.assertNull(constructedAttachment.getContentId());
                Assert.assertEquals(constructedAttachment.getContentType(), "text/plain");
                Assert.assertEquals(constructedAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-8");
                
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        soapMessageAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSoapMessageWithAttachmentData2Test() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setMessageSender(messageSender);
        soapMessageAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        soapMessageAction.setContentId("myAttachment");
        soapMessageAction.setContentType("text/xml");
        soapMessageAction.setAttachmentData("<TestAttachment><Message>Hello World!</Message></TestAttachment>");
        soapMessageAction.setCharsetName("UTF-16");

        reset(messageSender);
        
        messageSender.send((Message)anyObject(), (Attachment)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment constructedAttachment = (SoapAttachment)EasyMock.getCurrentArguments()[1];
                Assert.assertEquals(constructedAttachment.getContentId(), "myAttachment");
                Assert.assertEquals(constructedAttachment.getContentType(), "text/xml");
                Assert.assertEquals(constructedAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-16");
                
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        soapMessageAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSoapMessageWithEmptyAttachmentContentTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setMessageSender(messageSender);
        soapMessageAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        reset(messageSender);
        
        messageSender.send((Message)anyObject());
        expectLastCall().once();
        
        replay(messageSender);
        
        soapMessageAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSoapMessageWithAttachmentResourceTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setMessageSender(messageSender);
        soapMessageAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        soapMessageAction.setAttachmentResource(new ClassPathResource("test-attachment.xml", SendSoapMessageActionTest.class));

        reset(messageSender);
        
        messageSender.send((Message)anyObject(), (Attachment)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment constructedAttachment = (SoapAttachment)EasyMock.getCurrentArguments()[1];
                Assert.assertNull(constructedAttachment.getContentId());
                Assert.assertEquals(constructedAttachment.getContentType(), "text/plain");
                Assert.assertEquals(constructedAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-8");
                
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        soapMessageAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSoapMessageWithAttachmentDataVariableSupportTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setMessageSender(messageSender);
        soapMessageAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        context.setVariable("myText", "Hello World!");
        
        soapMessageAction.setAttachmentData("<TestAttachment><Message>${myText}</Message></TestAttachment>");

        reset(messageSender);
        
        messageSender.send((Message)anyObject(), (Attachment)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment constructedAttachment = (SoapAttachment)EasyMock.getCurrentArguments()[1];
                Assert.assertNull(constructedAttachment.getContentId());
                Assert.assertEquals(constructedAttachment.getContentType(), "text/plain");
                Assert.assertEquals(constructedAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-8");
                
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        soapMessageAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSoapMessageWithAttachmentResourceVariablesSupportTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setMessageSender(messageSender);
        soapMessageAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        context.setVariable("myText", "Hello World!");
        
        soapMessageAction.setAttachmentResource(new ClassPathResource("test-attachment-with-variables.xml", SendSoapMessageActionTest.class));

        reset(messageSender);
        
        messageSender.send((Message)anyObject(), (Attachment)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment constructedAttachment = (SoapAttachment)EasyMock.getCurrentArguments()[1];
                Assert.assertNull(constructedAttachment.getContentId());
                Assert.assertEquals(constructedAttachment.getContentType(), "text/plain");
                Assert.assertEquals(constructedAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-8");
                
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        soapMessageAction.execute(context);
        
        verify(messageSender);
    }
}
