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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;

import org.easymock.IAnswer;
import org.easymock.classextension.EasyMock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.core.Message;
import org.springframework.ws.mime.Attachment;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.jms.JmsMessageSender;
import com.consol.citrus.message.CitrusMessageHeaders;
import com.consol.citrus.message.MessageSender;
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
    public void testSoapMessageWithDefaultAttachmentDataTest() throws Exception {
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
    public void testSoapMessageWithAttachmentDataTest() throws Exception {
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
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSoapMessageWithHeaderContentTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setMessageSender(messageSender);
        soapMessageAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        soapMessageAction.setHeaderData("<TestHeader><operation>soapOperation</operation></TestHeader>");

        reset(messageSender);
        
        messageSender.send((Message)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Message constructedMessage = (Message)EasyMock.getCurrentArguments()[0];

                Assert.assertNotNull(constructedMessage.getHeaders().get(CitrusMessageHeaders.HEADER_CONTENT));
                Assert.assertEquals(constructedMessage.getHeaders().get(CitrusMessageHeaders.HEADER_CONTENT), 
                        "<TestHeader><operation>soapOperation</operation></TestHeader>");
                
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        soapMessageAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSoapMessageWithHeaderResourceTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setMessageSender(messageSender);
        soapMessageAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        soapMessageAction.setHeaderResource(new ClassPathResource("test-header-resource.xml", SendSoapMessageActionTest.class));

        reset(messageSender);
        
        messageSender.send((Message)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Message constructedMessage = (Message)EasyMock.getCurrentArguments()[0];

                Assert.assertNotNull(constructedMessage.getHeaders().get(CitrusMessageHeaders.HEADER_CONTENT));
                Assert.assertEquals(constructedMessage.getHeaders().get(CitrusMessageHeaders.HEADER_CONTENT), 
                        "<TestHeader><operation>soapOperation</operation></TestHeader>");
                
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        soapMessageAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSoapMessageWithHeaderContentVariableSupportTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setMessageSender(messageSender);
        soapMessageAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        context.setVariable("operation", "soapOperation");
        soapMessageAction.setHeaderData("<TestHeader><operation>${operation}</operation></TestHeader>");

        reset(messageSender);
        
        messageSender.send((Message)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Message constructedMessage = (Message)EasyMock.getCurrentArguments()[0];

                Assert.assertNotNull(constructedMessage.getHeaders().get(CitrusMessageHeaders.HEADER_CONTENT));
                Assert.assertEquals(constructedMessage.getHeaders().get(CitrusMessageHeaders.HEADER_CONTENT), 
                        "<TestHeader><operation>soapOperation</operation></TestHeader>");
                
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        soapMessageAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSoapMessageWithHeaderResourceVariableSupportTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setMessageSender(messageSender);
        soapMessageAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        context.setVariable("operation", "soapOperation");
        soapMessageAction.setHeaderResource(new ClassPathResource("test-header-resource-with-variables.xml", SendSoapMessageActionTest.class));

        reset(messageSender);
        
        messageSender.send((Message)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Message constructedMessage = (Message)EasyMock.getCurrentArguments()[0];

                Assert.assertNotNull(constructedMessage.getHeaders().get(CitrusMessageHeaders.HEADER_CONTENT));
                Assert.assertEquals(constructedMessage.getHeaders().get(CitrusMessageHeaders.HEADER_CONTENT), 
                        "<TestHeader><operation>soapOperation</operation></TestHeader>");
                
                return null;
            }
        }).once();
        
        replay(messageSender);
        
        soapMessageAction.execute(context);
        
        verify(messageSender);
    }
    
    @Test
    public void testWrongMessageSenderImplementationTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        MessageSender jmsMessageSender = new JmsMessageSender();
        soapMessageAction.setMessageSender(jmsMessageSender);
        soapMessageAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        try {
            soapMessageAction.execute(context);
        } catch (CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Sending SOAP messages requires a " +
                    "'com.consol.citrus.ws.message.WebServiceMessageSender' but was 'com.consol.citrus.jms.JmsMessageSender'");
            return;
        }
        
        Assert.fail("Missing exception because of unsupported MessageSender implementation");
    }
}
