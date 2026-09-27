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

package org.citrusframework.testng.actions.dsl;

import java.util.List;

import org.citrusframework.base.DefaultTestCase;
import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.TestBehavior;
import org.citrusframework.TestCase;
import org.citrusframework.testng.UnitTestSupport;
import org.citrusframework.actions.ApplyTestBehaviorAction;
import org.citrusframework.actions.CreateVariablesAction;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.container.Sequence;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @since 2.3
 */
public class ApplyTestBehaviorTest extends UnitTestSupport {

    @Test
    public void testBehaviorFrontPosition() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(apply().behavior(new FooBehavior()));
        builder.$(echo("test"));

        Assert.assertEquals(context.getVariable("foo"), "test");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), ApplyTestBehaviorAction.class);
        assertFooBehavior(test.getActions().get(0));

        Assert.assertEquals(test.getActions().get(1).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(1)).getMessage(), "test");
    }

    @Test
    public void testBehaviorWithFinally() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(echo("test"));

        builder.$(doFinally().actions(
            echo("finally")
        ));

        builder.$(apply().behavior(runner -> {
            runner.run(echo("behavior"));

            runner.run(doFinally().actions(
                echo("behaviorFinally")
            ));
        }));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);

        Assert.assertEquals(test.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(0)).getMessage(), "test");

        Assert.assertEquals(test.getActions().get(1).getClass(), ApplyTestBehaviorAction.class);
        ApplyTestBehaviorAction behavior = (ApplyTestBehaviorAction) test.getActions().get(1);
        Assert.assertEquals(behavior.getExecutedActions().size(), 1);
        Assert.assertEquals(((EchoAction)behavior.getExecutedActions().get(0)).getMessage(), "behavior");

        Assert.assertTrue(test instanceof DefaultTestCase);
        List<TestAction> finalActions = ((DefaultTestCase)test).getFinalActions();
        Assert.assertEquals(finalActions.size(), 2);
        Assert.assertEquals(finalActions.get(0).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)finalActions.get(0)).getMessage(), "finally");

        Assert.assertEquals(finalActions.get(1).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)finalActions.get(1)).getMessage(), "behaviorFinally");
    }

    @Test
    public void testBehaviorInContainer() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(sequential().actions(
                    echo("before"),
                    builder.applyBehavior(runner -> runner.run(echo("behavior"))),
                    echo("after")
                ));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);

        Assert.assertEquals(test.getActions().get(0).getClass(), Sequence.class);
        Sequence sequence = (Sequence) test.getActions().get(0);
        Assert.assertEquals(sequence.getActionCount(), 3);

        Assert.assertEquals(sequence.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)sequence.getActions().get(0)).getMessage(), "before");

        Assert.assertEquals(sequence.getActions().get(1).getClass(), ApplyTestBehaviorAction.class);

        Assert.assertEquals(sequence.getActions().get(2).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)sequence.getActions().get(2)).getMessage(), "after");

        ApplyTestBehaviorAction behavior = (ApplyTestBehaviorAction) sequence.getExecutedActions().get(1);
        Assert.assertEquals(behavior.getExecutedActions().size(), 1);
        Assert.assertEquals(((EchoAction)behavior.getExecutedActions().get(0)).getMessage(), "behavior");
    }

    @Test
    public void testBehaviorInContainerWithFinally() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(doFinally().actions(
            echo("finally")
        ));

        builder.$(sequential().actions(
            echo("test"),

            builder.applyBehavior(runner -> {
                runner.run(echo("behavior"));

                runner.run(doFinally().actions(
                    echo("behaviorFinally")
                ));
            })
        ));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);

        Assert.assertEquals(test.getActions().get(0).getClass(), Sequence.class);
        Sequence sequence = (Sequence) test.getActions().get(0);
        Assert.assertEquals(sequence.getActionCount(), 2);

        Assert.assertEquals(sequence.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)sequence.getActions().get(0)).getMessage(), "test");

        Assert.assertEquals(sequence.getActions().get(1).getClass(), ApplyTestBehaviorAction.class);

        ApplyTestBehaviorAction behavior = (ApplyTestBehaviorAction) sequence.getExecutedActions().get(1);
        Assert.assertEquals(behavior.getExecutedActions().size(), 1);
        Assert.assertEquals(((EchoAction)behavior.getExecutedActions().get(0)).getMessage(), "behavior");

        Assert.assertTrue(test instanceof DefaultTestCase);
        List<TestAction> finalActions = ((DefaultTestCase)test).getFinalActions();
        Assert.assertEquals(finalActions.size(), 2);
        Assert.assertEquals(finalActions.get(0).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)finalActions.get(0)).getMessage(), "finally");

        Assert.assertEquals(finalActions.get(1).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)finalActions.get(1)).getMessage(), "behaviorFinally");
    }

    @Test
    public void testApplyBehavior() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.variable("test", "test");

        builder.$(apply().behavior(new FooBehavior()));

        builder.$(echo("test"));

        builder.$(apply().behavior(new BarBehavior()));

        Assert.assertNotNull(context.getVariable("test"));
        Assert.assertEquals(context.getVariable("test"), "test");
        Assert.assertEquals(context.getVariable("foo"), "test");
        Assert.assertEquals(context.getVariable("bar"), "test");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 3);
        Assert.assertEquals(test.getActions().get(0).getClass(), ApplyTestBehaviorAction.class);
        assertFooBehavior(test.getActions().get(0));

        Assert.assertEquals(test.getActions().get(1).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(1)).getMessage(), "test");

        Assert.assertEquals(test.getActions().get(2).getClass(), ApplyTestBehaviorAction.class);
        ApplyTestBehaviorAction bar = (ApplyTestBehaviorAction) test.getActions().get(2);
        Assert.assertEquals(bar.getName(), "bar behavior");
        Assert.assertEquals(bar.getExecutedActions().size(), 2);
        Assert.assertEquals(bar.getExecutedActions().get(0).getClass(), CreateVariablesAction.class);
        Assert.assertEquals(((EchoAction)bar.getExecutedActions().get(1)).getMessage(), "barBehavior");
    }

    @Test
    public void testApplyBehaviorTwice() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        FooBehavior behavior = new FooBehavior();
        builder.$(apply().behavior(behavior));

        builder.$(echo("test"));

        builder.$(apply().behavior(behavior));

        Assert.assertEquals(context.getVariable("foo"), "test");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 3);
        Assert.assertEquals(test.getActions().get(0).getClass(), ApplyTestBehaviorAction.class);
        assertFooBehavior(test.getActions().get(0));

        Assert.assertEquals(test.getActions().get(1).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(1)).getMessage(), "test");

        Assert.assertEquals(test.getActions().get(2).getClass(), ApplyTestBehaviorAction.class);
        assertFooBehavior(test.getActions().get(2));
    }

    @Test
    public void testApplyBehaviorInContainerTwice() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        FooBehavior behavior = new FooBehavior();

        builder.$(sequential().actions(
            builder.applyBehavior(behavior),
            echo("test"),
            builder.applyBehavior(behavior)
        ));

        Assert.assertEquals(context.getVariable("foo"), "test");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);

        Assert.assertEquals(test.getActions().get(0).getClass(), Sequence.class);
        Sequence sequence = (Sequence) test.getActions().get(0);
        Assert.assertEquals(sequence.getActionCount(), 3);

        Assert.assertEquals(sequence.getActions().get(0).getClass(), ApplyTestBehaviorAction.class);

        Assert.assertEquals(sequence.getActions().get(1).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)sequence.getActions().get(1)).getMessage(), "test");

        Assert.assertEquals(sequence.getActions().get(2).getClass(), ApplyTestBehaviorAction.class);

        assertFooBehavior(sequence.getExecutedActions().get(0));
        assertFooBehavior(sequence.getExecutedActions().get(2));
    }

    private static void assertFooBehavior(TestAction action) {
        ApplyTestBehaviorAction foo = (ApplyTestBehaviorAction) action;
        Assert.assertEquals(foo.getName(), "foo behavior");
        Assert.assertEquals(foo.getExecutedActions().size(), 2);
        Assert.assertEquals(foo.getExecutedActions().get(0).getClass(), CreateVariablesAction.class);
        Assert.assertEquals(((EchoAction)foo.getExecutedActions().get(1)).getMessage(), "fooBehavior");
    }

    private static class FooBehavior implements TestBehavior, TestActionSupport {
        public void apply(TestActionRunner runner) {
            runner.run(createVariables().variable("foo", "test"));

            runner.run(echo("fooBehavior"));
        }
    }

    private static class BarBehavior implements TestBehavior, TestActionSupport {
        public void apply(TestActionRunner runner) {
            runner.run(createVariables().variable("bar", "test"));

            runner.run(echo("barBehavior"));
        }
    }
}
