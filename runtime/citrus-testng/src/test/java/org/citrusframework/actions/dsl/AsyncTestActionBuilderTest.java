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

package org.citrusframework.actions.dsl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.actions.SleepAction;
import org.citrusframework.container.Async;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.actions.FailAction.Builder.fail;
import static org.citrusframework.actions.SleepAction.Builder.sleep;
import static org.citrusframework.container.Async.Builder.async;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class AsyncTestActionBuilderTest extends UnitTestSupport {

    @Test
    public void testAsyncBuilder() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.variable("var", "foo");

        builder.$(async()
            .actions(
                echo("${var}"),
                sleep().milliseconds(100L)
            ));

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), Async.class);
        assertEquals(test.getActions().get(0).getName(), "async");

        Async container = (Async)test.getActions().get(0);
        assertEquals(container.getActionCount(), 2);
        assertEquals(container.getActions().get(0).getClass(), EchoAction.class);
        assertEquals(container.getActions().get(1).getClass(), SleepAction.class);
    }

    @Test
    public void testAsyncBuilderWithAnonymousAction() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.variable("var", "foo");

        builder.$(async()
            .actions(
                echo("${var}"),
                () -> new AbstractTestAction() {
                    @Override
                    public void doExecute(TestContext context) {
                        context.setVariable("anonymous", "anonymous");
                    }
                },
                sleep().milliseconds(100L),
                () -> new AbstractTestAction() {
                    @Override
                    public void doExecute(TestContext context) {
                        context.getVariable("anonymous");
                    }
                }
            ));

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), Async.class);
        assertEquals(test.getActions().get(0).getName(), "async");

        Async container = (Async)test.getActions().get(0);
        assertEquals(container.getActionCount(), 4);
        assertEquals(container.getActions().get(0).getClass(), EchoAction.class);
        assertTrue(container.getActions().get(1).getClass().isAnonymousClass());
        assertEquals(container.getActions().get(2).getClass(), SleepAction.class);
        assertTrue(container.getActions().get(3).getClass().isAnonymousClass());
    }

    @Test(expectedExceptions = TestCaseFailedException.class)
    public void testAsyncError() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.variable("var", "foo");

        builder.$(async()
            .actions(
                fail("Something went wrong!")
            ));

        builder.stop();
    }

    @Test(expectedExceptions = TestCaseFailedException.class)
    public void testAsyncErrorActions() throws Exception {
        CompletableFuture<Boolean> successActionPerformed = new CompletableFuture<>();
        CompletableFuture<Boolean> errorActionPerformed = new CompletableFuture<>();

        try {
            DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
            builder.variable("var", "foo");

            builder.$(async()
                .actions(
                    sleep().milliseconds(100L),
                    fail("Something went wrong!")
                ).successAction(new AbstractTestAction() {
                    @Override
                    public void doExecute(TestContext context) {
                        successActionPerformed.complete(true);
                    }
                }).errorAction(new AbstractTestAction() {
                    @Override
                    public void doExecute(TestContext context) {
                        errorActionPerformed.complete(true);
                    }
                }));

            builder.stop();
        } finally {
            assertFalse(successActionPerformed.isDone());
            errorActionPerformed.get(1000L, TimeUnit.MILLISECONDS);
        }
    }

    @Test
    public void testAsyncSuccessActions() throws Exception {
        CompletableFuture<Boolean> successActionPerformed = new CompletableFuture<>();
        CompletableFuture<Boolean> errorActionPerformed = new CompletableFuture<>();

        try {
            DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
            builder.variable("var", "foo");

            builder.$(async()
                .actions(
                    sleep().milliseconds(100L),
                    echo("Do something!")
                ).successAction(new AbstractTestAction() {
                    @Override
                    public void doExecute(TestContext context) {
                        successActionPerformed.complete(true);
                    }
                }).errorAction(new AbstractTestAction() {
                    @Override
                    public void doExecute(TestContext context) {
                        errorActionPerformed.complete(true);
                    }
                }));
        } finally {
            assertFalse(errorActionPerformed.isDone());
            successActionPerformed.get(1000L, TimeUnit.MILLISECONDS);
            Assert.assertEquals(context.getExceptions().size(), 0L);
        }

    }
}
