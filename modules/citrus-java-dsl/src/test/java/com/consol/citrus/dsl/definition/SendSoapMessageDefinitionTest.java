/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.dsl.definition;

import static org.easymock.EasyMock.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.integration.support.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import com.consol.citrus.message.MessageSender;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.ws.SoapAttachment;
import com.consol.citrus.ws.actions.SendSoapMessageAction;
import com.consol.citrus.ws.message.WebServiceMessageSender;

/**
 * @author Christoph Deppisch
 */
public class SendSoapMessageDefinitionTest {
    
    private WebServiceMessageSender soapMessageSender = EasyMock.createMock(WebServiceMessageSender.class);
    
    private ApplicationContext applicationContext = EasyMock.createMock(ApplicationContext.class);
    
    private Resource resource = EasyMock.createMock(Resource.class);
    
    private SoapAttachment testAttachment = new SoapAttachment();
    
    /**
     * Setup test attachment.
     */
    @BeforeClass
    public void setup() {
        testAttachment.setContentId("attachment01");
        testAttachment.setContent("This is an attachment");
        testAttachment.setContentType("text/plain");
        testAttachment.setCharsetName("UTF-8");
    }
    
    @Test
    public void testFork() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                send(soapMessageSender)
                    .message(MessageBuilder.withPayload("Foo").setHeader("operation", "foo").build());
                
                send(soapMessageSender)
                    .message(MessageBuilder.withPayload("Foo").setHeader("operation", "foo").build())
                    .fork(true);
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 2);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), SendSoapMessageAction.class);
        Assert.assertEquals(builder.getTestCase().getActions().get(1).getClass(), SendSoapMessageAction.class);
        
        SendSoapMessageAction action = ((SendSoapMessageAction)builder.getTestCase().getActions().get(0));
        Assert.assertEquals(action.getName(), SendSoapMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageSender(), soapMessageSender);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);
        
        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "Foo");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 1L);
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("operation"), "foo");
        
        Assert.assertFalse(action.isForkMode());
        
        action = ((SendSoapMessageAction)builder.getTestCase().getActions().get(1));
        Assert.assertEquals(action.getName(), SendSoapMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageSender(), soapMessageSender);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);
        
        Assert.assertTrue(action.isForkMode());
    }
    
    @Test
    public void testSoapAttachment() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                send(soapMessageSender)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .attatchment(testAttachment);
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), SendSoapMessageAction.class);
        
        SendSoapMessageAction action = ((SendSoapMessageAction)builder.getTestCase().getActions().get(0));
        Assert.assertEquals(action.getName(), SendSoapMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageSender(), soapMessageSender);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        
        Assert.assertNull(action.getAttachmentResource());
        Assert.assertEquals(action.getAttachmentData(), testAttachment.getContent());
        Assert.assertEquals(action.getAttachment().getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachment().getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachment().getCharsetName(), testAttachment.getCharsetName());
    }
    
    @Test
    public void testSoapAttachmentData() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                send(soapMessageSender)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .attatchment(testAttachment.getContentId(), testAttachment.getContentType(), testAttachment.getContent());
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), SendSoapMessageAction.class);
        
        SendSoapMessageAction action = ((SendSoapMessageAction)builder.getTestCase().getActions().get(0));
        Assert.assertEquals(action.getName(), SendSoapMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageSender(), soapMessageSender);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        
        Assert.assertNull(action.getAttachmentResource());
        Assert.assertEquals(action.getAttachmentData(), testAttachment.getContent());
        Assert.assertEquals(action.getAttachment().getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachment().getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachment().getCharsetName(), testAttachment.getCharsetName());
    }
    
    @Test
    public void testSoapAttachmentResource() throws IOException {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                send(soapMessageSender)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .attatchment(testAttachment.getContentId(), testAttachment.getContentType(), resource);
            }
        };
        
        reset(resource);
        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("someAttachmentData".getBytes())).once();
        replay(resource);
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), SendSoapMessageAction.class);
        
        SendSoapMessageAction action = ((SendSoapMessageAction)builder.getTestCase().getActions().get(0));
        Assert.assertEquals(action.getName(), SendSoapMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageSender(), soapMessageSender);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        
        Assert.assertEquals(action.getAttachmentData(), "someAttachmentData");
        Assert.assertEquals(action.getAttachment().getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachment().getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachment().getCharsetName(), testAttachment.getCharsetName());
        
        verify(resource);
    }
    
    @Test
    public void testSendBuilderWithSenderName() {
        MessageSender messageSender = EasyMock.createMock(MessageSender.class);
        
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                send("soapMessageSender")
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .header("operation", "soapOperation")
                    .soap()
                    .attatchment(testAttachment);
                
                send("messageSender")
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };
        
        builder.setApplicationContext(applicationContext);
        
        reset(applicationContext);
        
        expect(applicationContext.getBean("soapMessageSender", MessageSender.class)).andReturn(soapMessageSender).once();
        expect(applicationContext.getBean("messageSender", MessageSender.class)).andReturn(messageSender).once();
        
        replay(applicationContext);
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 2);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), SendSoapMessageAction.class);
        Assert.assertEquals(builder.getTestCase().getActions().get(1).getClass(), SendMessageAction.class);
        
        SendMessageAction action = ((SendSoapMessageAction)builder.getTestCase().getActions().get(0));
        Assert.assertEquals(action.getName(), SendSoapMessageAction.class.getSimpleName());
        Assert.assertEquals(action.getMessageSender(), soapMessageSender);
        
        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 1L);
        Assert.assertTrue(messageBuilder.getMessageHeaders().containsKey("operation"));
        
        action = ((SendMessageAction)builder.getTestCase().getActions().get(1));
        Assert.assertEquals(action.getName(), SendMessageAction.class.getSimpleName());
        Assert.assertEquals(action.getMessageSender(), messageSender);
        
        verify(applicationContext);
    }
    
}
