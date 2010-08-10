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

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.testng.AbstractBaseTest;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.ValidationContext;
import com.consol.citrus.ws.SoapAttachment;
import com.consol.citrus.ws.validation.SoapAttachmentValidator;

/**
 * @author Christoph Deppisch
 */
public class ReceiveSoapMessageActionTest extends AbstractBaseTest {
    
    private MessageReceiver messageReceiver = EasyMock.createMock(MessageReceiver.class);
    
    private SoapAttachmentValidator attachmentValidator = EasyMock.createMock(SoapAttachmentValidator.class);
    
    private MessageValidator messageValidator = EasyMock.createMock(MessageValidator.class);
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSoapMessageWithDefaultAttachmentDataTest() throws Exception {
        ReceiveSoapMessageAction soapMessageAction = new ReceiveSoapMessageAction();
        soapMessageAction.setMessageReceiver(messageReceiver);
        soapMessageAction.setAttachmentValidator(attachmentValidator);
        soapMessageAction.setValidator(messageValidator);
        soapMessageAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        soapMessageAction.setAttachmentData("TestAttachment!");

        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();
        
        reset(messageReceiver, attachmentValidator, messageValidator);
        
        expect(messageReceiver.receive()).andReturn(controlMessage);
        
        messageValidator.validateMessage((Message)anyObject(), (TestContext)anyObject(), (ValidationContext)anyObject());
        expectLastCall().once();
        
        attachmentValidator.validateAttachment((Message)anyObject(), (SoapAttachment) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment soapAttachment = (SoapAttachment)EasyMock.getCurrentArguments()[1];
                Assert.assertEquals(soapAttachment.getContent(), "TestAttachment!");
                Assert.assertNull(soapAttachment.getContentId());
                Assert.assertEquals(soapAttachment.getContentType(), "text/plain");
                return null;
            }
        });
        
        replay(messageReceiver, attachmentValidator, messageValidator);
        
        soapMessageAction.execute(context);
        
        verify(messageReceiver, attachmentValidator, messageValidator);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSoapMessageWithAttachmentDataTest() throws Exception {
        ReceiveSoapMessageAction soapMessageAction = new ReceiveSoapMessageAction();
        soapMessageAction.setMessageReceiver(messageReceiver);
        soapMessageAction.setAttachmentValidator(attachmentValidator);
        soapMessageAction.setValidator(messageValidator);
        soapMessageAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        soapMessageAction.setContentId("myAttachment");
        soapMessageAction.setContentType("text/xml");
        soapMessageAction.setAttachmentData("<TestAttachment><Message>Hello World!</Message></TestAttachment>");
        soapMessageAction.setCharsetName("UTF-16");
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();
        
        reset(messageReceiver, attachmentValidator, messageValidator);
        
        expect(messageReceiver.receive()).andReturn(controlMessage);
        
        messageValidator.validateMessage((Message)anyObject(), (TestContext)anyObject(), (ValidationContext)anyObject());
        expectLastCall().once();
        
        attachmentValidator.validateAttachment((Message)anyObject(), (SoapAttachment) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment soapAttachment = (SoapAttachment)EasyMock.getCurrentArguments()[1];
                Assert.assertEquals(soapAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(soapAttachment.getContentId(), "myAttachment");
                Assert.assertEquals(soapAttachment.getContentType(), "text/xml");
                Assert.assertEquals(soapAttachment.getCharsetName(), "UTF-16");
                return null;
            }
        });
        
        replay(messageReceiver, attachmentValidator, messageValidator);
        
        soapMessageAction.execute(context);
        
        verify(messageReceiver, attachmentValidator, messageValidator);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSoapMessageWithEmptyAttachmentContentTest() throws Exception {
        ReceiveSoapMessageAction soapMessageAction = new ReceiveSoapMessageAction();
        soapMessageAction.setMessageReceiver(messageReceiver);
        soapMessageAction.setAttachmentValidator(attachmentValidator);
        soapMessageAction.setValidator(messageValidator);
        soapMessageAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        soapMessageAction.setContentId("myAttachment");
        soapMessageAction.setAttachmentData("");
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();
        
        reset(messageReceiver, attachmentValidator, messageValidator);
        
        expect(messageReceiver.receive()).andReturn(controlMessage);
        
        messageValidator.validateMessage((Message)anyObject(), (TestContext)anyObject(), (ValidationContext)anyObject());
        expectLastCall().once();
        
        attachmentValidator.validateAttachment((Message)anyObject(), (SoapAttachment) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment soapAttachment = (SoapAttachment)EasyMock.getCurrentArguments()[1];
                Assert.assertEquals(soapAttachment.getContent(), "");
                Assert.assertEquals(soapAttachment.getContentId(), "myAttachment");
                Assert.assertEquals(soapAttachment.getContentType(), "text/plain");
                Assert.assertEquals(soapAttachment.getCharsetName(), "UTF-8");
                return null;
            }
        });
        
        replay(messageReceiver, attachmentValidator, messageValidator);
        
        soapMessageAction.execute(context);
        
        verify(messageReceiver, attachmentValidator, messageValidator);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSoapMessageWithNoAttachmentExpected() throws Exception {
        ReceiveSoapMessageAction soapMessageAction = new ReceiveSoapMessageAction();
        soapMessageAction.setMessageReceiver(messageReceiver);
        soapMessageAction.setAttachmentValidator(attachmentValidator);
        soapMessageAction.setValidator(messageValidator);
        soapMessageAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();
        
        reset(messageReceiver, attachmentValidator, messageValidator);
        
        expect(messageReceiver.receive()).andReturn(controlMessage);
        
        messageValidator.validateMessage((Message)anyObject(), (TestContext)anyObject(), (ValidationContext)anyObject());
        expectLastCall().once();
        
        replay(messageReceiver, attachmentValidator, messageValidator);
        
        soapMessageAction.execute(context);
        
        verify(messageReceiver, attachmentValidator, messageValidator);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSoapMessageWithAttachmentResourceTest() throws Exception {
        ReceiveSoapMessageAction soapMessageAction = new ReceiveSoapMessageAction();
        soapMessageAction.setMessageReceiver(messageReceiver);
        soapMessageAction.setAttachmentValidator(attachmentValidator);
        soapMessageAction.setValidator(messageValidator);
        soapMessageAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        soapMessageAction.setContentId("myAttachment");
        soapMessageAction.setContentType("text/xml");
        soapMessageAction.setAttachmentResource(new ClassPathResource("test-attachment.xml", SendSoapMessageActionTest.class));
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();
        
        reset(messageReceiver, attachmentValidator, messageValidator);
        
        expect(messageReceiver.receive()).andReturn(controlMessage);
        
        messageValidator.validateMessage((Message)anyObject(), (TestContext)anyObject(), (ValidationContext)anyObject());
        expectLastCall().once();
        
        attachmentValidator.validateAttachment((Message)anyObject(), (SoapAttachment) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment soapAttachment = (SoapAttachment)EasyMock.getCurrentArguments()[1];
                Assert.assertEquals(soapAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(soapAttachment.getContentId(), "myAttachment");
                Assert.assertEquals(soapAttachment.getContentType(), "text/xml");
                Assert.assertEquals(soapAttachment.getCharsetName(), "UTF-8");
                return null;
            }
        });
        
        replay(messageReceiver, attachmentValidator, messageValidator);
        
        soapMessageAction.execute(context);
        
        verify(messageReceiver, attachmentValidator, messageValidator);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSoapMessageWithAttachmentResourceVariablesSupportTest() throws Exception {
        ReceiveSoapMessageAction soapMessageAction = new ReceiveSoapMessageAction();
        soapMessageAction.setMessageReceiver(messageReceiver);
        soapMessageAction.setAttachmentValidator(attachmentValidator);
        soapMessageAction.setValidator(messageValidator);
        soapMessageAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        context.setVariable("myText", "Hello World!");
        
        soapMessageAction.setContentId("myAttachment");
        soapMessageAction.setContentType("text/xml");
        soapMessageAction.setAttachmentResource(new ClassPathResource("test-attachment-with-variables.xml", SendSoapMessageActionTest.class));
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();
        
        reset(messageReceiver, attachmentValidator, messageValidator);
        
        expect(messageReceiver.receive()).andReturn(controlMessage);
        
        messageValidator.validateMessage((Message)anyObject(), (TestContext)anyObject(), (ValidationContext)anyObject());
        expectLastCall().once();
        
        attachmentValidator.validateAttachment((Message)anyObject(), (SoapAttachment) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment soapAttachment = (SoapAttachment)EasyMock.getCurrentArguments()[1];
                Assert.assertEquals(soapAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(soapAttachment.getContentId(), "myAttachment");
                Assert.assertEquals(soapAttachment.getContentType(), "text/xml");
                Assert.assertEquals(soapAttachment.getCharsetName(), "UTF-8");
                return null;
            }
        });
        
        replay(messageReceiver, attachmentValidator, messageValidator);
        
        soapMessageAction.execute(context);
        
        verify(messageReceiver, attachmentValidator, messageValidator);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSoapMessageWithAttachmentDataVariablesSupportTest() throws Exception {
        ReceiveSoapMessageAction soapMessageAction = new ReceiveSoapMessageAction();
        soapMessageAction.setMessageReceiver(messageReceiver);
        soapMessageAction.setAttachmentValidator(attachmentValidator);
        soapMessageAction.setValidator(messageValidator);
        soapMessageAction.setMessageData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        context.setVariable("myText", "Hello World!");
        
        soapMessageAction.setContentId("myAttachment");
        soapMessageAction.setContentType("text/xml");
        soapMessageAction.setAttachmentData("<TestAttachment><Message>${myText}</Message></TestAttachment>");
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();
        
        reset(messageReceiver, attachmentValidator, messageValidator);
        
        expect(messageReceiver.receive()).andReturn(controlMessage);
        
        messageValidator.validateMessage((Message)anyObject(), (TestContext)anyObject(), (ValidationContext)anyObject());
        expectLastCall().once();
        
        attachmentValidator.validateAttachment((Message)anyObject(), (SoapAttachment) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment soapAttachment = (SoapAttachment)EasyMock.getCurrentArguments()[1];
                Assert.assertEquals(soapAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(soapAttachment.getContentId(), "myAttachment");
                Assert.assertEquals(soapAttachment.getContentType(), "text/xml");
                Assert.assertEquals(soapAttachment.getCharsetName(), "UTF-8");
                return null;
            }
        });
        
        replay(messageReceiver, attachmentValidator, messageValidator);
        
        soapMessageAction.execute(context);
        
        verify(messageReceiver, attachmentValidator, messageValidator);
    }
}
