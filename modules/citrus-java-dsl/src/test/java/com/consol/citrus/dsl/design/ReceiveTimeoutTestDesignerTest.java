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

package com.consol.citrus.dsl.design;

import com.consol.citrus.TestCase;
import com.consol.citrus.actions.ReceiveTimeoutAction;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.mockito.Mockito.*;


/**
 * @author Christoph Deppisch
 * @since 1.3
 */
public class ReceiveTimeoutTestDesignerTest extends AbstractTestNGUnitTest {
    
    private Endpoint messageEndpoint = Mockito.mock(Endpoint.class);
    
    private ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    @Test
    public void testReceiveTimeoutBuilder() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                receiveTimeout(messageEndpoint)
                    .timeout(5000)
                    .selector("TestMessageSelectorString");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveTimeoutAction.class);
         
        ReceiveTimeoutAction action = (ReceiveTimeoutAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive-timeout");
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageSelector(),"TestMessageSelectorString"); 
        Assert.assertEquals(action.getTimeout(), 5000);
    }
    
    @Test
    public void testReceiveTimeoutBuilderWithEndpointName() {
        reset(applicationContextMock);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());
        MockTestDesigner builder = new MockTestDesigner(applicationContextMock, context) {
            @Override
            public void configure() {
                receiveTimeout("fooMessageEndpoint")
                    .timeout(500);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveTimeoutAction.class);
         
        ReceiveTimeoutAction action = (ReceiveTimeoutAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive-timeout");
        Assert.assertEquals(action.getEndpointUri(), "fooMessageEndpoint");
        Assert.assertEquals(action.getTimeout(), 500);

    }
}
