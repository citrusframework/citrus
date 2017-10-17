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
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.messaging.SelectiveConsumer;
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
 * @since 2.3
 */
public class PurgeEndpointTestRunnerTest extends AbstractTestNGUnitTest {

    private Endpoint endpoint1 = Mockito.mock(Endpoint.class);
    private Endpoint endpoint2 = Mockito.mock(Endpoint.class);
    private Endpoint endpoint3 = Mockito.mock(Endpoint.class);
    private Endpoint endpoint4 = Mockito.mock(Endpoint.class);

    private Consumer consumer = Mockito.mock(Consumer.class);
    private SelectiveConsumer selectiveConsumer = Mockito.mock(SelectiveConsumer.class);

    private ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    @Test
    public void testPurgeEndpointsBuilderWithEndpoints() {
        reset(endpoint1, endpoint2, endpoint3, endpoint4, consumer, selectiveConsumer);

        when(endpoint1.getName()).thenReturn("e1");
        when(endpoint2.getName()).thenReturn("e2");
        when(endpoint3.getName()).thenReturn("e3");

        when(endpoint1.createConsumer()).thenReturn(consumer);
        when(endpoint2.createConsumer()).thenReturn(consumer);
        when(endpoint3.createConsumer()).thenReturn(consumer);

        doThrow(new ActionTimeoutException()).when(consumer).receive(any(TestContext.class), eq(100L));

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                purgeEndpoints(builder -> builder.endpoints(endpoint1, endpoint2)
                        .endpoint(endpoint3));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), PurgeEndpointAction.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "purge-endpoint");

        PurgeEndpointAction action = (PurgeEndpointAction) test.getActions().get(0);
        Assert.assertEquals(action.getEndpoints().size(), 3);
        Assert.assertEquals(action.getEndpoints().toString(), "[" + endpoint1.toString() + ", " + endpoint2.toString() + ", " + endpoint3.toString() + "]");
        Assert.assertEquals(action.getMessageSelectorMap().size(), 0);
        Assert.assertNull(action.getMessageSelector());


    }
    
    @Test
    public void testPurgeEndpointBuilderWithNames() {
        reset(applicationContextMock, endpoint1, endpoint2, endpoint3, endpoint4, consumer, selectiveConsumer);

        when(applicationContextMock.getBean(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        when(endpoint1.getName()).thenReturn("e1");
        when(endpoint2.getName()).thenReturn("e2");
        when(endpoint3.getName()).thenReturn("e3");
        when(endpoint4.getName()).thenReturn("e4");

        when(endpoint1.createConsumer()).thenReturn(selectiveConsumer);
        when(endpoint2.createConsumer()).thenReturn(consumer);
        when(endpoint3.createConsumer()).thenReturn(selectiveConsumer);
        when(endpoint4.createConsumer()).thenReturn(consumer);

        when(applicationContextMock.getBean("e1", Endpoint.class)).thenReturn(endpoint1);
        when(applicationContextMock.getBean("e2", Endpoint.class)).thenReturn(endpoint2);
        when(applicationContextMock.getBean("e3", Endpoint.class)).thenReturn(endpoint3);
        when(applicationContextMock.getBean("e4", Endpoint.class)).thenReturn(endpoint4);

        doThrow(new ActionTimeoutException()).when(consumer).receive(any(TestContext.class), eq(100L));
        doThrow(new ActionTimeoutException()).when(selectiveConsumer).receive(eq("operation = 'sayHello'"), any(TestContext.class), eq(100L));

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock, context) {
            @Override
            public void execute() {
                purgeEndpoints(builder -> builder.endpointNames("e1", "e2", "e3")
                        .endpoint("e4")
                        .selector("operation = 'sayHello'"));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), PurgeEndpointAction.class);

        PurgeEndpointAction action = (PurgeEndpointAction) test.getActions().get(0);
        Assert.assertEquals(action.getEndpointNames().size(), 4);
        Assert.assertEquals(action.getEndpointNames().toString(), "[e1, e2, e3, e4]");
        Assert.assertTrue(action.getBeanFactory() instanceof ApplicationContext);
        Assert.assertEquals(action.getMessageSelectorMap().size(), 0);
        Assert.assertEquals(action.getMessageSelector(), "operation = 'sayHello'");

    }

    @Test
    public void testCustomEndpointResolver() {
        reset(applicationContextMock, endpoint1, consumer, selectiveConsumer);

        when(applicationContextMock.getBean(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        when(endpoint1.getName()).thenReturn("e1");
        when(endpoint1.createConsumer()).thenReturn(consumer);

        when(applicationContextMock.getBean("e1", Endpoint.class)).thenReturn(endpoint1);
        doThrow(new ActionTimeoutException()).when(consumer).receive(any(TestContext.class), eq(100L));

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock, context) {
            @Override
            public void execute() {
                purgeEndpoints(builder -> builder.endpoint("e1")
                        .withApplicationContext(applicationContextMock));
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

    }
}
