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

package com.consol.citrus.dsl.runner;

import com.consol.citrus.TestCase;
import com.consol.citrus.actions.*;
import com.consol.citrus.container.Async;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.TestCaseFailedException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.*;

import static org.testng.Assert.*;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class AsyncTestRunnerTest extends AbstractTestNGUnitTest {

    @Test
    public void testAsyncBuilder() {
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                variable("var", "foo");

                async()
                    .actions(
                        echo("${var}"),
                        sleep(100L)
                    );
            }
        };

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
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                variable("var", "foo");

                async()
                    .actions(
                        echo("${var}"),
                        new AbstractTestAction() {
                            @Override
                            public void doExecute(TestContext context) {
                                context.setVariable("anonymous", "anonymous");
                            }
                        },
                        sleep(100L),
                        new AbstractTestAction() {
                            @Override
                            public void doExecute(TestContext context) {
                                context.getVariable("anonymous");
                            }
                        }
                    );
            }
        };

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
        new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                variable("var", "foo");

                async()
                    .actions(
                        fail("Something went wrong!")
                    );
            }
        };
    }

    @Test(expectedExceptions = TestCaseFailedException.class)
    public void testAsyncErrorActions() throws Exception {
        CompletableFuture<Boolean> successActionPerformed = new CompletableFuture<>();
        CompletableFuture<Boolean> errorActionPerformed = new CompletableFuture<>();

        try {
            new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
                @Override
                public void execute() {
                    variable("var", "foo");

                    async()
                        .actions(
                            sleep(100),
                            fail("Something went wrong!")
                        ).addSuccessAction(new AbstractTestAction() {
                            @Override
                            public void doExecute(TestContext context) {
                                successActionPerformed.complete(true);
                            }
                        }).addErrorAction(new AbstractTestAction() {
                            @Override
                            public void doExecute(TestContext context) {
                                errorActionPerformed.complete(true);
                            }
                        });
                }
            };
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
            new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
                @Override
                public void execute() {
                    variable("var", "foo");

                    async()
                        .actions(
                            sleep(100),
                            echo("Do something!")
                        ).addSuccessAction(new AbstractTestAction() {
                            @Override
                            public void doExecute(TestContext context) {
                                successActionPerformed.complete(true);
                            }
                        }).addErrorAction(new AbstractTestAction() {
                            @Override
                            public void doExecute(TestContext context) {
                                errorActionPerformed.complete(true);
                            }
                        });
                }
            };
        } finally {
            assertFalse(errorActionPerformed.isDone());
            successActionPerformed.get(1000L, TimeUnit.MILLISECONDS);
            Assert.assertEquals(context.getExceptions().size(), 0L);
        }

    }
}
