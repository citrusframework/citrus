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
import org.testng.annotations.Test;
import org.testng.Assert;

import com.consol.citrus.actions.ReceiveTimeoutAction;
import com.consol.citrus.message.MessageReceiver;

public class ReceiveTimeoutBuilderTest {
    
    private MessageReceiver messageReceiver = EasyMock.createMock(MessageReceiver.class);
     
    @Test
    public void TestReceiveTimeoutBuilder() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
                expectTimeout(messageReceiver)
                    .timeout(5000)
                    .selector("TestMessageSelectorString");
            }
        };
         
        builder.configure();
         
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ReceiveTimeoutAction.class);
         
        ReceiveTimeoutAction action = (ReceiveTimeoutAction)builder.getTestCase().getActions().get(0);
        Assert.assertEquals(action.getName(), ReceiveTimeoutAction.class.getSimpleName());
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getMessageSelector(),"TestMessageSelectorString"); 
        Assert.assertEquals(action.getTimeout(), 5000);
    }
}
