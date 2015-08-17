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
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class ReceiveTimeoutTestRunnerTest extends AbstractTestNGUnitTest {
    
    private Endpoint messageEndpoint = EasyMock.createMock(Endpoint.class);
    private Consumer messageConsumer = EasyMock.createMock(Consumer.class);
    private ApplicationContext applicationContextMock = EasyMock.createMock(ApplicationContext.class);

    @Test
    public void testReceiveTimeoutBuilder() {
        reset(messageEndpoint, messageConsumer);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageConsumer.receive(anyObject(TestContext.class), eq(250L))).andAnswer(new IAnswer<Message>() {
            @Override
            public Message answer() throws Throwable {
                Thread.sleep(500L);
                return null;
            }
        }).once();
        replay(messageEndpoint, messageConsumer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
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

        verify(messageEndpoint, messageConsumer);
    }
    
    @Test
    public void testReceiveTimeoutBuilderWithEndpointName() {
        reset(applicationContextMock, messageEndpoint, messageConsumer);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageConsumer.receive(anyObject(TestContext.class), eq(500L))).andAnswer(new IAnswer<Message>() {
            @Override
            public Message answer() throws Throwable {
                Thread.sleep(600L);
                return null;
            }
        }).once();

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.getBean("fooMessageEndpoint", Endpoint.class)).andReturn(messageEndpoint).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();
        replay(applicationContextMock, messageEndpoint, messageConsumer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
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
        
        verify(applicationContextMock, messageEndpoint, messageConsumer);
    }

    @Test
    public void testReceiveTimeoutBuilderFailure() {
        reset(messageEndpoint, messageConsumer);
        expect(messageEndpoint.createConsumer()).andReturn(messageConsumer).once();
        expect(messageConsumer.receive(anyObject(TestContext.class), eq(250L))).andAnswer(new IAnswer<Message>() {
            @Override
            public Message answer() throws Throwable {
                Thread.sleep(100L);
                return new DefaultMessage("Hello Citrus!");
            }
        }).once();
        replay(messageEndpoint, messageConsumer);

        try {
            new MockTestRunner(getClass().getSimpleName(), applicationContext) {
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
            verify(messageEndpoint, messageConsumer);
        }
    }
}
