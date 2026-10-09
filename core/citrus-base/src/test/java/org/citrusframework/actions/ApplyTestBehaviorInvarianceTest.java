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

import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestBehavior;
import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

/**
 * Asserts the semantics of applying a test behavior: execution order, variable visibility and timing,
 * exception propagation and finally action placement. Nesting the behavior's actions under the applied
 * action changes the test case structure only, so these assertions hold before and after that change.
 */
public class ApplyTestBehaviorInvarianceTest extends UnitTestSupport implements TestActionSupport {

    private final List<String> log = new ArrayList<>();

    private DefaultTestCaseRunner runner;

    @BeforeMethod
    public void setupRunner() {
        log.clear();
        runner = new DefaultTestCaseRunner(context);
    }

    @Test
    public void shouldKeepExecutionOrder() {
        runner.run(record("before"));
        runner.run(apply(behavior -> {
            behavior.run(record("first"));
            behavior.run(record("second"));
        }));
        runner.run(record("after"));

        assertEquals(log, List.of("before", "first", "second", "after"));
    }

    @Test
    public void shouldKeepExecutionOrderInContainer() {
        runner.run(sequential().actions(
                record("before"),
                runner.applyBehavior(behavior -> behavior.run(record("inside"))),
                record("after")
        ));

        assertEquals(log, List.of("before", "inside", "after"));
    }

    @Test
    public void shouldKeepExecutionOrderOfNestedBehaviors() {
        TestBehavior inner = behavior -> behavior.run(record("inner"));

        runner.run(apply(behavior -> {
            behavior.run(record("outer start"));
            behavior.run(apply(inner));
            behavior.run(record("outer end"));
        }));

        assertEquals(log, List.of("outer start", "inner", "outer end"));
    }

    @Test
    public void shouldKeepVariableVisibility() {
        runner.variable("caller", "visible");

        runner.run(apply(behavior -> {
            behavior.run(recordVariable("caller"));
            behavior.run(createVariables().variable("fromBehavior", "published"));
        }));
        runner.run(recordVariable("fromBehavior"));

        assertEquals(log, List.of("caller=visible", "fromBehavior=published"));
        assertEquals(context.getVariable("fromBehavior"), "published");
    }

    @Test
    public void shouldKeepVariableTiming() {
        runner.run(apply(behavior -> {
            behavior.run(createVariables().variable("counter", "1"));
            behavior.run(recordVariable("counter"));
            behavior.run(createVariables().variable("counter", "2"));
            behavior.run(recordVariable("counter"));
        }));
        runner.run(recordVariable("counter"));

        assertEquals(log, List.of("counter=1", "counter=2", "counter=2"));
    }

    @Test
    public void shouldKeepVariableOverwriteOfCaller() {
        runner.variable("shared", "caller");

        runner.run(apply(behavior -> behavior.run(createVariables().variable("shared", "behavior"))));

        assertEquals(context.getVariable("shared"), "behavior");
    }

    @Test
    public void shouldKeepExceptionPropagation() {
        TestCaseFailedException failure = expectThrows(TestCaseFailedException.class, () -> runner.run(apply(behavior -> {
            behavior.run(record("before failure"));
            behavior.run(fail("boom"));
            behavior.run(record("never"));
        })));

        assertEquals(failure.getMessage(), "boom");
        assertEquals(causeChain(failure), List.of(TestCaseFailedException.class, TestCaseFailedException.class, CitrusRuntimeException.class));
        assertEquals(log, List.of("before failure"));
    }

    @Test
    public void shouldKeepExceptionPropagationInContainer() {
        TestCaseFailedException failure = expectThrows(TestCaseFailedException.class, () -> runner.run(sequential().actions(
                runner.applyBehavior(behavior -> behavior.run(fail("boom"))),
                record("never")
        )));

        assertEquals(failure.getMessage(), "boom");
        assertEquals(causeChain(failure), List.of(TestCaseFailedException.class, TestCaseFailedException.class, CitrusRuntimeException.class));
        assertEquals(log, List.of());
    }

    @Test
    public void shouldKeepExceptionVisibleToBehavior() {
        runner.run(apply(behavior -> {
            try {
                behavior.run(fail("boom"));
            } catch (TestCaseFailedException e) {
                log.add("caught " + e.getMessage());
            }

            behavior.run(record("continued"));
        }));

        assertEquals(log, List.of("caught boom", "continued"));
    }

    @Test
    public void shouldKeepFinallyPlacement() {
        runner.start();
        runner.run(doFinally().actions(record("test finally")));
        runner.run(apply(behavior -> {
            behavior.run(doFinally().actions(record("behavior finally")));
            behavior.run(record("behavior"));
        }));
        runner.run(record("after"));
        runner.stop();

        assertEquals(log, List.of("behavior", "after", "test finally", "behavior finally"));
    }

    @Test
    public void shouldKeepFinallyPlacementInContainer() {
        runner.start();
        runner.run(sequential().actions(
                runner.applyBehavior(behavior -> behavior.run(doFinally().actions(record("behavior finally")))),
                record("inside")
        ));
        runner.run(record("after"));
        runner.stop();

        assertEquals(log, List.of("inside", "after", "behavior finally"));
    }

    private TestActionBuilder<?> record(String entry) {
        return action(context -> log.add(entry));
    }

    private TestActionBuilder<?> recordVariable(String name) {
        return action(context -> log.add(name + "=" + context.getVariable(name)));
    }

    private static List<Class<?>> causeChain(Throwable failure) {
        List<Class<?>> chain = new ArrayList<>();
        for (Throwable current = failure; current != null; current = current.getCause()) {
            chain.add(current.getClass());
        }
        return chain;
    }
}
