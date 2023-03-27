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

package org.citrusframework.citrus.dsl.runner;

import java.util.HashMap;

import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.actions.PurgeEndpointAction;
import org.citrusframework.citrus.container.SequenceAfterTest;
import org.citrusframework.citrus.container.SequenceBeforeTest;
import org.citrusframework.citrus.spi.ReferenceResolver;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.endpoint.Endpoint;
import org.citrusframework.citrus.exceptions.ActionTimeoutException;
import org.citrusframework.citrus.messaging.Consumer;
import org.citrusframework.citrus.messaging.SelectiveConsumer;
import org.citrusframework.citrus.report.TestActionListeners;
import org.citrusframework.citrus.dsl.UnitTestSupport;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class PurgeEndpointTestRunnerTest extends UnitTestSupport {

    private Endpoint endpoint1 = Mockito.mock(Endpoint.class);
    private Endpoint endpoint2 = Mockito.mock(Endpoint.class);
    private Endpoint endpoint3 = Mockito.mock(Endpoint.class);
    private Endpoint endpoint4 = Mockito.mock(Endpoint.class);

    private Consumer consumer = Mockito.mock(Consumer.class);
    private SelectiveConsumer selectiveConsumer = Mockito.mock(SelectiveConsumer.class);

    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);

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

        context.setReferenceResolver(referenceResolver);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
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
        reset(referenceResolver, endpoint1, endpoint2, endpoint3, endpoint4, consumer, selectiveConsumer);

        when(referenceResolver.resolve(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        when(endpoint1.getName()).thenReturn("e1");
        when(endpoint2.getName()).thenReturn("e2");
        when(endpoint3.getName()).thenReturn("e3");
        when(endpoint4.getName()).thenReturn("e4");

        when(endpoint1.createConsumer()).thenReturn(selectiveConsumer);
        when(endpoint2.createConsumer()).thenReturn(consumer);
        when(endpoint3.createConsumer()).thenReturn(selectiveConsumer);
        when(endpoint4.createConsumer()).thenReturn(consumer);

        when(referenceResolver.resolve("e1", Endpoint.class)).thenReturn(endpoint1);
        when(referenceResolver.resolve("e2", Endpoint.class)).thenReturn(endpoint2);
        when(referenceResolver.resolve("e3", Endpoint.class)).thenReturn(endpoint3);
        when(referenceResolver.resolve("e4", Endpoint.class)).thenReturn(endpoint4);

        doThrow(new ActionTimeoutException()).when(consumer).receive(any(TestContext.class), eq(100L));
        doThrow(new ActionTimeoutException()).when(selectiveConsumer).receive(eq("operation = 'sayHello'"), any(TestContext.class), eq(100L));

        context.setReferenceResolver(referenceResolver);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
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
        Assert.assertNotNull(action.getReferenceResolver());
        Assert.assertEquals(action.getMessageSelectorMap().size(), 0);
        Assert.assertEquals(action.getMessageSelector(), "operation = 'sayHello'");

    }

    @Test
    public void testCustomEndpointResolver() {
        reset(referenceResolver, endpoint1, consumer, selectiveConsumer);

        when(referenceResolver.resolve(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        when(endpoint1.getName()).thenReturn("e1");
        when(endpoint1.createConsumer()).thenReturn(consumer);

        when(referenceResolver.resolve("e1", Endpoint.class)).thenReturn(endpoint1);
        doThrow(new ActionTimeoutException()).when(consumer).receive(any(TestContext.class), eq(100L));

        context.setReferenceResolver(referenceResolver);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                purgeEndpoints(builder -> builder.endpoint("e1")
                        .withReferenceResolver(referenceResolver));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), PurgeEndpointAction.class);

        PurgeEndpointAction action = (PurgeEndpointAction) test.getActions().get(0);
        Assert.assertEquals(action.getEndpointNames().size(), 1);
        Assert.assertEquals(action.getEndpointNames().toString(), "[e1]");
        Assert.assertNotNull(action.getReferenceResolver());
        Assert.assertEquals(action.getReferenceResolver(), referenceResolver);

    }
}
