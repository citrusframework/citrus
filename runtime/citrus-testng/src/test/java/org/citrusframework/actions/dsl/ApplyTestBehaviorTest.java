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

import java.util.List;

import org.citrusframework.DefaultTestCase;
import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestBehavior;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.ApplyTestBehaviorAction;
import org.citrusframework.actions.CreateVariablesAction;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.container.Sequence;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.ApplyTestBehaviorAction.Builder.apply;
import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariables;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.container.FinallySequence.Builder.doFinally;
import static org.citrusframework.container.Sequence.Builder.sequential;

/**
 * @author Christoph Deppisch
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
        Assert.assertEquals(test.getActionCount(), 4);
        Assert.assertEquals(test.getActions().get(0).getClass(), ApplyTestBehaviorAction.class);

        Assert.assertEquals(test.getActions().get(1).getClass(), CreateVariablesAction.class);

        Assert.assertEquals(test.getActions().get(2).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(2)).getMessage(), "fooBehavior");

        Assert.assertEquals(test.getActions().get(3).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(3)).getMessage(), "test");
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
        Assert.assertEquals(test.getActionCount(), 3);

        Assert.assertEquals(test.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(0)).getMessage(), "test");

        Assert.assertEquals(test.getActions().get(1).getClass(), ApplyTestBehaviorAction.class);

        Assert.assertEquals(test.getActions().get(2).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(2)).getMessage(), "behavior");

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
        Assert.assertEquals(test.getActionCount(), 2);

        Assert.assertEquals(test.getActions().get(0).getClass(), Sequence.class);
        Sequence sequence = (Sequence) test.getActions().get(0);
        Assert.assertEquals(sequence.getActionCount(), 3);


        Assert.assertEquals(sequence.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)sequence.getActions().get(0)).getMessage(), "before");

        Assert.assertEquals(sequence.getActions().get(1).getClass(), ApplyTestBehaviorAction.class);

        Assert.assertEquals(sequence.getActions().get(2).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)sequence.getActions().get(2)).getMessage(), "after");

        Assert.assertEquals(test.getActions().get(1).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(1)).getMessage(), "behavior");
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
        Assert.assertEquals(test.getActionCount(), 2);

        Assert.assertEquals(test.getActions().get(0).getClass(), Sequence.class);
        Sequence sequence = (Sequence) test.getActions().get(0);
        Assert.assertEquals(sequence.getActionCount(), 2);

        Assert.assertEquals(sequence.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)sequence.getActions().get(0)).getMessage(), "test");

        Assert.assertEquals(sequence.getActions().get(1).getClass(), ApplyTestBehaviorAction.class);

        Assert.assertEquals(test.getActions().get(1).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(1)).getMessage(), "behavior");

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
        Assert.assertEquals(test.getActionCount(), 7);
        Assert.assertEquals(test.getActions().get(0).getClass(), ApplyTestBehaviorAction.class);

        Assert.assertEquals(test.getActions().get(1).getClass(), CreateVariablesAction.class);

        Assert.assertEquals(test.getActions().get(2).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(2)).getMessage(), "fooBehavior");

        Assert.assertEquals(test.getActions().get(3).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(3)).getMessage(), "test");

        Assert.assertEquals(test.getActions().get(4).getClass(), ApplyTestBehaviorAction.class);

        Assert.assertEquals(test.getActions().get(5).getClass(), CreateVariablesAction.class);

        Assert.assertEquals(test.getActions().get(6).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(6)).getMessage(), "barBehavior");
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
        Assert.assertEquals(test.getActionCount(), 7);
        Assert.assertEquals(test.getActions().get(0).getClass(), ApplyTestBehaviorAction.class);

        Assert.assertEquals(test.getActions().get(1).getClass(), CreateVariablesAction.class);
        Assert.assertEquals(test.getActions().get(2).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(2)).getMessage(), "fooBehavior");

        Assert.assertEquals(test.getActions().get(3).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(3)).getMessage(), "test");

        Assert.assertEquals(test.getActions().get(4).getClass(), ApplyTestBehaviorAction.class);

        Assert.assertEquals(test.getActions().get(5).getClass(), CreateVariablesAction.class);
        Assert.assertEquals(test.getActions().get(6).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(6)).getMessage(), "fooBehavior");
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
        Assert.assertEquals(test.getActionCount(), 5);

        Assert.assertEquals(test.getActions().get(0).getClass(), Sequence.class);
        Sequence sequence = (Sequence) test.getActions().get(0);
        Assert.assertEquals(sequence.getActionCount(), 3);

        Assert.assertEquals(sequence.getActions().get(0).getClass(), ApplyTestBehaviorAction.class);

        Assert.assertEquals(sequence.getActions().get(1).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)sequence.getActions().get(1)).getMessage(), "test");

        Assert.assertEquals(sequence.getActions().get(2).getClass(), ApplyTestBehaviorAction.class);

        Assert.assertEquals(test.getActions().get(1).getClass(), CreateVariablesAction.class);
        Assert.assertEquals(test.getActions().get(2).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(2)).getMessage(), "fooBehavior");

        Assert.assertEquals(test.getActions().get(3).getClass(), CreateVariablesAction.class);
        Assert.assertEquals(test.getActions().get(4).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)test.getActions().get(4)).getMessage(), "fooBehavior");
    }

    private static class FooBehavior implements TestBehavior {
        public void apply(TestActionRunner runner) {
            runner.run(createVariables().variable("foo", "test"));

            runner.run(echo("fooBehavior"));
        }
    }

    private static class BarBehavior implements TestBehavior {
        public void apply(TestActionRunner runner) {
            runner.run(createVariables().variable("bar", "test"));

            runner.run(echo("barBehavior"));
        }
    }
}
