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
import org.springframework.integration.support.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;

/**
 * @author Christoph Deppisch
 */
public class ReceiveMessageBuilderTest {
    
    private MessageReceiver messageReceiver = EasyMock.createMock(MessageReceiver.class);
    
    private ApplicationContext applicationContext = EasyMock.createMock(ApplicationContext.class);
    
    @Test
    public void testReceiveBuilder() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
                receive(MessageBuilder.withPayload("Foo").setHeader("operation", "foo").build())
                    .with(messageReceiver);
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.getTestCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);
    }
    
    @Test
    public void testReceiveBuilderWithPayloadString() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
                receive()
                    .with(messageReceiver)
                    .validatePayload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.getTestCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);
    }
    
    @Test
    public void testReceiveBuilderWithReceiverName() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
                receive()
                    .with("fooMessageReceiver")
                    .validatePayload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };
        
        builder.setApplicationContext(applicationContext);
        
        reset(applicationContext);
        
        expect(applicationContext.getBean("fooMessageReceiver", MessageReceiver.class)).andReturn(messageReceiver).once();
        
        replay(applicationContext);
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveMessageAction)builder.getTestCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        
        verify(applicationContext);
    }
}
