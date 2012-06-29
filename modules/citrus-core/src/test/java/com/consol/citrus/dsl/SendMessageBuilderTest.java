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

import org.easymock.EasyMock;
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
    
    @Test
    public void testNG() {
        CitrusTestNGTestCaseBuilder builder = new CitrusTestNGTestCaseBuilder() {
            @Override
            protected void configure() {
                send(MessageBuilder.withPayload("Foo").setHeader("operation", "foo").build())
                    .with(messageSender);
                
                send()
                    .with(messageSender)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };
        
        builder.initialize();
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 2);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), SendMessageAction.class);
        Assert.assertEquals(builder.getTestCase().getActions().get(1).getClass(), SendMessageAction.class);
        
        Assert.assertEquals(((SendMessageAction)builder.getTestCase().getActions().get(0)).getName(), SendMessageAction.class.getSimpleName());
        
        Assert.assertEquals(((SendMessageAction)builder.getTestCase().getActions().get(0)).getMessageSender(), messageSender);
        Assert.assertEquals(((SendMessageAction)builder.getTestCase().getActions().get(1)).getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);
    }
}
