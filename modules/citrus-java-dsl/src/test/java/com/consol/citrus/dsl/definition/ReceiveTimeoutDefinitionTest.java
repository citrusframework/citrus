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

import com.consol.citrus.actions.ReceiveTimeoutAction;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.easymock.EasyMock.*;

public class ReceiveTimeoutDefinitionTest extends AbstractTestNGUnitTest {
    
    private MessageReceiver messageReceiver = EasyMock.createMock(MessageReceiver.class);
    
    private ApplicationContext applicationContextMock = EasyMock.createMock(ApplicationContext.class);
     
    @Test
    public void testReceiveTimeoutBuilder() {
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                expectTimeout(messageReceiver)
                    .timeout(5000)
                    .selector("TestMessageSelectorString");
            }
        };
         
        builder.run(null, null);
         
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), ReceiveTimeoutAction.class);
         
        ReceiveTimeoutAction action = (ReceiveTimeoutAction)builder.testCase().getActions().get(0);
        Assert.assertEquals(action.getName(), ReceiveTimeoutAction.class.getSimpleName());
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getMessageSelector(),"TestMessageSelectorString"); 
        Assert.assertEquals(action.getTimeout(), 5000);
    }
    
    @Test
    public void testReceiveTimeoutBuilderWithReceiverName() {
        MockBuilder builder = new MockBuilder(applicationContextMock) {
            @Override
            public void configure() {
                expectTimeout("fooMessageReceiver")
                    .timeout(500);
            }
        };
        
        reset(applicationContextMock);
        
        expect(applicationContextMock.getBean("fooMessageReceiver", MessageReceiver.class)).andReturn(messageReceiver).once();
        expect(applicationContextMock.getBean(TestListeners.class)).andReturn(new TestListeners()).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        
        replay(applicationContextMock);
        
        builder.run(null, null);
         
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), ReceiveTimeoutAction.class);
         
        ReceiveTimeoutAction action = (ReceiveTimeoutAction)builder.testCase().getActions().get(0);
        Assert.assertEquals(action.getName(), ReceiveTimeoutAction.class.getSimpleName());
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getTimeout(), 500);
        
        verify(applicationContextMock);
    }
}
