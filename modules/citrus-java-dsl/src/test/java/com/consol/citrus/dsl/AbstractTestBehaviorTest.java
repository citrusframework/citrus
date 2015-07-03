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

package com.consol.citrus.dsl;

import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseMetaInfo;
import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.dsl.behavior.AbstractTestBehavior;
import com.consol.citrus.dsl.definition.MockDesigner;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class AbstractTestBehaviorTest extends AbstractTestNGUnitTest {

    @Test
    public void testBehaviorFrontPosition() {
        MockDesigner builder = new MockDesigner(applicationContext) {
            @Override
            public void configure() {
                applyBehavior(new FooBehavior());

                description("This is a Test");
                author("Christoph");
                status(TestCaseMetaInfo.Status.FINAL);

                echo("test");
            }
        };

        builder.execute();

        TestCase test = builder.build();
        Assert.assertEquals(test.getActions().size(), 2);
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
        MockDesigner builder = new MockDesigner(applicationContext) {
            @Override
            public void configure() {
                description("This is a Test");
                author("Christoph");
                status(TestCaseMetaInfo.Status.FINAL);

                echo("test");

                doFinally(
                    echo("finally")
                );

                applyBehavior(new AbstractTestBehavior() {
                    @Override
                    public void apply() {
                        echo("behavior");

                        doFinally(
                                echo("behaviorFinally")
                        );
                    }
                });
            }
        };

        builder.execute();

        TestCase test = builder.build();
        Assert.assertEquals(test.getActions().size(), 2);
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
    public void testApplyBehavior() {
        MockDesigner builder = new MockDesigner(applicationContext) {
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

        builder.execute();

        TestCase test = builder.build();
        Assert.assertEquals(test.getActions().size(), 3);
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
        MockDesigner builder = new MockDesigner(applicationContext) {
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

        builder.execute();

        TestCase test = builder.build();
        Assert.assertEquals(test.getActions().size(), 3);
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
