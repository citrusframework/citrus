/*
 * Copyright the original author or authors.
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

package org.citrusframework.condition;

import java.util.concurrent.atomic.AtomicInteger;

import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.actions.FailAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

/**
 * @since 2.7.6
 */
public class ActionConditionTest {

    private final TestContext context = Mockito.mock(TestContext.class);

    @Test
    public void isSatisfiedShouldSucceed() {
        ActionCondition testling = new ActionCondition(new EchoAction.Builder().build());

        Assert.assertTrue(testling.isSatisfied(context));
    }

    @Test
    public void isSatisfiedShouldFail() {
        ActionCondition testling = new ActionCondition(new FailAction.Builder().message("Fail!").build());

        when(context.replaceDynamicContentInString("Fail!")).thenReturn("Fail!");

        Assert.assertFalse(testling.isSatisfied(context));
    }

    @Test
    public void isSatisfiedShouldReturnFalseWithNullAction() {
        ActionCondition testling = new ActionCondition();

        Assert.assertFalse(testling.isSatisfied(context));
    }

    @Test
    public void shouldRetryAndSucceedAfterInitialFailures() {
        AtomicInteger executionCount = new AtomicInteger();

        ActionCondition testling = new ActionCondition(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                if (executionCount.incrementAndGet() <= 2) {
                    throw new CitrusRuntimeException("Not ready yet - attempt " + executionCount.get());
                }
            }
        });

        Assert.assertFalse(testling.isSatisfied(context));
        Assert.assertEquals(executionCount.get(), 1);
        Assert.assertNotNull(testling.getCaughtException());

        Assert.assertFalse(testling.isSatisfied(context));
        Assert.assertEquals(executionCount.get(), 2);
        Assert.assertNotNull(testling.getCaughtException());

        Assert.assertTrue(testling.isSatisfied(context));
        Assert.assertEquals(executionCount.get(), 3);
        Assert.assertNull(testling.getCaughtException());
    }

    @Test
    public void shouldClearCaughtExceptionOnSuccess() {
        AtomicInteger executionCount = new AtomicInteger();

        ActionCondition testling = new ActionCondition(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                if (executionCount.incrementAndGet() == 1) {
                    throw new CitrusRuntimeException("First call fails");
                }
            }
        });

        Assert.assertFalse(testling.isSatisfied(context));
        Assert.assertNotNull(testling.getCaughtException());
        Assert.assertTrue(testling.getCaughtException().getMessage().contains("First call fails"));

        Assert.assertTrue(testling.isSatisfied(context));
        Assert.assertNull(testling.getCaughtException());
    }

    @Test
    public void shouldUpdateCaughtExceptionOnEachFailure() {
        AtomicInteger executionCount = new AtomicInteger();

        ActionCondition testling = new ActionCondition(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                throw new CitrusRuntimeException("Failure #" + executionCount.incrementAndGet());
            }
        });

        Assert.assertFalse(testling.isSatisfied(context));
        Assert.assertTrue(testling.getCaughtException().getMessage().contains("Failure #1"));

        Assert.assertFalse(testling.isSatisfied(context));
        Assert.assertTrue(testling.getCaughtException().getMessage().contains("Failure #2"));

        Assert.assertFalse(testling.isSatisfied(context));
        Assert.assertTrue(testling.getCaughtException().getMessage().contains("Failure #3"));
    }

    @Test
    public void shouldProvideCorrectMessagesOnRetry() {
        AtomicInteger executionCount = new AtomicInteger();

        ActionCondition testling = new ActionCondition(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                if (executionCount.incrementAndGet() <= 1) {
                    throw new CitrusRuntimeException("Action not ready");
                }
            }
        });

        Assert.assertFalse(testling.isSatisfied(context));
        String errorMessage = testling.getErrorMessage(context);
        Assert.assertTrue(errorMessage.contains("did not perform as expected"));
        Assert.assertTrue(errorMessage.contains("Action not ready"));

        Assert.assertTrue(testling.isSatisfied(context));
        String successMessage = testling.getSuccessMessage(context);
        Assert.assertTrue(successMessage.contains("did perform as expected"));
    }

    @Test
    public void shouldReExecuteActionOnEachIsSatisfiedCall() {
        AtomicInteger executionCount = new AtomicInteger();

        ActionCondition testling = new ActionCondition(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                executionCount.incrementAndGet();
            }
        });

        Assert.assertTrue(testling.isSatisfied(context));
        Assert.assertEquals(executionCount.get(), 1);

        Assert.assertTrue(testling.isSatisfied(context));
        Assert.assertEquals(executionCount.get(), 2);

        Assert.assertTrue(testling.isSatisfied(context));
        Assert.assertEquals(executionCount.get(), 3);
    }
}
