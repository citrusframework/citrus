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
import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class ApplyTestRunnerBehaviorTest extends AbstractTestNGUnitTest {

    @Test
    public void testBehaviorFrontPosition() {
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                applyBehavior(new FooBehavior());

                echo("test");
            }
        };

        TestContext context = builder.createTestContext();
        Assert.assertEquals(context.getVariable("foo"), "test");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(0)).getMessage(), "fooBehavior");

        Assert.assertEquals(test.getActions().get(1).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(1)).getMessage(), "test");
    }

    @Test
    public void testBehaviorWithFinally() {
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                echo("test");

                doFinally().actions(
                    echo("finally")
                );

                applyBehavior(new AbstractTestBehavior() {
                    @Override
                    public void apply() {
                        echo("behavior");

                        doFinally().actions(
                            echo("behaviorFinally")
                        );
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);

        Assert.assertEquals(test.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(0)).getMessage(), "test");

        Assert.assertEquals(test.getActions().get(1).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(1)).getMessage(), "behavior");

        Assert.assertEquals(test.getFinalActions().size(), 2);
        Assert.assertEquals(test.getFinalActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getFinalActions().get(0)).getMessage(), "finally");

        Assert.assertEquals(test.getFinalActions().get(1).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getFinalActions().get(1)).getMessage(), "behaviorFinally");
    }

    @Test
    public void testApplyBehavior() {
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                variable("test", "test");

                applyBehavior(new FooBehavior());

                echo("test");

                applyBehavior(new BarBehavior());
            }
        };

        TestContext context = builder.createTestContext();
        Assert.assertNotNull(context.getVariable("test"));
        Assert.assertEquals(context.getVariable("test"), "test");
        Assert.assertEquals(context.getVariable("foo"), "test");
        Assert.assertEquals(context.getVariable("bar"), "test");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 3);
        Assert.assertEquals(test.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(0)).getMessage(), "fooBehavior");

        Assert.assertEquals(test.getActions().get(1).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(1)).getMessage(), "test");

        Assert.assertEquals(test.getActions().get(2).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(2)).getMessage(), "barBehavior");
    }

    @Test
    public void testApplyBehaviorTwice() {
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                FooBehavior behavior = new FooBehavior();
                applyBehavior(behavior);

                echo("test");

                applyBehavior(behavior);
            }
        };

        TestContext context = builder.createTestContext();
        Assert.assertEquals(context.getVariable("foo"), "test");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 3);
        Assert.assertEquals(test.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(0)).getMessage(), "fooBehavior");

        Assert.assertEquals(test.getActions().get(1).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(1)).getMessage(), "test");

        Assert.assertEquals(test.getActions().get(2).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(2)).getMessage(), "fooBehavior");
    }

    private static class FooBehavior extends AbstractTestBehavior {
        public void apply() {
            variable("foo", "test");

            echo("fooBehavior");
        }
    }

    private static class BarBehavior extends AbstractTestBehavior {
        public void apply() {
            variable("bar", "test");

            echo("barBehavior");
        }
    }
}
