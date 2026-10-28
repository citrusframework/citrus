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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.citrusframework.TestBehavior;
import org.citrusframework.context.TestContext;
import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.container.Iterate;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApplyTestBehaviorScopeTest extends UnitTestSupport implements TestActionSupport {

    private final List<String> log = new ArrayList<>();

    private DefaultTestCaseRunner runner;

    private final TestBehavior createOrder = behavior -> {
        behavior.run(action(context -> log.add("customer=" + context.getVariable("customer"))));
        behavior.run(createVariables()
                .variable("orderId", "1001")
                .variable("customer", "overwritten")
                .variable("scratch", "temporary"));
    };

    @BeforeEach
    public void setupRunner() {
        log.clear();
        runner = new DefaultTestCaseRunner(context);
        runner.variable("customer", "alice");
    }

    @Test
    public void shouldShareVariablesByDefault() {
        runner.run(apply(createOrder));

        assertTrue(((ApplyTestBehaviorAction) runner.getTestCase().getActions().get(0)).isGlobalContext());
        assertEquals("1001", context.getVariable("orderId"));
        assertEquals("overwritten", context.getVariable("customer"));
    }

    @Test
    public void shouldIsolateVariables() {
        runner.run(apply(createOrder).isolated());

        assertEquals(List.of("customer=alice"), log);
        assertEquals("alice", context.getVariable("customer"));
        assertFalse(context.getVariables().containsKey("orderId"));
        assertFalse(context.getVariables().containsKey("scratch"));
    }

    @Test
    public void shouldIsolateVariablesWithGlobalContextDisabled() {
        runner.run(apply(createOrder).globalContext(false));

        assertFalse(context.getVariables().containsKey("orderId"));
        assertEquals("alice", context.getVariable("customer"));
    }

    @Test
    public void shouldPublishVariablesFromIsolatedScope() {
        runner.run(apply(createOrder).isolated().publish("orderId"));

        assertEquals("1001", context.getVariable("orderId"));
        assertEquals("alice", context.getVariable("customer"));
        assertFalse(context.getVariables().containsKey("scratch"));
    }

    @Test
    public void shouldPublishVariableExpression() {
        runner.run(apply(createOrder).isolated().publish("${orderId}", "customer"));

        assertEquals("1001", context.getVariable("orderId"));
        assertEquals("overwritten", context.getVariable("customer"));
    }

    @Test
    public void shouldPublishObjectValue() {
        Object order = new Object();

        runner.run(apply(behavior -> behavior.run(action(context -> context.setVariable("order", order))))
                .isolated()
                .publish("order"));

        assertEquals(order, context.getVariableObject("order"));
    }

    @Test
    public void shouldFailWhenPublishedVariableIsMissing() {
        TestCaseFailedException failure = assertThrows(TestCaseFailedException.class,
                () -> runner.run(apply(TestBehavior.named("create order", createOrder)).isolated().publish("invoiceId")));

        assertEquals("Test behavior 'create order' did not set published variable 'invoiceId'", failure.getMessage());
    }

    @Test
    public void shouldFailWhenPublishedVariableIsMissingInGlobalScope() {
        TestCaseFailedException failure = assertThrows(TestCaseFailedException.class,
                () -> runner.run(apply(TestBehavior.named("create order", createOrder)).publish("invoiceId")));

        assertEquals("Test behavior 'create order' did not set published variable 'invoiceId'", failure.getMessage());
    }

    @Test
    public void shouldPublishInGlobalScope() {
        runner.run(apply(createOrder).publish("orderId"));

        assertEquals("1001", context.getVariable("orderId"));
    }

    @Test
    public void shouldHandBackExceptionsOfIsolatedScope() {
        runner.run(apply(behavior -> behavior.run(action(scope -> scope.addException(new CitrusRuntimeException("forked failure")))))
                .isolated());

        assertEquals(1, context.getExceptions().size());
        assertEquals("forked failure", context.getExceptions().get(0).getMessage());
    }

    @Test
    public void shouldHandBackLateExceptionsOfIsolatedScopeOnCompletion() {
        AtomicReference<TestContext> isolatedScope = new AtomicReference<>();
        runner.run(apply(behavior -> behavior.run(action(isolatedScope::set))).isolated());
        ApplyTestBehaviorAction applied = (ApplyTestBehaviorAction) runner.getTestCase().getActions().get(0);

        isolatedScope.get().addException(new CitrusRuntimeException("late forked failure"));

        assertTrue(applied.isDone(context));
        assertEquals(1, context.getExceptions().size());
        assertEquals("late forked failure", context.getExceptions().get(0).getMessage());
    }

    @Test
    public void shouldRaisePendingExceptionBeforeIsolatedBehavior() {
        TestCaseFailedException failure = assertThrows(TestCaseFailedException.class,
                () -> runner.run(sequential().actions(
                        action(caller -> caller.addException(new CitrusRuntimeException("pending failure"))),
                        apply(createOrder).isolated()
                )));

        assertEquals("pending failure", failure.getMessage());
        assertTrue(log.isEmpty());
    }

    @Test
    public void shouldHandBackFinallyActionsOfIsolatedScopeWithoutRunner() {
        ApplyTestBehaviorAction applied = new ApplyTestBehaviorAction.Builder()
                .behavior(behavior -> behavior.run(doFinally().actions(echo("cleanup"))))
                .isolated()
                .build();

        applied.execute(context);

        assertEquals(1, context.getFinalActions().size());
        assertEquals("cleanup", ((EchoAction) context.getFinalActions().get(0).build()).getMessage());
    }

    @Test
    public void shouldRunWithRequiredVariables() {
        runner.run(apply(createOrder).requires("customer", "${customer}"));

        assertEquals(List.of("customer=alice"), log);
    }

    @Test
    public void shouldFailBeforeAnyActionWhenRequiredVariableIsMissing() {
        TestCaseFailedException failure = assertThrows(TestCaseFailedException.class,
                () -> runner.run(apply(TestBehavior.named("create order", createOrder)).requires("customer", "product")));

        assertEquals("Missing required variable 'product' for test behavior 'create order'", failure.getMessage());
        assertEquals(CitrusRuntimeException.class, failure.getCause().getClass());
        assertTrue(log.isEmpty());
        assertEquals(0, ((ApplyTestBehaviorAction) runner.getTestCase().getActions().get(0)).getExecutedActions().size());
        assertFalse(context.getVariables().containsKey("orderId"));
    }

    @Test
    public void shouldCheckRequiredVariablesInIsolatedScope() {
        assertThrows(TestCaseFailedException.class,
                () -> runner.run(apply(createOrder).isolated().requires("product")));

        assertTrue(log.isEmpty());
    }

    @Test
    public void shouldKeepNestedActionsPerIteration() {
        runner.run(iterate()
                .condition("i lt 4")
                .actions(apply(behavior -> behavior.run(echo("iteration ${i}")))));

        Iterate iterate = (Iterate) runner.getTestCase().getActions().get(0);
        assertEquals(3, iterate.getExecutedActions().size());
        for (int i = 0; i < 3; i++) {
            ApplyTestBehaviorAction applied = (ApplyTestBehaviorAction) iterate.getExecutedActions().get(i);
            assertEquals(1, applied.getExecutedActions().size());
            assertEquals(1, applied.getActionCount());
        }
    }

    @Test
    public void shouldRejectStaticNestedActions() {
        CitrusRuntimeException failure = assertThrows(CitrusRuntimeException.class,
                () -> new ApplyTestBehaviorAction.Builder().behavior(createOrder).actions(echo("static")).build());

        assertTrue(failure.getMessage().startsWith("Test behavior action does not accept nested actions"));
    }
}
