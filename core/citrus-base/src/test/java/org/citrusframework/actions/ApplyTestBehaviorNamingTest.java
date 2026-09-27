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

package org.citrusframework.actions;

import java.util.List;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestBehavior;
import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.container.Sequence;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.expectThrows;

public class ApplyTestBehaviorNamingTest extends UnitTestSupport implements TestActionSupport {

    private DefaultTestCaseRunner runner;

    @BeforeMethod
    public void setupRunner() {
        runner = new DefaultTestCaseRunner(context);
    }

    @Test
    public void shouldNameActionAfterNamedClass() {
        runner.run(apply(new PlaceAnOrder()));

        assertEquals(appliedAction(0).getName(), "place an order");
    }

    @Test
    public void shouldNameActionAfterRecord() {
        runner.run(apply(new SayHello("Hello")));

        assertEquals(appliedAction(0).getName(), "say hello");
    }

    @Test
    public void shouldNameLambdaGenerically() {
        runner.run(apply(behavior -> behavior.run(echo("lambda"))));

        assertEquals(appliedAction(0).getName(), "behavior");
    }

    @Test
    public void shouldUseExplicitBehaviorName() {
        runner.run(apply(TestBehavior.named("sign in", behavior -> behavior.run(echo("signed in")))));

        assertEquals(appliedAction(0).getName(), "sign in");
    }

    @Test
    public void shouldPreferActionNameOverBehaviorName() {
        runner.run(apply(new PlaceAnOrder()).name("order SKU-1"));

        assertEquals(appliedAction(0).getName(), "order SKU-1");
    }

    @Test
    public void shouldNameInContainer() {
        runner.run(sequential().actions(apply(new PlaceAnOrder())));

        Sequence sequence = (Sequence) runner.getTestCase().getActions().get(0);
        assertEquals(sequence.getExecutedActions().get(0).getName(), "place an order");
    }

    @Test
    public void shouldKeepDefaultNameWithoutBehavior() {
        ApplyTestBehaviorAction action = new ApplyTestBehaviorAction.Builder().build();

        assertEquals(action.getName(), "apply-behavior");
        CitrusRuntimeException failure = expectThrows(CitrusRuntimeException.class, () -> action.execute(context));
        assertEquals(failure.getMessage(), "Missing test behavior to apply");
    }

    @Test
    public void shouldComposeWithAndThen() {
        TestBehavior signIn = TestBehavior.named("sign in", behavior -> behavior.run(echo("signed in")));

        runner.run(apply(signIn.andThen(new PlaceAnOrder())));

        ApplyTestBehaviorAction composite = appliedAction(0);
        assertEquals(composite.getName(), "sign in then place an order");
        assertEquals(runner.getTestCase().getActionCount(), 1);
        assertEchoMessages(composite.getExecutedActions(), "signed in", "order placed");
    }

    @Test
    public void shouldComposeList() {
        TestBehavior signIn = TestBehavior.named("sign in", behavior -> behavior.run(echo("signed in")));
        TestBehavior signOut = TestBehavior.named("sign out", behavior -> behavior.run(echo("signed out")));

        runner.run(apply(List.of(signIn, new PlaceAnOrder(), signOut)));

        ApplyTestBehaviorAction composite = appliedAction(0);
        assertEquals(composite.getName(), "sign in then place an order then sign out");
        assertEchoMessages(composite.getExecutedActions(), "signed in", "order placed", "signed out");
    }

    @Test
    public void shouldComposeSingletonList() {
        runner.run(apply(List.of(new PlaceAnOrder())));

        assertEquals(appliedAction(0).getName(), "place an order");
    }

    @Test
    public void shouldRejectEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> apply(List.<TestBehavior>of()));
    }

    @Test
    public void shouldNestBehaviorInBehavior() {
        runner.run(apply(TestBehavior.named("check out", behavior -> {
            behavior.run(apply(new PlaceAnOrder()));
            behavior.run(echo("paid"));
        })));

        ApplyTestBehaviorAction checkOut = appliedAction(0);
        assertEquals(checkOut.getExecutedActions().size(), 2);
        ApplyTestBehaviorAction placeAnOrder = (ApplyTestBehaviorAction) checkOut.getExecutedActions().get(0);
        assertEquals(placeAnOrder.getName(), "place an order");
        assertEchoMessages(placeAnOrder.getExecutedActions(), "order placed");
    }

    private ApplyTestBehaviorAction appliedAction(int index) {
        return (ApplyTestBehaviorAction) runner.getTestCase().getActions().get(index);
    }

    private static void assertEchoMessages(List<TestAction> actions, String... messages) {
        assertEquals(actions.stream().map(action -> ((EchoAction) action).getMessage()).toList(), List.of(messages));
    }

    private static class PlaceAnOrder implements TestBehavior, TestActionSupport {
        @Override
        public void apply(TestActionRunner runner) {
            runner.run(echo("order placed"));
        }
    }

    private record SayHello(String greeting) implements TestBehavior, TestActionSupport {
        @Override
        public void apply(TestActionRunner runner) {
            runner.run(echo(greeting));
        }
    }
}
