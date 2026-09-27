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

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActionContainers;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestActions;
import org.citrusframework.TestBehavior;
import org.citrusframework.actions.ApplyTestBehaviorAction;
import org.citrusframework.api.container.TestActionContainer;
import org.citrusframework.container.FinallySequence;
import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.DefaultTestActions;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.citrusframework.message.DefaultMessageProcessors;
import org.citrusframework.message.DefaultPayloadBuilders;
import org.citrusframework.message.PayloadBuilders;
import org.citrusframework.message.Processors;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.validation.DefaultValidations;
import org.citrusframework.validation.Validations;
import org.citrusframework.variable.DefaultVariableExtractors;
import org.citrusframework.variable.VariableExtractors;

/**
 * Test action runner handed to a test behavior while it is applied. Actions run by the behavior are
 * recorded on the given container, so test reports show them nested under the applied behavior instead
 * of flattened into the test case.
 * <p>
 * Actions are executed the way the test case executes them: pending exceptions of the test context are
 * raised before the next action, disabled actions are skipped and failures are wrapped in a
 * {@link TestCaseFailedException}. Finally actions are not handled here. They are forwarded to the outer
 * runner, which places them on the test case exactly as before.
 */
public final class NestedTestActionRunner implements GherkinTestActionRunner {

    private final TestActionContainer container;
    private final TestContext context;
    private final TestActionRunner outer;

    /**
     * @param container the container recording the nested actions.
     * @param context the test context the nested actions run in, either the global or an isolated one.
     * @param outer the runner that applied the behavior, receives finally actions. May be null, then
     *              finally actions are registered on the test context.
     */
    public NestedTestActionRunner(TestActionContainer container, TestContext context, TestActionRunner outer) {
        this.container = container;
        this.context = context;
        this.outer = outer;
    }

    @Override
    public <T extends TestAction> TestActionRunner run(TestActionBuilder<T> builder) {
        if (builder instanceof FinallySequence.Builder && outer != null) {
            outer.run(builder);
            return this;
        }

        if (builder instanceof ReferenceResolverAware referenceResolverAware) {
            referenceResolverAware.setReferenceResolver(context.getReferenceResolver());
        }

        TestActionRunnerInjector.inject(builder, this);

        T action = builder.build();
        container.addTestAction(action);
        execute(action);

        return this;
    }

    private void execute(TestAction action) {
        if (context.hasExceptions()) {
            throw context.getExceptions().remove(0);
        }

        try {
            container.setActiveAction(action);

            if (!action.isDisabled(context)) {
                action.execute(context);
            }
        } catch (Exception | Error e) {
            throw new TestCaseFailedException(e);
        } finally {
            container.setExecutedAction(action);
        }
    }

    @Override
    public ApplyTestBehaviorAction.Builder applyBehavior(TestBehavior behavior) {
        return new ApplyTestBehaviorAction.Builder()
                .behavior(behavior)
                .on(this);
    }

    @Override
    public TestActions actions() {
        return new DefaultTestActions();
    }

    @Override
    public TestActionContainers containers() {
        return new DefaultTestActions();
    }

    @Override
    public Validations validation() {
        return new DefaultValidations();
    }

    @Override
    public VariableExtractors extractor() {
        return new DefaultVariableExtractors();
    }

    @Override
    public Processors processor() {
        return new DefaultMessageProcessors();
    }

    @Override
    public PayloadBuilders buildPayload() {
        return new DefaultPayloadBuilders();
    }
}
