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
import org.citrusframework.citrus.actions.ReceiveTimeoutAction;
import org.citrusframework.citrus.container.SequenceAfterTest;
import org.citrusframework.citrus.container.SequenceBeforeTest;
import org.citrusframework.citrus.spi.ReferenceResolver;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.endpoint.Endpoint;
import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.citrusframework.citrus.message.DefaultMessage;
import org.citrusframework.citrus.messaging.Consumer;
import org.citrusframework.citrus.report.TestActionListeners;
import org.citrusframework.citrus.dsl.UnitTestSupport;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class ReceiveTimeoutTestRunnerTest extends UnitTestSupport {

    private Endpoint messageEndpoint = Mockito.mock(Endpoint.class);
    private Consumer messageConsumer = Mockito.mock(Consumer.class);
    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);

    @Test
    public void testReceiveTimeoutBuilder() {
        reset(messageEndpoint, messageConsumer);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        doAnswer(invocation -> {
            Thread.sleep(500L);
            return null;
        }).when(messageConsumer).receive(any(TestContext.class), eq(250L));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receiveTimeout(builder -> builder.endpoint(messageEndpoint)
                        .timeout(250)
                        .selector("TestMessageSelector"));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveTimeoutAction.class);
        Assert.assertEquals(test.getActiveAction().getClass(), ReceiveTimeoutAction.class);

        ReceiveTimeoutAction action = (ReceiveTimeoutAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive-timeout");
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageSelector(),"TestMessageSelector");
        Assert.assertEquals(action.getTimeout(), 250);

    }

    @Test
    public void testReceiveTimeoutBuilderWithEndpointName() {
        TestContext context = applicationContext.getBean(TestContext.class);

        reset(referenceResolver, messageEndpoint, messageConsumer);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        doAnswer(invocation -> {
            Thread.sleep(600L);
            return null;
        }).when(messageConsumer).receive(any(TestContext.class), eq(500L));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve("fooMessageEndpoint", Endpoint.class)).thenReturn(messageEndpoint);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receiveTimeout(builder -> builder.endpoint("fooMessageEndpoint")
                        .timeout(500));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveTimeoutAction.class);
        Assert.assertEquals(test.getActiveAction().getClass(), ReceiveTimeoutAction.class);

        ReceiveTimeoutAction action = (ReceiveTimeoutAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive-timeout");
        Assert.assertEquals(action.getEndpointUri(), "fooMessageEndpoint");
        Assert.assertEquals(action.getTimeout(), 500);

    }

    @Test
    public void testReceiveTimeoutBuilderFailure() {
        reset(messageEndpoint, messageConsumer);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        doAnswer(invocation -> {
            Thread.sleep(100L);
            return new DefaultMessage("Hello Citrus!");
        }).when(messageConsumer).receive(any(TestContext.class), eq(250L));
        try {
            new MockTestRunner(getClass().getSimpleName(), context) {
                @Override
                public void execute() {
                    receiveTimeout(builder -> builder.endpoint(messageEndpoint)
                            .timeout(250)
                            .selector("TestMessageSelector"));
                }
            };

            Assert.fail("Missing validation exception due to message received during timeout");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("Message timeout validation failed"));
        }
    }
}
