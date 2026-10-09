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

package org.citrusframework.validation.assertj;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.citrusframework.TestAction;
import org.citrusframework.actions.FailAction;
import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.container.Conditional;
import org.citrusframework.container.Iterate;
import org.citrusframework.container.RepeatOnErrorUntilTrue;
import org.citrusframework.container.RepeatUntilTrue;
import org.citrusframework.container.Wait;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.validation.assertj.AssertJ.conditionOf;

/**
 * AssertJ conditions in the containers that evaluate conditions: iterate, repeat, repeat-on-error, conditional and wait.
 */
public class ContainerConditionTest extends UnitTestSupport implements TestActionSupport {

    @Test
    public void shouldIterateWhileAssertionHolds() {
        AtomicInteger executions = new AtomicInteger();

        new Iterate.Builder()
                .condition(conditionOf(i -> assertThat(i).isLessThan(3)))
                .index("i")
                .actions(counting(executions))
                .build()
                .execute(context);

        assertThat(executions).hasValue(2);
        assertThat(context.getVariable("i")).isEqualTo("2");
    }

    @Test
    public void shouldRepeatUntilVariableReachesValue() {
        context.setVariable("count", "0");

        new RepeatUntilTrue.Builder()
                .condition(conditionOf(Integer.class, "${count}", c -> assertThat(c).isEqualTo(3)))
                .index("i")
                .actions(context -> context.setVariable("count", String.valueOf(Integer.parseInt(context.getVariable("count")) + 1)))
                .build()
                .execute(context);

        assertThat(context.getVariable("count")).isEqualTo("3");
    }

    @Test
    public void shouldRepeatOnErrorUntilAssertionHolds() {
        AtomicInteger executions = new AtomicInteger();

        RepeatOnErrorUntilTrue repeat = new RepeatOnErrorUntilTrue.Builder()
                .condition(conditionOf(i -> assertThat(i).isEqualTo(3)))
                .index("i")
                .autoSleep(Duration.ZERO)
                .actions(() -> counting(executions), new FailAction.Builder())
                .build();

        // the condition is checked before each run, so runs 1 and 2 fail and index 3 ends the loop with the last error
        assertThatThrownBy(() -> repeat.execute(context)).isInstanceOf(CitrusRuntimeException.class);
        assertThat(executions).hasValue(2);
    }

    @Test
    public void shouldExecuteConditionalOnlyWhenAssertionHolds() {
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.variable("var", 5);

        runner.$(conditional().when(conditionOf(Integer.class, "${var}", v -> assertThat(v).isEqualTo(5)))
                .actions(createVariable("execution", "true")));
        runner.$(conditional().when(conditionOf(Integer.class, "${var}", v -> assertThat(v).isLessThan(5)))
                .actions(createVariable("noExecution", "false")));

        assertThat(context.getVariable("execution")).isEqualTo("true");
        assertThat(context.getVariables()).doesNotContainKey("noExecution");
        assertThat(((Conditional) runner.getTestCase().getActions().get(0)).getConditionExpression()).isInstanceOf(AssertJConditionExpression.class);
    }

    @Test
    public void shouldWaitUntilAssertionHolds() {
        AtomicInteger checks = new AtomicInteger();

        new Wait.Builder<>()
                .condition(conditionOf(Integer.class, "${status}", s -> assertThat(checks.incrementAndGet()).isGreaterThanOrEqualTo(3)))
                .interval(10L)
                .milliseconds(2000L)
                .build()
                .execute(withStatus());

        assertThat(checks).hasValueGreaterThanOrEqualTo(3);
    }

    @Test
    public void shouldTimeOutWithAssertJDescription() {
        Wait wait = new Wait.Builder<>()
                .condition(conditionOf(Integer.class, "${status}", s -> assertThat(s).isGreaterThan(5)))
                .interval(10L)
                .milliseconds(100L)
                .build();

        assertThatThrownBy(() -> wait.execute(withStatus()))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("AssertJ condition not satisfied")
                .hasMessageContaining("to be greater than");
    }

    private org.citrusframework.context.TestContext withStatus() {
        context.setVariable("status", "1");
        return context;
    }

    private static TestAction counting(AtomicInteger executions) {
        return context -> executions.incrementAndGet();
    }
}
