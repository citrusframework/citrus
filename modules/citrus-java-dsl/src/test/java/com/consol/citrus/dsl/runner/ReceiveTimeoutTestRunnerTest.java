/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.dsl.runner;

import com.consol.citrus.TestCase;
import com.consol.citrus.actions.ReceiveTimeoutAction;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.builder.ReceiveTimeoutBuilder;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class ReceiveTimeoutTestRunnerTest extends AbstractTestNGUnitTest {
    
    private Endpoint messageEndpoint = Mockito.mock(Endpoint.class);
    private Consumer messageConsumer = Mockito.mock(Consumer.class);
    private ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    @Test
    public void testReceiveTimeoutBuilder() {
        reset(messageEndpoint, messageConsumer);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        doAnswer(new Answer<Message>() {
            @Override
            public Message answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(500L);
                return null;
            }
        }).when(messageConsumer).receive(any(TestContext.class), eq(250L));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                receiveTimeout(new BuilderSupport<ReceiveTimeoutBuilder>() {
                    @Override
                    public void configure(ReceiveTimeoutBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .timeout(250)
                                .selector("TestMessageSelectorString");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveTimeoutAction.class);
        Assert.assertEquals(test.getLastExecutedAction().getClass(), ReceiveTimeoutAction.class);

        ReceiveTimeoutAction action = (ReceiveTimeoutAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive-timeout");
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageSelector(),"TestMessageSelectorString"); 
        Assert.assertEquals(action.getTimeout(), 250);

    }
    
    @Test
    public void testReceiveTimeoutBuilderWithEndpointName() {
        TestContext context = applicationContext.getBean(TestContext.class);
        context.setApplicationContext(applicationContextMock);

        reset(applicationContextMock, messageEndpoint, messageConsumer);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        doAnswer(new Answer<Message>() {
            @Override
            public Message answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(600L);
                return null;
            }
        }).when(messageConsumer).receive(any(TestContext.class), eq(500L));

        when(applicationContextMock.getBean(TestContext.class)).thenReturn(context);
        when(applicationContextMock.getBean("fooMessageEndpoint", Endpoint.class)).thenReturn(messageEndpoint);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock, context) {
            @Override
            public void execute() {
                receiveTimeout(new BuilderSupport<ReceiveTimeoutBuilder>() {
                    @Override
                    public void configure(ReceiveTimeoutBuilder builder) {
                        builder.endpoint("fooMessageEndpoint")
                                .timeout(500);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveTimeoutAction.class);
        Assert.assertEquals(test.getLastExecutedAction().getClass(), ReceiveTimeoutAction.class);

        ReceiveTimeoutAction action = (ReceiveTimeoutAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive-timeout");
        Assert.assertEquals(action.getEndpointUri(), "fooMessageEndpoint");
        Assert.assertEquals(action.getTimeout(), 500);

    }

    @Test
    public void testReceiveTimeoutBuilderFailure() {
        reset(messageEndpoint, messageConsumer);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        doAnswer(new Answer<Message>() {
            @Override
            public Message answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(100L);
                return new DefaultMessage("Hello Citrus!");
            }
        }).when(messageConsumer).receive(any(TestContext.class), eq(250L));
        try {
            new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
                @Override
                public void execute() {
                    receiveTimeout(new BuilderSupport<ReceiveTimeoutBuilder>() {
                        @Override
                        public void configure(ReceiveTimeoutBuilder builder) {
                            builder.endpoint(messageEndpoint)
                                    .timeout(250)
                                    .selector("TestMessageSelectorString");
                        }
                    });
                }
            };

            Assert.fail("Missing validation exception due to message received during timeout");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("Message timeout validation failed"));
        }
    }
}
