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

package com.consol.citrus.dsl;

import static org.easymock.EasyMock.*;

import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.integration.support.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.message.MessageSender;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;

/**
 * @author Christoph Deppisch
 */
public class SendMessageBuilderTest {
    
    private MessageSender messageSender = EasyMock.createMock(MessageSender.class);
    
    private ApplicationContext applicationContext = EasyMock.createMock(ApplicationContext.class);
    
    private Resource resource = EasyMock.createMock(Resource.class);
    
    @Test
    public void testSendBuilder() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
                send(messageSender)
                    .message(MessageBuilder.withPayload("Foo").setHeader("operation", "foo").build());
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), SendMessageAction.class);
        
        SendMessageAction action = ((SendMessageAction)builder.getTestCase().getActions().get(0));
        Assert.assertEquals(action.getName(), SendMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageSender(), messageSender);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);
    }
    
    @Test
    public void testSendBuilderWithPayloadData() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
                send(messageSender)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), SendMessageAction.class);
        
        SendMessageAction action = ((SendMessageAction)builder.getTestCase().getActions().get(0));
        Assert.assertEquals(action.getName(), SendMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageSender(), messageSender);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);
    }
    
    @Test
    public void testSendBuilderWithPayloadResource() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
                send(messageSender)
                    .payload(resource);
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), SendMessageAction.class);
        
        SendMessageAction action = ((SendMessageAction)builder.getTestCase().getActions().get(0));
        Assert.assertEquals(action.getName(), SendMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageSender(), messageSender);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);
    }
    
    @Test
    public void testSendBuilderWithSenderName() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
                send("fooMessageSender")
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };
        
        builder.setApplicationContext(applicationContext);
        
        reset(applicationContext);
        
        expect(applicationContext.getBean("fooMessageSender", MessageSender.class)).andReturn(messageSender).once();
        
        replay(applicationContext);
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), SendMessageAction.class);
        
        SendMessageAction action = ((SendMessageAction)builder.getTestCase().getActions().get(0));
        Assert.assertEquals(action.getName(), SendMessageAction.class.getSimpleName());
        Assert.assertEquals(action.getMessageSender(), messageSender);
        
        verify(applicationContext);
    }
}
