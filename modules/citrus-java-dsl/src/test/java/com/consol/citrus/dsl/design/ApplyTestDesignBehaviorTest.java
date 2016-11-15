/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.dsl.design;

import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseMetaInfo;
import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.container.Sequence;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class ApplyTestDesignBehaviorTest extends AbstractTestNGUnitTest {

    @Test
    public void testBehaviorFrontPosition() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                applyBehavior(new FooBehavior());

                description("This is a Test");
                author("Christoph");
                status(TestCaseMetaInfo.Status.FINAL);

                echo("test");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getDescription(), "This is a Test");
        Assert.assertEquals(test.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(test.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);

        Assert.assertEquals(test.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(0)).getMessage(), "fooBehavior");

        Assert.assertEquals(test.getActions().get(1).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(1)).getMessage(), "test");
    }

    @Test
    public void testBehaviorWithFinally() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                description("This is a Test");
                author("Christoph");
                status(TestCaseMetaInfo.Status.FINAL);

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

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getDescription(), "This is a Test");
        Assert.assertEquals(test.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(test.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);

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
    public void testBehaviorInContainer() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                sequential().actions(
                        echo("before"),
                        applyBehavior(new AbstractTestBehavior() {
                            @Override
                            public void apply() {
                                echo("behavior");
                            }
                        }),
                        echo("after")
                );
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);

        Assert.assertEquals(test.getActions().get(0).getClass(), Sequence.class);
        Sequence sequence = (Sequence) test.getActions().get(0);
        Assert.assertEquals(sequence.getActionCount(), 3);

        Assert.assertEquals(sequence.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)sequence.getActions().get(0)).getMessage(), "before");

        Assert.assertEquals(sequence.getActions().get(1).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)sequence.getActions().get(1)).getMessage(), "behavior");

        Assert.assertEquals(sequence.getActions().get(2).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)sequence.getActions().get(2)).getMessage(), "after");
    }

    @Test
    public void testBehaviorInContainerWithFinally() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                doFinally().actions(
                    echo("finally")
                );

                sequential().actions(
                        echo("test"),

                        applyBehavior(new AbstractTestBehavior() {
                            @Override
                            public void apply() {
                                echo("behavior");

                                doFinally().actions(
                                    echo("behaviorFinally")
                                );
                            }
                        })
                );
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);

        Assert.assertEquals(test.getActions().get(0).getClass(), Sequence.class);
        Sequence sequence = (Sequence) test.getActions().get(0);
        Assert.assertEquals(sequence.getActionCount(), 2);

        Assert.assertEquals(sequence.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)sequence.getActions().get(0)).getMessage(), "test");

        Assert.assertEquals(sequence.getActions().get(1).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)sequence.getActions().get(1)).getMessage(), "behavior");

        Assert.assertEquals(test.getFinalActions().size(), 2);
        Assert.assertEquals(test.getFinalActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getFinalActions().get(0)).getMessage(), "finally");

        Assert.assertEquals(test.getFinalActions().get(1).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getFinalActions().get(1)).getMessage(), "behaviorFinally");
    }

    @Test
    public void testApplyBehavior() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                description("This is a Test");
                author("Christoph");
                status(TestCaseMetaInfo.Status.FINAL);

                variable("test", "test");

                applyBehavior(new FooBehavior());

                echo("test");

                applyBehavior(new BarBehavior());
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 3);
        Assert.assertEquals(test.getDescription(), "This is a Test");
        Assert.assertEquals(test.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(test.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);

        Assert.assertEquals(test.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(0)).getMessage(), "fooBehavior");

        Assert.assertEquals(test.getActions().get(1).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(1)).getMessage(), "test");

        Assert.assertEquals(test.getActions().get(2).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(2)).getMessage(), "barBehavior");

        Assert.assertEquals(builder.getVariables().size(), 3);
        Assert.assertEquals(builder.getVariables().get("test"), "test");
        Assert.assertEquals(builder.getVariables().get("foo"), "test");
        Assert.assertEquals(builder.getVariables().get("bar"), "test");
    }

    @Test
    public void testApplyBehaviorTwice() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                description("This is a Test");
                author("Christoph");
                status(TestCaseMetaInfo.Status.FINAL);

                FooBehavior behavior = new FooBehavior();
                applyBehavior(behavior);

                echo("test");

                applyBehavior(behavior);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 3);
        Assert.assertEquals(test.getDescription(), "This is a Test");
        Assert.assertEquals(test.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(test.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);

        Assert.assertEquals(test.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(0)).getMessage(), "fooBehavior");

        Assert.assertEquals(test.getActions().get(1).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(1)).getMessage(), "test");

        Assert.assertEquals(test.getActions().get(2).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(2)).getMessage(), "fooBehavior");
    }

    @Test
    public void testApplyBehaviorInContainerTwice() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                FooBehavior behavior = new FooBehavior();

                sequential().actions(
                    applyBehavior(behavior),

                    echo("test"),

                    applyBehavior(behavior)
                );
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);

        Assert.assertEquals(test.getActions().get(0).getClass(), Sequence.class);
        Sequence sequence = (Sequence) test.getActions().get(0);
        Assert.assertEquals(sequence.getActionCount(), 3);

        Assert.assertEquals(sequence.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)sequence.getActions().get(0)).getMessage(), "fooBehavior");

        Assert.assertEquals(sequence.getActions().get(1).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)sequence.getActions().get(1)).getMessage(), "test");

        Assert.assertEquals(sequence.getActions().get(2).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)sequence.getActions().get(2)).getMessage(), "fooBehavior");

        Assert.assertEquals(builder.getVariables().size(), 1);
        Assert.assertEquals(builder.getVariables().get("foo"), "test");
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
