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

package org.citrusframework.actions.dsl;

import java.util.HashMap;

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.ReceiveTimeoutAction;
import org.citrusframework.container.SequenceAfterTest;
import org.citrusframework.container.SequenceBeforeTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.report.TestActionListeners;
import org.citrusframework.spi.ReferenceResolver;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.ReceiveTimeoutAction.Builder.expectTimeout;
import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class ExpectTimeoutTestActionBuilderTest extends UnitTestSupport {

    private final Endpoint messageEndpoint = Mockito.mock(Endpoint.class);
    private final Consumer messageConsumer = Mockito.mock(Consumer.class);
    private final ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);

    @Test
    public void testReceiveTimeoutBuilder() {
        reset(messageEndpoint, messageConsumer);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        doAnswer(invocation -> {
            Thread.sleep(500L);
            return null;
        }).when(messageConsumer).receive(any(TestContext.class), eq(250L));
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(expectTimeout().endpoint(messageEndpoint)
                        .timeout(250)
                        .selector("TestMessageSelector"));

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
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(expectTimeout().endpoint("fooMessageEndpoint")
                        .timeout(500));

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
            DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
            builder.$(expectTimeout().endpoint(messageEndpoint)
                            .timeout(250)
                            .selector("TestMessageSelector"));

            Assert.fail("Missing validation exception due to message received during timeout");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("Message timeout validation failed"));
        }
    }
}
