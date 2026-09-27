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

import org.citrusframework.TestBehavior;
import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.container.Iterate;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

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

    @BeforeMethod
    public void setupRunner() {
        log.clear();
        runner = new DefaultTestCaseRunner(context);
        runner.variable("customer", "alice");
    }

    @Test
    public void shouldShareVariablesByDefault() {
        runner.run(apply(createOrder));

        assertTrue(((ApplyTestBehaviorAction) runner.getTestCase().getActions().get(0)).isGlobalContext());
        assertEquals(context.getVariable("orderId"), "1001");
        assertEquals(context.getVariable("customer"), "overwritten");
    }

    @Test
    public void shouldIsolateVariables() {
        runner.run(apply(createOrder).isolated());

        assertEquals(log, List.of("customer=alice"));
        assertEquals(context.getVariable("customer"), "alice");
        assertFalse(context.getVariables().containsKey("orderId"));
        assertFalse(context.getVariables().containsKey("scratch"));
    }

    @Test
    public void shouldIsolateVariablesWithGlobalContextDisabled() {
        runner.run(apply(createOrder).globalContext(false));

        assertFalse(context.getVariables().containsKey("orderId"));
        assertEquals(context.getVariable("customer"), "alice");
    }

    @Test
    public void shouldPublishVariablesFromIsolatedScope() {
        runner.run(apply(createOrder).isolated().publish("orderId"));

        assertEquals(context.getVariable("orderId"), "1001");
        assertEquals(context.getVariable("customer"), "alice");
        assertFalse(context.getVariables().containsKey("scratch"));
    }

    @Test
    public void shouldPublishVariableExpression() {
        runner.run(apply(createOrder).isolated().publish("${orderId}", "customer"));

        assertEquals(context.getVariable("orderId"), "1001");
        assertEquals(context.getVariable("customer"), "overwritten");
    }

    @Test
    public void shouldPublishObjectValue() {
        Object order = new Object();

        runner.run(apply(behavior -> behavior.run(action(context -> context.setVariable("order", order))))
                .isolated()
                .publish("order"));

        assertEquals(context.getVariableObject("order"), order);
    }

    @Test
    public void shouldFailWhenPublishedVariableIsMissing() {
        TestCaseFailedException failure = expectThrows(TestCaseFailedException.class,
                () -> runner.run(apply(TestBehavior.named("create order", createOrder)).isolated().publish("invoiceId")));

        assertEquals(failure.getMessage(), "Test behavior 'create order' did not set published variable 'invoiceId'");
    }

    @Test
    public void shouldRunWithRequiredVariables() {
        runner.run(apply(createOrder).requires("customer", "${customer}"));

        assertEquals(log, List.of("customer=alice"));
    }

    @Test
    public void shouldFailBeforeAnyActionWhenRequiredVariableIsMissing() {
        TestCaseFailedException failure = expectThrows(TestCaseFailedException.class,
                () -> runner.run(apply(TestBehavior.named("create order", createOrder)).requires("customer", "product")));

        assertEquals(failure.getMessage(), "Missing required variable 'product' for test behavior 'create order'");
        assertEquals(failure.getCause().getClass(), CitrusRuntimeException.class);
        assertTrue(log.isEmpty());
        assertEquals(((ApplyTestBehaviorAction) runner.getTestCase().getActions().get(0)).getExecutedActions().size(), 0);
        assertFalse(context.getVariables().containsKey("orderId"));
    }

    @Test
    public void shouldCheckRequiredVariablesInIsolatedScope() {
        expectThrows(TestCaseFailedException.class,
                () -> runner.run(apply(createOrder).isolated().requires("product")));

        assertTrue(log.isEmpty());
    }

    @Test
    public void shouldKeepNestedActionsPerIteration() {
        runner.run(iterate()
                .condition("i lt 4")
                .actions(apply(behavior -> behavior.run(echo("iteration ${i}")))));

        Iterate iterate = (Iterate) runner.getTestCase().getActions().get(0);
        assertEquals(iterate.getExecutedActions().size(), 3);
        for (int i = 0; i < 3; i++) {
            ApplyTestBehaviorAction applied = (ApplyTestBehaviorAction) iterate.getExecutedActions().get(i);
            assertEquals(applied.getExecutedActions().size(), 1);
            assertEquals(applied.getActionCount(), 1);
        }
    }

    @Test
    public void shouldRejectStaticNestedActions() {
        CitrusRuntimeException failure = expectThrows(CitrusRuntimeException.class,
                () -> new ApplyTestBehaviorAction.Builder().behavior(createOrder).actions(echo("static")).build());

        assertTrue(failure.getMessage().startsWith("Test behavior action does not accept nested actions"));
    }
}
