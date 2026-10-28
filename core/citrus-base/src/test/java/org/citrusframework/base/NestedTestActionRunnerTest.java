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

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActionContainers;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestActionRunnerAware;
import org.citrusframework.TestActions;
import org.citrusframework.TestBehavior;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.actions.ApplyTestBehaviorAction;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.container.FinallySequence;
import org.citrusframework.container.Sequence;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.citrusframework.message.PayloadBuilders;
import org.citrusframework.message.Processors;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.validation.Validations;
import org.citrusframework.variable.VariableExtractors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class NestedTestActionRunnerTest extends UnitTestSupport implements TestActionSupport {

    private final List<String> log = new ArrayList<>();

    private Sequence container;

    @BeforeEach
    public void setupContainer() {
        log.clear();
        container = new Sequence.Builder().build();
    }

    @Test
    public void shouldExecuteActionInContainer() {
        TestActionRunner outer = mock(TestActionRunner.class);
        NestedTestActionRunner runner = new NestedTestActionRunner(container, context, outer);

        runner.run(echo("nested"));
        runner.run(createVariables().variable("foo", "bar"));

        assertEquals(2, container.getActionCount());
        assertEquals(2, container.getExecutedActions().size());
        assertEquals("nested", ((EchoAction) container.getExecutedActions().get(0)).getMessage());
        assertEquals("bar", context.getVariable("foo"));
        verifyNoInteractions(outer);
    }

    @Test
    public void shouldForwardFinallyToOuterRunner() {
        TestActionRunner outer = mock(TestActionRunner.class);
        NestedTestActionRunner runner = new NestedTestActionRunner(container, context, outer);
        FinallySequence.Builder doFinally = doFinally().actions(echo("finally"));

        runner.run(doFinally);

        verify(outer).run(doFinally);
        assertEquals(0, container.getActionCount());
        assertTrue(context.getFinalActions().isEmpty());
    }

    @Test
    public void shouldPlaceFinallyOnTestCaseOfOuterRunner() {
        DefaultTestCaseRunner outer = new DefaultTestCaseRunner(context);
        NestedTestActionRunner runner = new NestedTestActionRunner(container, context, outer);

        runner.run(echo("nested"));
        runner.run(doFinally().actions(echo("behaviorFinally")));

        List<TestAction> finalActions = ((DefaultTestCase) outer.getTestCase()).getFinalActions();
        assertEquals(1, finalActions.size());
        assertEquals("behaviorFinally", ((EchoAction) finalActions.get(0)).getMessage());
        assertEquals(1, container.getActionCount());
        assertEquals(0, outer.getTestCase().getActionCount());
    }

    @Test
    public void shouldPlaceFinallyOnTestCaseThroughSupportClass() {
        DefaultTestCaseRunner delegate = new DefaultTestCaseRunner(context);
        GherkinOnlySupport outer = new GherkinOnlySupport(delegate);

        delegate.run(new ApplyTestBehaviorAction.Builder()
                .behavior(behavior -> behavior.run(doFinally().actions(echo("behaviorFinally"))))
                .on(outer));

        List<TestAction> finalActions = ((DefaultTestCase) delegate.getTestCase()).getFinalActions();
        assertEquals(1, finalActions.size());
        assertEquals("behaviorFinally", ((EchoAction) finalActions.get(0)).getMessage());
    }

    @Test
    public void shouldPlaceFinallyInContextWithoutOuterRunner() {
        NestedTestActionRunner runner = new NestedTestActionRunner(container, context, null);

        runner.run(doFinally().actions(echo("behaviorFinally")));

        assertEquals(1, container.getActionCount());
        assertEquals(1, context.getFinalActions().size());
        assertEquals("behaviorFinally", ((EchoAction) context.getFinalActions().get(0).build()).getMessage());
    }

    @Test
    public void shouldPropagateReferenceResolver() {
        NestedTestActionRunner runner = new NestedTestActionRunner(container, context, null);
        ResolverAwareBuilder builder = new ResolverAwareBuilder();

        runner.run(builder);

        assertSame(context.getReferenceResolver(), builder.referenceResolver);
    }

    @Test
    public void shouldInjectItselfIntoRunnerAwareBuilders() {
        NestedTestActionRunner runner = new NestedTestActionRunner(container, context, null);
        RunnerAwareBuilder direct = new RunnerAwareBuilder();
        RunnerAwareBuilder nested = new RunnerAwareBuilder();

        runner.run(direct);
        runner.run(sequential().actions(nested));

        assertSame(runner, direct.runner);
        assertSame(runner, nested.runner);
    }

    @Test
    public void shouldWrapFailureLikeTestCase() {
        NestedTestActionRunner runner = new NestedTestActionRunner(container, context, null);

        TestCaseFailedException failure = assertThrows(TestCaseFailedException.class, () -> runner.run(fail("boom")));

        assertEquals("boom", failure.getMessage());
        assertEquals(CitrusRuntimeException.class, failure.getCause().getClass());
        assertEquals(1, container.getExecutedActions().size());
    }

    @Test
    public void shouldNotFailTestForFailureCaughtByBehavior() {
        DefaultTestCaseRunner outer = new DefaultTestCaseRunner(context);
        outer.start();

        outer.run(apply(behavior -> {
            try {
                behavior.run(fail("boom"));
            } catch (TestCaseFailedException e) {
                log.add("caught " + e.getMessage());
            }
        }));
        outer.stop();

        assertEquals(List.of("caught boom"), log);
        assertTrue(outer.getTestCase().getTestResult().isSuccess());
    }

    @Test
    public void shouldRaisePendingContextException() {
        NestedTestActionRunner runner = new NestedTestActionRunner(container, context, null);
        CitrusRuntimeException pending = new CitrusRuntimeException("async failure");
        context.addException(pending);

        CitrusRuntimeException failure = assertThrows(CitrusRuntimeException.class, () -> runner.run(echo("never")));

        assertSame(pending, failure);
        assertEquals(0, container.getExecutedActions().size());
    }

    @Test
    public void shouldSkipDisabledAction() {
        NestedTestActionRunner runner = new NestedTestActionRunner(container, context, null);
        TestAction disabled = new TestAction() {
            @Override
            public void execute(org.citrusframework.context.TestContext context) {
                log.add("executed");
            }

            @Override
            public boolean isDisabled(org.citrusframework.context.TestContext context) {
                return true;
            }
        };

        runner.run(disabled);

        assertTrue(log.isEmpty());
        assertEquals(1, container.getExecutedActions().size());
    }

    @Test
    public void shouldApplyBehaviorOnNestedRunner() {
        NestedTestActionRunner runner = new NestedTestActionRunner(container, context, null);
        TestBehavior inner = behavior -> behavior.run(echo("inner"));

        runner.run(runner.applyBehavior(inner));

        assertEquals(1, container.getActionCount());
        ApplyTestBehaviorAction applied = (ApplyTestBehaviorAction) container.getExecutedActions().get(0);
        assertEquals(1, applied.getActionCount());
    }

    @Test
    public void shouldProvideDslAccess() {
        NestedTestActionRunner runner = new NestedTestActionRunner(container, context, null);

        assertNotNull(runner.actions());
        assertNotNull(runner.containers());
        assertNotNull(runner.validation());
        assertNotNull(runner.extractor());
        assertNotNull(runner.processor());
        assertNotNull(runner.buildPayload());
    }

    /**
     * Support class shape used by JUnit4CitrusSupport: a Gherkin runner that is no test case runner.
     */
    private record GherkinOnlySupport(TestCaseRunner delegate) implements GherkinTestActionRunner {

        @Override
        public <T extends TestAction> TestActionRunner run(TestActionBuilder<T> builder) {
            return delegate.run(builder);
        }

        @Override
        public <T extends TestAction> TestActionBuilder<T> applyBehavior(TestBehavior behavior) {
            return delegate.applyBehavior(behavior);
        }

        @Override
        public TestActions actions() {
            return delegate.actions();
        }

        @Override
        public TestActionContainers containers() {
            return delegate.containers();
        }

        @Override
        public Validations validation() {
            return delegate.validation();
        }

        @Override
        public VariableExtractors extractor() {
            return delegate.extractor();
        }

        @Override
        public Processors processor() {
            return delegate.processor();
        }

        @Override
        public PayloadBuilders buildPayload() {
            return delegate.buildPayload();
        }
    }

    private static class ResolverAwareBuilder implements TestActionBuilder<TestAction>, ReferenceResolverAware {
        private ReferenceResolver referenceResolver;

        @Override
        public TestAction build() {
            return context -> {};
        }

        @Override
        public void setReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
        }
    }

    private static class RunnerAwareBuilder implements TestActionBuilder<TestAction>, TestActionRunnerAware {
        private TestActionRunner runner;

        @Override
        public TestAction build() {
            return context -> {};
        }

        @Override
        public void setTestActionRunner(TestActionRunner runner) {
            this.runner = runner;
        }
    }
}
