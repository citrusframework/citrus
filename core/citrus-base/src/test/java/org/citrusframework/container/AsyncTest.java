/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.container;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.citrusframework.TestAction;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.FailAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class AsyncTest extends UnitTestSupport {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(AsyncTest.class);

    private final TestAction action = Mockito.mock(TestAction.class);
    private final TestAction success = Mockito.mock(TestAction.class);
    private final TestAction error = Mockito.mock(TestAction.class);

    @Test
    public void testSingleAction() throws Exception {
        reset(action, success, error);

        Async container = new Async.Builder()
                .actions(action)
                .successAction(success)
                .errorAction(error)
                .build();
        container.execute(context);

        waitForDone(container, context, 2000);

        verify(action).execute(context);
        verify(success).execute(context);
        verify(error, times(0)).execute(context);
    }

    @Test
    public void testMultipleActions() throws Exception {
        TestAction action1 = Mockito.mock(TestAction.class);
        TestAction action2 = Mockito.mock(TestAction.class);
        TestAction action3 = Mockito.mock(TestAction.class);

        reset(action1, action2, action3, success, error);

        Async container = new Async.Builder()
                .actions(action1, action2, action3)
                .successAction(success)
                .errorAction(error)
                .build();
        container.execute(context);

        waitForDone(container, context, 2000);

        verify(action1).execute(context);
        verify(action2).execute(context);
        verify(action3).execute(context);
        verify(success).execute(context);
        verify(error, times(0)).execute(context);
    }

    @Test
    public void testFailingAction() throws Exception {
        TestAction action1 = Mockito.mock(TestAction.class);
        TestAction action2 = Mockito.mock(TestAction.class);
        TestAction action3 = Mockito.mock(TestAction.class);

        reset(action1, action2, action3, success, error);

        Async container = new Async.Builder()
                .actions(action1, new FailAction.Builder().build(), action2, action3)
                .successAction(success)
                .errorAction(error)
                .build();
        container.execute(context);

        waitForDone(container, context, 2000);

        Assert.assertEquals(context.getExceptions().size(), 1L);
        Assert.assertEquals(context.getExceptions().get(0).getClass(), CitrusRuntimeException.class);
        Assert.assertEquals(context.getExceptions().get(0).getMessage(), "Generated error to interrupt test execution");

        verify(action1).execute(context);
        verify(action2, times(0)).execute(context);
        verify(action3, times(0)).execute(context);
        verify(error).execute(context);
        verify(success, times(0)).execute(context);
    }

    @Test(expectedExceptions = TimeoutException.class)
    public void testWaitForFinishTimeout() throws Exception {
        reset(action, success, error);

        doAnswer(invocation -> {
            Thread.sleep(500L);
            return null;
        }).when(action).execute(context);

        Async container = new Async.Builder()
                .actions(action)
                .build();
        container.execute(context);

        waitForDone(container, context, 100);
    }

    @Test
    public void testWaitForFinishError() throws Exception {
        reset(action, success, error);

        doThrow(new CitrusRuntimeException("FAILED!")).when(action).execute(context);

        Async container = new Async.Builder()
                .actions(action)
                .build();
        container.execute(context);

        waitForDone(container, context, 2000);

        Assert.assertEquals(context.getExceptions().size(), 1L);
        Assert.assertEquals(context.getExceptions().get(0).getClass(), CitrusRuntimeException.class);
        Assert.assertEquals(context.getExceptions().get(0).getMessage(), "FAILED!");
    }

    private void waitForDone(Async container, TestContext context, long timeout) throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Boolean> done = new CompletableFuture<>();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            if (container.isDone(context)) {
                done.complete(true);
            } else {
                logger.debug("Async action execution not finished yet ...");
            }
        }, 100, timeout / 10, TimeUnit.MILLISECONDS);

        done.get(timeout, TimeUnit.MILLISECONDS);
    }
}
