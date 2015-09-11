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
import com.consol.citrus.actions.PurgeEndpointAction;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.builder.PurgeEndpointsBuilder;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.messaging.SelectiveConsumer;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class PurgeEndpointTestRunnerTest extends AbstractTestNGUnitTest {

    private Endpoint endpoint1 = EasyMock.createMock(Endpoint.class);
    private Endpoint endpoint2 = EasyMock.createMock(Endpoint.class);
    private Endpoint endpoint3 = EasyMock.createMock(Endpoint.class);
    private Endpoint endpoint4 = EasyMock.createMock(Endpoint.class);

    private Consumer consumer = EasyMock.createMock(Consumer.class);
    private SelectiveConsumer selectiveConsumer = EasyMock.createMock(SelectiveConsumer.class);

    private ApplicationContext applicationContextMock = EasyMock.createMock(ApplicationContext.class);

    @Test
    public void testPurgeEndpointsBuilderWithEndpoints() {
        reset(endpoint1, endpoint2, endpoint3, endpoint4, consumer, selectiveConsumer);

        expect(endpoint1.getName()).andReturn("e1").atLeastOnce();
        expect(endpoint2.getName()).andReturn("e2").atLeastOnce();
        expect(endpoint3.getName()).andReturn("e3").atLeastOnce();

        expect(endpoint1.createConsumer()).andReturn(consumer).once();
        expect(endpoint2.createConsumer()).andReturn(consumer).once();
        expect(endpoint3.createConsumer()).andReturn(consumer).once();

        expect(consumer.receive(anyObject(TestContext.class), eq(100L))).andThrow(new ActionTimeoutException()).times(3);

        replay(endpoint1, endpoint2, endpoint3, endpoint4, consumer, selectiveConsumer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                purgeEndpoints(new BuilderSupport<PurgeEndpointsBuilder>() {
                    @Override
                    public void configure(PurgeEndpointsBuilder builder) {
                        builder.endpoints(endpoint1, endpoint2)
                                .endpoint(endpoint3);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), PurgeEndpointAction.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "purge-endpoint");

        PurgeEndpointAction action = (PurgeEndpointAction) test.getActions().get(0);
        Assert.assertEquals(action.getEndpoints().size(), 3);
        Assert.assertEquals(action.getEndpoints().toString(), "[" + endpoint1.toString() + ", " + endpoint2.toString() + ", " + endpoint3.toString() + "]");
        Assert.assertEquals(action.getMessageSelector().size(), 0);
        Assert.assertNull(action.getMessageSelectorString());

        verify(endpoint1, endpoint2, endpoint3, endpoint4, consumer, selectiveConsumer);

    }
    
    @Test
    public void testPurgeEndpointBuilderWithNames() {
        reset(applicationContextMock, endpoint1, endpoint2, endpoint3, endpoint4, consumer, selectiveConsumer);

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        expect(endpoint1.getName()).andReturn("e1").atLeastOnce();
        expect(endpoint2.getName()).andReturn("e2").atLeastOnce();
        expect(endpoint3.getName()).andReturn("e3").atLeastOnce();
        expect(endpoint4.getName()).andReturn("e4").atLeastOnce();

        expect(endpoint1.createConsumer()).andReturn(selectiveConsumer).once();
        expect(endpoint2.createConsumer()).andReturn(consumer).once();
        expect(endpoint3.createConsumer()).andReturn(selectiveConsumer).once();
        expect(endpoint4.createConsumer()).andReturn(consumer).once();

        expect(applicationContextMock.getBean("e1", Endpoint.class)).andReturn(endpoint1).once();
        expect(applicationContextMock.getBean("e2", Endpoint.class)).andReturn(endpoint2).once();
        expect(applicationContextMock.getBean("e3", Endpoint.class)).andReturn(endpoint3).once();
        expect(applicationContextMock.getBean("e4", Endpoint.class)).andReturn(endpoint4).once();

        expect(consumer.receive(anyObject(TestContext.class), eq(100L))).andThrow(new ActionTimeoutException()).times(2);
        expect(selectiveConsumer.receive(eq("operation = 'sayHello'"), anyObject(TestContext.class), eq(100L))).andThrow(new ActionTimeoutException()).times(2);

        replay(applicationContextMock, endpoint1, endpoint2, endpoint3, endpoint4, consumer, selectiveConsumer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
            @Override
            public void execute() {
                purgeEndpoints(new BuilderSupport<PurgeEndpointsBuilder>() {
                    @Override
                    public void configure(PurgeEndpointsBuilder builder) {
                        builder.endpointNames("e1", "e2", "e3")
                                .endpoint("e4")
                                .selector("operation = 'sayHello'");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), PurgeEndpointAction.class);

        PurgeEndpointAction action = (PurgeEndpointAction) test.getActions().get(0);
        Assert.assertEquals(action.getEndpointNames().size(), 4);
        Assert.assertEquals(action.getEndpointNames().toString(), "[e1, e2, e3, e4]");
        Assert.assertTrue(action.getBeanFactory() instanceof ApplicationContext);
        Assert.assertEquals(action.getMessageSelector().size(), 0);
        Assert.assertEquals(action.getMessageSelectorString(), "operation = 'sayHello'");

        verify(applicationContextMock, endpoint1, endpoint2, endpoint3, endpoint4, consumer, selectiveConsumer);
    }

    @Test
    public void testCustomEndpointResolver() {
        reset(applicationContextMock, endpoint1, consumer, selectiveConsumer);

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        expect(endpoint1.getName()).andReturn("e1").atLeastOnce();
        expect(endpoint1.createConsumer()).andReturn(consumer).once();

        expect(applicationContextMock.getBean("e1", Endpoint.class)).andReturn(endpoint1).once();
        expect(consumer.receive(anyObject(TestContext.class), eq(100L))).andThrow(new ActionTimeoutException()).once();

        replay(applicationContextMock, endpoint1, consumer, selectiveConsumer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
            @Override
            public void execute() {
                purgeEndpoints(new BuilderSupport<PurgeEndpointsBuilder>() {
                    @Override
                    public void configure(PurgeEndpointsBuilder builder) {
                        builder.endpoint("e1")
                                .withApplicationContext(applicationContextMock);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), PurgeEndpointAction.class);

        PurgeEndpointAction action = (PurgeEndpointAction) test.getActions().get(0);
        Assert.assertEquals(action.getEndpointNames().size(), 1);
        Assert.assertEquals(action.getEndpointNames().toString(), "[e1]");
        Assert.assertNotNull(action.getBeanFactory());
        Assert.assertEquals(action.getBeanFactory(), applicationContextMock);

        verify(applicationContextMock, endpoint1, consumer, selectiveConsumer);
    }
}
