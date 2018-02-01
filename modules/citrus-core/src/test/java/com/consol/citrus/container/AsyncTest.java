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

package com.consol.citrus.container;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.FailAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class AsyncTest extends AbstractTestNGUnitTest {

    private TestAction action = Mockito.mock(TestAction.class);
    private TestAction success = Mockito.mock(TestAction.class);
    private TestAction error = Mockito.mock(TestAction.class);

    @Test
    public void testSingleAction() throws Exception {
        Async container = new Async();

        reset(action, success, error);

        container.setActions(Collections.singletonList(action));

        container.addSuccessAction(success);
        container.addErrorAction(error);
        
        container.execute(context);

        waitForDone(container, context, 2000);

        verify(action).execute(context);
        verify(success).execute(context);
        verify(error, times(0)).execute(context);
    }

    @Test
    public void testMultipleActions() throws Exception {
        Async container = new Async();

        TestAction action1 = Mockito.mock(TestAction.class);
        TestAction action2 = Mockito.mock(TestAction.class);
        TestAction action3 = Mockito.mock(TestAction.class);

        reset(action1, action2, action3, success, error);

        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(action1);
        actionList.add(action2);
        actionList.add(action3);

        container.setActions(actionList);

        container.addSuccessAction(success);
        container.addErrorAction(error);

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
        Async container = new Async();

        TestAction action1 = Mockito.mock(TestAction.class);
        TestAction action2 = Mockito.mock(TestAction.class);
        TestAction action3 = Mockito.mock(TestAction.class);

        reset(action1, action2, action3, success, error);

        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(action1);
        actionList.add(new FailAction());
        actionList.add(action2);
        actionList.add(action3);

        container.setActions(actionList);

        container.addSuccessAction(success);
        container.addErrorAction(error);

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
        Async container = new Async();

        reset(action, success, error);

        doAnswer(invocation -> {
            Thread.sleep(500L);
            return null;
        }).when(action).execute(context);

        container.setActions(Collections.singletonList(action));

        container.execute(context);

        waitForDone(container, context, 100);
    }

    @Test
    public void testWaitForFinishError() throws Exception {
        Async container = new Async();

        reset(action, success, error);

        doThrow(new CitrusRuntimeException("FAILED!")).when(action).execute(context);

        container.setActions(Collections.singletonList(action));

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
                log.debug("Async action execution not finished yet ...");
            }
        }, 100, timeout / 10, TimeUnit.MILLISECONDS);

        done.get(timeout, TimeUnit.MILLISECONDS);
    }
}