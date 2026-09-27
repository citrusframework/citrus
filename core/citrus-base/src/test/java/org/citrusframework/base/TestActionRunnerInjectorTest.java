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

package org.citrusframework.base;

import java.util.List;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestActionRunnerAware;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.container.Sequence;
import org.citrusframework.dsl.TestActionSupport;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

public class TestActionRunnerInjectorTest extends UnitTestSupport implements TestActionSupport {

    private final TestActionRunner runner = mock(TestActionRunner.class);

    @Test
    public void shouldInjectRunnerAwareBuilder() {
        RunnerAwareBuilder builder = new RunnerAwareBuilder();

        TestActionRunnerInjector.inject(builder, runner);

        assertSame(builder.runner, runner);
        assertEquals(builder.injections, 1);
    }

    @Test
    public void shouldInjectBuildersNestedInContainers() {
        RunnerAwareBuilder nested = new RunnerAwareBuilder();
        RunnerAwareBuilder deeplyNested = new RunnerAwareBuilder();

        TestActionRunnerInjector.inject(sequential().actions(
                nested,
                iterate().condition("i lt 2").actions(deeplyNested)
        ), runner);

        assertSame(nested.runner, runner);
        assertSame(deeplyNested.runner, runner);
    }

    @Test
    public void shouldInjectDelegate() {
        RunnerAwareBuilder delegate = new RunnerAwareBuilder();

        TestActionRunnerInjector.inject(new DelegatingBuilder(delegate), runner);

        assertSame(delegate.runner, runner);
    }

    @Test
    public void shouldInjectSharedBuilderOnce() {
        RunnerAwareBuilder shared = new RunnerAwareBuilder();

        TestActionRunnerInjector.inject(sequential().actions(shared, sequential().actions(shared)), runner);

        assertEquals(shared.injections, 1);
    }

    @Test
    public void shouldStopAtCycles() {
        Sequence.Builder container = sequential();
        RunnerAwareBuilder nested = new RunnerAwareBuilder();
        container.getActions().add(nested);
        container.getActions().add(new DelegatingBuilder(container));

        TestActionRunnerInjector.inject(container, runner);

        assertEquals(nested.injections, 1);
    }

    @Test
    public void shouldIgnoreBuildersNotAwareOfRunner() {
        TestActionRunnerInjector.inject(echo("no runner needed"), runner);
        TestActionRunnerInjector.inject(null, runner);
    }

    @Test
    public void shouldPlaceFinallyOnTestCaseForBehaviorInContainer() {
        DefaultTestCaseRunner testCaseRunner = new DefaultTestCaseRunner(context);

        testCaseRunner.run(sequential().actions(
                apply(behavior -> behavior.run(doFinally().actions(echo("behaviorFinally"))))
        ));

        List<TestAction> finalActions = ((DefaultTestCase) testCaseRunner.getTestCase()).getFinalActions();
        assertEquals(finalActions.size(), 1);
        assertEquals(((EchoAction) finalActions.get(0)).getMessage(), "behaviorFinally");
        assertTrue(context.getFinalActions().isEmpty());
    }

    @Test
    public void shouldPlaceFinallyOnTestCaseForBehaviorInContainerOfBehavior() {
        DefaultTestCaseRunner testCaseRunner = new DefaultTestCaseRunner(context);

        testCaseRunner.run(apply(outer -> outer.run(sequential().actions(
                apply(inner -> inner.run(doFinally().actions(echo("innerFinally"))))
        ))));

        List<TestAction> finalActions = ((DefaultTestCase) testCaseRunner.getTestCase()).getFinalActions();
        assertEquals(finalActions.size(), 1);
        assertEquals(((EchoAction) finalActions.get(0)).getMessage(), "innerFinally");
    }

    @Test
    public void shouldApplyBehaviorInFinallyWithoutExplicitRunner() {
        DefaultTestCaseRunner testCaseRunner = new DefaultTestCaseRunner(context);
        testCaseRunner.start();

        testCaseRunner.run(doFinally().actions(
                apply(behavior -> behavior.run(createVariables().variable("cleanedUp", "true")))
        ));
        testCaseRunner.stop();

        assertEquals(context.getVariable("cleanedUp"), "true");
    }

    private static class RunnerAwareBuilder implements TestActionBuilder<TestAction>, TestActionRunnerAware {
        private TestActionRunner runner;
        private int injections;

        @Override
        public TestAction build() {
            return context -> {};
        }

        @Override
        public void setTestActionRunner(TestActionRunner runner) {
            this.runner = runner;
            injections++;
        }
    }

    private record DelegatingBuilder(TestActionBuilder<?> delegate) implements TestActionBuilder.DelegatingTestActionBuilder<TestAction> {

        @Override
        public TestActionBuilder<?> getDelegate() {
            return delegate;
        }

        @Override
        public TestAction build() {
            return delegate.build();
        }
    }
}
